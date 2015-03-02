


#ifndef IMSG_OTHERFUNCS
#define IMSG_OTHERFUNCS

//#include "imsgData.h"



//#include <ws2tcpip.h>
//#include <winsock2.h>
  // reads imsg packet, decodes each meta variable
	imsgData ParseDataBlock(byte * data,int datalen,int len0,int len1);



/////////////////////////////////////////////////////////////////////////////
///////////////////////  important stuff ////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////
	
	char * HashNumToStr(char * b);

	const char * getMD5(char * msg,int len, int * lenOut);

	BYTE * int2bytes(int x);
	int bytes2int(BYTE * b);

	BYTE * float2bytes (float value);
	float bytes2float(byte * b);

	BYTE * long2bytes (long value);
	long bytes2long(BYTE * b);

	void byteprint(BYTE * arr);

	BYTE * flipbytes(BYTE * arr, int len);

	void printbytearr(BYTE * arr);

	BYTE * byteArrConcat(BYTE * arr1, BYTE * arr2,int len0,int len1);

	BYTE ** splitFirstNBytes(BYTE * arr, int arrsiz, int n);

	bool cmpstr(char* str1,char* str2);


	

  // reads imsg packet, decodes each meta variable
imsgData ParseDataBlock(BYTE * data,int datalen){
	BYTE ** temp;
	BYTE * values;
	char* imsgName;
	char* imsgOrder;

	temp = splitFirstNBytes(data,datalen,4);
	datalen-=4;
	data=temp[1];
	int imsgNameLen = bytes2int(temp[0]); // parse imsg name's length

	temp = splitFirstNBytes(data,datalen,imsgNameLen);
	datalen-=imsgNameLen;
	data=temp[1];
	char * nametemp=new char[imsgNameLen+1];
	memcpy(nametemp,temp[0],imsgNameLen);
	nametemp[imsgNameLen]='\0';
	imsgName = nametemp; // parse the name of the imsg

	temp = splitFirstNBytes(data,datalen,4);
	datalen-=4;
	data=temp[1];
	int imsgOrderLen = bytes2int(temp[0]); // parse the networked variables' order string's length
    
	temp = splitFirstNBytes(data,datalen,imsgOrderLen);
	datalen-=imsgOrderLen;
	data=temp[1];
	imsgOrder=(char*)temp[0];  // parse the networked variables' order string

	values = data; // the remaining stuff is byte data of the variables
	imsgData datafinal((BYTE*)imsgOrder,imsgOrderLen,values,datalen,imsgName);
    return datafinal;
}

/////////////////////////////////////////////////////////////////////////////
///////////////////////  important stuff ////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////

char * HashNumToStr(char * b, int len){
	return b;
}

BYTE * getMD5String(char * inputString,int len)
{
	return new BYTE[0];
      //return BitConverter::ToString(byteArrayHash);
}


const char * getMD5(char * msg,int len, int * lenOut){
	std::string tmp = GetHashText(msg,len,HashMd5);
	int lenn=tmp.length();

	char * out= new char[lenn+1];
	memcpy(out,tmp.c_str(),lenn);
	out[lenn]='\0';


	lenOut=&lenn;
	return out;

}


int bytes2int(BYTE * b){
	int ret=0;
	memcpy(&ret,b,4);
	return ret;
}

BYTE * int2bytes(int x){
	BYTE * ret = new BYTE[4];
	memcpy(ret,&x,4);
	return ret;
}


BYTE * float2bytes (float value){
	BYTE * ret = new BYTE[4];
	memcpy(ret,&value,4);
	return ret;
}

float bytes2float(byte * b){
	float ret=0;
	memcpy(&ret,b,4);
	return ret;
}

//BYTE * long2bytes (long value){

//}
//long bytes2long(BYTE * b){

//}

void byteprint(BYTE * arr){

}

BYTE * flipbytes(BYTE * arr, int len){
	BYTE * temp= new BYTE[len];
	for(int k=0;k<len;k++){
		temp[(len-1)-k]=arr[k];
	}
	return temp;
}

void printbytearr(BYTE * arr){

}

BYTE * byteArrConcat(BYTE * arr1, BYTE * arr2,int len0,int len1){
	BYTE * retArr= new BYTE[len0+len1];
	memcpy(retArr,		arr1,	len0);
	memcpy(retArr+len0,	arr1,	len1);
	return retArr;
}

BYTE ** splitFirstNBytes(BYTE * arr, int arrsiz, int n){
	BYTE ** ret=new BYTE*[2];
	ret[0]=new BYTE[n];
	ret[1]=new BYTE[arrsiz-n];
	memcpy(ret[0],		arr,	n);
	memcpy(ret[1],		arr+n,	arrsiz-n);
	return ret;
}

bool cmpstr(char* str1,char* str2){
	if(strlen(str1)!=strlen(str2)){
		return false;
	} else {
		bool neq=false;
		for(unsigned int k=0;k<strlen(str1);k++){
			if(str1[k] != str2[k]){
				return false;
			}
		}
		return true;
	}
	return false;
}

#endif