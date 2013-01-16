package org.yousense.common;

import android.content.Intent;
import android.os.Bundle;
import org.apache.commons.lang3.StringUtils;

import java.util.TreeSet;

public class Pretty {

	public static String prettyPrintIntent(Intent intent) {
		if (intent == null)
			return "Intent: NULL";
		
		StringBuffer sb = new StringBuffer("Intent:\n");
		if (intent.getAction() != null)
			sb.append("action=" + intent.getAction() + "\n");
		if (intent.getCategories() != null)
			sb.append("categories=[" + StringUtils.join(intent.getCategories(), ", ") + "]\n");
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
