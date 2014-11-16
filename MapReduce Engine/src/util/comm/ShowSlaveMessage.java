package util.comm;

import java.util.HashMap;

public class ShowSlaveMessage extends Message {
	
	private static final long serialVersionUID = -7406401747697018708L;
	private HashMap<Integer, String> slaveList;

	public ShowSlaveMessage(String content) {
		super(content);
	}

	public HashMap<Integer, String> getSlaveList() {
		return slaveList;
	}

	public void setSlaveList(HashMap<Integer, String> slaveList) {
		this.slaveList = slaveList;
	}

}
