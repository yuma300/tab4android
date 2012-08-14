package com.wondershelf.tabandroid;


import java.io.IOException;


import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;


import org.json.JSONException;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.wondershelf.misc.SBDialogManager;
import com.wondershelf.tablib.TabLib;
import com.wondershelf.tablib.TabPagenatedItem;
import com.wondershelf.tablib.TabPagenatedItems;


public class TabAppActivity extends MapActivity implements LocationListener, OnItemClickListener, OnTouchListener{
	private MyLocationOverlay mOverlay = null;
	private LocationManager mLocationManager;
	private TabAppMapView mapview;
	//private JSONArray mItems = null;
	ImageView mItemImage = null;
	PinItemizedOverlay mPinItems = null;
	TabAppOverlayItem mCurrentOverlayItem = null;
	ProgressBar mPbar = null;
	private TabAppAdapter adapter = null;
	GetItemsTask getitemstask = null;
	static private String TAG = "TabAppActivity";
	ListView itemlist = null;	
	final private static String preferenceFileName = "setting_info";
	SetImageTask mImageTask = null;
	ImageButton mSlide = null;
	EditText mFilter = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		setContentView(R.layout.main);
		mapview = (TabAppMapView)this.findViewById(R.id.map);
		mapview.setEnabled(true);
		mapview.setClickable(true);
		mapview.setBuiltInZoomControls(true);
		mapview.getController().setZoom(15);
		mapview.getZoomButtonsController().setOnZoomListener(new TabAppOnZoomListener(mapview));
		mapview.getZoomButtonsController().getZoomControls().setVisibility(View.GONE);
		mapview.setOnChangeListener(new MapViewChangeListener(this));
		mapview.setFocusable(true);
		
		mPbar = (ProgressBar)findViewById(R.id.mapprogress);
		mPbar.setMax(100);

		mSlide = (ImageButton)findViewById(R.id.slider);
		mSlide.setOnTouchListener(this);


		mFilter = (EditText)findViewById(R.id.filter);
		mFilter.setOnKeyListener(new OnKeyListener(){
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
					GeoPoint center = mapview.getMapCenter();
					getItems(center.getLatitudeE6(), center.getLongitudeE6());
					InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					// ボタンを押したときにソフトキーボードを閉じる
					inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(),0);
					return true;
				}
				return false;
			}
		});

		InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		// ボタンを押したときにソフトキーボードを閉じる
		inputMethodManager.hideSoftInputFromWindow(mFilter.getWindowToken(),0);

		itemlist = (ListView)findViewById(R.id.appitemlist);
		itemlist.setOnItemClickListener(this);
		adapter = new TabAppAdapter(this, R.layout.applistitem);
		itemlist.setAdapter((ArrayAdapter)adapter);
		//task = new GetItemDetailTask(this);

		Drawable pin = getResources().getDrawable(R.drawable.pin);
		mPinItems = new PinItemizedOverlay(pin) {
			@Override
			protected boolean onTap(int index) {
				if (mCurrentOverlayItem != null) {
					mCurrentOverlayItem.setMarker(boundCenterBottom(TabAppActivity.this.getResources().getDrawable(R.drawable.pin)));
				}
				mCurrentOverlayItem = (TabAppOverlayItem)getItem(index);
				mCurrentOverlayItem.setMarker(boundCenterBottom(TabAppActivity.this.getResources().getDrawable(R.drawable.pinselect)));
				mPinItems.setFocus(mCurrentOverlayItem);
				adapter.notifyDataSetChanged();

				itemlist.setSelectionFromTop(index, 0);
				
				mapview.invalidate();
				return super.onTap(index);
			}
		};
		mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		//位置情報サービスの要求条件をピックアップする
		//速度、電力消費などから適切な位置情報サービスを選択する
		Criteria criteria = new Criteria();
		//使える中で最も条件にヒットする位置情報サービスを取得する
		String bestProvider_ = mLocationManager.getBestProvider(criteria, true);
		if (bestProvider_ == null) {
			SBDialogManager.showOKDialog(this, "注意", "位置情報機能が有効になっていないため現在地の取得ができません。設定から有効にして下さい", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					//finish();
				}});
			//Toast.makeText(this, "位置情報機能が有効になっていません。設定から有効にして下さい", Toast.LENGTH_LONG).show();
			mOverlay = new MyLocationOverlay(getApplicationContext(), mapview);
		} else {
			// マップ上にオーバレイを定義
			mOverlay = new MyLocationOverlay(getApplicationContext(), mapview);
			mOverlay.onProviderEnabled(bestProvider_);
			mOverlay.enableMyLocation();
			mLocationManager.requestLocationUpdates(bestProvider_, 0, 0, this);
		}

		getitemstask = new GetItemsTask();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!getSharedPreferences(preferenceFileName, Context.MODE_PRIVATE).getBoolean("filter", true)) {
			mFilter.setVisibility(View.GONE);
		} else {
			mFilter.setVisibility(View.VISIBLE);
		}
		if (mCurrentOverlayItem != null) {
			mCurrentOverlayItem.setMarker(PinItemizedOverlay.boundCenterBottom(TabAppActivity.this.getResources().getDrawable(R.drawable.pinselect)));
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if ((getitemstask != null && getitemstask.getStatus() == AsyncTask.Status.RUNNING) || (mImageTask != null && mImageTask.getStatus() == AsyncTask.Status.RUNNING)) {
			if (getitemstask != null) {
				getitemstask.cancel(true);
				getitemstask = null;
			}
			if (mImageTask != null) {
				mImageTask.cancel(true);
				mImageTask = null;
			}
		}
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if(event.getKeyCode() == KeyEvent.KEYCODE_BACK){
			if(event.getAction() == KeyEvent.ACTION_UP){
				if ((getitemstask != null && getitemstask.getStatus() == AsyncTask.Status.RUNNING) || (mImageTask != null && mImageTask.getStatus() == AsyncTask.Status.RUNNING)) {
					if (getitemstask != null) {
						getitemstask.cancel(true);
						getitemstask = null;
					}

					if (mImageTask != null) {
						mImageTask.cancel(true);
						mImageTask = null;
					}

					Toast.makeText(this, "キャンセルしました。終了するには再度バックキーを押して下さい。", Toast.LENGTH_SHORT).show();
					mPbar.setProgress(100);
					return false;
				} else {
					SBDialogManager.showYESNODialog(this, "確認", "本アプリを終了しますか？", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}}, null);
					return false;
				}
			}
		}
		return super.dispatchKeyEvent(event);
	}

	public class SetImageTask extends AsyncTask<Integer, Integer, Integer> implements com.wondershelf.tablib.OnProgressListener{
		private ArrayList<TabPagenatedItem> mItems = null;  
		private ArrayList<Bitmap> mBitmaps = null;
		private boolean cancel = false;
		public SetImageTask(ArrayList<TabPagenatedItem> items) {
			mItems = items;
			mBitmaps = new ArrayList<Bitmap>();
		}

		public SetImageTask() {
			
		}

		@Override  
		protected void onProgressUpdate(Integer... progress) {
			mPbar.setProgress(progress[0]);
		}

		@Override  
		protected void onCancelled() {
			Log.d(TAG, "onCancelled");
			if (adapter != null) {
				adapter.setBitmap(mBitmaps);
				adapter.notifyDataSetChanged();
			}
			cancel = true;
		}

		
		@Override
		protected Integer doInBackground(Integer... arg0) {
			for (int i = 0; i < mItems.size(); i++) {
				BitmapFactory.Options bmOptions;
				bmOptions = new BitmapFactory.Options();
				bmOptions.inSampleSize = 1;
				try {
					Bitmap mBm = LoadImage(mItems.get(i).getItemCropSImageURL(), bmOptions);
					mBitmaps.add(mBm);
				} catch (IOException e) {
					e.printStackTrace();
					return -1;
				} catch (JSONException e) {
					e.printStackTrace();
					return -1;
				}
				if (cancel) {
					cancel = false;
					mPbar.setProgress(100);
					return 0;
				}
				publishProgress(50 + 50 * i / mItems.size());
			}
			return 0;
		}

		@Override
		protected void onPreExecute() {
		}
		
		@Override  
		protected void onPostExecute(Integer result) {
			if (result == 0) {
				if (adapter != null) {
					adapter.setBitmap(mBitmaps);
					adapter.notifyDataSetChanged();
				}
			}
			mPbar.setProgress(100);
		}

		@Override
		public void onProgress(int progress) {
		}
	}
	
	private void getItems(int lat, int lon) {
		if (getitemstask != null && getitemstask.getStatus() == AsyncTask.Status.RUNNING) {
			getitemstask.cancel(false);
		}
		if (mFilter.getVisibility() != View.GONE) {
			getitemstask = new GetItemsTask(this, convE6toE0(lat), convE6toE0(lon), mFilter.getText().toString());
		} else {
			getitemstask = new GetItemsTask(this, convE6toE0(lat), convE6toE0(lon));
		}
		getitemstask.execute(0);
	}

	private class GetItemsTask extends AsyncTask<Integer, Integer, Integer> implements com.wondershelf.tablib.OnProgressListener{
		double mLon;
		double mLat;
		Context mCont;
		TabPagenatedItems mItems = null;
		TabLib tlib = null;
		String mFilword = null;
		GetItemsTask(Context cont, double lat, double lon) {
			mLon = lon;
			mLat = lat;
			mCont = cont;
		}

		GetItemsTask(Context cont, double lat, double lon, String fil) {
			mLon = lon;
			mLat = lat;
			mCont = cont;
			mFilword = fil;
		}
	
		GetItemsTask() {
			
		}
		
		@Override  
		protected void onProgressUpdate(Integer... progress) {
			if (progress[0] == -1) {
				Toast.makeText(mCont, "アイテムが100件以上あり、地図上に表示されていないアイテムがあります。", Toast.LENGTH_LONG).show();
			} else if (progress[0] == - 3) {
/*				TabPagenatedItem tabitem = mItems.getItems().get(progress[1]);
				GeoPoint point;
				try {
					point = new GeoPoint((int)tabitem.getLatitudeE6(), (int)tabitem.getLongtitudeE6());
					mPinItems.addPin(new TabAppOverlayItem(point, "位置情報:" + "", "緯度:" + "" + "経度:" + "" + "\n精度:±" + "" + "m", tabitem));
					mapview.invalidate();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}*/
			} else {
				mPbar.setProgress(progress[0]);
			}
		}

		@Override  
		protected void onCancelled() {
			if (tlib != null) {
				tlib.cancel();
			}
		}

		@Override
		protected Integer doInBackground(Integer... arg0) {
			//tabから周辺情報取得
			int ret = 0;
			int lonspan = mapview.getLongitudeSpan();
			int latspan = mapview.getLatitudeSpan();
			GeoPoint center = mapview.getMapCenter();
			int latcenter = center.getLatitudeE6();
			int loncenter = center.getLongitudeE6();				
			float[] distance = new float[1];
			Location.distanceBetween (convE6toE0(latcenter + (latspan / 2)), convE6toE0(loncenter + (lonspan /2)), convE6toE0(latcenter - (latspan / 2)), convE6toE0(loncenter - (lonspan /2)), distance);
			Log.d(TAG, "distance is " + distance[0]);
			if (distance[0] / 4 > 100000) {
				distance[0] = 100000;
			} else {
				distance[0] = distance[0] / 4;
			}

			try {
				tlib = new TabLib();
				tlib.setOnProgressListener(this);
				if (mFilword == null) {
					mItems = tlib.getItemsNearby(mLat, mLon, distance[0]);
				} else {
					mItems = tlib.getItemsNearby(mLat, mLon, mFilword, distance[0]);
				}
				if (mItems == null) {
					//キャンセル
					return -2;
				}
				if (mItems.getTotal() > 100) {
					publishProgress(-1);
				}
				for (int i = 0; i < mItems.getItems().size(); i++) {
					publishProgress(-3, i);
				}
			} catch (Exception e) {
				e.printStackTrace();
				ret = 1;
			}
			return ret;
		}

		@Override
		protected void onPreExecute() {
			if (mImageTask != null) {
				mImageTask.cancel(false);
				mImageTask = null;
			}
			mPbar.setProgress(0);
			//mPinItems.removeAllPin();
			//mapview.getOverlays().clear();
			//mapview.invalidate();
			//mapview.getOverlays().add(mPinItems);
			//adapter.clear();
			//adapter.notifyDataSetChanged();
		}
		
		@Override  
		protected void onPostExecute(Integer result) {
			if (result == 0) {
				mPinItems.removeAllPin();
				mapview.getOverlays().clear();
				adapter.clear();

				int currentselect = 0;
				for (int i = 0; i < mItems.getItems().size(); i++) {
					TabPagenatedItem tabitem = mItems.getItems().get(i);
					GeoPoint point;
					try {
						point = new GeoPoint((int)tabitem.getLatitudeE6(), (int)tabitem.getLongtitudeE6());
						TabAppOverlayItem oitem = new TabAppOverlayItem(point, "位置情報:" + "", "緯度:" + "" + "経度:" + "" + "\n精度:±" + "" + "m", tabitem);
						if (mCurrentOverlayItem != null && mCurrentOverlayItem.getTabItem().getItemID().equals(tabitem.getItemID())) {
							mCurrentOverlayItem = oitem;
							oitem.setMarker(PinItemizedOverlay.boundCenterBottom(TabAppActivity.this.getResources().getDrawable(R.drawable.pinselect)));
							mPinItems.setFocus(mCurrentOverlayItem);
							currentselect = i;
						}
						mPinItems.addPin(oitem);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				mapview.getOverlays().add(mPinItems);
				mapview.getOverlays().add(mOverlay);
				mapview.invalidate();
				adapter.setList(mItems.getItems());
				adapter.notifyDataSetChanged();
				itemlist.setSelectionFromTop(currentselect, 0);
				Log.d(TAG, "start ImageTask");
				if (mImageTask != null && mImageTask.getStatus() == AsyncTask.Status.RUNNING) {
					mImageTask.cancel(false);
					mImageTask = new SetImageTask(mItems.getItems());
					mImageTask.execute(0);
				} else {
					mImageTask = new SetImageTask(mItems.getItems());
					mImageTask.execute(0);
				}
			} else if (result == 1) {
				SBDialogManager.showOKDialog(mCont, "通信エラー", "データの取得ができませんでした。再度お試し下さい。", null);
				mPbar.setProgress(100);
			} else if (result == 2) {
				mPbar.setProgress(100);
			}
		}

		@Override
		public void onProgress(int progress) {
			publishProgress(progress);
		}
	}

	public class TabAppAdapter extends ArrayAdapter {  
		private ArrayList<TabPagenatedItem> items;  
		private ArrayList<Bitmap> bitmaps;
		private LayoutInflater inflater;
		private Context mCont = null;
		
		public TabAppAdapter(Context context, int textViewResourceId) {
			super(context, textViewResourceId);
			mCont = context;
			this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);  
		}
		
		public TabAppAdapter(Context context, int textViewResourceId, ArrayList<TabPagenatedItem> items) {
			super(context, textViewResourceId, items);
			mCont = context;
			this.items = items;
			this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);  
		}
		
		public void setList(ArrayList<TabPagenatedItem> items) {
			this.items = items;
		}

		public void setBitmap(ArrayList<Bitmap> picts) {
			bitmaps = picts;
		}

		
		public TabPagenatedItem getTabPagenatedItem(int index) {
			return items.get(index);
		}

		@Override  
		public View getView(int position, View convertView, ViewGroup parent) { 
			// ビューを受け取る  
			View view = convertView;
			if (view == null) {  
				// 受け取ったビューがnullなら新しくビューを生成  
				view = inflater.inflate(R.layout.applistitem, null);  
				// 背景画像をセットする  
				//view.setBackgroundResource(R.drawable.loading);  
			}
			TextView title = (TextView)view.findViewById(R.id.searchitemtitle);  
			title.setText(items.get(position).getTitle());
			
			ImageView image = (ImageView)view.findViewById(R.id.searchitemimage);
			if (bitmaps != null && position < bitmaps.size()) {
				image.setImageBitmap(bitmaps.get(position));
			} else {
				image.setImageResource(R.drawable.sloading);
			}
			if (mCurrentOverlayItem != null && mCurrentOverlayItem.getTabItem().equals(items.get(position))) {
				view.setBackgroundColor(Color.BLUE);
			} else {
				view.setBackgroundColor(Color.BLACK);
			}
			
			return view;
		}
		@Override
		public int getPosition(Object obj) {
			return items.indexOf(obj);
		}

		@Override
		public void clear() {
			super.clear();
			if (items != null) {
				items.clear();
			}
			if (bitmaps != null) {
				bitmaps.clear();
			}
		}

		@Override
		public int getCount() {
			if (items == null) {
				return 0;
			}else{
				return items.size();
			}
		}
	}
	
	private Bitmap LoadImage(String URL, BitmapFactory.Options options) throws IOException {
		Bitmap bitmap = null;
		InputStream in = null;
		in = OpenHttpConnection(URL);
		bitmap = BitmapFactory.decodeStream(in, null, options);
		in.close();
		return bitmap;
	}

	
	private InputStream OpenHttpConnection(String strURL) throws IOException{
		InputStream inputStream = null;
		URL url = new URL(strURL);
		URLConnection conn = url.openConnection();

		HttpURLConnection httpConn = (HttpURLConnection)conn;
		httpConn.setRequestMethod("GET");
		httpConn.connect();

		if (httpConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
			inputStream = httpConn.getInputStream();
		}
		return inputStream;
	}

	@Override
	protected
	boolean isLocationDisplayed() {
		return true;
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		if (mLocationManager != null) mLocationManager.removeUpdates(this);
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		boolean ret = super.onCreateOptionsMenu(menu);
		menu.add(0 , Menu.FIRST , Menu.NONE , "現在地");
		menu.add(0 , Menu.FIRST +1, Menu.NONE , "更新");
		menu.add(0 , Menu.FIRST +2, Menu.NONE , "検索");
		menu.add(0 , Menu.FIRST + 3 ,Menu.NONE , "設定");
		menu.add(0 , Menu.FIRST + 4 ,Menu.NONE , "ログイン");
		menu.add(0 , Menu.FIRST + 5 ,Menu.NONE , "プロファイル");
		return ret;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == Menu.FIRST) {
			if (mLocationManager != null) {
				Log.e(TAG, "request update my location");
				Criteria criteria = new Criteria();
				//使える中で最も条件にヒットする位置情報サービスを取得する
				String bestProvider_ = mLocationManager.getBestProvider(criteria, true);
				if (bestProvider_ == null) {
					SBDialogManager.showOKDialog(this, "注意", "位置情報機能が有効になっていません。設定から有効にして下さい", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							//finish();
						}});
				} else {
					mOverlay.onProviderEnabled(bestProvider_);
					mOverlay.enableMyLocation();
					mLocationManager.requestLocationUpdates(bestProvider_, 0, 0, this);
				}
			}
		} else if (item.getItemId() == Menu.FIRST + 1) {
			Log.e(TAG, "request update displayed location");
			GeoPoint center = mapview.getMapCenter();
			getItems(center.getLatitudeE6(), center.getLongitudeE6());
		} else if (item.getItemId() == Menu.FIRST + 2) {
			Intent intent = new Intent(this, TabAppSearchActivity.class);
			startActivity(intent);
		} else if (item.getItemId() == Menu.FIRST + 3) {
			Intent intent = new Intent(this, TabAppSettingActivity.class);
			startActivity(intent);
		} else if (item.getItemId() == Menu.FIRST + 4) {
			Intent intent = new Intent(this, TabAppAuthActivity.class);
			startActivity(intent);
		} else if (item.getItemId() == Menu.FIRST + 5) {
			Intent intent = new Intent(this, TabAppProfileActivity.class);
			startActivity(intent);
		}
		return super.onOptionsItemSelected(item);
	}

	private double convE6toE0(int val) {
		return ((double)val / 1000000);
	}

	@Override
	public void onLocationChanged(Location location) {
		Log.e(TAG, "get updated location");
		// マップ上で新たな現在位置へ移動
		GeoPoint g = mOverlay.getMyLocation();
		if (g == null) {
			return;
		}
		mapview.getController().animateTo(g);
		mapview.getController().setCenter(g);
		Log.d("GPS操作", "位置の更新を検知");
		getItems(g.getLatitudeE6(), g.getLongitudeE6());

		mLocationManager.removeUpdates(this);
	}
	@Override
	public void onProviderDisabled(String provider) {}
	@Override
	public void onProviderEnabled(String provider) {}
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {}


	private class MapViewChangeListener implements TabAppMapView.OnChangeListener 
	{
		Context mCont = null;
		MapViewChangeListener(Context cont) {
			mCont = cont;
		}
		@Override
		public void onChange(MapView view, GeoPoint newCenter, GeoPoint oldCenter, int newZoom, int oldZoom)
		{
			// Check values
			if ((!newCenter.equals(oldCenter)) && (newZoom != oldZoom))
			{
				if (getSharedPreferences(preferenceFileName, Context.MODE_PRIVATE).getBoolean("zoomreload", true)) {
					getItems(newCenter.getLatitudeE6(), newCenter.getLongitudeE6());
				}
				// ZoomとPanがされた
			} else if (!newCenter.equals(oldCenter)) {
				if (getSharedPreferences(preferenceFileName, Context.MODE_PRIVATE).getBoolean("movereload", false)) {
					getItems(newCenter.getLatitudeE6(), newCenter.getLongitudeE6());
				}
				// Pan がされた
			} else if (newZoom != oldZoom) {
				if (getSharedPreferences(preferenceFileName, Context.MODE_PRIVATE).getBoolean("zoomreload", true)) {
					getItems(newCenter.getLatitudeE6(), newCenter.getLongitudeE6());
				}
			// Zoom 変更がされた
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		if (mCurrentOverlayItem != null) {
			mCurrentOverlayItem.setMarker(PinItemizedOverlay.boundCenterBottom(TabAppActivity.this.getResources().getDrawable(R.drawable.pin)));
			if (mCurrentOverlayItem.equals((TabAppOverlayItem)mPinItems.getItem(arg2))) {
				Intent intent = new Intent(this, TabAppItemActivity.class);
				try {
					intent.putExtra("ITEMID", mCurrentOverlayItem.getTabItem().getItemID());
					startActivity(intent);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return;
			}
		}
		mCurrentOverlayItem = (TabAppOverlayItem)mPinItems.getItem(arg2);
		mCurrentOverlayItem.setMarker(PinItemizedOverlay.boundCenterBottom(TabAppActivity.this.getResources().getDrawable(R.drawable.pinselect)));
		mPinItems.setFocus(mCurrentOverlayItem);

		adapter.notifyDataSetChanged();

		mapview.invalidate();

/*		TabAppAdapter adapter = (TabAppAdapter)((ListView)arg0).getAdapter();
		TabPagenatedItem item = (TabPagenatedItem)adapter.getTabPagenatedItem(arg2);
		Intent intent = new Intent(this, TabAppItemActivity.class);
		try {
			intent.putExtra("ITEMID", item.getItemID());
			startActivity(intent);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (v.equals(mSlide)) {
			DisplayMetrics metrics = null;
			FrameLayout l = null;
			LinearLayout.LayoutParams params = null;
			switch (event.getAction()) {
			case MotionEvent.ACTION_UP:
				metrics = new DisplayMetrics();  
				getWindowManager().getDefaultDisplay().getMetrics(metrics);
				params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, (int)(event.getRawY()));
				l = (FrameLayout)findViewById(R.id.itemlayout);
				l.setLayoutParams(params);
				
				Log.d("test", "density=" + metrics.density);  
				Log.d("test", "densityDpi=" + metrics.densityDpi);  
				Log.d("test", "scaledDensity=" + metrics.scaledDensity);  
				Log.d("test", "widthPixels=" + metrics.widthPixels);
				Log.d("test", "heightPixels=" + metrics.heightPixels);
				Log.d("test", "xDpi=" + metrics.xdpi);
				Log.d("test", "yDpi=" + metrics.ydpi);
				Log.d("TouchEvent", "X:" + event.getRawX() + ",Y:" + event.getRawY());
				Log.d("TouchEvent", "getAction()" + "ACTION_UP");
				break;
			case MotionEvent.ACTION_MOVE:
				metrics = new DisplayMetrics();  
				getWindowManager().getDefaultDisplay().getMetrics(metrics);
				params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, (int)(event.getRawY()));
				l = (FrameLayout)findViewById(R.id.itemlayout);
				l.setLayoutParams(params);

				Log.d("TouchEvent", "getAction()" + "ACTION_MOVE");
				break;
/*			case MotionEvent.ACTION_DOWN:
				Log.d("TouchEvent", "getAction()" + "ACTION_DOWN");
				break;
			case MotionEvent.ACTION_CANCEL:
				Log.d("TouchEvent", "getAction()" + "ACTION_CANCEL");
				break;*/
			}
		}
		return false;
	}

}