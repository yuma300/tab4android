package com.wondershelf.tablib.auth;

import android.webkit.WebView;

public class TabAppAuth {
	
	String id;
	
	public void getLoginView(WebView web) {
		web.setWebViewClient(new TabAppAuthWebViewClient());
		web.loadUrl("https://tab.do/oauth2/authorize?response_type=code&client_id=6546657ddd0be82f92a012ec89b8c102&redirect_uri=tab4android://callback/oauth2");
	}
}
