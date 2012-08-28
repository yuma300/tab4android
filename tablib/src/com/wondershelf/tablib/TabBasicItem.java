package com.wondershelf.tablib;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TabBasicItem {
	JSONObject tabitem = null;
	JSONObject mItem = null;
	TabUser mUser = null;
	
	public TabBasicItem(JSONObject item) throws JSONException {
		tabitem = item;
		mItem = tabitem.getJSONObject("item");
		mUser = new TabUser(mItem.getJSONObject("user"));
	}
	
	public JSONObject getJSONTabItem() {
		return tabitem;
	}

	public String getTitle() throws JSONException {
			return mItem.getString("title");
	}
	/*
	public double getLatitudeE6() throws JSONException {
		JSONArray places = tabitem.getJSONArray("places");
		JSONObject place = places.getJSONObject(0);
		return place.getDouble("lat") * 1000000;
	}
	
	public double getLongtitudeE6() throws JSONException {
		JSONArray places = tabitem.getJSONArray("places");
		JSONObject place = places.getJSONObject(0);
		return place.getDouble("lon") * 1000000;
	}
	*/
	public String getItemOrigImageURL() throws JSONException {
		JSONArray itemimagesarry = mItem.getJSONArray("image_urls");
		JSONObject itemimages = itemimagesarry.getJSONObject(0);
		return itemimages.getString("original");
	}

	public String getItemCropSImageURL() throws JSONException {
		JSONArray itemimagesarry = mItem.getJSONArray("image_urls");
		JSONObject itemimages = itemimagesarry.getJSONObject(0);
		return itemimages.getString("crop_S");
	}

	public String getItemCropM1ImageURL() throws JSONException {
		JSONArray itemimagesarry = mItem.getJSONArray("image_urls");
		JSONObject itemimages = itemimagesarry.getJSONObject(0);
		return itemimages.getString("crop_M1");
	}

	public String getItemCropM2ImageURL() throws JSONException {
		JSONArray itemimagesarry = mItem.getJSONArray("image_urls");
		JSONObject itemimages = itemimagesarry.getJSONObject(0);
		return itemimages.getString("crop_M2");
	}

	public String getItemID() throws JSONException {
		return mItem.getString("id");
	}

	public String getDescription() throws JSONException {
		return mItem.getString("description");
	}

	public TabUser getTabUser() {
		return mUser;
	}
}
