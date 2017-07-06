#ifndef __blockh
    #define __blockh

    #include <sm3.h>
    #include <person.h>

    class HashTree{
        private:
            struct Node{
                Node * lft , * rht;
                int l , r;
                sm3_context ctx;

                Node(){
                    lft = rht = NULL , l = r = 0;
                    sm3_starts( &ctx );
                }

            };
            Node * root;
        public:
            HashTree(){root = NULL;}
            void Construct( int l , int r ); // 构造出一颗区间为[l,r]的哈希树
            void Delete(); //删除整颗哈希树
            void Insert( int x , std::string y ); //插入一个哈希值
    };



    class block{
        private:
           HashTree Tree; // 哈希树
           std:: map < std::string , int > lstoccurance; // 索引表
           std:: vector < person > data; // 数据
           int mxsize , cursize;
        public:
            block(){
                mxsize = rand() % 8 + 4; // 随机指定块的最大大小
                cursize = 4;
                Tree.Construct( 0 , (1 << cursize) - 1 );
            }

            void emplace_back( const person & x ); // 插入某一个人的信息到区块末端
            std::pair < person , bool > find( const std::string & account ); //查询某账号的最新记录
            std::vector < person > getdata(); //获取data 
            int size(); //获取区块大小
            int maxsize(); //获取区块最大大小
    };

    std::string Sm3ContextToString( sm3_context x ); // 获取哈希值

    class chainblock{
        private:
            std::vector < block > List; //总区块链
            std::mutex Locker; //线程锁
        public:
            void emplace_back( const person & x ); //区块插入
            std::pair < person , bool > find( const std::string account ); //区块链中查某人
            void write( std::string address = "./" ); // 区块信息写出
            void read( std::string address = "./" ); // 区块信息读取
            std::vector < person > transform() ; // 输出区块信息
    };

    void * mallockey( int length ); // 产生秘钥

#endif