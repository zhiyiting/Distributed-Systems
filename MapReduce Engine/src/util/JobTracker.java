package util;

import conf.Configuration;

public class JobTracker {

	public JobTracker() {
		CoordListener coord = new CoordListener(Configuration.SERVER_PORT, this);
		Thread t = new Thread(coord);
		t.setDaemon(false);
		t.start();
	}

	public void list() {

	}
	
	public void stop() {
		
	}
	
	@Override
	public String toString() {
		return null;
	}
}
