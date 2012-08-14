package com.wondershelf.tabandroid;

import android.content.Context;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

public class TabAppMapView extends MapView {

	// ------------------------------------------------------------------------
    // MEMBERS
    // ------------------------------------------------------------------------
 
    private TabAppMapView mThis;
    private boolean mIsTouched = false;
    private GeoPoint mLastCenterPosition;
    private int mLastZoomLevel;
    private TabAppMapView.OnChangeListener mChangeListener = null;
    DelayTimerTask task = null;
    final private String TAG = "TabAppMapView";

    public TabAppMapView(Context arg0, String arg1) {
		super(arg0, arg1);
		init();
	}

	public TabAppMapView(Context arg0, AttributeSet arg1) {
		super(arg0, arg1);
		init();
	}

	
	public TabAppMapView(Context arg0, AttributeSet arg1, int arg2) {
		super(arg0, arg1, arg2);
		init();
	}

	private void init()
	{
		mThis = this;
		mLastCenterPosition = this.getMapCenter();
		mLastZoomLevel = this.getZoomLevel();
		task = new DelayTimerTask();
	}

	// ------------------------------------------------------------------------
	// GETTERS / SETTERS
	// ------------------------------------------------------------------------
 
	public void setOnChangeListener(TabAppMapView.OnChangeListener l)
	{
		mChangeListener = l;
	}
	// Listener の定義
	public interface OnChangeListener
	{
		public void onChange(MapView view, GeoPoint newCenter, GeoPoint oldCenter, int newZoom, int oldZoom);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev)
	{
		// Set touch internal

		mIsTouched = (ev.getAction() != MotionEvent.ACTION_UP);
		return super.onTouchEvent(ev);
	}

	@Override
	public void computeScroll()
	{
		super.computeScroll();
		
		// Check for change
		if (isSpanChange() || isZoomChange())
		{
			// If computeScroll called before timer counts down we should drop it and
			// start counter over again
			resetMapChangeTimer();
		}
	}
	// ------------------------------------------------------------------------
	// TIMER RESETS
	// ------------------------------------------------------------------------

	private void resetMapChangeTimer()
	{
		if (task.getStatus() != AsyncTask.Status.RUNNING) {
			task = new DelayTimerTask();
			task.execute(0);
		}
	}

	public class DelayTimerTask extends AsyncTask<Integer, Integer, Integer> {
		DelayTimerTask() {
		}

		@Override  
		protected void onProgressUpdate(Integer... progress) {
		
		
		}

		@Override
		protected Integer doInBackground(Integer... arg0) {
			try {
				Log.d("TabAppMapView", "sleep start");
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				Log.d("TabAppMapView", "sleep fail");
				e.printStackTrace();
				return 1;
			} 
			return 0;
		}
		
		@Override  
		protected void onPostExecute(Integer result) {
			Log.d(TAG, "sleep end");
			if (result == 0) {
				if (mChangeListener != null) mChangeListener.onChange(mThis, getMapCenter(), mLastCenterPosition, getZoomLevel(), mLastZoomLevel);
				mLastCenterPosition = getMapCenter();
				mLastZoomLevel = getZoomLevel();
			} else {
				mLastCenterPosition = getMapCenter();
				mLastZoomLevel = getZoomLevel();
			}
		}

		@Override  
		protected void onCancelled() {
			Log.d("TabAppMapView", "sleep calcel");
			mLastCenterPosition = getMapCenter();
			mLastZoomLevel = getZoomLevel();
		}
	}
	
	
	// ------------------------------------------------------------------------
	// CHANGE FUNCTIONS
	// ------------------------------------------------------------------------
 
	private boolean isSpanChange()
	{
		return !mIsTouched && !getMapCenter().equals(mLastCenterPosition);
	}

	private boolean isZoomChange()
	{
		return (getZoomLevel() != mLastZoomLevel);
	}
}