


#ifndef IMSG_DATA
#define IMSG_DATA

//#include "otherfuncs.h"

class imsgData{
public:
	BYTE * varOrder;
	int varOrderLen;
	BYTE * varData;
	int varDataLen;
	char * name;
	imsgData(BYTE * vo, int vol, BYTE * vd, int vdl, char * n);
	int ReadInt();

	char * ReadString();

	float ReadFloat();
private:
};



class VerifiedIMSGPacket{
public:
  int packetID;
  int clientID;
  char * md5;
  int md5Len;
  BYTE * data;
  int dataLen;
  bool isValid();
  void setValid(bool v);
private:
  bool valid;
};

#endif