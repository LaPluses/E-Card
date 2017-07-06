#include <socket.h>
#include <block.h>

extern chainblock currentchainblock;
extern std::map < std::string , std::string > IDPool;
const int TimePoolCheckTime = 15 * 60 * 1000; // 15min
extern std::map < std::string , std::pair < std::string , time_t > > TimePool; // 时间池
extern std::set < std::string > AuxiliaryTimePool ; // 辅助时间池
void SystemExit();

void SendData( int client_id , void * buf , int buflen ){
    int SendLength = htonl( buflen );
    if( DebugMode )
     printf( "Ready to Send package , Length is %d\n" , buflen );
    if( send( client_id , (void*)& SendLength , 4 , MSG_CONFIRM ) != 4 ){
        printf("send msg error: %s(errno: %d)\n", strerror(errno), errno);
        return ;
    }
    char * ptr = (char *)buf;
    int hassend = 0;
    while( hassend < buflen ){
        int i = send( client_id , buf , buflen , MSG_CONFIRM );
        if( i < 1 )
            break;
        ptr += i;
        hassend += i;
    }
    free( buf );
    if( DebugMode ){
         printf( "Final Send Length is %d " , hassend );
        if( hassend == buflen )
            printf( "pass\n");
        else
            printf( "error\n");
    }
}

std::pair < void * , bool > RecvFixdPackage( int clinet_id , int length ){
    int cur = 0 , cnt = 0;
    void * s = malloc( length ) , * p = s ;
    while( cur < length ){
        int z = recv( clinet_id , p , length - cur , 0 );
        if( z <= 0 ){
            printf( "Recv Length is %d %s\n" , z , strerror(errno) );
            break;
        }
        cur += z;
        p = (void*)((char*)p + z);
    }
    if( DebugMode )
        printf( "Final Length is %d\n" , cur );
    if( cur != length )
        free( s );
    return std::make_pair( s , cur == length );
}

std::pair < void * , bool > RecvData( int client_id , int & buflen ){
    std::pair < void * , bool > ret = RecvFixdPackage( client_id , 4 );
    if( ret.second == false ){
        if( DebugMode )
            printf( "Recv Package length error !\n" ); 
        return ret;
    }
    buflen = ntohl(*((int*)ret.first));
    if( DebugMode )
        printf( "Ready to recv package , length is %d\n" , buflen );
    ret = RecvFixdPackage( client_id , buflen );
    if( ret.second == false ){
         if( DebugMode )
            printf( "Recv Final Package error !\n" ); 
        return ret;
    }
    if( DebugMode )
        printf( "Recv package complete , length is %d , pass \n" , buflen );
    return ret;
}

bool facechecker( std::vector < double > x , std::vector < double > y ){
    if( x.size() != y.size() )
        return false;
    double c1 = 0, c2 = 0, c3 = 0;
	for (int i = 0; i < x.size(); ++ i ){
		c1 += x[i] * y[i];
		c2 += x[i] * x[i];
		c3 += y[i] * y[i];
	}
    c2 = sqrt( c2 ) , c3 = sqrt( c3 );
    if( DebugMode ){
        printf( "Face checker : Vector Distance is %.6lf " , 1 - c1 / ( c2 * c3 ) );
        if( 1 - c1 / (c2 * c3) < 0.25 )
            printf( ", Log in Succ\n" );
        else
            printf( ", Log in Fail\n" );
    }
    return 1 - c1 / (c2 * c3) < 0.25;
}

void process_client_request( int client_id ){
    int buflen;
    std::pair < void * , bool > ret = RecvData( client_id , buflen );
    if( ret.second == false ){
        close( client_id );
        return;
    }
    void * buf = ret.first;
    MessageBox F , Send;
    if( !F.ParseFromArray( buf , buflen ) ){
        if( DebugMode )
            printf( "Parse error !\n" );
        close( client_id );
        return;
    }
    free( buf );
    int type = F.type();
    if( DebugMode )
        printf( "Request type is %d\n" , type );
    switch( type ){
        case 1 :{
            if( F.trans_str_size() != 2 ){
                if( DebugMode ){
                    printf( "Refused , error package information\n" );
                }
                close( client_id );
                return;
            }
            std::string account = F.trans_str(0);
            std::string password = F.trans_str(1);
            if( currentchainblock.find( account ).second == false ){
                Send.set_type(-1);
                std::vector < double > face;
			    for (int i = 0; i < F.trans_photo_size() ; ++ i) face.emplace_back(F.trans_photo(i));
                if( DebugMode ){
                    printf( "eigenvalue #1 : " );
                    for(int i = 0 ; i < 10 ; ++ i) printf( "%.6lf " , face[i] );
                    puts( "" );
                    printf( "eigenvalue #2 : " );
                    for(int i = 0 ; i < 10 ; ++ i) printf( "%.6lf " , face[i + 10] );
                    puts( "" );
                    printf( "eigenvalue #3 : " );
                    for(int i = 0 ; i < 10 ; ++ i) printf( "%.6lf " , face[i + 20] );
                    puts( "" );
                }
                std::string id = mallocID( account );
                if( DebugMode )
                    printf( "Register a new user , account is %s , password is %s , id is %s \n" , account.c_str() , password.c_str() , id.c_str() );
                Send.add_trans_str( id );
                Send.add_trans_str( mallocTimer( account ) );
                Refresh( account );
                currentchainblock.emplace_back( person( "" , "" , "" , account , password , face , std::vector < LocalCard >() , id ) );
            }
            else{
                Send.set_type(-2);
                if( DebugMode ){
                    printf( "Register failed , account is %s \n" , account.c_str() );
                }
            }
            break;
        }

        case 2 :{
            if( F.trans_str_size() != 2 ){
                if( DebugMode ){
                    printf( "Refused , error package information\n" );
                }
                close( client_id );
                return;
            }
            std::string account = F.trans_str(0);
            std::string password = F.trans_str(1);
            std:: pair < person , bool > result = currentchainblock.find( account );
            if( result.second == true && result.first.getpassword() == password ){
                Send.set_type( -1 );
                if( DebugMode )
                    printf( "result id %s\n" , result.first.getid().c_str() );
                Send.add_trans_str( result.first.getid() );
                Send.add_trans_str( mallocTimer( account ) );
                Refresh( account );
                if( DebugMode )
                    printf( "Log in succ , account is %s \n" , account.c_str() );
            }
            else{
                Send.set_type( -2 );
                if( DebugMode )
                    printf( "Log in fail , account is %s \n" , account.c_str() );
            }
            break;
        }

        case 3 :{
            if( F.trans_str_size() != 1 ){
                if( DebugMode ){
                    printf( "Refused , error package information\n" );
                }
                close( client_id );
                return;
            }
            std::string account = F.trans_str(0);
            std::pair < person , bool > result = currentchainblock.find( account );
            std::vector < double > base;
            for (int i = 0; i < F.trans_photo_size() ; ++ i) base.emplace_back( F.trans_photo( i ) );
            if( result.second == true && facechecker( result.first.getface() , base ) ){
                Send.set_type( -1 );
                Send.add_trans_str( result.first.getid() );
                Send.add_trans_str( mallocTimer( account ) );
                Refresh( account );
            }
            else
                Send.set_type( -2 );
            break;
        }

        case 4 :{
            if( F.trans_str_size() != 1 ){
                if( DebugMode ){
                    printf( "Refused , error package information\n" );
                }
                close( client_id );
                return;
            }
            std::string account = F.trans_str( 0 );
            ret = RecvData( client_id , buflen );
            if( ret.second == false ){
                close( client_id );
                return;
            }
            buf = ret.first;
            person add = currentchainblock.find( account ).first;
            ECarD::Card ApL;
		    ApL.ParseFromArray( buf , buflen );
            free( buf );
            LocalCard fw;
		    fw.cardtype = ApL.cardtype();
		    fw.cardinfo = ApL.image();
	    	fw.cardindex = add.getnextid();
            printf( "add a card to account %s , card type is " , account.c_str() );
            if( fw.cardtype == 1 )
                printf( "IDCard\n" );
            else if( fw.cardtype == 2 )
                printf( "DriverCard\n" );
            else if( fw.cardtype == 3 )
                printf( "StudentCard\n" );
            else if( fw.cardtype == 4 )
                printf( "SocialSecurityCard\n");
            else
                printf( "DisabledCard\n");
		    for (auto it : ApL.fields())
			    fw.fields.emplace_back(it);
            add.emplace_back(fw);
            currentchainblock.emplace_back( add );
            Send.set_type(-1);
		    Send.add_trans_str(std::to_string( fw.cardindex ));
            break;
        }


        case 5 :{
            if( F.trans_str_size() != 1 ){
                if( DebugMode ){
                    printf( "Refused , error package information\n" );
                }
                close( client_id );
                return;
            }
            std::string account = F.trans_str( 0 );
            ECarD::Cards res;
            std::pair < person , bool > fw = currentchainblock.find( account );
            if( fw.second == true ){
                std::vector < LocalCard > Seq = fw.first.getEcard();
                for(auto it : Seq){
                    ECarD::Card * G = res.add_cards();
                    if( it.cardtype == 1 )
						G->set_cardtype(ECarD::IDCard);
					else if(it.cardtype == 2)
						G->set_cardtype(ECarD::DriverCard);
					else if (it.cardtype == 3)
						G->set_cardtype(ECarD::StudentCard);
					else if (it.cardtype == 4)
						G->set_cardtype(ECarD::SocialSecurityCard);
					else
						G->set_cardtype(ECarD::DisabledCard);
                    G->set_image(it.cardinfo);
					G->set_cardid(it.cardindex);
                    G->set_checkflag(it.checkflag);
					for (auto it2 : it.fields)
						*(G->add_fields()) = it2;
                }
            }
            buflen = res.ByteSize();
            buf = malloc( buflen );
            res.SerializeToArray( buf , buflen );
            SendData( client_id , buf , buflen );
            break;
        }

        case 6 :{
            if( F.trans_str_size() != 2 ){
                if( DebugMode ){
                    printf( "Refused , error package information\n" );
                }
                close( client_id );
                return;
            }
            std::string account = F.trans_str(0);
            std::function < int( std::string ) > z = [](std:: string x){
                int res = 0;
                for(auto it : x) res = res * 10 + it - '0';
                return res;
            };
            int deleteindex = z( F.trans_str( 1 ) );
            std::pair < person , bool > res = currentchainblock.find( account );
            if( res.second == true && res.first.deleteindex( deleteindex ) ){
                Send.set_type( -1 );
                currentchainblock.emplace_back( res.first );
            }
            else
                Send.set_type( -2 );
            break;
        }

        case 7 :{
             if( F.trans_str_size() != 2 ){
                if( DebugMode ){
                    printf( "Refused , error package information\n" );
                }
                close( client_id );
                return;
            }
            std::string account = F.trans_str(0);
            std::function < int( std::string ) > z = [](std:: string x){
                int res = 0;
                for(auto it : x) res = res * 10 + it - '0';
                return res;
            };
            int checkindex = z( F.trans_str( 1 ) );
            std::pair < person , bool > res = currentchainblock.find( account );
            if( res.second == true && res.first.checkindex( checkindex ) ){
                Send.set_type( -1 );
                currentchainblock.emplace_back( res.first );
            }
            else
                Send.set_type( -2 );
            break;
        }

        case 8 :{
             if( F.trans_str_size() != 1 ){
                if( DebugMode ){
                    printf( "Refused , error package information\n" );
                }
                close( client_id );
                return;
            }
            std::string account = F.trans_str( 0 );
            std::pair < person , bool > res = currentchainblock.find( account );
            if( res.second == true )
                Send.set_type( -1 ),
                *Send.add_trans_str() = res.first.getid();
            else
                Send.set_type( -2 );
        }

        case 9 :{
            if( F.trans_str_size() != 3 ){
                if( DebugMode ){
                    printf( "Refused , error package information\n" );
                }
                close( client_id );
                return;
            }
            std::string account = F.trans_str(0);
            std::string id = F.trans_str(1);
            std::string dynamicid = F.trans_str( 2 );
            if( IDPool.count( id ) && IDPool[id] == account && AuxiliaryTimePool.count( account ) && TimePool[account].first == dynamicid && difftime( clock() , TimePool[account].second ) / 1000. < TimePoolCheckTime )
                Send.set_type( -1 );
            else
                Send.set_type( -2 );
            break;
        }

        case 10 :{
            if( F.trans_str_size() != 1 ){
                if( DebugMode ){
                    printf( "Refused , error package information\n" );
                }
                close( client_id );
                return;
            }
            std::string account = F.trans_str( 0 );
            ECarD::Cards res;
            std::pair < person , bool > fw = currentchainblock.find( account );
            if( fw.second == true ){
                std::vector < LocalCard > Seq = fw.first.getEcard();
                for(auto it : Seq){
                    if( it.checkflag == 0 ) continue;
                    ECarD::Card * G = res.add_cards();
                    if( it.cardtype == 1 )
						G->set_cardtype(ECarD::IDCard);
					else if(it.cardtype == 2)
						G->set_cardtype(ECarD::DriverCard);
					else if (it.cardtype == 3)
						G->set_cardtype(ECarD::StudentCard);
					else if (it.cardtype == 4)
						G->set_cardtype(ECarD::SocialSecurityCard);
					else
						G->set_cardtype(ECarD::DisabledCard);
                    G->set_image(it.cardinfo);
					G->set_cardid(it.cardindex);
                    G->set_checkflag(it.checkflag);
					for (auto it2 : it.fields)
						*(G->add_fields()) = it2;
                }
            }
            buflen = res.ByteSize();
            buf = malloc( buflen );
            res.SerializeToArray( buf , buflen );
            SendData( client_id , buf , buflen );
        }

        case 11 :{
            if( F.trans_str_size() != 1 ){
                if( DebugMode ){
                    printf( "Refused , error package information\n" );
                }
                close( client_id );
                return;
            }
            std::string account = F.trans_str(0);
            std::vector < double > face;
		    for (int i = 0; i < F.trans_photo_size() ; ++ i) face.emplace_back(F.trans_photo(i));
            std::pair < person , bool > fr = currentchainblock.find( account );
            if( fr.second == true ){
                Send.set_type( -1 );
                fr.first.setface( face );
                currentchainblock.emplace_back( fr.first );
            }else
                Send.set_type( -2 );
            break;
        }

        case 12 :{
            if( F.trans_str_size() != 1 ){
                if( DebugMode ){
                    printf( "Refused , error package information\n" );
                }
                close( client_id );
                return;
            }
            std::string account = F.trans_str(0);
            if( Refresh( account ) )
                Send.set_type( -1 );
            else
                Send.set_type( -2 );
            break;
        }

        default:
            break;
    }
    if( type != 5 && type >= 0 ){
        buflen = Send.ByteSize();
        buf = malloc( buflen );
        if ( ! Send.SerializeToArray( buf , buflen ) )
            std:: cout << "SerializeToArray Error" << std::endl;
        SendData( client_id , buf , buflen );
    }
    if( DebugMode )
        printf( "Close connet from %d \n" , client_id );
    close( client_id );
}

void ServerThread(){
    if( DebugMode )
        printf( "#Debug : Server is opening !\n" );    
    int socket_id = socket(AF_INET,SOCK_STREAM,IPPROTO_TCP);
    if( socket_id == -1 ){
        printf("create socket error: %s(errno: %d)\n",strerror(errno),errno);
        SystemExit();
    }
    sockaddr_in servaddr;
    servaddr.sin_family = AF_INET;
    servaddr.sin_addr.s_addr = htonl(INADDR_ANY);
    servaddr.sin_port = htons(serverport);
    if( bind( socket_id , (struct sockaddr*)& servaddr , sizeof( servaddr ) ) == -1 ){
        printf("bind socket error: %s(errno: %d)\n",strerror(errno),errno);
        SystemExit();
    }
    listen( socket_id , 16 );
    printf( "now running ...\n" );
    while( 1 ){
        int client_id = accept(socket_id, (struct sockaddr*)NULL, NULL);
        //printf( "client_id is %d\n" , client_id );
        if( DebugMode )
            printf( "Accepet new connet from %d\n" , client_id );
        if( client_id == -1 ){
             printf("accept socket error: %s(errno: %d)",strerror(errno),errno);
             continue;
        }
        std::thread processthread(process_client_request,client_id); //启动处理请求子线程
	    processthread.detach();
    }
    close( socket_id );
}