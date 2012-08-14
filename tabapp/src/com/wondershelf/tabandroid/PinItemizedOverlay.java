package com.wondershelf.tabandroid;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class PinItemizedOverlay extends ItemizedOverlay<OverlayItem> {
	private ArrayList<TabAppOverlayItem> items = new ArrayList<TabAppOverlayItem>();
	public PinItemizedOverlay(Drawable defaultMarker) {
		super(boundCenterBottom(defaultMarker));
	}
	@Override
	protected OverlayItem createItem(int i) {
		return items.get(i);
	}
	@Override
	public int size() {
		return items.size();
	}

	static public Drawable boundCenterBottom(Drawable pict) {
		return ItemizedOverlay.boundCenterBottom(pict);
	}
	
	public void addPin(TabAppOverlayItem tabitem) {
		//items.add(new TabAppOverlayItem(point, title, snippet, tabitem));
		items.add(tabitem);
		setLastFocusedIndex(-1);
		populate();
	}
	
	public void removeAllPin() {
		items.clear();
		setLastFocusedIndex(-1);
		populate();
	}
}