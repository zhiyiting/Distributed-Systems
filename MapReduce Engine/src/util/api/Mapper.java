package util.api;

import util.io.Context;

/**
 * Mapper class
 * 
 * @author zhiyiting
 *
 */
public abstract class Mapper {
	public abstract void map(String key, String value, Context context);
}
