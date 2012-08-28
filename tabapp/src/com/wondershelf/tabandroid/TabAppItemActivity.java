package com.wondershelf.tabandroid;


import java.io.IOException;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONException;

import com.wondershelf.misc.SBDialogManager;
import com.wondershelf.tablib.TabBasicItem;
import com.wondershelf.tablib.TabLib;
import com.wondershelf.tablib.TabPagenatedComment;
import com.wondershelf.tablib.TabPagenatedComments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class TabAppItemActivity extends Activity implements OnClickListener{

	ImageView mUserIcon = null;
	ImageView mItemImage = null;
	TextView mUserName = null;
	TextView mItemDesc = null;
	TextView mItemTitle = null;

	LinearLayout mComments = null;
	LinearLayout muserLayout = null;

	ProgressBar mPbar = null;
	TabBasicItem mItem = null;
	String mId = null;
	SetDetailTask task = null;
	String mUserId = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tabitem);
		
		mUserIcon = (ImageView)findViewById(R.id.usericon);		
		mItemImage = (ImageView)findViewById(R.id.itemimage);
		mUserName = (TextView)findViewById(R.id.username);
		mItemDesc = (TextView)findViewById(R.id.itemdescription);
		mItemTitle = (TextView)findViewById(R.id.itemdetailtitle);
		mPbar = (ProgressBar)findViewById(R.id.itemprogress);
		mPbar.setMax(100);
		
		muserLayout = (LinearLayout)findViewById(R.id.userlayout);
		muserLayout.setOnClickListener(this);

		Intent intent = getIntent();
		mId = intent.getStringExtra("ITEMID");
		task = new SetDetailTask(this, mId);
		task.execute(0);
	}

	@Override
	public void onResume() {
		super.onResume();

	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (task != null) {
			task.cancel(true);
			task = null;
		}
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		boolean ret = super.onCreateOptionsMenu(menu);
		menu.add(0 , Menu.FIRST , Menu.NONE , "共有");
		menu.add(0 , Menu.FIRST + 1 , Menu.NONE , "web表示");
		return ret;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == Menu.FIRST) {
			if (mItem != null) {
				Intent intent = new Intent(android.content.Intent.ACTION_SEND);
				intent.setType("text/plain");
				String title = "";
				try {
					title = mItem.getTitle();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				intent.putExtra(Intent.EXTRA_TEXT, title + " http://tab.do/items/" + mId);
				startActivity(intent);
			} else {
				SBDialogManager.showOKDialog(this, "エラー", "アイテム情報を読み込み中です", null);
			}
		} else if (item.getItemId() == Menu.FIRST + 1) {
			Intent intent = new Intent(this, TabItemWebActivity.class);
			intent.putExtra("ITEMID", mId);
			startActivity(intent);
		}
		return super.onOptionsItemSelected(item);
	}

	public class SetDetailTask extends AsyncTask<Integer, Integer, Integer> {
		String mId = null;
		Context mCont = null;
		TabPagenatedComments mComments = null;
		Bitmap mIcon = null;
		Bitmap mImage = null;
		TabLib tablib = null;
		
		public SetDetailTask(Context cont, String id) {
			mCont = cont;
			mId = id;
		}

		public SetDetailTask() {
			
		}

		@Override  
		protected void onCancelled() {
			if (tablib != null) {
				tablib.cancel();
				tablib = null;
			}
		}

		@Override  
		protected void onProgressUpdate(Integer... progress) {
			if (progress[0] == -1) {
				try {
					mItemTitle.setText(mItem.getTitle());
					mUserName.setText(mItem.getTabUser().getScreenName());
					mItemDesc.setText(mItem.getDescription());
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (progress[0] == -2) {
				mUserIcon.setImageBitmap(mIcon);
				mItemImage.setImageBitmap(mImage);
			} else {
				mPbar.setProgress(progress[0]);
			}
		}

		@Override
		protected Integer doInBackground(Integer... arg0) {
			int ret= 0;
			tablib = new TabLib();
			try {
				publishProgress(10);
				mItem = tablib.getAnItem(mId);
				mUserId = mItem.getTabUser().getId();
				publishProgress(-1);
				publishProgress(50);
				BitmapFactory.Options bmOptions;
				bmOptions = new BitmapFactory.Options();
				bmOptions.inSampleSize = 1;
				mIcon = LoadImage(mItem.getTabUser().getUserCropM1ImageURL(), bmOptions);
				publishProgress(70);
				mImage = LoadImage(mItem.getItemCropM1ImageURL(), bmOptions);
				publishProgress(-2);
				publishProgress(90);

				mComments = tablib.getComments(mId);
				
			} catch (Exception e) {
				ret = -1;
				e.printStackTrace();
			}
			return ret;
		}

		@Override
		protected void onPreExecute() {
			mPbar.setProgress(100);
		}
		
		@Override  
		protected void onPostExecute(Integer result) {
			if (result == 0) {
				try {
					LinearLayout commentlayout = (LinearLayout)((Activity) mCont).findViewById(R.id.commentlayout);
					ArrayList<TabPagenatedComment> mCommentList = mComments.getItems();
					Iterator i = mCommentList.iterator();
					while(i.hasNext()) {
						TabPagenatedComment commentobj = (TabPagenatedComment)i.next();
						LinearLayout layout = (LinearLayout)((Activity)mCont).getLayoutInflater().inflate(R.layout.tabcomment, null);
						TextView user = (TextView)layout.findViewById(R.id.username);
						user.setText(commentobj.getScreenName());
						TextView comment = (TextView)layout.findViewById(R.id.comment);
						comment.setText(commentobj.getText());
						commentlayout.addView(layout);
					}
					mPbar.setProgress(100);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			} else {
				SBDialogManager.showOKDialog(mCont, "エラー", "データの取得ができませんでした。存在しないアイテムの可能性があります。", null);
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
	public void onClick(View arg0) {
		if (arg0.equals(muserLayout) && mUserId != null) {
			Intent intent = null;
			intent = new Intent(this, TabAppProfileActivity.class);
			intent.putExtra("userid", mUserId);
			startActivity(intent);
		}
		
	}
}
