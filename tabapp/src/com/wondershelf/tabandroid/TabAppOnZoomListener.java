package com.wondershelf.tabandroid;

import com.google.android.maps.MapView;

import android.util.Log;
import android.widget.ZoomButtonsController.OnZoomListener;

public class TabAppOnZoomListener implements OnZoomListener {
	
	MapView mMap;
	int mLastZoomLevel;
	TabAppOnZoomListener(MapView m) {
		mMap = m;
		mLastZoomLevel = mMap.getZoomLevel();
	}
	@Override
	public void onVisibilityChanged(boolean visible) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onZoom(boolean zoomIn) {
		// TODO Auto-generated method stub
		if (zoomIn) {
			mMap.getController().zoomIn();
			int latspan = mMap.getLatitudeSpan();
			int lonspan = mMap.getLongitudeSpan();
			Log.d("TabAppOnZoomListener", "Zoom in latspan="+ latspan + " lonspan=" + lonspan);
			if (latspan > lonspan) {
				
			} else {
				
			}
		} else {
			mMap.getController().zoomOut();
			int latspan = mMap.getLatitudeSpan();
			int lonspan = mMap.getLongitudeSpan();
			Log.d("TabAppOnZoomListener", "Zoom out latspan="+ latspan + " lonspan=" + lonspan);
			if (latspan > lonspan) {
				
			} else {
				
			}
		}
	}

}
