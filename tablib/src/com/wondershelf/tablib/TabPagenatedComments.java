package com.wondershelf.tablib;

import java.util.ArrayList;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;


public class TabPagenatedComments {
	int mTotal = 0;
	int mPage = 0;
	int mLimit = 0;
	ArrayList<TabPagenatedComment> mCommentlist = null;

	public TabPagenatedComments(JSONObject rootjson) throws JSONException {
		mTotal = rootjson.getInt("total");
		mPage = rootjson.getInt("page");
		mLimit = rootjson.getInt("limit");
		mCommentlist = new ArrayList<TabPagenatedComment>();
		JSONArray items = rootjson.getJSONArray("comments");
		for (int i = 0; i < items.length(); i++) {
			JSONObject item = items.getJSONObject(i);
			TabPagenatedComment tabcomment = new TabPagenatedComment(item);
			mCommentlist.add(tabcomment);
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
	
	public ArrayList<TabPagenatedComment> getItems() {
		return mCommentlist;
	}
}
