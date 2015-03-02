
imsgData::imsgData(BYTE * vo, int vol, BYTE * vd, int vdl, char * n){
    varOrder=vo;
	varOrderLen=vol;
    varData=vd;
	varDataLen=vdl;
    name=n;
}

int imsgData::ReadInt(){
	int ret=0;

	if(varOrder[0]=='i'){
		byte ** temp = splitFirstNBytes(varOrder,varOrderLen,1);
		varOrderLen-=1;
		varOrder=temp[1];

		BYTE ** temp2=splitFirstNBytes(flipbytes(varData,varDataLen),varDataLen,4);
		varDataLen-=4;
		varData=flipbytes(temp2[1],varDataLen);
		ret = bytes2int(flipbytes(temp2[0],4));

	}else{
		printf("ERROR: WRONG READ ORDER IN IMSG \'%s\'!",name);
	}
	return ret;
}

char * imsgData::ReadString(){
	char * ret;

	if(varOrder[0]=='s'){
		BYTE ** temp = splitFirstNBytes(varOrder,varOrderLen,1);
		varOrderLen-=1;
		varOrder=temp[1];

		BYTE ** temp2=splitFirstNBytes(flipbytes(varData,varDataLen),varDataLen,4);
		varDataLen-=4;
		BYTE * result_before_string_sub = temp2[1]; // string with rest of data

        int strl = bytes2int(flipbytes(temp2[0],4));
		
		BYTE ** after_string_sub = splitFirstNBytes(result_before_string_sub, varDataLen  , strl);
		varDataLen -=strl;
		char * tempstr=(char*)flipbytes(after_string_sub[0],strl);
		
		ret= new char[strl+1];
		memcpy(ret,tempstr,strl);
		ret[strl]=0; // terminate
		varData=flipbytes(after_string_sub[1],varDataLen); //rest of data
	}else{
		printf("ERROR: WRONG READ ORDER IN IMSG \'%s\'!",name);
	}
	return ret;
}

float imsgData::ReadFloat(){
	float ret=0;

	if(varOrder[0]=='f'){
		byte ** temp = splitFirstNBytes(varOrder,varOrderLen,1);
		varOrder=temp[1];
		varOrderLen-=1;

		BYTE ** temp2=splitFirstNBytes(flipbytes(varData,varDataLen),varDataLen,4);
		varDataLen-=4;
		varData=flipbytes(temp2[1],varDataLen);
		ret = bytes2float(flipbytes(temp2[0],4));
	}else{
		printf("ERROR: WRONG READ ORDER IN IMSG \'"); ///+name+"\'!");
	}
	return ret;

}

//#include "imsgData.h"

//////////////////////////////////////////////////

bool VerifiedIMSGPacket::isValid(){
	return valid;
}

void VerifiedIMSGPacket::setValid(bool v){
	valid = v;
}

