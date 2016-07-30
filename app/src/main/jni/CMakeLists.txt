cmake_minimum_required(VERSION 3.4.1)

add_library(khronos-media SHARED
            khronos-media.c)

target_link_libraries(khronos-media log android OpenSLES)