package util.api;

import util.io.Context;

/**
 * Reducer class
 * 
 * @author zhiyiting
 *
 */
public abstract class Reducer {
	public abstract void reduce(String key, Iterable<String> values,
			Context context);
}
