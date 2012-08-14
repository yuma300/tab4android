package com.wondershelf.tabandroid;

import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;

import com.google.android.maps.OverlayItem;
import com.wondershelf.tablib.TabPagenatedItem;

public class TabAppOverlayItem extends OverlayItem {
	TabPagenatedItem tabitem = null;
	
	public TabAppOverlayItem(GeoPoint arg0, String arg1, String arg2, TabPagenatedItem item) {
		super(arg0, arg1, arg2);
		tabitem = item;
	}
	
	public TabPagenatedItem getTabItem() {
		return tabitem;
	}
	
/*	public void setMaker(Drawable pict) {
		this.setMarker(pict);
	*/
}
