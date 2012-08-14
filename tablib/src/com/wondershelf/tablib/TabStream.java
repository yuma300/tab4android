package com.wondershelf.tablib;

import org.json.JSONException;
import org.json.JSONObject;

public class TabStream {
	JSONObject tabstream = null;
	
	public TabStream(JSONObject item) {
		tabstream = item;
	}

	public String getTitle() {
		try {
			return tabstream.getString("title");
		} catch (JSONException e) {
			try {
				throw e;
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
		return null;
	}

	public JSONObject getJSONTabItem() {
		return tabstream;
	}

	public String getItemID() throws JSONException {
		return tabstream.getString("id");
	}

}
