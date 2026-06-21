#include <iostream>
#include <boost/asio.hpp>
#include "Manager.hpp"

using boost::asio::ip::tcp;
namespace asio = boost::asio;

int main(int argc, char* argv[])
{
    short port = 8080;
    std::cout << "Running SlabServer on port: " << port << std::endl;

    boost::asio::io_context io;

    Manager manager(io, port);

    io.run(); // run asio event loop
}