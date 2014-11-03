package util;

import conf.Configuration;

public class Job {
	
	private Configuration conf;
	private String jobName;
	private String inputPath;
	private String outputPath;

	public Job(Configuration conf, String jobName) {
		this.conf = conf;
		this.jobName = jobName;
		this.inputPath = (String) conf.get("input-path");
		this.outputPath = (String) conf.get("output-path");
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
	
	public void start() {
		//ClientMonitor cm = new ClientMonitor();
		//cm.run();
	}
}
