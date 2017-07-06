
#ifndef __socketh
    #define __socketh
    
    #include <header.h>

    void ServerThread(); //初始化服务器
    void SendData( int client_id , void * buf , int buflen ); //发送数据
    std::string RecvData( int clinet_id ); //接受数据
#endif