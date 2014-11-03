package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import conf.Configuration;

public class Worker implements Runnable{

	private boolean canRun;
	private BufferedReader br;

	
	public Worker() {
		this.canRun = true;
		this.br = new BufferedReader(new InputStreamReader(System.in));
	}

	@Override
	public void run() {
		while (canRun) {
			try {
				String in = br.readLine();
				switch (in) {
				// print all the jobs
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
