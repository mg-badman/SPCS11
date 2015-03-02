
#include "imsgClient.h"

imsgClient * addToArray(imsgClient * arr,int arrlength , imsgClient e);



	imsgClient * addToArray(imsgClient * arr,int arrlength, imsgClient e){
		imsgClient * ret = new imsgClient[arrlength+1];
		for(int k=0;k<arrlength;k++){
			ret[k]=arr[k];
		}
		ret[arrlength]=e;
		return ret;
	}
