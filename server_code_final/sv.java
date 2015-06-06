import java.util.*;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.io.Console;

public class sv{

	ServerSocket svSock;
	//final HashMap<ChatClient,Integer> clients= new HashMap<ChatClient,Integer>();
	int NUM_CLIENTS=64;
	ChatClient [] clients;
	sv_accept acceptThread;
	sv_readclients readClientsThread;


	int nextFreeIndex(){
		for(int k=0;k<NUM_CLIENTS;k++){
			if(clients[k]==null){
				return k;
			}
		}
		return -1;
	}



/*public synchronized ArrayList<String> GetMessagesFromIgorDB(String usrname){
		ArrayList<String> out = new ArrayList<String>();
		try{

	Connection connect=
			//DriverManager.getConnection("jdbc:mysql://localhost:8889/java_demo","root",pass);
			DriverManager.getConnection("jdbc:mysql://igor.gold.ac.uk:3306/ma201mg_idp_coursework","ma201mg","erochure");

			Statement st = connect.createStatement();
			ResultSet resultSet = st.executeQuery("SELECT * from messages");
			while (resultSet.next()){
				int primkey = resultSet.getInt("primkey");
				String sender = resultSet.getString("sent_by");
				String receiver = resultSet.getString("sent_to");
				String message = resultSet.getString("message");
				int message_type = resultSet.getInt("message_type");

				if(message_type == 0){
					out.add(sender+": "+message);
					continue;
				}
				if(message_type == 1 & (sender.equals(usrname) | receiver.equals(usrname))){ // private chat messages
					out.add(sender+" --> "+receiver+": "+message);
					continue;
				}
				if(message_type == 2 & (sender.equals(usrname) | receiver.equals(usrname))){ // @ chat messages
					String tempout="";
					if(sender.equals(usrname)){
						tempout="You to \""+receiver+"\": "+message;
					}else if(receiver.equals(usrname)){
						tempout="\""+sender+"\" to You: "+message;
					}
					out.add(tempout);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	return out;
}
	private synchronized void AddMessageToIgorDB(String sent_by, String sent_to, String message, int message_type){
		try{
			Connection connect=DriverManager.getConnection("jdbc:mysql://igor.gold.ac.uk:3306/ma201mg_idp_coursework","ma201mg","erochure");

			Statement st = connect.createStatement();
			String query="INSERT INTO messages (sent_by, sent_to, message, message_type) VALUES ('"+sent_by+"', '"+sent_to+"', '"+message+"', '"+message_type+"')";
			PreparedStatement ps = connect.prepareStatement(query);
			ps.executeUpdate();
		}catch(Exception e){
			e.printStackTrace();
		}
	//		ResultSet resultSet = st.executeUpdate(query);
	}*/

	public sv(int port){
		try{
			//Class.forName("com.mysql.jdbc.Driver");
			svSock = new ServerSocket(port);
			clients = new ChatClient[64];
			acceptThread 		= new sv_accept(this);
			readClientsThread 	= new sv_readclients(this);
			readClientsThread.start();
			acceptThread.start();
		}catch(Exception e){
			e.printStackTrace();
		}

	}

	synchronized void addClient(Socket s){
		//synchronized (clients){
			int indx=nextFreeIndex();
			if(indx != -1){
				clients[indx]=new ChatClient(s);
			}else{
				System.out.println("Err: no more free slots");
			}
		//}
	}
	void CLRemove(ChatClient cl){
		for(int k=0;k<NUM_CLIENTS;k++){
			if(clients[k] != null){
				if(clients[k]==cl){
					try{
					clients[k].sock.close();
					clients[k]=null;
					}catch(Exception e){
						e.printStackTrace();
					}
				}
			}
		}
	}
	ChatClient CLFind(String name){
		for(int k=0;k<NUM_CLIENTS;k++){
			if(clients[k] != null){
				if(clients[k].name.equals(name)){
					return clients[k];
				}
			}
		}
		return null;
	}
	static String fp (String s){
		String result="";
		for (int i=0;i<s.length();i++){
			if (s.charAt(i)==' '){
				return result;
			}else{
				result=result+s.charAt(i);
			}
		}
		return result;
	}

	static String fpat (String s){
		String result="";
		for (int i=1;i<s.length();i++){
			if (s.charAt(i)==' '){
				return result;
			}else{
				result=result+s.charAt(i);
			}
		}
		return result;
	}


	static String sp (String s){
		String result="";
		int i=0;
		for (;i<s.length();i++){
			if (s.charAt(i)==' '){
				i++;
				break;
			}else{
				//result=result+s.charAt(i);
			}
		}
		for (;i<s.length();i++){
			if (s.charAt(i)==' '){
				return result.trim();
			}else{
				result=result+s.charAt(i);
			}
		}
		return result;
	}


	static String spEnd (String s){
		String result="";
		int i=0;
		for (;i<s.length();i++){
			if (s.charAt(i)==' '){
				i++;
				break;
			}else{
				//result=result+s.charAt(i);
			}
		}
		for (;i<s.length();i++){
			//if (s.charAt(i)==' '){
			//	return result.trim();
			//}else{
				result=result+s.charAt(i);
			//}
		}
		return result;
	}




	public void messageReceived( ChatClient cl , String msg ){
		System.out.println("Client \""+cl.name+"\" said \""+msg+"\"");

		if(fp(msg).equals("!name")){
			//AddMessageToIgorDB(cl.name,"",msg,0);
			String t = sp(msg);
			if( !t.equals("")){
				cl.name = t;
				cl.write("You have joined the chat as \""+cl.name+"\".");
				Broadcast("User \""+cl.name+"\" has joined the chat.",cl);
				//ArrayList<String> temp = GetMessagesFromIgorDB(cl.name);
				//String tmp="";
				//for(String k : temp){
				//	tmp+=k+"\n";
				//}
				//cl.write(tmp);

			}else{
				cl.write("The name \""+t+"\" is not a valid name. Disconnecting.");


				//clients.remove(cl);
				cl.toBeRemoved=true;
				CLRemove(cl);
				//clients.remove(cl);
			}
			return;
		}

		if(fp(msg).equals("!quit")){
			//AddMessageToIgorDB(cl.name,"",msg,0);
			Broadcast("Client \""+cl.name+"\" has left the chat.",cl);

				CLRemove(cl);
			//clients.remove(cl);
			System.out.println("no issue");
			return;
		}

		if(fp(msg).equals("!leave")){
			//AddMessageToIgorDB(cl.name,"",msg,0);
			ChatClient cltarget = CLFind(cl.usernameJoined);
			if(cltarget != null){
				if(cl.joinedUser && cltarget.joinedUser && cltarget.usernameJoined.equals(cl.name)){

					cl.write("You are now leaving private chat with "+cltarget.name+".");
					cltarget.write(cl.name+" has left the private chat. (command !leave)");
				}
			}
			cl.joinedUser=false;
			cl.usernameJoined="";
			return;
		}
		if(fp(msg).equals("!join")){
			//AddMessageToIgorDB(cl.name,"",msg,0);
			String t = sp(msg);
			if( !t.equals("")){
				cl.usernameJoined = t;
				cl.joinedUser=true;
				ChatClient tmpcl = CLFind(cl.usernameJoined);
				if(tmpcl != null){
					if(tmpcl.joinedUser && tmpcl.usernameJoined.equals(cl.name)){
						tmpcl.write("You are now in a private chat with \""+cl.name+"\".");
						cl.write("You are now in a private chat with \""+tmpcl.name+"\".");
					}else{
						cl.write("You have invited user \""+cl.usernameJoined+"\" to join your private chat.");
						tmpcl.write("You have been invited to a private chat by \""+cl.name+"\". type !join "+cl.name+" to chat with them");
					}
				}else{
					cl.write("Unfortunatelly a client with that name was not found. Returning to public mode.");
					cl.usernameJoined="";
					cl.joinedUser=false;
				}
				//Broadcast("User \""+cl.name+"\" has joined the chat.",cl);
			}else{
				cl.write("The name \""+t+"\" is not a valid name.");
			}
			return;
		}

		if(msg.charAt(0)=='@'){
			String n = fpat(msg);
			ChatClient tmp = CLFind(n);
			String t = spEnd(msg);

			if( tmp != null){
				cl.write("You to \""+tmp.name+"\": "+t);
				tmp.write("\""+cl.name+"\" to You: "+t);
				//AddMessageToIgorDB(cl.name,tmp.name,t,2);
			}else{
				cl.write("Could not find such user.");
			}
			return;
		}

		if(fp(msg).equals("!who")){

			//AddMessageToIgorDB(cl.name,"",msg,0);
			String output="";
			for(int k=0;k<NUM_CLIENTS;k++){
				if(clients[k] != null){
					output+="["+k+"]"+clients[k].name+"\n";
				}
			}
			cl.write(output);
			return;
		}

		if(cl.joinedUser){
			ChatClient cltarget = CLFind(cl.usernameJoined);
			if(cltarget != null){
				if(cltarget.joinedUser && cltarget.usernameJoined.equals(cl.name)){
					cl.write(cl.name+" --> "+cltarget.name+": \""+msg+"\"");
					cltarget.write(cl.name+" --> "+cltarget.name+": \""+msg+"\"");
					//AddMessageToIgorDB(cl.name,cltarget.name,msg,1);
				}
			}else{
				//AddMessageToIgorDB(cl.name,cltarget.name,msg,1);
				cl.write(cl.name+" --> "+cltarget.name+": \""+msg+"\"");
				cl.write("Seems like that user does not exist.");
			}
		}else{
			String lol = "<font color='#00AA00'>"+cl.name+"</font>: \""+msg+"\"";
			String lol2= "<font color='#0000AA'>"+cl.name+"</font>: \""+msg+"\"";
			String old =cl.name + ": \"" + msg + "\"";
//AddMessageToIgorDB(cl.name,"",msg,0);
			Broadcast(lol,cl);
			cl.write(lol2);
		}
	}

	public synchronized void Broadcast(String msg){
		//synchronized (clients){
			for( int k=0;k<NUM_CLIENTS;k++ ){
				ChatClient client = clients[k];
				if(client != null){
					if(client.toBeRemoved){
						Broadcast("User "+client.name+" has disconnected.",client);

						CLRemove(client);
					}else{
						if(!client.joinedUser){
							client.write(msg);
						}
					}
				}
			}
		//}
	}

	public synchronized void Broadcast(String msg, ChatClient exclude){
		//synchronized (clients){
			for( int k = 0; k<NUM_CLIENTS; k++ ){

				ChatClient client = clients[k];
				if(client != null){
					if(client.toBeRemoved){
						CLRemove(client);
						Broadcast("User "+client.name+" has disconnected.",client);
					}else{
						if(client != exclude){
							if(!client.joinedUser){
								client.write(msg);
							}
						}
					}
				}
			}
		//}
	}
}

class sv_accept extends Thread{
	sv server;
	public sv_accept(sv s){
		server=s;
	}

	public void run(){
		try{
			while(true){
				try{
					Thread.currentThread().sleep(50);
				}catch(Exception e){
					e.printStackTrace();
				}
				Socket s = server.svSock.accept();
				//synchronized (server.clients){
					server.addClient(s);
				//}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}

class sv_readclients extends Thread{
	sv server;
	public sv_readclients(sv s){
		server=s;
	}

	public void run(){
		while(true){
			//synchronized (server.clients){
				try{
					Thread.currentThread().sleep(50);
				}catch(Exception e){
					e.printStackTrace();
				}
				checkReceived();
			//}
		}
	}

	public synchronized void checkReceived(){
		for( int k=0; k<server.NUM_CLIENTS; k++ ){
			ChatClient client = server.clients[k];
			if(client != null){
				if(client.toBeRemoved){
					server.Broadcast("User "+client.name+" has disconnected.",client);

				server.CLRemove(client);
				}else{
					String res = client.read();
					if(!res.equals("")){
						server.messageReceived(client,res);
					}
				}
			}
		}
	}

	public synchronized void syncRead(){

	}
}
