import java.util.*;

import java.util.*;
import java.security.*;
import java.math.BigInteger;

import java.io.*;
import java.net.*;

public class imsgEvent{

	Map<String, eventObj> Events = new HashMap<String, eventObj>();

	public void Call(String name,imsgData d,int clientID){
		try{
			Events.get(name).call(d,clientID);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public void Add(String name,eventObj event){
		Events.put(name, event);
	}

}

interface eventObj{
	//void call(imsgData data);
	void call(imsgData data,int ID);

}


