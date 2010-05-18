package org.fedorahosted.flies.model.po;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.fedorahosted.flies.rest.dto.po.HeaderEntry;
import org.fedorahosted.openprops.Properties;

public class PoUtility {

	public static String concatFlags(List<String> flags) {
		return concat(flags, ',');
	}
	public static String concatRefs(List<String> refs) {
		return concat(refs, ' ');
	}
	private static String concat(List<String> strings, char c) {
		StringBuilder sb = new StringBuilder();
		for (Iterator<String> it = strings.iterator(); it.hasNext();) {
			String s = it.next();
			sb.append(s);
			if (it.hasNext())
				sb.append(c);
		}
		return sb.toString();
	}
	
	public static String listToHeader(List<HeaderEntry> entries) {
		Properties props = new Properties();
		for (HeaderEntry entry : entries) {
			props.setProperty(entry.getKey(), entry.getValue());
		}
		return propertiesToHeader(props);
	}
	
	static String propertiesToHeader(Properties entries) {
//		String lineSep = System.getProperty("line.separator");
		StringWriter writer = new StringWriter();
		try {
			entries.store(writer, null);
		} catch (IOException e) {
			throw new RuntimeException("unexpected IO exception", e);
		}
//		StringBuffer buffer = writer.getBuffer();
//		assert buffer.charAt(0) == '#';
//		int newline = buffer.indexOf(lineSep);
//		return buffer.substring(newline+lineSep.length());
		return writer.toString();
	}
	
	public static List<HeaderEntry> headerToList(String entries) {
		Properties props = headerToProperties(entries);
		List<HeaderEntry> result = new ArrayList<HeaderEntry>();
		for (String key : props.keySet()) {
			result.add(new HeaderEntry(key, props.getProperty(key)));
		}
		return result;
	}
	
	static Properties headerToProperties(String entries) {
		Properties result = new Properties();
		try {
			result.load(new StringReader(entries));
		} catch (IOException e) {
			throw new RuntimeException("unexpected IO exception", e);
		}
		return result;
	}
	
	public static List<String> splitFlags(String flags) {
		return split(flags, ',');
	}
	public static List<String> splitRefs(String refs) {
		return split(refs, ' ');
	}
	private static List<String> split(String s, char c) {
		List<String> strings = new ArrayList<String>();
		StringTokenizer tok = new StringTokenizer(s, ""+c);
		while (tok.hasMoreTokens()) {
			String o = tok.nextToken();
			strings.add(o);
		}
		return strings;
	}

}
