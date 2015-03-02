import java.util.*;
import java.security.*;
import java.math.BigInteger;
import java.nio.*;

import java.io.*;
import java.net.*;

public class otherfuncs{
  // reads imsg packet, decodes each meta variable
  public static imsgData ParseDataBlock(byte[] data){

    byte[][] temp = new byte[0][0];
    byte[] values = new byte[0];
    String imsgName="";
    String imsgOrder="";
    try{
      //byteprint(data);
      //print("=============");
      temp = splitFirstNBytes(data,4);
      data=temp[1];
      int imsgNameLen = bytes2int(flipbytes(temp[0])); // parse imsg name's length
      //println(imsgNameLen);

      temp = splitFirstNBytes(data,imsgNameLen);
      data=temp[1];
      imsgName = new String(temp[0]); // parse the name of the imsg
      //println("IMSG NAME: "+imsgName);

      temp = splitFirstNBytes(data,4);
      data=temp[1];
      int imsgOrderLen = bytes2int(flipbytes(temp[0])); // parse the networked variables' order string's length
      //println("imsgOrderLen: "+imsgOrderLen);

      temp = splitFirstNBytes(data,imsgOrderLen);
      data=temp[1];
      imsgOrder=new String(temp[0]);  // parse the networked variables' order string

      values = data; // the remaining stuff is byte data of the variables
    }catch(Exception e){
      e.printStackTrace();
    }
    //println("putting together imsgData");
    imsgData datafinal = new imsgData(imsgOrder.getBytes(),values,imsgName);
    return datafinal;
  }




/////////////////////////////////////////////////////////////////////////////
///////////////////////  important stuff ////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////

public static String HashNumToStr(byte[] b){
  String str="";
  for(int k=0; k<b.length; k++){
    str += Integer.toString(b[k],16);
  }
  return str;
}

public static String getMD5(char[] msg){
	return getMD5(charArrTobyteArr(msg));
}
public static String getMD5(byte[] msg){
  String value = "";
  try{
    byte[] bytesOfMessage = msg;// msg.getBytes("UTF-8");
    MessageDigest md = MessageDigest.getInstance("MD5");
    byte[] thedigest = md.digest(bytesOfMessage);
    //value = HashNumToStr(thedigest);
    StringBuffer sb = new StringBuffer();
	        for (int i = 0; i < thedigest.length; ++i) {
	          sb.append(Integer.toHexString((thedigest[i] & 0xFF) | 0x100).substring(1,3));
       }
    value = sb.toString();
  }catch(Exception e){
    e.printStackTrace();
  }
  return value;
}


public static byte[] int2bytes(int x){
  byte[]temp=new byte[4];
  byte[]temp2=flipbytes(BigInteger.valueOf(x).toByteArray());

  for(int k=0;k<temp2.length;k++){
    temp[k]=temp2[k];
  }
  for(int k=temp2.length;k<temp.length;k++){
    temp[k]=0;
  }

  return temp;
}

public static int bytes2int(byte[] b){
  return new BigInteger(b).intValue();
}

public static byte [] float2bytes (float value){
	byte[]temp=new byte[4];
	byte[]temp2=flipbytes(ByteBuffer.allocate(4).putFloat(value).array());

	for(int k=0;k<temp2.length;k++){
		temp[k]=temp2[k];
	}

	for(int k=temp2.length;k<temp.length;k++){
		temp[k]=0;
	}

	return temp;
}

public static float bytes2float(byte[] b){
	return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getFloat();
}

public static byte [] long2bytes (long value){
	byte[]temp=new byte[8];
	byte[]temp2=flipbytes(ByteBuffer.allocate(8).putLong(value).array());
	System.out.println("lolo: "+temp2.length);
	for(int k=0;k<temp2.length;k++){
		temp[k]=temp2[k];
	}

	for(int k=temp2.length;k<temp.length;k++){
		temp[k]=0;
	}

	return temp;
}

public static long bytes2long(byte[] b){
	return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getLong();
}



public static void byteprint(byte[] arr){
  for(int k=0;k<arr.length;k++){
    System.out.println("["+k+"]"+" "+arr[k]+" \'"+(char)arr[k]+"\'");
  }
}
public static byte[] flipbytes(byte[] arr){
  byte[] temp= new byte[arr.length];
  for(int k=0;k<arr.length;k++){
    temp[(arr.length-1)-k]=arr[k];
  }
  return temp;
}


public static void printbytearr(byte[] arr){
  String a="{ ";
  for(int k=0; k<arr.length;k++){
    a+=arr[k];
    a+=", ";
  }
  a+=" };";
  System.out.println(a);
}


public static byte[] byteArrConcat(byte[] arr1, byte[] arr2){
  byte[] retArr= new byte[arr1.length+arr2.length];
  System.arraycopy(arr1,0,retArr,0,arr1.length);
  System.arraycopy(arr2,0,retArr,arr1.length,arr2.length);
  return retArr;
}

public static byte[][] splitFirstNBytes(byte[] arr,int n){
  //if(n>=arr.length){println("ERR TRIED TO SPLIT MORE BYTES THAN AVAILABLE");}
  byte[][] ret=new byte[2][];
  ret[0]=new byte[n];
  ret[1]=new byte[arr.length-n];


  System.arraycopy(arr,0,ret[0],0,n);
  System.arraycopy(arr,n,ret[1],0,arr.length-n);

  //for(int k=0;k<n;k++){
  //  ret[0][k]=arr[k];
  //}
  //
  //for(int k=0;k<arr.length-n;k++){
  //  ret[1][k]=arr[k+n];
  //}

  return ret;
}

static byte[] charArrTobyteArr(char[] arr){
	byte[]out=new byte[arr.length];
	for(int k=0;k<arr.length;k++){
		out[k]=(byte)arr[k];
	}
	return out;
}


}


