package com.wondershelf.tablib;

import org.json.JSONException;
import org.json.JSONObject;

public class TabUser {

	JSONObject mUser = null;
	
	TabUser(JSONObject user) {
		mUser = user;
	}
	
	public String getScreenName() throws JSONException {
		return mUser.getString("screen_name");
	}
	
	public String getId() throws JSONException {
		return mUser.getString("id");
	}
	
	public String getUserCropM1ImageURL() throws JSONException {
		JSONObject itemimages = mUser.getJSONObject("profile_image_url");
		return itemimages.getString("crop_M1");
	}
}
