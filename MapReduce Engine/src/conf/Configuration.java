package conf;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

public class Configuration {
	private static Properties config;
	public static String MASTERADDRESS;
	public static String[] SLAVEADDRESS;
	public static int SERVERPORT;
	public static int CLIENTPORT;
	public static String INPUTDIR;
	public static String OUTPUTDIR;
	
	static {
		config = new Properties();
		try {
			config.loadFromXML(new FileInputStream("Config.xml"));
			MASTERADDRESS = config.getProperty("master address");
			SLAVEADDRESS = config.getProperty("slave address").split(" ");
			SERVERPORT = Integer.parseInt(config.getProperty("server port"));
			CLIENTPORT = Integer.parseInt(config.getProperty("client port"));
			INPUTDIR = config.getProperty("input dir");
			OUTPUTDIR = config.getProperty("output dir");
			
		} catch (InvalidPropertiesFormatException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
