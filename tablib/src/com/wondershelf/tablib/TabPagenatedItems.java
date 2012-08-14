package com.wondershelf.tablib;

import java.util.ArrayList;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;


public class TabPagenatedItems {
	int mTotal = 0;
	int mPage = 0;
	int mLimit = 0;
	ArrayList<TabPagenatedItem> mItemlist = null;

	public TabPagenatedItems(JSONObject rootjson) throws JSONException {
		mTotal = rootjson.getInt("total");
		mPage = rootjson.getInt("page");
		mLimit = rootjson.getInt("limit");
		mItemlist = new ArrayList<TabPagenatedItem>();
		JSONArray items = rootjson.getJSONArray("items");
		for (int i = 0; i < items.length(); i++) {
			JSONObject item = items.getJSONObject(i);
			TabPagenatedItem tabitem = new TabPagenatedItem(item);
			mItemlist.add(tabitem);
		}

	}
	
	public int getTotal() {
		return mTotal;
	}

	public int getPage() {
		return mPage;
	}
	
	public int getLimit() {
		return mLimit;
	}
	
	public ArrayList<TabPagenatedItem> getItems() {
		return mItemlist;
	}
}
