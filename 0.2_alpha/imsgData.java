import java.util.*;
import java.security.*;
import java.math.BigInteger;

import java.io.*;
import java.net.*;

public class imsgData{
  public byte[] varOrder;
  public byte[] varData;
  public String name;
  public imsgData(byte[] vo, byte[] vd, String n){
    varOrder=vo.clone();
    varData=vd.clone();
    name=n;
  }
  public int ReadInt(){
    int ret=0;

    if(varOrder[0]=='i'){
      byte[][] temp = otherfuncs.splitFirstNBytes(varOrder,1);
      varOrder=temp[1];

      byte[][] temp2=otherfuncs.splitFirstNBytes(otherfuncs.flipbytes(varData),4);
      varData=otherfuncs.flipbytes(temp2[1]);
      ret = otherfuncs.bytes2int(temp2[0]);
    }else{
		System.out.println("ERROR: WRONG READ ORDER IN IMSG \'"+name+"\'!");
    }
    return ret;
  }

  public String ReadString(){
      String ret="";

      if(varOrder[0]=='s'){
        byte[][] temp = otherfuncs.splitFirstNBytes(varOrder,1);
        varOrder=temp[1];

        byte[][] temp2=otherfuncs.splitFirstNBytes(otherfuncs.flipbytes(varData),4);
        int strlen = otherfuncs.bytes2int(temp2[0]);
		//System.out.println("strlen: "+strlen);
        byte[] result_before_string_sub = temp2[1]; // string with rest of data

        byte[][] after_string_sub = otherfuncs.splitFirstNBytes(result_before_string_sub, strlen);
        ret=new String(otherfuncs.flipbytes(after_string_sub[0])); // just string
        varData=otherfuncs.flipbytes(after_string_sub[1]); //rest of data
      }else{
        System.out.println("ERROR: WRONG READ ORDER IN IMSG \'"+name+"\'!");
      }
      return ret;
  }

	public float ReadFloat(){
		float ret=0;
		if(varOrder[0]=='f'){
			byte[][] temp = otherfuncs.splitFirstNBytes(varOrder,1);
			varOrder=temp[1];

			byte[][] temp2=otherfuncs.splitFirstNBytes(otherfuncs.flipbytes(varData),4);
			varData=otherfuncs.flipbytes(temp2[1]);
			ret = otherfuncs.bytes2float(otherfuncs.flipbytes(temp2[0]));
		}else{
			System.out.println("ERROR: WRONG READ ORDER IN IMSG \'"+name+"\'!");
		}
		return ret;
	}

}



class VerifiedIMSGPacket{
  private boolean valid=false;
  public int packetID=-1;
  public int clientID=-1;
  public String md5;
  public byte[] data;
  public boolean isValid(){
    return valid;
  }
  public void setValid(boolean v){
    valid = v;
  }
}
