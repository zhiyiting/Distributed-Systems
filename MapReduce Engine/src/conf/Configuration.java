package conf;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

public class Configuration implements Serializable {

	private static final long serialVersionUID = 2425337396764969957L;
	private Properties config;
	public static final String MASTER_ADDRESS = "unix1.andrew.cmu.edu";
	public static final int SERVER_PORT = 18111;
	public static final int CLIENT_PORT = 18112;
	public static final int MAP_PER_NODE = 3;
	public static final int REDUCE_PER_NODE = 1;
	public static final int HEART_BEAT_INTERVAL = 1000;
	public String INPUT_DIR;
	public String OUTPUT_DIR;
	public int RECORD_SIZE;
	public int REPLICA;
	public int REDUCER_NUM;
	public static int RETRY_NUM = 3;

	public Configuration(String in) {
		config = new Properties();
		try {
			config.loadFromXML(new FileInputStream(in));
			INPUT_DIR = config.getProperty("input dir") + "/";
			OUTPUT_DIR = config.getProperty("output dir") + "/";
			RECORD_SIZE = Integer.parseInt(config.getProperty("record size"));
			REPLICA = Integer.parseInt(config.getProperty("replica"));
			REDUCER_NUM = Integer.parseInt(config.getProperty("reducer num"));
		} catch (InvalidPropertiesFormatException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
