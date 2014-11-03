package util;

public abstract class Reducer {
	public abstract void reduce(String key, Iterable<Integer> values, Context context);
}
