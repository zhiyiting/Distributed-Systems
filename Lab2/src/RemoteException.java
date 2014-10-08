/**
 * All methods of remote object throw Remote Exception
 * @author zhiyiting
 *
 */
public class RemoteException extends Exception{

	private static final long serialVersionUID = 1315014456808159497L;
	private String message;
	
	public RemoteException() {
		
	}
	
	public RemoteException(String s) {
		this.message = s;
	}
	
	@Override
	public String getMessage() {
		return message;
	}

}
