package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import conf.Configuration;

public class CoordinatorConsole implements Runnable {
	private boolean canRun;
	private BufferedReader br;
	
	public CoordinatorConsole() {
		this.canRun = true;
		this.br = new BufferedReader(new InputStreamReader(System.in));
		Coordinator coord = new Coordinator(Configuration.SERVERPORT);
		Thread t = new Thread(coord);
		t.setDaemon(true);
		t.start();
	}

	@Override
	public void run() {
		while (canRun) {
			try {
				String in = br.readLine();
				switch (in) {
				// print all the jobs and associated mappers
				case "list":
					break;
				// stop the node
				case "quit":
					canRun = false;
					break;
				
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
}
