#include <iostream>
#include <boost/asio.hpp>
#include "Server.hpp"


using boost::asio::ip::tcp;

int main(int argc, char* argv[])
{
    short port = 8080;
    std::cout << "Running SlabServer on port: " << port << std::endl;

    boost::asio::io_context io;

    Server server(io, port);



    io.run(); // run asio event loop
}