package com.wondershelf.tabandroid;

import com.wondershelf.tablib.R;
import com.wondershelf.tablib.TabLib;
import com.wondershelf.tablib.auth.TabAppAuth;
import com.wondershelf.tablib.auth.TabAppAuthWebViewClient;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class TabAppAuthActivity extends Activity {

	WebView mWeb = null;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.auth);

		TabAppAuth lib = new TabAppAuth();
		mWeb = (WebView)findViewById(R.id.authweb);
		lib.getLoginView(mWeb);
	}

}
