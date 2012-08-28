package com.wondershelf.tablib.auth;

public interface OnAuthListener {
	void onSuccesLogin(TabAccount account);
	void onFailLogin();
}
