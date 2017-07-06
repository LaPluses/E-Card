#include <header.h>
#include <socket.h>
#include <block.h>

chainblock currentchainblock; // 主区块链
std::map < std::string , std::string > IDPool; // ID池
std::map < std::string , std:: pair < std::string , time_t > > TimePool; // 时间池
std::set < std::string > AuxiliaryTimePool ; // 辅助时间池

const static int CheckerTime = 10; // 服务器写出间隔

void ChekerWriteThread(){
   // printf( "AutoSync start .... \n" );
    currentchainblock.write();
  //  printf( "AutoSync complete .... \n" );
    sleep( CheckerTime );
    std::thread Cheker(ChekerWriteThread); //自动备份线程
    Cheker.detach();
}

void SystemExit(){
    currentchainblock.write();
    exit( 0 );
}

void KeyboardListenThread(){
    std::string s , account , password;
    while( std::cin >> s ){
        if( s == "quit" )
            SystemExit();
        if( s == "register" ){
            std::cin >> account >> password;
            std::string id = mallocID( account );
            if( DebugMode )
                printf( "Register a new user , account is %s , password is %s , id is %s \n" , account.c_str() ,password.c_str() , id.c_str() );
            currentchainblock.emplace_back( person( "" , "" , "" , account , password , std::vector < double >() , std::vector < LocalCard >() , id ) );
        }
        if( s == "DebugInfo" ){
            std::vector < person > debuginfo = currentchainblock.transform();
            printf( "-------------------------\n" );
            for(auto & it : debuginfo){
                std::string account = it.getaccount();
                std::string password = it.getpassword();
                std::string id = it.getid();
                int number = it.getEcardSize();
                printf( "account is %s , password is %s , id is %s , number is %d\n" , account.c_str() , password.c_str() , id.c_str() , number );
            }
            printf( "-------------------------\n" );
        }
    }
}

int main( int argc , char * argv[] ){
    currentchainblock.read( "./" ); //区块链初始化
    std::thread KeyboardThread(KeyboardListenThread); //键盘监听线程
    KeyboardThread.detach();
    std::thread Cheker(ChekerWriteThread); //自动备份线程
    Cheker.detach();
    std::thread MainThread(ServerThread); //启动服务器网络主线程
    MainThread.join();
    return 0;
}