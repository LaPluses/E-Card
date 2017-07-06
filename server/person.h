#ifndef __personh
    #define __personh

    #include <header.h>

    struct LocalCard{
        std::vector < std::string > fields; //证书的额外信息
        std::string cardinfo; //证书的图片
        int cardtype; //证书种类
        int cardindex; //证书的存储逻辑下标
        int checkflag; //证书是否已经被验证
        LocalCard(int cardtype = 0, std::string cardinfo = "", std::vector < std::string > fields = std::vector < std::string >() , int cardindex = 0 , int checkflag = 0 ) :
		    cardtype(cardtype), cardinfo(cardinfo), fields(fields) , cardindex(cardindex) , checkflag( checkflag ) {}
    };

    class person{
        private:
            std::string name; //名字
            std::string phonenumber; //电话
            std::string email; //邮箱
            std::string account; //账号
            std::string password; //密码
            std::string ID; //ID号
            std::vector < double > face; //脸部信息
            std::vector < LocalCard > Ecard; //电子证书
        public:
            person( std::string name = "",
                    std::string phonenumber = "",
                    std::string email = "",
                    std::string account = "",
                    std::string password = "",
                    std::vector < double > face = std::vector < double >(),
                    std::vector < LocalCard > Ecard = std::vector < LocalCard >(),
                    std::string ID = ""
            ){
                    this->name = name,
                    this->phonenumber = phonenumber,
                    this->email = email,
                    this->account = account,
                    this->password = password,
                    this->face = face,
                    this->Ecard = Ecard;
                    this->ID = ID;
            };

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
            int getEcardSize()const; //获取证书数量
    };

    std::string mallocID( const std::string & account ); //分配新的id
    bool findID( const std::string & x ); // 查询ID是否存在
    std::string mallocTimer( const std::string & account ); // 分配新的时间戳
    bool Refresh( const std::string & account ); //刷新
    
#endif