cmake_minimum_required(VERSION 3.4.1)

set(
	PATH_TO_LIBCUE
	CACHE STRING ""
)

message(${ANDROID_ABI})

file(GLOB CPP_FILES "*.cpp")

add_library(
	LibCueParser
	SHARED
	${CPP_FILES}
	${PATH_TO_LIBCUE}/cd.c
	${PATH_TO_LIBCUE}/cdtext.c
	${PATH_TO_LIBCUE}/rem.c
	${PATH_TO_LIBCUE}/time.c
	${PATH_TO_LIBCUE}/time.c
)

include_directories(src/main/jni)
include_directories(${PATH_TO_LIBCUE})

target_link_libraries(
    LibCueParser
    log
    android
)