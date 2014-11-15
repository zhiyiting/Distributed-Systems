package util.api;

import java.util.ArrayDeque;

public class OutputFormat {

	public OutputFormat() {

	}

	public ArrayDeque<String[]> getKVPair(String s) {
		ArrayDeque<String[]> result = new ArrayDeque<String[]>();
		int len = s.length();
		if (len <= 0) {
			return result;
		}
		String[] pair = new String[2];
		pair[0] = "";
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < len; i++) {
			char cur = s.charAt(i);
			if (cur == '\n') {
				pair[1] = sb.toString();
				result.add(pair);
				sb = new StringBuilder();
				pair = new String[2];
				pair[0] = "";
			} else {
				sb.append(cur);
			}
		}
		if (sb.length() > 0) {
			pair[1] = sb.toString();
			result.add(pair);
		}
		return result;
	}
}
