cmake_minimum_required(VERSION 3.20)

project(SlabServer VERSION 1.0 LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

find_package(Boost CONFIG REQUIRED)
find_package(SQLite3 REQUIRED)
find_package(nlohmann_json REQUIRED)

add_executable(slabserver
    main.cpp
)

include_directories(${SQLite3_INCLUDE_DIRS})
target_link_libraries(slabserver PRIVATE 
    ${SQLite3_LIBRARIES}
    nlohmann_json::nlohmann_json
)