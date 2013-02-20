package org.yousense.common;

import android.content.Intent;
import android.os.Bundle;
import org.apache.commons.lang3.StringUtils;

import java.util.TreeSet;

public class Pretty {

	public static String prettyPrintIntent(Intent intent) {
		if (intent == null)
			return "Intent: NULL";
		
		StringBuilder sb = new StringBuilder("Intent:\n");
		if (intent.getAction() != null)
			sb.append("action=").append(intent.getAction()).append("\n");
		if (intent.getCategories() != null)
			sb.append("categories=[").append(StringUtils.join(intent.getCategories(), ", ")).append("]\n");
		if (intent.getData() != null)
			sb.append("data=").append(intent.getDataString()).append("\n");
		if (intent.getComponent() != null)
			sb.append("component=").append(intent.getComponent().flattenToString()).append("\n");
		if (intent.getExtras() != null) {
			sb.append("extras:\n");
			sb.append(prettyPrintBundle(intent.getExtras()));
		}		
		return sb.toString();
	}
	
	public static String prettyPrintBundle(Bundle bundle) {
		if (bundle == null)
			return "Bundle: NULL";
		
		StringBuilder sb = new StringBuilder();
		for (String key : new TreeSet<String>(bundle.keySet())) { // iterate in sorted order
			sb.append(key).append("=").append(bundle.get(key).toString()).append("\n");
		}
		return sb.toString();
	}
}
