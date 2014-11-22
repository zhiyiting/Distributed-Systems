package util.comm;

import java.util.ArrayDeque;
import java.util.HashMap;

/**
 * Message to be transmitted with partition information
 * 
 * @author zhiyiting
 *
 */
public class PartitionMessage extends Message {

	private static final long serialVersionUID = 4718349054002088714L;

	private HashMap<Integer, ArrayDeque<String[]>> jobToPartition;

	/**
	 * Construct a partition message
	 * 
	 * @param content
	 * @param host
	 * @param port
	 */
	public PartitionMessage(String content, String host, int port) {
		super(content, host, port);
		jobToPartition = new HashMap<Integer, ArrayDeque<String[]>>();
	}

	/**
	 * Get the partition from the message
	 * 
	 * @return partition
	 */
	public HashMap<Integer, ArrayDeque<String[]>> getPartition() {
		return jobToPartition;
	}

	/**
	 * Set the partition for the message
	 * 
	 * @param jobID
	 * @param partition
	 */
	public void setPartition(int jobID, ArrayDeque<String[]> partition) {
		ArrayDeque<String[]> temp = jobToPartition.get(jobID);
		if (temp == null) {
			temp = new ArrayDeque<String[]>();
		}
		temp.addAll(partition);
		jobToPartition.put(jobID, temp);
	}
}
