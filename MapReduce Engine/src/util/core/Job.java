package util.core;

import java.io.Serializable;

import util.api.Mapper;
import util.api.Reducer;

public class Job implements Serializable{

	private static final long serialVersionUID = -8502305018282114686L;
	private int id;
	private String name;
	private String outputPath;
	private Class<? extends Mapper> mapper;
	private Class<? extends Reducer> reducer;

	public Job(String jobName) {
		this.name = jobName;
		this.setId(-1);
		this.outputPath = "";
		this.mapper = null;
		this.reducer = null;
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

	public String getOutputPath() {
		return outputPath;
	}

	public void setOutputPath(String outputPath) {
		this.outputPath = outputPath;
	}
}
