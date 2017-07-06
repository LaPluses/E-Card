#include <block.h>

extern std::map < std::string , std::string > IDPool;

void HashTree::Construct( int l , int r ){
    std::function<void(HashTree::Node *& x ,int l , int r)> f = [&f](HashTree::Node *& x , int l , int r){
        x = new HashTree::Node;
        x->l = l , x->r = r;
        if( l < r ){
            int mid = l + r >> 1;
            f( x->lft , l , mid );
            f( x->rht , mid + 1 , r );
        }
    };
    f( this->root , l , r );
}

void HashTree::Delete(){
    std::queue < HashTree::Node * > que; 
    if( this->root )
        que.push( this->root );
    while( !que.empty() ){
        auto x = que.front() ; que.pop();
        if( x->lft )
            que.push( x->lft );
        if( x->rht )
            que.push( x->rht );
        delete x;
    }
}

void HashTree::Insert( int x , std::string y ){
    char * buf = new char[y.size()] , *p = buf;
    int buflen = ( int )y.size();
    for(auto it : y) *p++=it;
    HashTree::Node * cur = this->root;
    while( 1 ){
        int l = cur->l, r = cur->r;
	    sm3_update(&cur->ctx, (unsigned char *)buf, buflen);
        if( l < r ){
            int mid = l + r >> 1;
            if( x <= mid )
                cur = cur->lft;
            else
                cur = cur->rht;
        }else
            break;
    }
    delete []buf;
}

std::string Sm3ContextToString( sm3_context ctx ){
    unsigned char output[32];
    sm3_finish(&ctx, output);
    char buff[256], *p = buff;
    for (int i = 0; i < 32; ++i, p += 2) sprintf(p, "%.2x", output[i]);
    std::string ret;
    for (char * s = buff; s < p; ++s) ret.push_back(*s);
    return ret;
}

void block::emplace_back( const person & x ){
    if( ( int )data.size() == ( 1 << cursize ) ){
        Tree.Delete();
        ++ cursize;
        Tree.Construct(0 , (1 << cursize) - 1 );
        int s = 0;
        for(auto & it : data) Tree.Insert( s++ , it.getunionstring() );
    }
    data.emplace_back( x );
    lstoccurance[x.getaccount()] = data.size() - 1;
    Tree.Insert( data.size() - 1 , x.getunionstring() );
}

int block::size(){
    return data.size();
}

int block::maxsize(){
    return mxsize;
}

std::pair < person , bool > block::find( const std::string & account ){
    if( lstoccurance.count( account ) )
        return std::make_pair( data[lstoccurance[account]] , true );
    return std::make_pair( person() , false );
}

std::vector < person > block::getdata(){
    return data;
}

std::pair < person , bool > chainblock::find( const std::string account ){
    chainblock::Locker.lock();
    std::pair < person , bool > result = std::make_pair( person() , false );
    for( int i = chainblock::List.size() - 1 ; i >= 0 && result.second == false ; -- i )
        result = chainblock::List[i].find( account );
    chainblock::Locker.unlock();
    return result;
}

void chainblock::emplace_back( const person & x ){
    chainblock::Locker.lock();
    if( chainblock::List.empty() || chainblock::List.back().maxsize() == chainblock::List.back().size() )
        chainblock::List.emplace_back( block() );
    IDPool[x.getid()] = x.getaccount();
    chainblock::List.back().emplace_back( x );
    chainblock::Locker.unlock();
}

void * mallockey( int length ){
    unsigned x = 123456789 , y = 362436069 , z = 521288629 , w = 88675111;
    std::function < unsigned( unsigned & , unsigned & , unsigned & , unsigned & ) > f = []( unsigned & x , unsigned & y , unsigned & z , unsigned & w ){
        	unsigned t = x ^ (x << 11);
	        x = y; y = z; z = w;
	        w = (w ^ (w >> 19)) ^ (t ^ (t >> 8));
	        return w % 256;
    };
    void * s = malloc( length ) , *p = s;
    for(int i = 0 ; i < length ; ++ i , p = (void * )((unsigned char *) p + 1 ) )
        *((unsigned char *)p) = f( x , y , z , w );
    return s;
}

void chainblock::write( std::string address ){
    chainblock::Locker.lock();
    for(int i = 0 ; i < chainblock::List.size() ; -- i){
        std::vector < person > data = chainblock::List[i].getdata();
        std::fstream fs;
        SaveData output;
        fs.open( address + "blockdata" + std::to_string( i ) + ".bs" , std::ios::binary | std::ios::out );
       // if( DebugMode )
       //      printf( "Ready to Write block %d\n" , i );
        for(auto x : data){
            Data * S = output.add_vector();
          //  printf( "Ready to write person %s %s \n " , x.getaccount().c_str() , x.getid().c_str() );
            S->set_account(x.getaccount());
            S->set_name(x.getname());
            S->set_email(x.getemail());
            S->set_phone_number(x.getphonenumber());
            S->set_password(x.getpassword());
            S->set_id(x.getid());
           // printf( "Stage 1 complete \n " );
            std::vector < double > photoinfo = x.getface();
            for (auto it : photoinfo) S->add_photo_info(it);
            std::vector < LocalCard > CardData = x.getEcard();
            //printf( "Stage 2 complete \n " );
            for (auto it : CardData) {
                Card * F = S->add_cardinfo();
                F->set_cardid(it.cardtype);
                F->set_carddata(it.cardinfo);
                F->set_cardindex(it.cardindex);
                F->set_checkflag(it.checkflag);
                for (auto trs : it.fields)
                    *(F->add_fields()) = trs;
            }
           // printf( "Stage 3 complete \n " );
        }
        int finallength = output.ByteSize();
       // std::cout << "finallength is " << finallength << std::endl;
        void * key = mallockey( finallength ) , * buf = malloc( finallength );
        output.SerializeToArray( buf , finallength );
        for( unsigned char * s = (unsigned char *)buf , * p = (unsigned char *)key ; s - (unsigned char*)buf < finallength ; ++ s , ++ p ){
            *s ^= *p;
            fs << *s;
        }
       // std::cout << std::endl;
        free( buf );
        free( key );
        fs.close();
    }
    chainblock::Locker.unlock();
}

void chainblock::read( std::string address ){
    int mxnumber = 0;
    while( 1 ){
        std::fstream fs;
        fs.open( address + "blockdata" + std::to_string( mxnumber ) + ".bs" , std::ios::binary | std::ios::in );
        if( !fs )
            break;
        fs.close();
        ++ mxnumber;
    }
    if( DebugMode )
        printf( "in current disk , the number of data is %d\n " , mxnumber );
    for(int i = 0 ; i < mxnumber ; ++ i){
        std::fstream fs;
        fs.open( address + "blockdata" + std::to_string( i ) + ".bs" , std::ios::binary | std::ios::in );
        fs.seekg (0, fs.end);
    	int length = fs.tellg();
    	fs.seekg (0, fs.beg);
        char * buffer = new char [length];
        void * key = mallockey( length );
        fs.read( buffer , length );
        for( unsigned char * s = (unsigned char *)buffer , * p = (unsigned char *)key ; s - (unsigned char*)buffer < length ; ++ s , ++ p ) *s ^= *p;
        SaveData F;
        F.ParsePartialFromArray( buffer , length );
        delete []buffer;
        free( key );
        for (int j = 0; j < F.vector_size(); ++ j) {
            Data G = F.vector(j);
            person add;
            add.setaccount(G.account());
            add.setemail(G.email());
            add.setname(G.name());
            add.setpassword(G.password());
            add.setphonenumber(G.phone_number());
            add.setid(G.id());
            std::vector < double > face;
            for (int k = 0; k < G.photo_info_size(); ++k)
                face.emplace_back(G.photo_info(k));
            std::vector < LocalCard > Ecard;
            for (int k = 0; k < G.cardinfo_size(); ++k) {
                Card ff = G.cardinfo(k);
                std::vector < std::string > fid;
                std::string fq = ff.carddata();
                for (auto it : ff.fields()) fid.emplace_back(it);
                Ecard.emplace_back(LocalCard(ff.cardid(), fq, fid,ff.cardindex(),ff.checkflag() ));
            }
            add.setEcard(Ecard);
            add.setface(face);
            chainblock::emplace_back( add );
	    }
        fs.close();
        const char * p = ( address + "blockdata" + std::to_string( i ) + ".bs").c_str();
        std::cout << "Str is " << p << std::endl;
        std::remove( p );
    }
     if( DebugMode )
        printf( "Load Data Complete \n " );
}


std::vector < person > chainblock::transform(){
    std::vector < person > finalres;
    std::function < std::vector < person > ( const std::vector < person > & x , const std::vector < person > & y ) > add = [](const std::vector < person > & x , const std::vector < person > & y){
        std::vector < person > ret = x;
        for(auto & it : y) ret.emplace_back( it );
        return ret;
    };
    for(auto & it : List) finalres = add( finalres , it.getdata() );
    return finalres;
}