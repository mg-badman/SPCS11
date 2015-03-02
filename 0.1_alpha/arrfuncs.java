public class arrfuncs{
	public static Object [] addToArray(Object[] arr, Object e){
		Object [] ret = new Object[arr.length+1];
		for(int k=0;k<arr.length;k++){
			ret[k]=arr[k];
		}
		ret[arr.length]=e;
		return ret;
	}
	public static imsg.imsgClient [] addToArray(imsg.imsgClient[] arr, imsg.imsgClient e){
			imsg.imsgClient [] ret = new imsg.imsgClient[arr.length+1];
			for(int k=0;k<arr.length;k++){
				ret[k]=arr[k];
			}
			ret[arr.length]=e;
			return ret;
	}
}