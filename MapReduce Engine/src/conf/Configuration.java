package conf;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

public class Configuration {
	private static Properties config;
	public static String MASTER_ADDRESS;
	public static String[] SLAVE_ADDRESS;
	public static int SERVER_PORT;
	public static int CLIENT_PORT;
	public static String INPUT_DIR;
	public static String OUTPUT_DIR;
	public static int MAP_PER_NODE;
	public static int REDUCE_PER_NODE;
	public static int HEART_BEAT_INTERVAL;
	
	static {
		config = new Properties();
		try {
			config.loadFromXML(new FileInputStream("Config.xml"));
			MASTER_ADDRESS = config.getProperty("master address");
			SLAVE_ADDRESS = config.getProperty("slave address").split(" ");
			SERVER_PORT = Integer.parseInt(config.getProperty("server port"));
			CLIENT_PORT = Integer.parseInt(config.getProperty("client port"));
			INPUT_DIR = config.getProperty("input dir");
			OUTPUT_DIR = config.getProperty("output dir");
			MAP_PER_NODE = Integer.parseInt(config.getProperty("map per node"));
			REDUCE_PER_NODE = Integer.parseInt(config.getProperty("reduce per node"));
			HEART_BEAT_INTERVAL = Integer.parseInt(config.getProperty("heart beat interval"));
		} catch (InvalidPropertiesFormatException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
