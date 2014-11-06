package util;

import java.io.Serializable;

import conf.Configuration;

public class Job implements Serializable{

	private static final long serialVersionUID = -8502305018282114686L;
	private int id;
	private String name;

	public Job(String jobName) {
		this.name = jobName;
		this.id = -1;
	}
	
	public void setID(int n) {
		this.id = n;
	}
	
	public void setMapperClass(Class<? extends Mapper> cls) {
		
	}
	
	public void setCombinerClass(Class<? extends Combiner> cls) {
		
	}
	
	public void setReducerClass(Class<? extends Reducer> cls) {
		
	}
	
	public void setInputFormatClass(Class <?> cls) {
		
	}
	
	public void setOutputFormatClass(Class <?> cls) {
		
	}
}
