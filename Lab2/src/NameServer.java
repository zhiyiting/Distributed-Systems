import java.io.Serializable;

public interface NameServer extends MyRemote, Serializable {
	public RemoteObjectRef match(String name);

	public NameServer add(String s, RemoteObjectRef r, NameServer n);

	public NameServer next();
}