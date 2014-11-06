package example;

import java.util.StringTokenizer;

import client.ClientConsole;
import util.*;
import conf.Configuration;

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
	
	public static class MyCombine extends Combiner {
		public void combine(String key, Iterable<Integer> values, Context context) {
			int sum = 0;
			for (Integer val : values) {
				sum += val;
			}
			context.write(key, Integer.toString(sum));
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
		job.setCombinerClass(MyCombine.class);
		job.setReducerClass(MyReduce.class);

		job.setInputFormatClass(FileInputFormat.class);
		job.setOutputFormatClass(FileOutputFormat.class);

		ClientConsole cm = new ClientConsole(job);
		cm.run();
	}

}