package example;

import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import util.api.*;
import util.console.JobClient;
import util.core.Job;
import util.io.Context;

/**
 * A Word Count example
 * 
 * @author zhiyiting
 *
 */
public class WordCount {

	// custom mapper class for word count
	public static class WordCountMapper extends Mapper {
		public void map(String key, String value, Context context) {
			String line = value.toString();
			StringTokenizer tokenizer = new StringTokenizer(line);
			while (tokenizer.hasMoreTokens()) {
				String word = tokenizer.nextToken();
				// filter the unnecessary symbols
				Pattern pattern = Pattern.compile("[^\\w]*(\\w?.*\\w)[^\\w]*");
				Matcher matcher = pattern.matcher(word);
				if (matcher.find()) {
					word = matcher.group(1);
					if (word.length() > 0) {
						context.write(word.toLowerCase(), "1");
					}
				}
			}
		}
	}

	// custom reducer class for word count
	public static class WordCountReducer extends Reducer {
		public void reduce(String key, Iterable<String> values, Context context) {
			int sum = 0;
			for (String val : values) {
				sum += Integer.parseInt(val);
			}
			context.write(key, Integer.toString(sum));
		}
	}

	public static void main(String[] args) throws Exception {
		// name the job
		Job job = new Job("wordcount");
		// set mapper, reducer and configuration
		job.setMapperClass(WordCountMapper.class);
		job.setReducerClass(WordCountReducer.class);
		job.setConfiguration("Config.xml");

		// start the job and wait for termination
		JobClient client = new JobClient(job);
		client.startJob();
	}

}