package com.wondershelf.tablib.auth;



import com.wondershelf.tablib.misc.SharedPreferenceUtils;

import android.content.Context;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;

public class TabLibAuth {
	
	String id;
	
	public TabAccount getMyAccount(Context cont) {
		SharedPreferenceUtils util = new SharedPreferenceUtils();
		String myid = util.getStringPreference(cont, "user_id");
		if (myid != null) {
			return new TabAccount(myid);
		} else {
			return null;
		}
	}
	
	public String getMyId(Context cont) {
		SharedPreferenceUtils util = new SharedPreferenceUtils();
		return util.getStringPreference(cont, "user_id");
	}
	
	public void login(WebView web) {
		web.setWebViewClient(new TabAppAuthWebViewClient());
		web.loadUrl("https://tab.do/oauth2/authorize?response_type=code&client_id=6546657ddd0be82f92a012ec89b8c102&redirect_uri=tab4android://callback/oauth2");
	}

	public void login(WebView web, OnAuthListener listener) {
		TabAppAuthWebViewClient client = new TabAppAuthWebViewClient();
		client.setOnAuthListener(listener);
		web.setWebViewClient(client);
		web.loadUrl("https://tab.do/oauth2/authorize?response_type=code&client_id=6546657ddd0be82f92a012ec89b8c102&redirect_uri=tab4android://callback/oauth2");
	}
	
	public void logout(Context cont) {
		SharedPreferenceUtils sutil = new SharedPreferenceUtils();
		sutil.setStringPreference(cont, "access_token", null);
		sutil.setStringPreference(cont, "refresh_token", null);
		sutil.setStringPreference(cont, "user_id", null);
		CookieSyncManager.createInstance(cont);
		CookieManager cMgr = CookieManager.getInstance();
		cMgr.removeSessionCookie();
		cMgr.removeAllCookie();
		CookieSyncManager.getInstance().sync();
	}
}
