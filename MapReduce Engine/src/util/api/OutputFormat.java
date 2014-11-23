package util.api;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayDeque;

/**
 * OutputFormat class parse the file by lines to generate output
 * User can extend it with custom output format class
 * 
 * @author zhiyiting
 *
 */
public class OutputFormat {

	/**
	 * Get key-value pair from the input file
	 * 
	 * @param path
	 * @return key-value pairs
	 */
	public ArrayDeque<String[]> getKVPair(String path) {
		ArrayDeque<String[]> result = new ArrayDeque<String[]>();
		File file = new File(path);
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			// read the file line by line
			while ((line = br.readLine()) != null) {
				String[] pair = { "", line };
				result.add(pair);
			}
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
}
