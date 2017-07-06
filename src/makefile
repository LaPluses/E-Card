Clean: main
	rm *.o
	rm *.gch
main: main.cc socket.o MessageUtil.pb.o SaveData.pb.o Cards.pb.o sm3.o sm4.o block.o person.o
	g++ -o main main.cc socket.o MessageUtil.pb.o SaveData.pb.o Cards.pb.o sm3.o sm4.o block.o person.o -lprotobuf -pthread -I /home/xiper/Desktop/project -std=c++0x -O2
person.o: person.cc person.h
	g++ -c person.cc person.h -I /home/xiper/Desktop/project -std=c++0x -O2
block.o: block.cc block.h person.o
	g++ -c block.cc block.h -L person.o -I /home/xiper/Desktop/project -std=c++0x -O2
socket.o: socket.cc socket.h block.o
	g++ -c socket.cc socket.h -L block.o -I /home/xiper/Desktop/project -std=c++0x -O2
MessageUtil.o: MessageUtil.pb.cc MessageUtil.pb.h
	g++ -c MessageUtil.pb.cc MessageUtil.pb.h
SaveData.o: SaveData.pb.cc SaveData.pb.h
	g++ -c SaveData.pb.cc SaveData.pb.h
Cards.o: Cards.pb.cc Cards.pb.h
	g++ -c Cards.pb.cc Cards.pb.h
sm3.o: sm3.cc sm3.h
	g++ -c sm3.cc sm3.h -I /home/xiper/Desktop/project -std=c++0x -O2
sm4.o: sm4.cc sm4.h
	g++ -c sm4.cc sm4.h -I /home/xiper/Desktop/project -std=c++0x -O2