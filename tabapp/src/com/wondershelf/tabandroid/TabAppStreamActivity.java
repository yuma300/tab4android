package com.wondershelf.tabandroid;

import java.io.IOException;
import java.io.InputStream;

import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.json.JSONException;

import com.wondershelf.misc.SBDialogManager;
import com.wondershelf.tabandroid.TabAppSearchActivity.SetImageTask;
import com.wondershelf.tablib.TabBasicList;
import com.wondershelf.tablib.TabLib;
import com.wondershelf.tablib.TabPagenatedItem;
import com.wondershelf.tablib.TabPagenatedItems;
import com.wondershelf.tablib.misc.NotLoginException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

public class TabAppStreamActivity extends Activity implements OnItemClickListener{
	private ListView itemlist = null;
	private TabPagenatedItemAdapter adapter = null;
	String mId = null;
	ProgressBar mPbar = null;
	TabPagenatedItems mItems = null;
	GetItemsTask mTask = null;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.stream);

		Intent intent = getIntent();
		mId = intent.getStringExtra("TABID");
		
		
		itemlist = (ListView)findViewById(R.id.itemlist);
		itemlist.setOnItemClickListener(this);
		
		mPbar = (ProgressBar)findViewById(R.id.searchprogress);
		mPbar.setMax(100);
		executeTask();
	}

	private void executeTask() {
		if (mTask != null && mTask.getStatus() == AsyncTask.Status.RUNNING) {
			mTask.cancel(true);
			mTask = new GetItemsTask(this);
			mTask.execute(0);
		} else {
			mTask = new GetItemsTask(this);
			mTask.execute(0);
		}
	}
	
	private class GetItemsTask extends AsyncTask<Integer, Integer, Integer> implements com.wondershelf.tablib.OnProgressListener{
		Context mCont = null;
		private ArrayList<Bitmap> mBitmaps = null;
		public GetItemsTask(Context cont) {
			mCont = cont;
			mBitmaps = new ArrayList<Bitmap>();
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
		protected void onProgressUpdate(Integer... progress) {
			mPbar.setProgress(progress[0]);
			if (progress[0].intValue() == 50) {
				if (adapter == null) {
					adapter = new TabPagenatedItemAdapter(mCont, R.layout.searchlistitem, mItems.getItems());
					itemlist.setAdapter((ArrayAdapter)adapter);
				} else {
					adapter.setList(mItems.getItems());
				}
			}
		}

		@Override
		protected Integer doInBackground(Integer... arg0) {
			int ret= 0;
			try {
				publishProgress(10);
				TabLib tlib = new TabLib();
				tlib.setOnProgressListener(this);
				mItems = tlib.getItemsStream(mId);
				publishProgress(50);
				for (int i = 0; i < mItems.getItems().size(); i++) {
					BitmapFactory.Options bmOptions;
					bmOptions = new BitmapFactory.Options();
					bmOptions.inSampleSize = 1;
					try {
						Bitmap mBm = LoadImage(mItems.getItems().get(i).getItemCropSImageURL(), bmOptions);
						mBitmaps.add(mBm);
					} catch (IOException e) {
						e.printStackTrace();
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					publishProgress(50 + 40 * i / mItems.getItems().size());
				}
			
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
			if (adapter != null) {
				adapter.setBitmap(mBitmaps);
				adapter.notifyDataSetChanged();
			}
		}

		@Override
		public void onProgress(int progress) {
			publishProgress(progress / 2);
			
		}

	}

	public boolean onCreateOptionsMenu(Menu menu) {
		boolean ret = super.onCreateOptionsMenu(menu);
		menu.add(0 , Menu.FIRST , Menu.NONE , "共有");
		return ret;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == Menu.FIRST) {
			if (mItems != null) {
				Intent intent = new Intent(android.content.Intent.ACTION_SEND);
				intent.setType("text/plain");
				String title = "";
				try {
					title = mItems.getItems().get(0).getStream().getTitle();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				intent.putExtra(Intent.EXTRA_TEXT, title + " http://tab.do/streams/" + mId + " #tab_do");
				startActivity(intent);
			} else {
				SBDialogManager.showOKDialog(this, "エラー", "アイテム情報を読み込み中です", null);
			}
		}
		return super.onOptionsItemSelected(item);
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
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onDestroy () {
		super.onDestroy();
		if (mTask != null) {
			mTask.cancel(true);
		}
	}
}
