package org.yousense.common;

import android.content.Intent;
import android.os.Bundle;

import java.util.TreeSet;

public class Pretty {

	public static String join(String separator, Iterable<String> elements) {
		StringBuffer sb = new StringBuffer();
		boolean first = true;
		for (String elem : elements) {
			if (first) {
				sb.append(elem);
				first = false;
			} else {
				sb.append(separator);
				sb.append(elem);
			}
		}		
		return sb.toString();


	}
	
	public static String hexString(byte[] bytes) {
		String hex = "0123456789abcdef";
		StringBuilder sb = new StringBuilder();		
		for (byte b : bytes) {
			sb.append(hex.charAt((b >> 4) & 0xF));
			sb.append(hex.charAt(b & 0xF));
		}
		return sb.toString();
	}
	
	public static String prettyPrintIntent(Intent intent) {
		if (intent == null)
			return "Intent: NULL";
		
		StringBuffer sb = new StringBuffer("Intent:\n");
		if (intent.getAction() != null)
			sb.append("action=" + intent.getAction() + "\n");
		if (intent.getCategories() != null)
			sb.append("categories=[" + Strings.join(", ", intent.getCategories()) + "]\n");
		if (intent.getData() != null)
			sb.append("data=" + intent.getDataString() + "\n");
		if (intent.getComponent() != null)
			sb.append("component=" + intent.getComponent().flattenToString() + "\n");		
		if (intent.getExtras() != null) {
			sb.append("extras:\n");
			sb.append(prettyPrintBundle(intent.getExtras()));
		}		
		return sb.toString();
	}
	
	public static String prettyPrintBundle(Bundle bundle) {
		if (bundle == null)
			return "Bundle: NULL";
		
		StringBuffer sb = new StringBuffer();
		for (String key : new TreeSet<String>(bundle.keySet())) { // iterate in sorted order
			sb.append(key + "=" + bundle.get(key).toString() + "\n");
		}
		return sb.toString();
	}
}
