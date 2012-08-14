package com.wondershelf.tablib;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TabPagenatedComment {
	JSONObject tabitem = null;

	public TabPagenatedComment(JSONObject item) {
		tabitem = item;
	}
	
	public JSONObject getJSONTabItem() {
		return tabitem;
	}

/*	public String getTitle() {
		try {
			return tabitem.getString("title");
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
	
	public String getItemOrigImageURL() throws JSONException {
		JSONArray itemimagesarry = tabitem.getJSONArray("image_urls");
		JSONObject itemimages = itemimagesarry.getJSONObject(0);
		return itemimages.getString("original");
	}

	public String getItemCropSImageURL() throws JSONException {
		JSONArray itemimagesarry = tabitem.getJSONArray("image_urls");
		JSONObject itemimages = itemimagesarry.getJSONObject(0);
		return itemimages.getString("crop_S");
	}

	public String getItemCropM1ImageURL() throws JSONException {
		JSONArray itemimagesarry = tabitem.getJSONArray("image_urls");
		JSONObject itemimages = itemimagesarry.getJSONObject(0);
		return itemimages.getString("crop_M1");
	}*/
	
	public String getCommentID() throws JSONException {
		return tabitem.getString("id");
	}

	public String getText() throws JSONException {
		return tabitem.getString("text");
	}

	public String getScreenName() throws JSONException {
		return tabitem.getJSONObject("user").getString("screen_name");
	}
}
