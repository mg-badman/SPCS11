import java.util.*;
import java.io.*;
import java.net.*;

class ChatClient{

	public Socket sock;
	public InputStream inStream;
 	public OutputStream outStream;

	public String name = "<unnamed client>";
	public boolean toBeRemoved=false;

	///// joining another user

	boolean joinedUser=false;
	String usernameJoined="";

 	public ChatClient(Socket s){
		try{
			sock = s;
			inStream = sock.getInputStream();
			outStream = sock.getOutputStream();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public void write(String msg){
		try{
			outStream.write(msg.getBytes());
		}catch(Exception e){
			System.out.println("failed to write to client "+name);
			toBeRemoved=true;
		}
	}

	public String read(){
		String out="";
		try{
			while(inStream.available()>0){
				out = out + (char)inStream.read();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return new String(out);
	}
}