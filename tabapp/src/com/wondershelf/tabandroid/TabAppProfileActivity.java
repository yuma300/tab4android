package com.wondershelf.tabandroid;

import java.io.IOException;

import java.io.InputStream;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.json.JSONException;

import com.wondershelf.tablib.TabBasicList;
import com.wondershelf.tablib.TabLib;
import com.wondershelf.tablib.TabStream;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;

public class TabAppProfileActivity extends Activity implements OnClickListener, OnItemClickListener{
	private ListView itemlist = null;
	private TabStreamAdapter adapter = null;
	private Button mOwn = null;
	private Button mFollow = null;
	private ProgressBar mPbar = null;
	TabBasicList mItems;
	GetStreamsTask mTask = null;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.profile);
		
		itemlist = (ListView)findViewById(R.id.itemlist);
		itemlist.setOnItemClickListener(this);
		
		mOwn = (Button)findViewById(R.id.own);
		mOwn.setOnClickListener(this);

		mFollow = (Button)this.findViewById(R.id.follow);
		mFollow.setOnClickListener(this);
		
		mPbar = (ProgressBar)findViewById(R.id.searchprogress);
		
/*		TabLib lib = new TabLib();
		try {
			TabBasicList items;
			items = lib.getMyOwnTabs(this);

			adapter = new TabStreamAdapter(this, R.layout.searchlistitem, items.getItems());
			itemlist.setAdapter((ArrayAdapter)adapter);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotLoginException e) {
			//ログインしていない
			Intent intent = new Intent(this, TabAppAuthActivity.class);
			startActivity(intent);
			e.printStackTrace();
		}*/
	}
	
	private void executeTask(int mode) {
		if (mTask != null && mTask.getStatus() == AsyncTask.Status.RUNNING) {
			mTask.cancel(true);
			mTask = new GetStreamsTask(this, mode);
			mTask.execute(0);
		} else {
			mTask = new GetStreamsTask(this, mode);
			mTask.execute(0);
		}
	}

	private class GetStreamsTask extends AsyncTask<Integer, Integer, Integer> implements com.wondershelf.tablib.OnProgressListener{
		Context mCont = null;
		private ArrayList<Bitmap> mBitmaps = null;
		int mMode = 0; //0の場合は自分で作成したtab、1の場合はフォローしたtab
		public GetStreamsTask(Context cont, int mode) {
			mCont = cont;
			mBitmaps = new ArrayList<Bitmap>();
			mMode = mode;
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
					adapter = new TabStreamAdapter(mCont, R.layout.searchlistitem, mItems.getItems());
					itemlist.setAdapter((ArrayAdapter)adapter);
				} else {
					adapter.setList(mItems.getItems());
					adapter.notifyDataSetChanged();
				}
			}
		}

		@Override
		protected Integer doInBackground(Integer... arg0) {
			int ret= 0;
			try {
				TabLib tlib = new TabLib();
				tlib.setOnProgressListener(this);
				if (mMode == 0) {
					mItems = tlib.getMyOwnTabs(mCont);
				} else {
					mItems = tlib.getMyFollowTabs(mCont);

				}
				publishProgress(50);
/*				for (int i = 0; i < mItems.getItems().size(); i++) {
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
				}*/
			
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
			mPbar.setProgress(0);
		}
		
		@Override  
		protected void onPostExecute(Integer result) {
			mPbar.setProgress(100);
			//adapter.setBitmap(mBitmaps);
			//adapter.notifyDataSetChanged();
		}

		@Override
		public void onProgress(int progress) {
			// TODO Auto-generated method stub
			publishProgress(progress / 2);
		}

	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		TabStreamAdapter adapter = (TabStreamAdapter)((ListView)arg0).getAdapter();
		TabStream item = (TabStream)adapter.getTabStream(arg2);
		Intent intent = new Intent(this, TabAppStreamActivity.class);
		try {
			String tabid = item.getItemID();
			intent.putExtra("TABID", tabid);
			startActivity(intent);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		executeTask(0);
	}

	@Override
	protected void onDestroy () {
		super.onDestroy();
		if (mTask != null) {
			mTask.cancel(true);
		}
	}

	@Override
	public void onClick(View arg0) {
		if (arg0.equals(mOwn)) {
			
		} else if (arg0.equals(mFollow)) {
			executeTask(1);
		}
	}
}
