
import java.net.Socket;

import java.lang.*;

public class imsgServer{

	static int lolcount=0;
	public static void main(String[]args){
		byte [] ar = otherfuncs.float2bytes(73358.56f);
		for(int k=0;k<4;k++){
			System.out.print((Byte)ar[k]+" ");
		}
		System.out.println();
		imsg Imsg=new imsg();
		boolean success = Imsg.initServer(6789);
		if(success){
			System.out.println("SUCCESS");
		}else{
			System.out.println("FAYUL");
		}

			//System.out.println(otherfuncs.bytes2float(otherfuncs.float2bytes(3.14211252f)));
			//System.out.println(otherfuncs.bytes2long(otherfuncs.long2bytes(4144141122l)));
			///System.out.println(4144141122l);
			//if(otherfuncs.bytes2long(otherfuncs.long2bytes(414l))==414l){
			//System.out.println("OMGWAAW");
			//}else{
			//System.out.println("fk");
			//}

		Imsg.AddEvent("LOL",new eventObj(){
			public void call( imsgData data, int ID){

				System.out.println("LOL was ran by client ["+ID+"]");
				System.out.println(data.ReadInt());
				System.out.println(data.ReadString());
				System.out.println(data.ReadFloat());
				lolcount++;
			}
		});

		while(true){
			if(lolcount==3){
				Imsg.Start("amaze");
					Imsg.WriteInt(5);
				Imsg.Broadcast();
				lolcount=0;
			}

			try{
				Thread.currentThread().sleep(60);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		//while(true){

		//}
		/*while( Imsg.clients.length == 0){  }

		System.out.println("Sever Sending Data");

		try{
			Thread.currentThread().sleep(1000);
			System.out.println("SENDIN DATA");
			Imsg.clients[0].socket.getOutputStream().write("WAWA".getBytes());
		}catch(Exception e){
			e.printStackTrace();
		}*/

	}
}
