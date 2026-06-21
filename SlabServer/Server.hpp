#pragma once

#include <boost/asio.hpp>
#include <boost/asio/co_spawn.hpp>
#include <iostream>
#include <memory>
#include <nlohmann/json.hpp>

using json = nlohmann::json;
using boost::asio::ip::tcp;
namespace asio = boost::asio;


struct Position {
    double x = 0.0;
    double y = 0.0;
    double z = 0.0;
    float yaw = 0.0f;
    float pitch = 0.0f;
    int dimension = 0;
    std::string world_name;
};

struct Client {
    std::string username;
    std::string uuid;
    Position position;
    tcp::socket socket;

    Client(tcp::socket sock) : socket(std::move(sock)) {}
};
using Clients = std::vector<std::weak_ptr<Client>>;

class Server
{
public:
    Server(asio::io_context& io, short port);


    void broadcast(std::string msg);
    void broadcast(const json& j) { broadcast(j.dump() + "\n"); }

    Clients getClients() { return m_clients; }

private:
    tcp::acceptor m_acceptor;
    asio::io_context& m_io;
    Clients m_clients; // actually owend in handle_client func


    asio::awaitable<void> listen();

    asio::awaitable<void> handle_client(std::shared_ptr<Client> c);
};