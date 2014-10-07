import java.io.IOException;
import java.util.Hashtable;
import java.util.LinkedList;

/**
 * An LRU Cache that keeps track of socket information
 * @author zhiyiting
 *
 */
public class SocketCache {
	// linkedlist to sort the socket depending on least recently used
	private LinkedList<String> socketList;
	// hashtable to provide O(1) to get socket information
	private Hashtable<String, SocketInfo> sockettbl;
	// cache capacity
	private int maxCount;
	
	/**
	 * Constructor that defines the cache capacity, default is 10
	 */
	public SocketCache() {
		maxCount = 10;
		socketList = new LinkedList<String>();
		sockettbl = new Hashtable<String, SocketInfo>();
	}
	
	/**
	 * Constructor that defines the cache capacity
	 * @param n
	 */
	public SocketCache(int n) {
		maxCount = n;
		socketList = new LinkedList<String>();
		sockettbl = new Hashtable<String, SocketInfo>();
	}
	
	/**
	 * Check if the cache contains the specific socket information
	 * @param host
	 * @param port
	 * @return found or not
	 */
	public boolean contains(String host, int port) {
		String key = generateKey(host, port);
		return sockettbl.containsKey(key);
	}
	
	/**
	 * Get the socket information
	 * @param host
	 * @param port
	 * @return socket information
	 */
	public SocketInfo get(String host, int port) {
		String key = generateKey(host, port);
		// update the rank of the socket to top
		socketList.remove(key);
		socketList.addFirst(key);
		return sockettbl.get(key);
	}
	
	/**
	 * Put the socket information into the table
	 * @param host
	 * @param port
	 * @param socketInfo
	 */
	public void put(String host, int port, SocketInfo socketInfo) {
		String key = generateKey(host, port);
		if (sockettbl.size() >= maxCount) {
			// remove the least used key from the table
			String lastUsedKey = socketList.removeLast();
			SocketInfo si = sockettbl.remove(lastUsedKey);
			// close the socket and IOs
			try {
				si.in.close();
				si.out.close();
				si.socket.close();
			} catch (IOException e) {
				System.out.println("IO Exception");
				e.printStackTrace();
			}
		}
		socketList.addFirst(key);
		sockettbl.put(key, socketInfo);
	}
	
	/**
	 * Generate a key using host and port
	 * @param host
	 * @param port
	 * @return key
	 */
	private String generateKey(String host, int port) {
		return host + ":" + port;
	}
	
	
}
