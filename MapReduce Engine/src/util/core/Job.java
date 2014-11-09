package util.core;

import java.io.Serializable;

import util.api.Combiner;
import util.api.Mapper;
import util.api.Reducer;

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
	
	public String getName() {
		return this.name;
	}
	
	public void setID(int n) {
		this.setId(n);
	}
	
	public void setMapperClass(Class<? extends Mapper> cls) {
		this.mapper = cls;
	}
	
	public Class<? extends Mapper> getMapper() {
		return this.mapper;
	}
	
	public void setCombinerClass(Class<? extends Combiner> cls) {
		this.combiner = cls;
	}
	
	public Class<? extends Combiner> getCombiner() {
		return this.combiner;
	}
	
	public void setReducerClass(Class<? extends Reducer> cls) {
		this.reducer = cls;
	}
	
	public Class<? extends Reducer> getReducer() {
		return this.reducer;
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
