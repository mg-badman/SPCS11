
// v0.2
// miroslav georgiev

// note, multiple imsg messages can arrive together in the socket buffer
// only the first one will be parsed, the algorithm doesnt check for remaining data (yet)

//#include <t
//#include <winsock.h>



#ifndef IMSG_MAIN
#define IMSG_MAIN

//#include <Windows.h>

#include "imsgData.h"
#include "otherfuncs.h"
#include "imsgDataDef.h"
#include "arrfuncs.h"
#include "imsgClient.h"
#include "imsgEvent.h"

//#include <thread>

	class imsg {

	public:

		static const int IMSG_TYPE_SERVER=0;
		static const int IMSG_TYPE_CLIENT=1;
		static const int IMSG_TYPE_NONE=-1;

		int imsg_type; //=IMSG_TYPE_NONE;

		SOCKET			CLsock; // socket that imsg client uses
		SOCKET			SVsock;
		char *			IPAddr;
		int				port;

		imsgEvent*		events;

		BYTE *	 		imsgBuff; // data send buffer
		int 			byteindx; // byte count for data send buffer
		char * 			imsgName; // name of the Imessage to be sent
		char *			imsgOrder; // order of the data in the data section
		int				imsgOrderLen; // length of the imsg order array

		//SocketAddress bindAddress;

		imsgClient ** clients; // if its a server, it's gonna need this
		bool clientsArrLocked;

		byte ** recvQueue;
		byte ** sendQueue;

		HANDLE SendThread;
		HANDLE RecvThread;
		HANDLE AcceptThread;
		HANDLE MainImsgThread;


		int packetIDfailCount;
		int packetTotalFailCount;
		int TotalPacketCount;
	
		boolean datadbg;

	//	imsg();
		int nextFreeIndex();

		bool initServer(int port);

		bool initClient(char * ip,int port);

		int MAX_CLIENTS;
		int recvbuflen; // receive buffer size


		void Start(char * name);  // begin a networked message

		void WriteInt(int num); // write an int to a networked message
	
		void WriteString(char * str); // write an int to a networked message

		void WriteFloat(float num);
		//////////////////////////////////////////////////////////
		///////////////////   prepares the packet data
		//////////////////////////////////////////////////////////

		BYTE * imsgPrepareDataToSend();
		void SendToServer(); // send networked message to server (if client)
	
		void Send(imsgClient * cl); // send networked message to client (if server)
	
		void Send(int ID); // send networked message to client (if server)
	
		void Broadcast(); // send networked message to all clients (if server)
	

		void onMessageReceived(int id, char * msg, int msgsiz);
		void onMessageReceivedCL(char * msg, int msgsiz);


		////////////////////////////////////////



		VerifiedIMSGPacket * ParseIMSGPacket(BYTE * data, int datasiz);

		BYTE * ComposeData(int * sizout);

		void AddEvent(char * evntname, DWORD ev);

	private:
		void initializeThreads();

	};


//imsg::imsg(){
	//events = new imsgEvent();
//}

int imsg::nextFreeIndex(){
	for(int k=0;k<MAX_CLIENTS;k++){
		if(clients[k]->active == false ){
			return k;
		}
	}
	return -1;
}


bool imsg::initServer(int port){
	imsg_type = IMSG_TYPE_SERVER;
	MAX_CLIENTS=64;
	int iResult=0;
	WSADATA wsaData;
	SOCKET ListenSocket;
	recvbuflen = 262144;
    
	// Initialize Winsock
	iResult = WSAStartup(MAKEWORD(2,2), &wsaData);
	if (iResult != 0) {
		printf("WSAStartup failed with error: %d\n", iResult);
		return 1;
	}
	struct addrinfo *result = NULL;
    struct addrinfo hints;
	ZeroMemory(&hints, sizeof(hints));
	hints.ai_family = AF_INET;
	hints.ai_socktype = SOCK_STREAM;
	hints.ai_protocol = IPPROTO_TCP;
	hints.ai_flags = AI_PASSIVE;

	// Resolve the server address and port
	char portnum[10];
	itoa(
		port,
		portnum,
		10
	);
	printf("Listening on port ");
	printf(portnum);
	printf("\n");

	iResult = getaddrinfo(NULL, portnum, &hints, &result);
	if ( iResult != 0 ) {
		printf("getaddrinfo failed with error: %d\n", iResult);
		WSACleanup();
		return 1;
	}

	// Create a SOCKET for connecting to server
	ListenSocket = socket(result->ai_family, result->ai_socktype, result->ai_protocol);
	if (ListenSocket == INVALID_SOCKET) {
		printf("socket failed with error: %ld\n", WSAGetLastError());
		freeaddrinfo(result);
		WSACleanup();
		return 1;
	}

	// Setup the TCP listening socket
	iResult = bind( ListenSocket, result->ai_addr, (int)result->ai_addrlen);
	if (iResult == SOCKET_ERROR) {
		printf("bind failed with error: %d\n", WSAGetLastError());
		freeaddrinfo(result);
		closesocket(ListenSocket);
		WSACleanup();
		return 1;
	}

	freeaddrinfo(result);

	iResult = listen(ListenSocket, SOMAXCONN);
	if (iResult == SOCKET_ERROR) {
		printf("listen failed with error: %d\n", WSAGetLastError());
		closesocket(ListenSocket);
		WSACleanup();
		return 1;
	}
	
	u_long iMode=1;
	ioctlsocket(ListenSocket,FIONBIO,&iMode);

	
	
	SVsock = ListenSocket;
	clients =  new imsgClient*[MAX_CLIENTS];
	for(int k=0;k<MAX_CLIENTS;k++){
		clients[k] = new imsgClient();
	}
	initializeThreads();
	return true;
}



bool imsg::initClient(char * ip,int port){
	imsg_type = IMSG_TYPE_CLIENT;
	int iResult=0;
	WSADATA wsaData;
	SOCKET ConnectSocket;
	iResult = WSAStartup(MAKEWORD(2,2), &wsaData);
    if (iResult != 0) {
        printf("WSAStartup failed with error: %d\n", iResult);
		
		//cin.get();
        return 1;
    }
	struct addrinfo *result = NULL,
                    *ptr = NULL,
                    hints;
    ZeroMemory( &hints, sizeof(hints) );
    hints.ai_family = AF_UNSPEC;
    hints.ai_socktype = SOCK_STREAM;
    hints.ai_protocol = IPPROTO_TCP;
	
	char * portnum;
	_itoa_s(
		port,
		portnum,
		1,
		10 
	);
	 
	iResult = getaddrinfo(ip, portnum, &hints, &result);
    if ( iResult != 0 ) {
        printf("getaddrinfo failed with error: %d\n", iResult);
        WSACleanup();
        return 0;
    }
	// Attempt to connect to an address until one succeeds
    for(ptr=result; ptr != NULL ;ptr=ptr->ai_next) {
		
		// Create a SOCKET for connecting to server
        ConnectSocket = socket(ptr->ai_family, ptr->ai_socktype, 
            ptr->ai_protocol);

		
		if (ConnectSocket == INVALID_SOCKET) {
            printf("socket failed with error: %ld\n", WSAGetLastError());
            WSACleanup();
			
			//cin.get();
            return 0;
        }
		
		// Connect to server.
		printf("Connecting to server.....");
		iResult = connect( ConnectSocket, ptr->ai_addr, (int)ptr->ai_addrlen);
        
		if (iResult == SOCKET_ERROR) {
			printf("err");
            closesocket(ConnectSocket);
            ConnectSocket = INVALID_SOCKET;
            continue;
        }
        break;
    }
	
    freeaddrinfo(result);
	CLsock = ConnectSocket;
	initializeThreads();
	return true;
}

void imsg::Start(char * name){  // begin a networked message
	imsgBuff = new byte[60000];
	byteindx=0;
	imsgName=name;
}

void imsg::WriteInt(int num){ // write an int to a networked message
	byte * xbytes = int2bytes(num);
	imsgBuff = byteArrConcat(xbytes,imsgBuff,byteindx,4);
	byteindx+=4;

	imsgOrder=(char*)byteArrConcat((BYTE *)imsgOrder,(BYTE *)"i",imsgOrderLen,1);
	imsgOrderLen+=1;
	
	
}
	
void imsg::WriteString(char * str){ // write an int to a networked message
	int strle=strlen(str);
	byte * xbytes = int2bytes(strle);
	imsgBuff=byteArrConcat(xbytes,imsgBuff,byteindx,4);
	byteindx+=4;
	imsgBuff=byteArrConcat((BYTE*)str,imsgBuff,byteindx,strle);
	byteindx+=strle;

	
	imsgOrder=(char*)byteArrConcat((BYTE *)imsgOrder,(BYTE *)"s",imsgOrderLen,1);
	imsgOrderLen+=1;

}

void imsg::WriteFloat(float num){
	byte * xbytes = float2bytes(num);
	imsgBuff=byteArrConcat(xbytes,imsgBuff,byteindx,4);
	byteindx+=4;
	
	
	imsgOrder=(char*)byteArrConcat((BYTE *)imsgOrder,(BYTE *)"f",imsgOrderLen,1);
	imsgOrderLen+=1;

}
	//////////////////////////////////////////////////////////
	///////////////////   prepares the packet data
	//////////////////////////////////////////////////////////

BYTE * imsg::imsgPrepareDataToSend(){
		int clientID=5;
		int datablocksiz=0;
		BYTE * data = ComposeData(&datablocksiz);
		int md5lentemp=0;
		const char * md5 =  getMD5((char*)data,byteindx,&md5lentemp);

		int baseindx=0;
		
		BYTE * packetID = 		int2bytes(20); // todo
		BYTE * clIDdata= 		int2bytes(clientID);
		BYTE * md5len= 			int2bytes(strlen(md5));
		BYTE * md5data = 		(BYTE*)md5;
		BYTE * datalen= 		int2bytes(datablocksiz);
		BYTE * bytedata = 		data;

		byte * temp = new byte[0];
		
		int count=0;
		temp = byteArrConcat(temp,packetID,count,4);
		count+=4;
		temp = byteArrConcat(temp,packetID,count,4);
		count+=4;
		temp = byteArrConcat(temp,packetID,count,4);
		count+=4;
		temp = byteArrConcat(temp,packetID,count,4); //4 times
		count+=4;
		temp = byteArrConcat(temp,clIDdata,count,4);
		count+=4;
		temp = byteArrConcat(temp,clIDdata,count,4); // 2 times
		count+=4;
		temp = byteArrConcat(temp,md5len,count,4);
		count+=4;
		temp = byteArrConcat(temp,md5len,count,4); // 2 times
		count+=4;
		temp = byteArrConcat(temp,md5data,count,md5lentemp); // 1 time
		count+=md5lentemp;
		temp = byteArrConcat(temp,datalen,count,4);
		count+=4;
		temp = byteArrConcat(temp,datalen,count,4); // 2 times
		count+=4;
		temp = byteArrConcat(temp,bytedata,count,datablocksiz); // 1 time
		count+=datablocksiz;
		return temp;
	
}

void imsg::SendToServer(){ // send networked message to server (if client)
	if(imsg_type == IMSG_TYPE_CLIENT){
		byte * senddata; // = new byte[4+4+md5data.length+4+bytedata.length];
		senddata=imsgPrepareDataToSend();
		if(datadbg){
			printf("CLIENT: ");
			printbytearr(senddata);
		}

		//println("[["+new String(senddata)+"]]");

		int iResult = send( CLsock, (char*)senddata, strlen((char*)senddata) /*(int)strlen(input)*/, 0 );
		if (iResult == WSASYSNOTREADY) {
			printf("network subsystem is unavailable: %d\n", WSAGetLastError());
			closesocket(CLsock);
			WSACleanup();
			getchar();
		}
		if (iResult == SOCKET_ERROR) {
			printf("epic fail: %d\n", WSAGetLastError());
			closesocket(CLsock);
			WSACleanup();
			getchar();
		}

	}else{
		printf("ERROR: A Server cannot send data to the server.");
	}

}
void imsg::Send(imsgClient* cl){ // send networked message to client (if server)
		if(imsg_type == IMSG_TYPE_SERVER){
			byte * senddata; // = new byte[4+4+md5data.length+4+bytedata.length];
			senddata=imsgPrepareDataToSend();
			if(datadbg){
				printf("CLIENT: ");
				printbytearr(senddata);
			}

			//println("[["+new String(senddata)+"]]");

			int iResult = send( cl->socket, (char*)senddata, strlen((char*)senddata) /*(int)strlen(input)*/, 0 );
			if (iResult == WSASYSNOTREADY) {
				printf("network subsystem is unavailable: %d\n", WSAGetLastError());
				closesocket(CLsock);
				WSACleanup();
				getchar();
			}
			if (iResult == SOCKET_ERROR) {
				printf("epic fail: %d\n", WSAGetLastError());
				closesocket(CLsock);
				WSACleanup();
				getchar();
			}	
		}else{
			printf("Error: attempted to invoke \"Send()\" using a client imsg object  ");
		}
}

void imsg::Send(int ID){ // send networked message to client (if server)
	Send(clients[ID]);
}

void imsg::Broadcast(){ // send networked message to all clients (if server)
	if(imsg_type == IMSG_TYPE_SERVER){
		for(int k=0;k<MAX_CLIENTS;k++){
			if( clients[k]->active== true){
				Send(k);
			}
		}
	}else{
		printf("Error: attempted to invoke \"Broadcast()\" using a client imsg object");
	}
}

void imsg::onMessageReceived(int id, char * msg, int msgsiz){
	VerifiedIMSGPacket * verpacket=ParseIMSGPacket((BYTE*)msg,msgsiz);
	if(verpacket->isValid()){
		int clientID = verpacket->clientID; //bytes2int(new byte[]{result[3],result[2],result[1],result[0]});
		
		char * md5= verpacket->md5;
		printf(md5);
		printf("\n");

		BYTE * data = verpacket->data;
		int thismd5len = verpacket->dataLen;
		printf("datalen: %d",thismd5len);
		printf("\n");
		int md5lenout=0;
		const char * thismd5 = getMD5((char*)data,verpacket->dataLen,&md5lenout);
		printf(thismd5);
		printf("\n");
		if(!(cmpstr((char *)thismd5,md5))){
			packetTotalFailCount++;
			printf("ERROR MD5 DOES NOT MATCH DATA's MD5!  x%d",packetTotalFailCount);
			// todo- handler to request the data to be resent
			return;
		}

		imsgData d = ParseDataBlock(data, verpacket->dataLen);

		events->Call(d.name,d,id);

	}else{
		printf("data in packet %d has errorz",verpacket->packetID);
	}
}

void imsg::onMessageReceivedCL(char * msg, int msgsiz){
	int id=-1; // this shows its a message from server
	//System.out.println("Client["+id+"]: "+msg);
	//Thread.currentThread().sleep(1000);
	//this.clients[id].socket.getOutputStream().write("WAWA".getBytes());
	VerifiedIMSGPacket * verpacket=ParseIMSGPacket((BYTE*)msg,msgsiz);
	if(verpacket->isValid()){
		int clientID = verpacket->clientID; //bytes2int(new byte[]{result[3],result[2],result[1],result[0]});

		char * md5= verpacket->md5;
		BYTE * data = verpacket->data;
		
		int thismd5len = verpacket->dataLen;
		int md5lenout;
		const char * thismd5 = getMD5((char*)data,thismd5len,&md5lenout);

		if(!(cmpstr((char *)thismd5,md5))){
			packetTotalFailCount++;
			printf("ERROR MD5 DOES NOT MATCH DATA's MD5!  x%d",packetTotalFailCount);
			// todo- handler to request the data to be resent
			return;
		}

		imsgData d = ParseDataBlock(data, verpacket->dataLen);
		events->Call(d.name,d,id);
	}else{
		printf("data in packet %d has errorz",verpacket->packetID);
    }
}


	////////////////////////////////////////



VerifiedIMSGPacket * imsg::ParseIMSGPacket(BYTE * data, int datasiz){
	byte ** temp; //= new byte[2][];
	temp = splitFirstNBytes(data,datasiz,4);
	datasiz-=4;
	data=temp[1];

	int packetID1 = bytes2int(temp[0]); // get packet id
	
	temp = splitFirstNBytes(data,datasiz,4);
	datasiz-=4;
	data=temp[1];
	int packetID2 = bytes2int(temp[0]); //

	temp = splitFirstNBytes(data,datasiz,4);
	datasiz-=4;
	data=temp[1];
	int packetID3 = bytes2int(temp[0]); //
	
	temp = splitFirstNBytes(data,datasiz,4);
	datasiz-=4;
	data=temp[1];
	int packetID4 = bytes2int(temp[0]); //
	
	if(packetID1==packetID2 & packetID2==packetID3 & packetID3==packetID4){
	      //no problem
	}else{
	      //big ass problem
		packetIDfailCount++;
		printf("ERROR: PACKET ID IS INVALID, MESSAGE LOST FOREVER...  x%d",packetIDfailCount);

		return new VerifiedIMSGPacket();
	}
	    //println("packetID1: "+packetID1);
	temp = splitFirstNBytes(data,datasiz,4);
	datasiz-=4;
	data=temp[1];
	int clientID1 = bytes2int(temp[0]); // parse clientid
	
	temp = splitFirstNBytes(data,datasiz,4);
	datasiz-=4;
	data=temp[1];
	int clientID2 = bytes2int(temp[0]); //
	
	if(clientID1==clientID2){

	}else{
		//problem
		VerifiedIMSGPacket * a=new VerifiedIMSGPacket();
		a->packetID=packetID1;
	}



	temp = splitFirstNBytes(data,datasiz,4);
	datasiz-=4;
	data=temp[1];
	int md5len1 = bytes2int(temp[0]); // md5 length

	temp = splitFirstNBytes(data,datasiz,4);
	datasiz-=4;
	data=temp[1];
	int md5len2 = bytes2int(temp[0]); //
	    
	if(md5len1==md5len2){

	}else{
		//problem
		VerifiedIMSGPacket * a=new VerifiedIMSGPacket();
		a->packetID=packetID1;
	}


	temp = splitFirstNBytes(data,datasiz,md5len1);
	datasiz-=md5len1;
	data=temp[1];
	char * md5 = new char[md5len1+1];
	for(int k=0;k<md5len1;k++){
		md5[k] = temp[0][k]; // md5
	}
	md5[md5len1]='\0';


	temp = splitFirstNBytes(data,datasiz,4);
	datasiz-=4;
	data=temp[1];
	int datalen1 = bytes2int(temp[0]); // data length
	    
	temp = splitFirstNBytes(data,datasiz,4);
	datasiz-=4;
	data=temp[1];
	int datalen2 = bytes2int(temp[0]); //
	    
	if(datalen1==datalen2){

	}else{
		//problem
		VerifiedIMSGPacket * a=new VerifiedIMSGPacket();
		a->packetID=packetID1;
	}

	temp = splitFirstNBytes(data,datasiz,datalen1);
	datasiz-=datalen1;
	data=temp[1];
	BYTE * datablock = temp[0]; // parse data
	    
	VerifiedIMSGPacket * r = new VerifiedIMSGPacket();
	r->packetID=packetID1;
	r->clientID=clientID1;
	r->md5=md5;
	r->md5Len = md5len1;
	r->data=datablock;
	r->dataLen=datalen1;
	r->setValid(true);
	return r;
}

BYTE * imsg::ComposeData(int * sizout){
	BYTE * imsgNameLen = int2bytes(strlen(imsgName));
	BYTE * imsgNameData = (BYTE*)imsgName;

	BYTE * imsgOrderLen = int2bytes(this->imsgOrderLen);
	BYTE * imsgOrderData= (BYTE*)imsgOrder;

	BYTE * imsgBuffData = new BYTE[byteindx];
	//System.arraycopy(imsgBuff,0,imsgBuffData,0,byteindx);
	memcpy(imsgBuffData,imsgBuff,byteindx);
	BYTE * temp = new byte[0];
	int count = 0;
	temp = byteArrConcat(temp,imsgNameLen,count,4);
	count+=4;
	temp = byteArrConcat(temp,imsgNameData,count,strlen(imsgName));
	count+=strlen(imsgName);
	temp = byteArrConcat(temp,imsgOrderLen,count,4);
	count+=4;
	temp = byteArrConcat(temp,imsgOrderData,count,this->imsgOrderLen);
	count+=this->imsgOrderLen;
	temp = byteArrConcat(temp,imsgBuffData,count,byteindx);
	count+=byteindx;
	sizout = &count;
	//println("DATA BLOCK SIZE: "+temp.length);
	//println("******************");
	//byteprint(temp);
	//println("******************");
	return temp;
}

void imsg::AddEvent(char * evntname, DWORD ev){
	events->Add(evntname, ev);
}


void cl_RecvThr(void * obj){
	imsg * thisObj= (imsg *)obj;
	while(true){
		//thisObj.CLsock.setSoTimeout(1);
		Sleep(60); // battery saving
		while( thisObj->clientsArrLocked ){}
		thisObj->clientsArrLocked = true;
		//int asd = thisObj.CLsock.getReceiveBufferSize();
		
		char * buff=new char[thisObj->recvbuflen];
		int iResult = recv(thisObj->CLsock , buff, thisObj->recvbuflen, 0);
			
		if(iResult == SOCKET_ERROR){
			delete[]buff;
			continue;
		}
		if(iResult == 0){
			delete[]buff;
			continue;
		}

		char * msg= new char[iResult];
		for(int k=0;k<iResult;k++){
			msg[k]=buff[k];
		}

		delete[]buff; // deallocate them kilobytes
			
		int datalen = iResult; //clients[k].socket.getInputStream().read(buff);
		
		if(iResult<4){
			printf("WTFFFFFFFFFFF"); //not even one integer was processed
		}
		thisObj->onMessageReceivedCL(msg,datalen);
			
		delete[]msg;
		////// MESSAGE RECEIVED
		
		
		
		thisObj->clientsArrLocked = false;
	}
}

void sv_AcceptThr(void * obj){
	imsg * thisObj = (imsg *)obj;
	while(true){
		Sleep(60); // battery saving
		SOCKET client = accept(thisObj->SVsock, NULL, NULL);	
		if (client == INVALID_SOCKET) {
			continue;
		}
		imsgClient * cl = new imsgClient(client);
		//cl.socket=client;
		//cl.
		//client.setSoTimeout(10);
		printf("client connected [todo: accept event]\n");
		while( thisObj->clientsArrLocked ){}
		thisObj->clientsArrLocked = true;
		int freeid=thisObj->nextFreeIndex();
		if(freeid!=-1){
			thisObj->clients[freeid] = cl;
			//clients = arrfuncs.addToArray(clients,cl);
			//clients[0] = cl;
		}else{
			printf("max clients reached [todo: reply event]");
		}
		thisObj->clientsArrLocked = false;
	}
}

void sv_RecvThr(void * obj){
	imsg * thisObj = (imsg *)obj;
	while(true){
		Sleep(60); // battery saving
		while( thisObj->clientsArrLocked ){}
		thisObj->clientsArrLocked = true;
		for(int k=0;k<thisObj->MAX_CLIENTS;k++){
			if (thisObj->clients[k]->active != true){
				continue;
			}
			//thisObj.clients[k].socket.setSoTimeout(1);
			//int asd = clients[k].socket.getReceiveBufferSize();
			//System.out.println(asd);
			char * buff=new char[thisObj->recvbuflen];
			int iResult = recv(thisObj->clients[k]->socket, buff, thisObj->recvbuflen, 0);
			
			if(iResult == SOCKET_ERROR){
				delete[]buff;
				continue;
			}
			if(iResult == 0){
				delete[]buff;
				continue;
			}

			char * msg= new char[iResult];
			for(int v=0;v<iResult;v++){
				msg[v]=buff[v];
			}

			delete[]buff; // deallocate them kilobytes
			
			int datalen = iResult; //clients[k].socket.getInputStream().read(buff);
			int tempCLIENT_ID=k;
			if(iResult<4){
				printf("WTFFFFFFFFFFF"); //not even one integer was processed
			}
			thisObj->onMessageReceived(tempCLIENT_ID,msg,datalen);
			
			delete[]msg;
			////// MESSAGE RECEIVED
		}
		thisObj->clientsArrLocked = false;
	}
}


void imsg::initializeThreads(){
		events = new imsgEvent();
//		std::thread first (foo);     // spawn new thread that calls foo()
//		std::thread second (bar,0);  // spawn new thread that calls bar(0)

		if(imsg_type == IMSG_TYPE_CLIENT){
			 ////////////////////////
			///                    ///
			 ////////////////////////

			RecvThread = (HANDLE)_beginthread( cl_RecvThr, 0, this);
			//RecvThread.start();

		}else if(imsg_type == IMSG_TYPE_SERVER){

			///////////////////////////
			///// Accept Thread ///////
			///////////////////////////

			AcceptThread = (HANDLE)_beginthread( sv_AcceptThr, 0, this);
			//AcceptThread.start();


			 ////////////////////////
			///   recieve thread   ///
			 ////////////////////////
			RecvThread = (HANDLE)_beginthread( sv_RecvThr, 0, this);
			//RecvThread.start();

		}
}


imsgClient::imsgClient(){
	active=false;
}

imsgClient::imsgClient(SOCKET s){
	socket=s;

	sockaddr client_info = {0};
	int addrsize = 0;
	getpeername(socket, &client_info, &addrsize);
	IP = client_info.sa_data;
	
	struct sockaddr_in sin;
	socklen_t len = sizeof(sin);
	getsockname(socket, (struct sockaddr *)&sin, &len);
	port = ntohs(sin.sin_port);

	active=true;
}

#endif







