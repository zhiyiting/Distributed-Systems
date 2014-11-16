package util.comm;

import java.util.ArrayDeque;
import java.util.HashMap;

public class PartitionMessage extends Message {

	private static final long serialVersionUID = 4718349054002088714L;
	
	private HashMap<Integer, ArrayDeque<String[]>> jobToPartition;
	private int jobID;
	
	public PartitionMessage(String content, String host, int port) {
		super(content, host, port);
		jobToPartition = new HashMap<Integer, ArrayDeque<String[]>>();
	}

	public HashMap<Integer, ArrayDeque<String[]>> getPartition() {
		return jobToPartition;
	}

	public void setPartition(int jobID, ArrayDeque<String[]> partition) {
		ArrayDeque<String[]> temp = jobToPartition.get(jobID);
		if (temp == null) {
			temp = new ArrayDeque<String[]>();
		}
		temp.addAll(partition);
		jobToPartition.put(jobID, temp);
	}

	public int getJobID() {
		return jobID;
	}

	public void setJobID(int jobID) {
		this.jobID = jobID;
	}
}
