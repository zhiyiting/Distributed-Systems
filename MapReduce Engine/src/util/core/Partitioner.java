package util.core;

public class Partitioner {
	
	public int getPartitionID(String key, int partitionNum) {
		return key.hashCode() % partitionNum;
	}
}
