#ifndef __header
    #define __header

    #include <cstdio>
    #include <iostream>
    #include <algorithm>
    #include <vector>
    #include <map>
    #include <mutex>
    #include <queue>
    #include <thread>
    #include <sys/types.h>  
    #include <sys/socket.h>  
    #include <netinet/in.h>
    #include <cstring>
    #include <cerrno>
    #include <unistd.h>
    #include <MessageUtil.pb.h>
    #include <Cards.pb.h>
    #include <SaveData.pb.h>
    #include <fstream>
    #include <cmath>
    #define DebugMode 1
    const int serverport = 8002;
    static int ServerRunID = 1;
#endif