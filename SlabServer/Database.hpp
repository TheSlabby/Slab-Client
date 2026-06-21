// i mostly used AI for this class

#pragma once
#include <boost/asio.hpp>
#include <sqlite3.h>
#include <string>
#include <vector>
#include <memory>
#include <stdexcept>

namespace asio = boost::asio;

class Database {
public:
    Database(asio::io_context& io, asio::thread_pool& pool, const std::string& path)
        : m_io(io), m_pool(pool), m_db(nullptr) {
        
        // Open SQLite in Serialized mode (SQLITE_OPEN_FULLMUTEX) to make the single 
        // connection handle thread-safe across the thread pool.
        int flags = SQLITE_OPEN_READWRITE | SQLITE_OPEN_CREATE | SQLITE_OPEN_FULLMUTEX;
        if (sqlite3_open_v2(path.c_str(), &m_db, flags, nullptr) != SQLITE_OK) {
            std::string err_msg = m_db ? sqlite3_errmsg(m_db) : "unknown error";
            if (m_db) {
                sqlite3_close(m_db); // Free memory if open fails but allocates handle
                m_db = nullptr;
            }
            throw std::runtime_error("Failed to open database: " + err_msg);
        }
    }

    ~Database() {
        if (m_db) sqlite3_close(m_db);
    }

    // no copying - one owner of the db handle
    Database(const Database&) = delete;
    Database& operator=(const Database&) = delete;

    // query that returns rows
    asio::awaitable<std::vector<std::vector<std::pair<std::string, std::string>>>> query(std::string sql) {
        // Shift execution to the thread pool (avoiding co_spawn overhead)
        co_await asio::post(m_pool, asio::use_awaitable);

        std::vector<std::vector<std::pair<std::string, std::string>>> rows;
        sqlite3_stmt* stmt = nullptr;
        if (sqlite3_prepare_v2(m_db, sql.c_str(), -1, &stmt, nullptr) != SQLITE_OK) {
            throw std::runtime_error("SQL error: " + std::string(sqlite3_errmsg(m_db)));
        }

        // Use unique_ptr to guarantee sqlite3_finalize is called even if exceptions are thrown
        std::unique_ptr<sqlite3_stmt, decltype(&sqlite3_finalize)> stmt_guard(stmt, &sqlite3_finalize);

        while (sqlite3_step(stmt) == SQLITE_ROW) {
            std::vector<std::pair<std::string, std::string>> row;
            int cols = sqlite3_column_count(stmt);
            for (int i = 0; i < cols; i++) {
                std::string name = sqlite3_column_name(stmt, i);
                const char* val = reinterpret_cast<const char*>(sqlite3_column_text(stmt, i));
                row.emplace_back(name, val ? val : "NULL");
            }
            rows.push_back(std::move(row));
        }

        // When returning, Asio automatically resumes the caller back on their original executor (io_context)
        co_return rows;
    }

    // execute that returns nothing (INSERT, UPDATE, DELETE)
    asio::awaitable<int> execute(std::string sql) {
        co_await asio::post(m_pool, asio::use_awaitable);

        char* err = nullptr;
        if (sqlite3_exec(m_db, sql.c_str(), nullptr, nullptr, &err) != SQLITE_OK) {
            std::string msg = err ? err : "unknown error";
            sqlite3_free(err);
            throw std::runtime_error("SQL error: " + msg);
        }

        co_return sqlite3_changes(m_db);
    }

private:
    asio::io_context& m_io;
    asio::thread_pool& m_pool;
    sqlite3* m_db;
};