package com.wondershelf.tabandroid;


import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;

public class TabItemWebActivity extends Activity {
	WebView mWeb = null;
	ProgressBar mBar = null;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tabitemweb);
		
		mBar = (ProgressBar)findViewById(R.id.itemprogress);
		
		mWeb = (WebView)findViewById(R.id.tabitemdetail);
		mWeb.setDrawingCacheEnabled(true);
		mWeb.setWebChromeClient(new TabAppWebChromeClient(mBar));
		
		WebSettings setting = mWeb.getSettings();
		setting.setBuiltInZoomControls(true);
		setting.setAppCacheMaxSize(1024 * 1024 * 1);
		setting.setAppCachePath("/data/data/" + this.getPackageName() + "/cache");
		setting.setAppCacheEnabled(true);
		setting.setJavaScriptEnabled(true);
		setting.setDomStorageEnabled(true);
		setting.setDatabasePath("/data/data/" + this.getPackageName() + "/app_databases");
		if(Build.VERSION.SDK_INT == Build.VERSION_CODES.ECLAIR_MR1) {
			//For 2.1
			setting.setPluginsEnabled(true);
		} else {
			//For 2.2 or later
			setting.setPluginState(WebSettings.PluginState.ON);
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		Intent intent = getIntent();
		if (mWeb != null) {
			String itemid = intent.getStringExtra("ITEMID");
			if (itemid != null) {
				mWeb.loadUrl("http://tab.do/items/" + itemid);
			} else {
				
			}
		}
	}

	/**
	 * 本クラスはAndroid SDKのWebChromeClientを継承している。本クラスにおいてWebViewのUIに関連する
	 * 振る舞いを定義する
	 *
	 */
	public class TabAppWebChromeClient extends WebChromeClient {
		ProgressBar mBar = null;
		TabAppWebChromeClient() {

		}

		TabAppWebChromeClient(ProgressBar bar) {
			mBar = bar;
		}

		/**
		 * 本メソッドはwebページ読み込み時に定期的に呼ばれる。
		 * その都度進捗状況を更新する。
		 */
		@Override
		public void onProgressChanged(WebView view, int newProgress){
			if (mBar != null) {
				mBar.setProgress(newProgress);
			}
		}
	}
}
