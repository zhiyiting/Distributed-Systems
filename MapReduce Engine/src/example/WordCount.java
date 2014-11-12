package example;

import java.util.StringTokenizer;

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
				context.write(word, "1");
			}
		}
	}

	public static class MyReduce extends Reducer {
		public void reduce(String key, Iterable<Integer> values, Context context) {
			int sum = 0;
			for (Integer val : values) {
				sum += val;
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