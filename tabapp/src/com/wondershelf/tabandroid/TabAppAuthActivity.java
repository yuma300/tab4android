package com.wondershelf.tabandroid;


import com.wondershelf.misc.SBDialogManager;

import com.wondershelf.tablib.auth.OnAuthListener;
import com.wondershelf.tablib.auth.TabAccount;
import com.wondershelf.tablib.auth.TabLibAuth;


import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.webkit.WebView;

public class TabAppAuthActivity extends Activity {

	
	WebView mWeb = null;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.auth);

		TabLibAuth auth = new TabLibAuth();
		mWeb = (WebView)findViewById(R.id.authweb);
		auth.login(mWeb, new OnAuthListener() {
			@Override
			public void onSuccesLogin(TabAccount account) {
				TabAppAuthActivity.this.finish();
			}
			@Override
			public void onFailLogin() {
				SBDialogManager.showOKDialog(TabAppAuthActivity.this, "エラー", "認証に失敗しました。再度お試し下さい", new OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) {
						TabLibAuth auth = new TabLibAuth();
						auth.logout(TabAppAuthActivity.this);
						TabAppAuthActivity.this.finish();
				
					}});
			}
		});
	}

}
