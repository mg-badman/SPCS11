
//#define WIN32_LEAN_AND_MEAN
/*
#pragma comment (lib, "kernel32.lib")
#pragma comment (lib, "user32.lib")
#pragma comment (lib, "gdi32.lib")
#pragma comment (lib, "winspool.lib")
#pragma comment (lib, "comdlg32.lib")
#pragma comment (lib, "advapi32.lib")
#pragma comment (lib, "shell32.lib")
#pragma comment (lib, "ole32.lib")
#pragma comment (lib, "oleaut32.lib")
#pragma comment (lib, "uuid.lib")
#pragma comment (lib, "odbc32.lib")
#pragma comment (lib, "odbccp32.lib")
*/
#pragma comment (lib, "Ws2_32.lib")
//#include <Windows.h>

//#include <WinSock2.h>


//#include <winsock2.h>
//#include <ws2tcpip.h>
//#include <map>

//#include "arrfuncs.cpp"

#include <process.h>
#include <winsock2.h>
#include <ws2tcpip.h>


#include <Windows.h>
#include <stdio.h>

#include <WinCrypt.h>
#include <vector>
#include <sstream>

#include <map>
#include "md5.h"

/*
#include "imsgData.h"
*/

/*
#include "otherfuncs.h"
#include "imsgClient.h"
#include "imsgEvent.h"
//#include <winsock2.h>
//#include <ws2tcpip.h>
*/
#include "imsg.h"
imsg * Imsg;

bool counterMessage=false;
int counterid=-1;
void LOL_event(imsgData d, int id){
	printf("LOL was ran by client with id [%d]\n",id);
	counterid=id;
	printf("%d",d.ReadInt());
	printf("\n");
	printf(d.ReadString());
	printf("\n");
	printf("%f",d.ReadFloat());
	printf("\n");
	counterMessage=true;
	
}

void main(){
	AllocConsole();
	//printf(getMD5("hello",5 ,NULL));
	printf("starting\n");
	char md5lol[] = {24,55,195,24,51,1,53,121,4,22,44,66};
	printf( getMD5(md5lol,12,NULL) );
	printf( "\n" );
	



	char * lol="abcdefg";
	BYTE ** ret = splitFirstNBytes((BYTE*)lol,7,3);
	
	printf((char*)ret[0]);
	printf("\n");

	printf((char*)ret[1]);
	printf("\n");

	printf("actualfuncaddr: %d\n",(DWORD)&LOL_event);
	Imsg = new imsg();
	Imsg->initServer(6789);
	Imsg->AddEvent("LOL",(DWORD)&LOL_event);

	while(!counterMessage){}

	Imsg->Start("LOLSENT");
	Imsg->WriteString("OLOLOLO, YOU CONNECTED.");
	Imsg->Send(counterid);

	while(true){

	}
}
