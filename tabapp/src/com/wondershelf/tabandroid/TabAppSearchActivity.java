package com.wondershelf.tabandroid;


import java.io.IOException;


import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;


import org.json.JSONException;

import com.wondershelf.tablib.TabLib;
import com.wondershelf.tablib.TabPagenatedItem;
import com.wondershelf.tablib.TabPagenatedItems;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;

public class TabAppSearchActivity extends Activity implements OnClickListener, OnItemClickListener{
	Button mSearch = null;
	EditText searchword = null;
	ProgressBar mPbar = null;
	ListView itemlist = null;
	TabPagenatedItems mItems = null;
	private TabPagenatedItemAdapter adapter = null;
	private final String TAG = "TabAppSearchActivity";
	GetItemsTask mTask = null;
	SetImageTask mImageTask = null;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search);
		mSearch = (Button)findViewById(R.id.searchbutton);
		mSearch.setOnClickListener(this);
		
		searchword = (EditText)findViewById(R.id.searchword);
		searchword.setOnKeyListener(new OnKeyListener(){
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
					executeSearch();
					return true;
				}
				return false;
			}
		});

		mPbar = (ProgressBar)findViewById(R.id.searchprogress);
		mPbar.setMax(100);
		
		itemlist = (ListView)findViewById(R.id.itemlist);
		itemlist.setOnItemClickListener(this);
		
		mTask = new GetItemsTask();
	}
	
	@Override
	public void onClick(View v) {
		if (v.equals(mSearch)) {
			if (searchword != null && !searchword.getText().toString().equals("")) {
				executeSearch();
			}
		}
		
	}

	private void executeSearch() {
		if (mTask != null && mTask.getStatus() == AsyncTask.Status.RUNNING) {
			mTask.cancel(true);
			mTask = new GetItemsTask(this, searchword.getText().toString());
			mTask.execute(0);
		} else {
			mTask = new GetItemsTask(this, searchword.getText().toString());
			mTask.execute(0);
		}
		InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		// ボタンを押したときにソフトキーボードを閉じる
		inputMethodManager.hideSoftInputFromWindow(searchword.getWindowToken(),0);
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
	
	
	public class SetImageTask extends AsyncTask<Integer, Integer, Integer> implements com.wondershelf.tablib.OnProgressListener{
		private ArrayList<TabPagenatedItem> mItems = null;  
		private ArrayList<Bitmap> mBitmaps = null;

		public SetImageTask(ArrayList<TabPagenatedItem> items) {
			mItems = items;
			mBitmaps = new ArrayList<Bitmap>();
		}

		public SetImageTask() {
			
		}

		@Override  
		protected void onProgressUpdate(Integer... progress) {
		
		
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
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return 0;
		}

		@Override
		protected void onPreExecute() {
		}
		
		@Override  
		protected void onPostExecute(Integer result) {
			if (adapter != null) {
				adapter.setBitmap(mBitmaps);
				adapter.notifyDataSetChanged();
			}
		}

		@Override
		public void onProgress(int progress) {
		}
	}

	
	
	private class GetItemsTask extends AsyncTask<Integer, Integer, Integer> implements com.wondershelf.tablib.OnProgressListener{
		String mSearchWord = null;
		Context mCont = null;
		public GetItemsTask(Context cont, String word) {
			mSearchWord = word;
			mCont = cont;
		}

		public GetItemsTask() {
			
		}

		@Override  
		protected void onProgressUpdate(Integer... progress) {
			mPbar.setProgress(progress[0]);
		}

		@Override
		protected Integer doInBackground(Integer... arg0) {
			int ret= 0;
			try {
				TabLib tlib = new TabLib();
				tlib.setOnProgressListener(this);
				mItems = tlib.getItemsSearch(mSearchWord);
			} catch (Exception e) {
				e.printStackTrace();
				ret = 1;
			}
			return ret;
		}

		@Override
		protected void onPreExecute() {
			if (adapter != null) {
				adapter.setBitmap(null);
			}
		}
		
		@Override  
		protected void onPostExecute(Integer result) {
			mPbar.setProgress(100);
			if (adapter == null) {
				adapter = new TabPagenatedItemAdapter(mCont, R.layout.searchlistitem, mItems.getItems());
				itemlist.setAdapter((ArrayAdapter)adapter);
			} else {
				//adapter.clear();
				adapter.setList(mItems.getItems());
				adapter.notifyDataSetChanged();
				//adapter.notifyDataSetInvalidated();
				//itemlist.invalidate();
			}
			if (mImageTask != null && mImageTask.getStatus() == AsyncTask.Status.RUNNING) {
				mImageTask.cancel(true);
				mImageTask = new SetImageTask(mItems.getItems());
				mImageTask.execute(0);
			} else {
				mImageTask = new SetImageTask(mItems.getItems());
				mImageTask.execute(0);
			}
		}

		@Override
		public void onProgress(int progress) {
			publishProgress(progress);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		TabPagenatedItemAdapter adapter = (TabPagenatedItemAdapter)((ListView)arg0).getAdapter();
		TabPagenatedItem item = (TabPagenatedItem)adapter.getTabPagenatedItem(arg2);
		Intent intent = new Intent(this, TabAppItemActivity.class);
		try {
			intent.putExtra("ITEMID", item.getItemID());
			startActivity(intent);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	protected void onPause() {
	super.onPause();
	}
	
	@Override
	protected void onResume() {
	super.onResume();
	}

	@Override
	protected void onStart() {
	super.onStart();
	}

	@Override
	protected void onStop() {
	super.onStop();
	}

	@Override
	protected void onDestroy () {
		super.onDestroy();
		if (mTask != null) {
			mTask.cancel(true);
		}

		if (mImageTask != null) {
			mImageTask.cancel(true);
		}
	}


}
