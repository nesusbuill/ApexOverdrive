cmake_minimum_required(VERSION 3.22.1)
project("apexnative")
add_library(apexnative SHARED native-lib.cpp)
target_compile_options(apexnative PRIVATE
    -O2
    -fvisibility=hidden
    -fvisibility-inlines-hidden
    -fstack-protector-all
    -D_FORTIFY_SOURCE=2)
set_target_properties(apexnative PROPERTIES
    CXX_STANDARD 17
    CXX_STANDARD_REQUIRED ON
    LINK_FLAGS "-Wl,--strip-all -Wl,-z,noexecstack -Wl,-z,relro -Wl,-z,now")
target_link_libraries(apexnative log android)
