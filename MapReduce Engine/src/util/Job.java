package util;

import java.io.Serializable;

import conf.Configuration;

public class Job implements Serializable{

	private static final long serialVersionUID = -8502305018282114686L;
	private int id;
	private String name;
	private Class<? extends Mapper> mapper;
	private Class<? extends Combiner> combiner;
	private Class<? extends Reducer> reducer;

	public Job(String jobName) {
		this.name = jobName;
		this.setId(-1);
	}
	
	public void setID(int n) {
		this.setId(n);
	}
	
	public void setMapperClass(Class<? extends Mapper> cls) {
		this.mapper = cls;
	}
	
	public void setCombinerClass(Class<? extends Combiner> cls) {
		this.combiner = cls;
	}
	
	public void setReducerClass(Class<? extends Reducer> cls) {
		this.reducer = cls;
	}
	
	public void setInputFormatClass(Class <?> cls) {
		
	}
	
	public void setOutputFormatClass(Class <?> cls) {
		
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}
