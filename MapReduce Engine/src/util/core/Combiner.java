package util.core;

import util.io.Context;

public abstract class Combiner {
	public abstract void combine(String key, Iterable<Integer> values, Context context);
}
