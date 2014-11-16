package example;

import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import util.api.*;
import util.console.ClientConsole;
import util.core.Job;
import util.io.Context;

public class WordCount {

	public static class MyMap extends Mapper {

		public void map(String key, String value, Context context) {
			String line = value.toString();
			StringTokenizer tokenizer = new StringTokenizer(line);
			while (tokenizer.hasMoreTokens()) {
				String word = tokenizer.nextToken();
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

	public static class MyReduce extends Reducer {
		public void reduce(String key, Iterable<String> values, Context context) {
			int sum = 0;
			for (String val : values) {
				sum += Integer.parseInt(val);
			}
			context.write(key, Integer.toString(sum));
		}
	}

	public static void main(String[] args) throws Exception {

		Job job = new Job("wordcount");

		job.setMapperClass(MyMap.class);
		job.setReducerClass(MyReduce.class);

		ClientConsole client = new ClientConsole(job);
		client.run();
	}

}