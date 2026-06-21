#include "Manager.hpp"

Manager::Manager(asio::io_context& io, short port) :
    m_server(io, port),
    m_updateTimer(io),
    m_io(io)
{
    startUpdateTimer();
}

void Manager::startUpdateTimer()
{
    m_updateTimer.expires_after(std::chrono::milliseconds(m_updateRateMs));
    m_updateTimer.async_wait([this](boost::system::error_code ec){
        if (!ec) {
            onTick();
            startUpdateTimer();
        }
    });
}


// UPDATE LOGIC
void Manager::onTick()
{
    json j;
    int totalClients = 0;

    // iterate all cleints
    json clientsJson = json::object(); // init as {}, not null
    for (auto c : m_server.getClients()) {
        // check weak pointer is valid
        if (auto client = c.lock()) {
            if (client->uuid.empty())
                continue; // exit if client didnt register yet

            json clientJson;
            clientJson["username"] = client->username;

            // position json
            json posJson;
            posJson["x"] = client->position.x;
            posJson["y"] = client->position.y;
            posJson["z"] = client->position.z;
            posJson["yaw"] = client->position.yaw;
            posJson["pitch"] = client->position.pitch;
            posJson["dimension"] = client->position.dimension;
            posJson["world_name"] = client->position.world_name;
            clientJson["position"] = posJson;
            // update master json
            clientsJson[client->uuid] = clientJson;

            totalClients++;
        }
    }

    j["clients"] = clientsJson;
    j["number_of_clients"] = totalClients;
    // broadcast this json
    m_server.broadcast(j);

    std::cout << "json string: "<< j.dump() << std::endl;
}