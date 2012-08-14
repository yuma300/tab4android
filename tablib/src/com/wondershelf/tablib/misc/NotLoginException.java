package com.wondershelf.tablib.misc;

public class NotLoginException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// UID
	private int id;
	// ユーザ名
	private String name;
	// コンストラクタ
	public NotLoginException() {
		super("このユーザは認証されていません。");
	}
	public void setUid(int i) {
		id = i;
	}
	public void setName(String user) {
		name = user;
	}
	public String getName() {
		return name;
	}
	public int getId() {
		return id;
	}
}