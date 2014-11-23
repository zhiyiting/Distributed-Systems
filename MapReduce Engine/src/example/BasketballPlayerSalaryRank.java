package example;

import util.api.Mapper;
import util.api.Reducer;
import util.console.ClientConsole;
import util.core.Job;
import util.io.Context;

/**
 * A basketball player salary rank example
 * 
 * @author zhiyiting
 *
 */
public class BasketballPlayerSalaryRank {
	// custom mapper class for word sort
	public static class BasketballPlayerSalaryMapper extends Mapper {
		public void map(String key, String value, Context context) {
			if (value.length() > 1) {
				String[] words = value.split("\\t");
				// use the salary as key to be sorted
				/*
				-0-	 pitcher's name,
				-1-  player's team at the end of in 1986,
				-2-  player's league at the end of 1986,
				-3-  number of wins in 1986,
				-4-  number of losses in 1986,
				-5-  earned run average in 1986,
				-6-  number of games in 1986,
				-7-  number of innings pitched in 1986,
				-8-  number of saves in 1986,
				-9-  number of years in the major leagues,
				-10- number of wins during his career,
				-11- number of losses during his career,
				-12- earned run average during his career,
				-13- number of games during his career,
				-14- number of innings pitched during his career,
				-15- number of saves during his career,
				-16- 1987 annual salary on opening day in thousands of dollars,
				-17- player's league at the beginning of 1987,
				-18- player's team at the beginning of 1987.
				*/
				if (words.length > 18) {
					int salary = 0;
					try {
						salary = (int)Float.parseFloat(words[16]);
					}
					catch (NumberFormatException e) {
						salary = 0;
					}
					context.write(String.format("%010d", salary), words[0] + " of " + words[18]);
				}
			}
		}
	}

	// custom reducer class for word sort
	public static class BasketballPlayerSalaryReducer extends Reducer {
		public void reduce(String key, Iterable<String> values, Context context) {
			for (String val : values) {
				// name + team, salary
				String salary = null;
				try {
					salary = Integer.valueOf(key).toString();
				}
				catch (NumberFormatException e){
					salary = key;
				}
				context.write(val, salary);
			}
		}
	}

	public static void main(String[] args) throws Exception {
		// create and name a job
		Job job = new Job("Basketball_Player_Salary_Rank");
		job.setMapperClass(BasketballPlayerSalaryMapper.class);
		job.setReducerClass(BasketballPlayerSalaryReducer.class);
		job.setConfiguration("Config2.xml");
		ClientConsole client = new ClientConsole(job);
		client.run();
		while (!client.isFinished()) {
			Thread.sleep(5000);
		}
		System.exit(0);
	}
}
