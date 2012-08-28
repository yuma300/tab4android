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
import com.wondershelf.tablib.auth.TabLibAuth;
import com.wondershelf.tablib.misc.NotLoginException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
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
	private String mId = null;
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
		mOwn.setBackgroundColor(Color.CYAN);
		mOwn.setTag(R.id.selected, true);

		mFollow = (Button)this.findViewById(R.id.follow);
		mFollow.setOnClickListener(this);
		mFollow.setTag(R.id.selected, false);

		mPbar = (ProgressBar)findViewById(R.id.searchprogress);

		executeTask(0);
	}
	
	private void executeTask(int mode) {
		Intent intent = this.getIntent();
		mId = intent.getStringExtra("userid");
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
					mItems = tlib.getOwnTabs(mCont, mId);
				} else {
					mItems = tlib.getFollowTabs(mCont, mId);

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
			
			}catch (NotLoginException e){
				e.printStackTrace();
				ret = -1;
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
			if (result == -1) {
				//ログインできていない
				Intent intent = new Intent(mCont, TabAppAuthActivity.class);
				((Activity) mCont).startActivityForResult (intent, 0);

				return;
			}
			//adapter.setBitmap(mBitmaps);
			//adapter.notifyDataSetChanged();
		}

		@Override
		public void onProgress(int progress) {
			// TODO Auto-generated method stub
			publishProgress(progress / 2);
		}

	}

	public boolean onCreateOptionsMenu(Menu menu) {
		boolean ret = super.onCreateOptionsMenu(menu);
		menu.add(0 , Menu.FIRST , Menu.NONE , "ログアウト");
		return ret;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == Menu.FIRST) {
			TabLibAuth auth = new TabLibAuth();
			auth.logout(this);
		}
		return super.onOptionsItemSelected(item);
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
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		executeTask(0);
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

	@Override
	public void onClick(View arg0) {
		if (arg0.equals(mOwn) && !(Boolean)mOwn.getTag(R.id.selected)) {
			mOwn.setBackgroundColor(Color.CYAN);
			mOwn.setTag(R.id.selected, true);
			mFollow.setBackgroundResource(R.drawable.color_stateful);
			mFollow.setTag(R.id.selected, false);
			executeTask(0);
		} else if (arg0.equals(mFollow) && !(Boolean)mFollow.getTag(R.id.selected)) {
			mOwn.setBackgroundResource(R.drawable.color_stateful);
			mOwn.setTag(R.id.selected, false);
			mFollow.setBackgroundColor(Color.CYAN);
			mFollow.setTag(R.id.selected, true);
			executeTask(1);
		}
	}
}
