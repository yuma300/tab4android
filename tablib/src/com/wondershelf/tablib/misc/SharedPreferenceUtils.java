package com.wondershelf.tablib.misc;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferenceUtils {
	final private static String preferenceFileName = "user_info";

	public SharedPreferenceUtils() {
		
	}

	public void setStringPreference(Context cont, String key, String value) {
		SharedPreferences.Editor editor = cont.getSharedPreferences(preferenceFileName, Context.MODE_PRIVATE).edit();
		editor.putString(key, value);
		editor.commit();
	}
}
