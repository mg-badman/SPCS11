
public class imsgClient {
	public static void main(String [] args){
		imsg Imsg = new imsg();
		boolean success = Imsg.initClient("localhost",6789);
		if(success){
			System.out.println("SUCCESS");
		}else{
			System.out.println("FAYUL");
		}
		try{
			Thread.currentThread().sleep(1000);
			System.out.println("SENDIN DATA");
			//Imsg.CLsock.getOutputStream().write("WAWA".getBytes());
		}catch(Exception e){
			e.printStackTrace();
		}
			Imsg.AddEvent("amaze",new eventObj(){
				public void call( imsgData data, int ID){
					System.out.println(data.ReadInt());
					System.out.println("You have done well, young padawan");
				}
			});
		Imsg.AddEvent("LOLSENT",new eventObj(){
			public void call( imsgData data, int ID){
				System.out.println(data.ReadString());
			}
		});
		//for(int k =0;k<500;k++){
			Imsg.Start("LOL");
				Imsg.WriteInt(4);
				Imsg.WriteString("This is a string. It works.");
				//Imsg.WriteInt(4);
				Imsg.WriteFloat(10.545664f);
			Imsg.SendToServer();
		//}
	}
}
