
// v0.2
// miroslav georgiev

// note, multiple imsg messages can arrive together in the socket buffer
// only the first one will be parsed, the algorithm doesnt check for remaining data (yet)

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.ServerSocket;

import java.io.InputStream;



public class imsg {


	public int MAX_CLIENTS=64;

	private int nextFreeIndex(){
		for(int k=0;k<MAX_CLIENTS;k++){
			if(clients[k].active!=true){
				return k;
			}
		}
		return -1;
	}

	public static final int IMSG_TYPE_SERVER=0;
	public static final int IMSG_TYPE_CLIENT=1;
	public static final int IMSG_TYPE_NONE=-1;

	int imsg_type=IMSG_TYPE_NONE;

	Socket			CLsock; // socket that imsg client uses
	ServerSocket	SVsock;
	String			IPAddr;
	int				port;

	imsgEvent		events;

	byte [] 		imsgBuff; // data send buffer
	int 			byteindx  		= 0; // byte count for data send buffer
	String 			imsgName 		= ""; // name of the Imessage to be sent
	String			imsgOrder		= ""; // order of the data in the data section

	SocketAddress bindAddress;

	imsgClient [] clients; // if its a server, it's gonna need this
	boolean clientsArrLocked=false;

	byte [][] recvQueue;
	byte [][] sendQueue;

	Thread SendThread;
	Thread RecvThread;
	Thread AcceptThread;
	Thread MainImsgThread;


	public int packetIDfailCount = 0;
	public int packetTotalFailCount = 0;
	public int TotalPacketCount = 0;

	public boolean initServer(int port){
		try{
			imsg_type = IMSG_TYPE_SERVER;
			//host = InetAddress.getByName("localhost");
			SVsock = new ServerSocket();
			bindAddress = new InetSocketAddress("localhost", port);
			SVsock.bind(bindAddress);
			clients = new imsgClient[MAX_CLIENTS];
			for(int k=0;k<MAX_CLIENTS;k++){
				clients[k]=new imsgClient();
			}
			initializeThreads();
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean initClient(String ip,int port){
		try{
			imsg_type = IMSG_TYPE_CLIENT;
			CLsock = new Socket();
			bindAddress = new InetSocketAddress(ip, port);
			CLsock.connect(bindAddress);
			initializeThreads();
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private void initializeThreads(){
		events = new imsgEvent();

		if(imsg_type == IMSG_TYPE_CLIENT){
			 ////////////////////////
			///                    ///
			 ////////////////////////

			RecvThread = new Thread(new Runnable() {
				public void run() {
					while(true){
						try{
					CLsock.setSoTimeout(1);
							//System.out.println("aaa");
							Thread.currentThread().sleep(60); // battery saving
							while( clientsArrLocked ){}
							clientsArrLocked = true;
							//System.out.println("aaa1");

								int asd = CLsock.getReceiveBufferSize();
								//System.out.println(asd);
								byte[]buff=new byte[asd];
								int count=0;
								//System.out.println("aaa2");
								try{
									InputStream SIn = CLsock.getInputStream();
									while(true){
										int r = SIn.read();
										//System.out.println("aaa3 "+r);
										if(r != -1){
											//System.out.println(r);
											buff[count]=(byte)r;
											count++;
										}else{
											break;
										}
									}
								}catch(Exception e){
									//e.printStackTrace();
								}
							//System.out.println("aaa4");
								int datalen = count; //clients[k].socket.getInputStream().read(buff);

									byte[]data=new byte[datalen];
									for(int v=0;v<datalen;v++){
										data[v]=buff[v];
									}
									if(data.length>0){
										////// MESSAGE RECEIVED
										//int tempCLIENT_ID=k;

										String msg=new String(data);
										onMessageReceivedCL(msg);

										//System.out.println("MESSAGE RECEIVED FROM CLIENT["+tempCLIENT_ID+"]: "+new String(data));
									}

							clientsArrLocked = false;

							//Socket client = SVsock.accept();
							//System.out.println("client connected [todo: accept event]");
						}catch(Exception e){
							e.printStackTrace();
							clientsArrLocked = false;
						}
					}
				}
			});
			RecvThread.start();

		}else if(imsg_type == IMSG_TYPE_SERVER){

			///////////////////////////
			///// Accept Thread ///////
			///////////////////////////

			AcceptThread = new Thread(new Runnable() {
				public void run() {
					while(true){
						try{
							Thread.currentThread().sleep(60); // battery saving
							Socket client = SVsock.accept();
							imsgClient cl = new imsgClient(client);
							//cl.socket=client;
							//cl.
							//client.setSoTimeout(10);
							System.out.println("client connected [todo: accept event]");
							while( clientsArrLocked ){}
							clientsArrLocked = true;
							int freeid=nextFreeIndex();
							if(freeid!=-1){
								clients[freeid] = cl;
								//clients = arrfuncs.addToArray(clients,cl);
								//clients[0] = cl;
							}
							clientsArrLocked = false;

						}catch(Exception e){
							//e.printStackTrace();
						}
					}
				}
			});
			AcceptThread.start();


			 ////////////////////////
			///                    ///
			 ////////////////////////
			RecvThread = new Thread(new Runnable() {
				public void run() {
					while(true){
						try{
							//System.out.println("aaa");
							Thread.currentThread().sleep(60); // battery saving
							while( clientsArrLocked ){}
							clientsArrLocked = true;
							for(int k=0;k<clients.length;k++){
								if (clients[k].active != true){
									continue;
								}
								clients[k].socket.setSoTimeout(1);
								int asd = clients[k].socket.getReceiveBufferSize();
								//System.out.println(asd);
								byte[]buff=new byte[asd];
								int count=0;
								try{
									InputStream SIn = clients[k].socket.getInputStream();
									while(true){
										int r = SIn.read();
										if(r != -1){
											//System.out.println(r);
											buff[count]=(byte)r;
											count++;
										}else{
											break;
										}
									}
								}catch(Exception e){
									//e.printStackTrace();
								}
								int datalen = count; //clients[k].socket.getInputStream().read(buff);

									byte[]data=new byte[datalen];
									for(int v=0;v<datalen;v++){
										data[v]=buff[v];
									}
									if(data.length>0){
										////// MESSAGE RECEIVED
										int tempCLIENT_ID=k;

										String msg=new String(data);
										onMessageReceived(tempCLIENT_ID,msg);

										//System.out.println("MESSAGE RECEIVED FROM CLIENT["+tempCLIENT_ID+"]: "+new String(data));
									}
							}
							clientsArrLocked = false;

							//Socket client = SVsock.accept();
							//System.out.println("client connected [todo: accept event]");
						}catch(Exception e){
							e.printStackTrace();
							clientsArrLocked = false;
						}
					}
				}
			});
			RecvThread.start();

		}
	}




	void Start(String name){  // begin a networked message
		imsgBuff = new byte[60000];
		byteindx=0;
		imsgName=name;
	}

	void WriteInt(int num){ // write an int to a networked message
		byte [] xbytes = otherfuncs.int2bytes(num);
	    imsgBuff=otherfuncs.byteArrConcat(xbytes,imsgBuff);
	    byteindx+=4;
	    imsgOrder=imsgOrder+'i';
	}

	void WriteString(String str){ // write an int to a networked message
			int strlen=str.length();
			byte [] xbytes = otherfuncs.int2bytes(strlen);
		    imsgBuff=otherfuncs.byteArrConcat(xbytes,imsgBuff);
		    imsgBuff=otherfuncs.byteArrConcat(str.getBytes(),imsgBuff);
		    byteindx+=4;
		    byteindx+=strlen;

		    imsgOrder=imsgOrder+'s';
	}

	void WriteFloat(float num){
		byte [] xbytes = otherfuncs.float2bytes(num);
	    imsgBuff=otherfuncs.byteArrConcat(xbytes,imsgBuff);
	    byteindx+=4;
	    imsgOrder=imsgOrder+'f';
	}
	//////////////////////////////////////////////////////////
	///////////////////   prepares the packet data
	//////////////////////////////////////////////////////////

	byte[] imsgPrepareDataToSend(){
		int clientID=5;
	byte[] data = ComposeData();
		String md5 = otherfuncs.getMD5(data);
		//System.out.println("datalen: "+data.length);
		int baseindx=0;

		byte [] packetID = 		otherfuncs.int2bytes(20); // todo
		byte [] clIDdata= 		otherfuncs.int2bytes(clientID);
		byte [] md5len= 		otherfuncs.int2bytes(md5.length());
		byte [] md5data = 		md5.getBytes();
		byte [] datalen= 		otherfuncs.int2bytes(data.length);
		byte [] bytedata = 		data;

		byte[] temp = new byte[0];

		if(datadbg){
			System.out.println();
			System.out.println();
			for(int k=0;k<4;k++){
				System.out.print("packetID"+(k+1)+":");
				otherfuncs.printbytearr(packetID);
			}
			for(int k=0;k<2;k++){
				System.out.print("clIDdata"+(k+1)+":");
				otherfuncs.printbytearr(clIDdata);
			}
			for(int k=0;k<2;k++){
				System.out.print("md5len"+(k+1)+":");
				otherfuncs.printbytearr(md5len);
			}
			System.out.print("md5data:");
			otherfuncs.printbytearr(md5data);
			for(int k=0;k<2;k++){
				System.out.print("datalen"+(k+1)+":");
				otherfuncs.printbytearr(datalen);
			}
			System.out.print("bytedata:");
			otherfuncs.printbytearr(bytedata);
		}
		temp = otherfuncs.byteArrConcat(temp,packetID);
		temp = otherfuncs.byteArrConcat(temp,packetID);
		temp = otherfuncs.byteArrConcat(temp,packetID);
		temp = otherfuncs.byteArrConcat(temp,packetID); //4 times
		temp = otherfuncs.byteArrConcat(temp,clIDdata);
		temp = otherfuncs.byteArrConcat(temp,clIDdata); // 2 times
		temp = otherfuncs.byteArrConcat(temp,md5len);
		temp = otherfuncs.byteArrConcat(temp,md5len); // 2 times
		temp = otherfuncs.byteArrConcat(temp,md5data); // 1 time
		temp = otherfuncs.byteArrConcat(temp,datalen);
		temp = otherfuncs.byteArrConcat(temp,datalen); // 2 times
		temp = otherfuncs.byteArrConcat(temp,bytedata); // 1 time
		return temp;
	}

	void SendToServer(){ // send networked message to server (if client)
		if(imsg_type == IMSG_TYPE_CLIENT){
			byte [] senddata; // = new byte[4+4+md5data.length+4+bytedata.length];
			senddata=imsgPrepareDataToSend();
			if(datadbg){
			  System.out.print("CLIENT: ");
			  otherfuncs.printbytearr(senddata);
			}

			//println("[["+new String(senddata)+"]]");
			try {
			  //sock.send(sendPacket);
			  this.CLsock.getOutputStream().write(senddata);
			}catch(Exception e) {
			  e.printStackTrace();
			}
		}else{
			System.out.println("ERROR: A Server cannot send data to the server.");
		}
	}

	void Send(imsgClient cl){ // send networked message to client (if server)
		if(imsg_type == IMSG_TYPE_SERVER){
			byte [] senddata; // = new byte[4+4+md5data.length+4+bytedata.length];
			senddata=imsgPrepareDataToSend();
			if(datadbg){
				System.out.print("CLIENT: ");
				otherfuncs.printbytearr(senddata);
			}

			//println("[["+new String(senddata)+"]]");
			try {
			  //sock.send(sendPacket);
			  //this.CLsock.getOutputStream().write(senddata);
			  cl.socket.getOutputStream().write(senddata);
			}catch(Exception e) {
			  e.printStackTrace();
			}
		}else{
			System.out.println("Error: attempted to invoke \"Send()\" using a client imsg object  ");
		}
	}

	void Send(int ID){ // send networked message to client (if server)
		Send(clients[ID]);
	}

	void Broadcast(){ // send networked message to all clients (if server)
		if(imsg_type == IMSG_TYPE_SERVER){
			for(int k=0;k<clients.length;k++){
				if( clients[k].active== true){
					Send(k);
				}
			}
		}else{
			System.out.println("Error: attempted to invoke \"Broadcast()\" using a client imsg object");
		}
	}


	void onMessageReceived(int id, String msg){
		try{
			//System.out.println("Client["+id+"]: "+msg);
			//Thread.currentThread().sleep(1000);
			//this.clients[id].socket.getOutputStream().write("WAWA".getBytes());
			VerifiedIMSGPacket verpacket=ParseIMSGPacket(msg.getBytes());
			      if(verpacket.isValid()){
			        int clientID = verpacket.clientID; //bytes2int(new byte[]{result[3],result[2],result[1],result[0]});

			        String md5= verpacket.md5;
			        byte[] data = verpacket.data;

			        if(!(otherfuncs.getMD5(data).equals(md5))){
			          packetTotalFailCount++;
			          System.out.println("ERROR MD5 DOES NOT MATCH DATA's MD5!  x"+packetTotalFailCount);
			          // todo- handler to request the data to be resent
			          return;
			        }

			        imsgData d = otherfuncs.ParseDataBlock(data);

			        events.Call(d.name,d,id);

			      }else{
			        System.out.println("data in packet "+verpacket.packetID+" has errorz");
      }
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	void onMessageReceivedCL(String msg){
		try{
			int id=-1; // this shows its a message from server
			//System.out.println("Client["+id+"]: "+msg);
			//Thread.currentThread().sleep(1000);
			//this.clients[id].socket.getOutputStream().write("WAWA".getBytes());
			VerifiedIMSGPacket verpacket=ParseIMSGPacket(msg.getBytes());
			      if(verpacket.isValid()){
			        int clientID = verpacket.clientID; //bytes2int(new byte[]{result[3],result[2],result[1],result[0]});

			        String md5= verpacket.md5;
			        byte[] data = verpacket.data;

			        if(!(otherfuncs.getMD5(data).equals(md5))){
			          packetTotalFailCount++;
			          System.out.println("ERROR MD5 DOES NOT MATCH DATA's MD5!  x"+packetTotalFailCount);
			          // todo- handler to request the data to be resent
			          return;
			        }

			        imsgData d = otherfuncs.ParseDataBlock(data);

			        events.Call(d.name,d,id);

			      }else{
			        System.out.println("data in packet "+verpacket.packetID+" has errorz");
      }


			//System.out.println("Server: "+msg);
			//Thread.currentThread().sleep(1000);
			//this.CLsock.getOutputStream().write("WAWA".getBytes());
		}catch(Exception e){
			e.printStackTrace();
		}
	}



	////////////////////////////////////////



	public boolean datadbg=false;
	public VerifiedIMSGPacket ParseIMSGPacket(byte[]data){
	    byte[][] temp = new byte[2][];
	    temp = otherfuncs.splitFirstNBytes(data,4);
	    data=temp[1];
	    int packetID1 = otherfuncs.bytes2int(otherfuncs.flipbytes(temp[0])); // get packet id
	    if(datadbg){
	      System.out.print("packetID1:");
	      otherfuncs.printbytearr(temp[0]);
	    }

	    temp = otherfuncs.splitFirstNBytes(data,4);
	    data=temp[1];
	    int packetID2 = otherfuncs.bytes2int(otherfuncs.flipbytes(temp[0])); //
	    if(datadbg){
	      System.out.print("packetID2:");
	      otherfuncs.printbytearr(temp[0]);
	    }

	    temp = otherfuncs.splitFirstNBytes(data,4);
	    data=temp[1];
	    int packetID3 = otherfuncs.bytes2int(otherfuncs.flipbytes(temp[0])); //
	    if(datadbg){
	      System.out.print("packetID3:");
	      otherfuncs.printbytearr(temp[0]);
	    }

	    temp = otherfuncs.splitFirstNBytes(data,4);
	    data=temp[1];
	    int packetID4 = otherfuncs.bytes2int(otherfuncs.flipbytes(temp[0])); //
	    if(datadbg){
	      System.out.print("packetID4:");
	      otherfuncs.printbytearr(temp[0]);
	    }

	    if(packetID1==packetID2 & packetID2==packetID3 & packetID3==packetID4){
	      //no problem
	    }else{
	      //big ass problem
	      packetIDfailCount++;
	      System.out.println("ERROR: PACKET ID IS INVALID, MESSAGE LOST FOREVER...  x"+packetIDfailCount);

	      return new VerifiedIMSGPacket();
	    }
	    //println("packetID1: "+packetID1);
	    temp = otherfuncs.splitFirstNBytes(data,4);
	    data=temp[1];
	    int clientID1 = otherfuncs.bytes2int(otherfuncs.flipbytes(temp[0])); // parse clientid
	    if(datadbg){
	      System.out.print("clientID1:");
	      otherfuncs.printbytearr(temp[0]);
	    }

	    temp = otherfuncs.splitFirstNBytes(data,4);
	    data=temp[1];
	    int clientID2 = otherfuncs.bytes2int(otherfuncs.flipbytes(temp[0])); //
	    if(datadbg){
	      System.out.print("clientID2:");
	      otherfuncs.printbytearr(temp[0]);
	    }

	    if(clientID1==clientID2){

	    }else{
	      //problem
	      VerifiedIMSGPacket a=new VerifiedIMSGPacket();
	      a.packetID=packetID1;
	    }



	    temp = otherfuncs.splitFirstNBytes(data,4);
	    data=temp[1];
	    int md5len1 = otherfuncs.bytes2int(otherfuncs.flipbytes(temp[0])); // md5 length
	    if(datadbg){
	      System.out.print("md5len1:");
	      otherfuncs.printbytearr(temp[0]);
	    }

	    temp = otherfuncs.splitFirstNBytes(data,4);
	    data=temp[1];
	    int md5len2 = otherfuncs.bytes2int(otherfuncs.flipbytes(temp[0])); //
	    if(datadbg){
	      System.out.print("md5len2:");
	      otherfuncs.printbytearr(temp[0]);
	    }

	    if(md5len1==md5len2){

	    }else{
	      //problem
	      VerifiedIMSGPacket a=new VerifiedIMSGPacket();
	      a.packetID=packetID1;
	    }


	    temp = otherfuncs.splitFirstNBytes(data,md5len1);
	    data=temp[1];
	    String md5 = new String(temp[0]); // md5
	    if(datadbg){
	      System.out.print("md5:");
	      otherfuncs.printbytearr(temp[0]);
	    }



	    temp = otherfuncs.splitFirstNBytes(data,4);
	    data=temp[1];
	    int datalen1 = otherfuncs.bytes2int(otherfuncs.flipbytes(temp[0])); // data length
	    if(datadbg){
	      System.out.print("datalen1:");
	      otherfuncs.printbytearr(temp[0]);
	    }

	    temp = otherfuncs.splitFirstNBytes(data,4);
	    data=temp[1];
	    int datalen2 = otherfuncs.bytes2int(otherfuncs.flipbytes(temp[0])); //
	    if(datadbg){
	      System.out.print("datalen2:");
	      otherfuncs.printbytearr(temp[0]);
	    }

	    if(datalen1==datalen2){

	    }else{
	      //problem
	      VerifiedIMSGPacket a=new VerifiedIMSGPacket();
	      a.packetID=packetID1;
	    }

	    temp = otherfuncs.splitFirstNBytes(data,datalen1);
	    data=temp[1];
	    byte[] datablock = temp[0]; // parse data
	    if(datadbg){
	System.out.println("data block length: "+datalen1);
	      System.out.print("datablock:");
	      otherfuncs.printbytearr(temp[0]);
	    }

	    //println("TEMP LEN: "+(32+8+md5.length()+datablock.length));
	    /*print("SERVER: ");
	    byte idk[] = new byte[400];
	    if(datadbg){
	      for(int k=0;k<400;k++){
	        idk[k]=data[k];
	      }
	      printbytearr(idk);
	    }*/


	    VerifiedIMSGPacket r = new VerifiedIMSGPacket();
	    r.packetID=packetID1;
	    r.clientID=clientID1;
	    r.md5=md5;
	    r.data=datablock;
	    r.setValid(true);
	    return r;
	}


	public byte[] ComposeData(){
		//byte[]d=new byte[byteindx+imsgName.length+4+imsgOrder.length+4];

		byte[] imsgNameLen = otherfuncs.int2bytes(imsgName.length());
		byte[] imsgNameData = imsgName.getBytes();

		byte[] imsgOrderLen = otherfuncs.int2bytes(imsgOrder.length());
		byte[] imsgOrderData= imsgOrder.getBytes();

		byte[] imsgBuffData = new byte[byteindx];
		System.arraycopy(imsgBuff,0,imsgBuffData,0,byteindx);

		byte[] temp = new byte[0];
		temp = otherfuncs.byteArrConcat(temp,imsgNameLen);
		temp = otherfuncs.byteArrConcat(temp,imsgNameData);
		temp = otherfuncs.byteArrConcat(temp,imsgOrderLen);
		temp = otherfuncs.byteArrConcat(temp,imsgOrderData);
		temp = otherfuncs.byteArrConcat(temp,imsgBuffData);

		//println("DATA BLOCK SIZE: "+temp.length);
		///println("******************");
		//byteprint(temp);
		//println("******************");
		return temp;
	}

	public void AddEvent(String evntname, eventObj ev){
		events.Add(evntname, ev);
	}


	class imsgClient{
		String IP;
		int port;
		Socket socket;
		boolean active=false;
		public imsgClient(Socket s){
			socket=s;
			IP = socket.getInetAddress().getHostAddress();
			port = socket.getPort();
			active=true;
		}
		public imsgClient(){
			active=false;
		}
	}
}


















