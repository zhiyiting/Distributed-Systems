package util.core;

import java.io.Serializable;

import conf.Configuration;
import util.api.Mapper;
import util.api.OutputFormat;
import util.api.Reducer;

public class Job implements Serializable{

	private static final long serialVersionUID = -8502305018282114686L;
	private int id;
	private String name;
	private String outputPath;
	private Class<? extends Mapper> mapper;
	private Class<? extends Reducer> reducer;
	private Class<? extends OutputFormat> outputformat;
	public Configuration conf;

	public Job(String jobName) {
		this.name = jobName;
		this.setId(-1);
		this.outputPath = "";
		this.mapper = null;
		this.reducer = null;
		this.outputformat = null;
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

	public Class<? extends OutputFormat> getOutputFormat() {
		return outputformat;
	}

	public void setOutputFormat(Class<? extends OutputFormat> outputformat) {
		this.outputformat = outputformat;
	}
	
	public void setConfiguration(String in) {
		conf = new Configuration(in);
	}
}
