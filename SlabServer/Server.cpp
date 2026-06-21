#include "Server.hpp"


Server::Server(asio::io_context& io, short port) :
    m_acceptor(io, tcp::endpoint(tcp::v4(), port)),
    m_io(io)
{
    co_spawn(io, listen(), asio::detached);
};

void Server::broadcast(std::string msg) {
    auto shared_msg = std::make_shared<std::string>(msg);
    
    auto it = m_clients.begin();
    while (it != m_clients.end()) {
        if (auto client = it->lock()) {
            asio::async_write(client->socket, asio::buffer(*shared_msg), [shared_msg, client](auto, auto) {});
            ++it;
        } else {
            it = m_clients.erase(it); // Remove dead clients in-place
        }
    }
}

asio::awaitable<void> Server::listen()
{
    while (true) {
        auto socket = co_await m_acceptor.async_accept(asio::use_awaitable);
        auto c = std::make_shared<Client>(std::move(socket));
        m_clients.push_back(c);
        // new corouotine for this client speicfically
        co_spawn(m_io, handle_client(c), asio::detached);
    }
}

asio::awaitable<void> Server::handle_client(std::shared_ptr<Client> c) {
    try {
        std::string data;
        while (true) {
            // read until newline
            auto n = co_await asio::async_read_until(c->socket, asio::dynamic_buffer(data), '\n', asio::use_awaitable);
            std::string msg = data.substr(0, n);
            data.erase(0, n);

            // parse as json
            try {
                json j = json::parse(msg);
                auto type = j["type"];
                if (type == "handshake") {
                    // CLIENT HANDSHAKE
                    c->username = j["username"];
                    c->uuid = j["uuid"];
                    std::cout << "Registered client: "
                        << c->username << ", " << c->uuid << std::endl;

                    // return handshake success
                    json handshake_success;
                    handshake_success["type"] = "handshake_response";
                    handshake_success["status"] = "success";
                    handshake_success["message"] = "Successfully registered " + c->username;
                    std::string msg = handshake_success.dump() + "\n";
                    co_await asio::async_write(c->socket, asio::buffer(msg), asio::use_awaitable);
                
                } else if (type == "position") {
                    // POSITION MESSAGE
                    c->position.x = j["x"];
                    c->position.y = j["y"];
                    c->position.z = j["z"];
                    c->position.yaw = j["yaw"];
                    c->position.pitch = j["pitch"];
                    c->position.dimension = j["dimension"];
                    c->position.world_name = j["world_name"];
                    std::cout << c->username << " moved to "
                        << c->position.x << ", " << c->position.y << ", " << c->position.z
                        << " (yaw=" << c->position.yaw << " pitch=" << c->position.pitch << ")"
                        << " in " << c->position.world_name << ", dimension: " << c->position.dimension << std::endl;

                }
            } catch (...) {
                std::cout << "invalid json: " << msg << std::endl;
            }
        }
    } catch (...) {
        std::cout << "client disconnected" << std::endl;
    }
    co_return;
}