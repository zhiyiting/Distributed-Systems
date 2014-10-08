public interface NameServer extends MyRemote {
	public RemoteObjectRef match(String name);

	public NameServer add(String s, RemoteObjectRef r, NameServer n);

	public NameServer next();
}