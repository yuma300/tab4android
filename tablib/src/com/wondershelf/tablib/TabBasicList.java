package com.wondershelf.tablib;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TabBasicList {
	ArrayList<TabStream> mItemlist = null;
	int mTotal = 0;
	
	public TabBasicList(JSONObject rootjson) throws JSONException {
		mItemlist = new ArrayList<TabStream>();
		JSONArray items = rootjson.getJSONArray("streams");
		mTotal = items.length();
		for (int i = 0; i < items.length(); i++) {
			JSONObject stream = items.getJSONObject(i);
			TabStream tabitem = new TabStream(stream);
			mItemlist.add(tabitem);
		}

	}
	
	public ArrayList<TabStream> getItems() {
		return mItemlist;
	}
}
