#pragma once

#include <boost/asio.hpp>
#include "Server.hpp"

namespace asio = boost::asio;

class Manager
{
public:
    Manager(asio::io_context& io, short port);

private:
    asio::io_context& m_io; // referneec to our io context
    Server m_server;

    // update timer
    asio::steady_timer m_updateTimer; // basically QTimer
    int m_updateRateMs {500}; // 2hz
    void startUpdateTimer();
    void onTick(); // update logic
};
