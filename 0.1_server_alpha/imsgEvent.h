


//#include "imsg.h"

//#include "imsgData.h"

#ifndef IMSG_EVENT
#define IMSG_EVENT

#include "imsgData.h"
typedef void func(imsgData,int);
//#include <Windows.h>
//#include <map>


//typedef std::map<char *, DWORD>  EvntMap;

class imsgEvent{
public:

	std::map<std::string, DWORD> * Events;

	imsgEvent();

	void Call(std::string name,imsgData d,int clientID);

	void Add(std::string name,DWORD event);
private:

};

imsgEvent::imsgEvent(){
	Events = new std::map<std::string, DWORD>();
}



void imsgEvent::Call(std::string name,imsgData d,int clientID){
	
	std::map<std::string,DWORD>::iterator itr = Events->find(name);
	DWORD evntFunc = (*itr).second;
	func* imsgevent = (func*)evntFunc;
	printf("addr: %d \n",evntFunc);
	imsgevent(d,clientID);
}

void imsgEvent::Add(std::string name,DWORD eventFunc){
	Events->insert(std::pair<std::string, DWORD>(name, eventFunc));
	
}


#endif


