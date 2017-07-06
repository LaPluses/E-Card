#include <person.h>

/*
    std::string getname()const; //获取名字
    std::string getphonenumber()const; //获取电话号码
    std::string getemail()const; //获取电子邮箱
    std::string getaccount()const; //获取账号
    std::string getpassword()const; //获取密码
    std::vector< double > getface()const; //获取脸部信息
    std::vector < LocalCard > getEcard()const; //获取电子证书信息
    std::string getid()const; //获取ID
    void setname(const std::string & x); //设置新的名字
    void setphonenumber(const std::string & x); //设置新的电话号码
    void setemail(const std::string & x); //设置新的邮箱
    void setaccount(const std::string & x); //设置新的账号
    void setpassword(const std::string & x); //设置新的密码
    void setface(const std::vector < double > & x); //设置新的脸部信息
    void setEcard(const std::vector < LocalCard > & x); //设置新的电子证书
    std::string getunionstring()const; //获取用于哈希计算的字符串
    void emplace_back(const LocalCard & x); //添加新的电子证书
    int getnextid(); //获取新的电子证书的下标
    bool deleteindex( int x ); //删除电子证书
    bool checkindex( int x ); //认证电子证书
    void setid(const std::string & x); //设置id
    int person::getEcardSize()const; //获取证书数量
*/

extern std::map < std::string , std::string > IDPool; // ID池
extern std::map < std::string , std::pair < std::string , time_t > > TimePool; // 时间池
extern std::set < std::string > AuxiliaryTimePool ; // 辅助时间池

std::string person::getname()const{
    return this->name;
}

std::string person::getphonenumber()const{
    return this->phonenumber;
}

std::string person::getemail()const{
    return this->email;
}

std::string person::getaccount()const{
    return this->account;
}

std::string person::getpassword()const{
    return this->password;
}

std::vector < double > person::getface()const{
    return this->face;
}

std::vector < LocalCard > person::getEcard()const{
    return this->Ecard;
}

std::string person::getid()const{
    return this->ID;
}

void person::setname(const std::string & x){
    this->name = x;
}

void person::setphonenumber(const std::string & x){
    this->phonenumber = x;
}

void person::setemail(const std::string & x){
    this->email = x;
}

void person::setaccount(const std::string & x){
    this->account = x;
}

void person::setpassword(const std::string & x){
    this->password = x;
}

void person::setface(const std::vector < double > & x){
    this->face = x;
}

void person::setEcard(const std::vector < LocalCard > & x){
    this->Ecard = x;
}

std::string person::getunionstring() const{
    return name + account + password + email + phonenumber;
}

void person::emplace_back(const LocalCard & x){
    this->Ecard.emplace_back( x );
}

int person::getnextid(){
    return Ecard.empty() ? 0 : Ecard.back().cardindex + 1;
}

bool person::deleteindex( int x ){
    bool result = false;
    for( std::vector < LocalCard > :: iterator s = Ecard.begin() ; s != Ecard.end() ; ++ s ){
        if( s->cardindex == x ){
            Ecard.erase( s );
            result = true;
            break;
        }
    }
    return result;
}

bool person::checkindex( int x ){
    bool result = false;
    for( std::vector < LocalCard > :: iterator s = Ecard.begin() ; s != Ecard.end() ; ++ s ){
        if( s->cardindex == x ){
            if( s->checkflag == 0 )
                result = true;
            s->checkflag = 1;
            break;
        }
    }
    return result;
}

void person::setid(const std::string & x){
    this->ID = x;
}


std::string mallocID( const std::string & account ){
    std::string finalres;
    srand( time( NULL ) );
    do{
        finalres.clear();
        for(int i = 0 ; i < 32 ; ++ i){
            int z = rand()%26;
            if( rand()%2 ) z += 'a';
            else z += 'A';
            finalres.push_back( z );
        }
    }while( IDPool.count( finalres ) );
    IDPool[finalres] = account;
    return finalres;
}


bool findID( const std::string & x ){
    return IDPool.count( x );
}

int person::getEcardSize() const{
    return this->Ecard.size();
}

std::string mallocTimer( const std::string & account ){
    if( AuxiliaryTimePool.count( account ) ){
        AuxiliaryTimePool.erase( account );
        TimePool.erase( account );
    }
    std::string finalres;
    srand( time( NULL ) );
    do{
        finalres.clear();
        for(int i = 0 ; i < 12 ; ++ i){
            int z = rand()%10 + '0';
            finalres.push_back( z );
        }
    }while( AuxiliaryTimePool.count( finalres ) );
    AuxiliaryTimePool.insert( account );
    TimePool[account] = std::make_pair( finalres , clock() );
    return finalres;
}

bool Refresh( const std::string & account ){
    if( !TimePool.count( account ) )
        return false;
    std::pair < std::string , time_t > & fr = TimePool[account];
    fr.second = clock();
    return true;
}