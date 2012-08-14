package com.wondershelf.misc;

import java.util.ArrayList;




import java.util.Iterator;



import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.view.View;
import android.widget.EditText;

/**
 * ダイアログを操作するための機能を集めたクラス
 *
 */
public class SBDialogManager implements OnDismissListener{

	static private final SBDialogManager dm = new SBDialogManager();
	
	private ArrayList<Dialog> dialogList;
	
	private SBDialogManager(){
		dialogList = new ArrayList<Dialog>();
	}
	
	public void onDismiss(DialogInterface dialog) {
		dialogList.remove(dialog);
	}
	
	/**
	 * 全てのダイアログを削除する
	 */
	static public void DismissAllDialog(){
		if(dm == null){
			return;
		}
		synchronized (dm) {
			Iterator<Dialog> iterator = dm.dialogList.iterator();
			while(iterator.hasNext()){
				Dialog dialog = iterator.next();
				if(dialog.isShowing()){
					dialog.dismiss();
				}
			}
			dm.dialogList.clear();
		}
	}
	
	/**
	 * 「はい」「いいえ」の選択肢をもつダイアログを表示する
	 * @param context コンテキスト
	 * @param title ダイアログのタイトル
	 * @param message ダイアログのメッセージ
	 * @param yesListener 「はい」が押されたときの処理　何もしない場合はnullで大丈夫
	 * @param noListener 「いいえ」が押されたときの処理　何もしない場合はnullで大丈夫
	 * @return 表示されたダイアログのインスタンス
	 */
	static public Dialog showYESNODialog(Context context, String title, String message, OnClickListener yesListener, OnClickListener noListener) {
		return showYESNODialog(context, title, message, "Yes", "No", yesListener, noListener, null);
	}

	/**
	 * 「はい」「いいえ」の選択肢をもつダイアログを表示する
	 * @param context コンテキスト
	 * @param title ダイアログのタイトル
	 * @param message ダイアログのメッセージ
	 * @param yesListener 「はい」が押されたときの処理　何もしない場合はnullで大丈夫
	 * @param noListener 「いいえ」が押されたときの処理　何もしない場合はnullで大丈夫
	 * @param cancelListener backボタン等でキャンセルされた時の処理 何もしない場合はnullで大丈夫
	 * @return 表示されたダイアログのインスタンス
	 */
	static public Dialog showYESNODialog(Context context, String title, String message, OnClickListener yesListener, OnClickListener noListener, OnCancelListener cancelListener) {
		return showYESNODialog(context, title, message, "Yes", "No", yesListener, noListener, cancelListener);
	}

	/**
	 * 「はい」「いいえ」の選択肢をもつダイアログを表示する
	 * @param context コンテキスト
	 * @param title ダイアログのタイトル
	 * @param message ダイアログのメッセージ
	 * @param yes 「はい」の代わりにボタンに表示する文字列
	 * @param no 「いいえ」の代わりにボタンに表示する文字列
	 * @param yesListener 「はい」が押されたときの処理　何もしない場合はnullで大丈夫
	 * @param noListener 「いいえ」が押されたときの処理　何もしない場合はnullで大丈夫
	 * @return 表示されたダイアログのインスタンス
	 */
	static public Dialog showYESNODialog(Context context, String title, String message, String yes, String no, OnClickListener yesListener, OnClickListener noListener) {
		return showYESNODialog(context, title, message, yes, no, yesListener, noListener, null);
	}

	/**
	 * 「はい」「いいえ」の選択肢をもつダイアログを表示する
	 * @param context コンテキスト
	 * @param title ダイアログのタイトル
	 * @param message ダイアログのメッセージ
	 * @param yes 「はい」の代わりにボタンに表示する文字列
	 * @param no 「いいえ」の代わりにボタンに表示する文字列
	 * @param yesListener 「はい」が押されたときの処理　何もしない場合はnullで大丈夫
	 * @param noListener 「いいえ」が押されたときの処理　何もしない場合はnullで大丈夫
	 * @param cancelListener backボタン等でキャンセルされた時の処理 何もしない場合はnullで大丈夫
	 * @return 表示されたダイアログのインスタンス
	 */
	static public Dialog showYESNODialog(Context context, String title, String message, String yes, String no, OnClickListener yesListener, OnClickListener noListener, OnCancelListener cancelListener) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
		if(title != null){
			alertDialogBuilder.setTitle(title);
		}
		if(message != null){
			alertDialogBuilder.setMessage(message);
		}
		if(yesListener != null){
			alertDialogBuilder.setPositiveButton(yes, yesListener);
		} else {
			alertDialogBuilder.setPositiveButton(yes, new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {}
			});
		}
		if(noListener != null){
			alertDialogBuilder.setNegativeButton(no, noListener);
		} else {
			alertDialogBuilder.setNegativeButton(no, new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {}
			});
		}
		if(cancelListener != null){
			alertDialogBuilder.setOnCancelListener(cancelListener);
		}
		AlertDialog alertDialog = alertDialogBuilder.create();
		if(dm != null){
			synchronized (dm) {
				alertDialog.setOnDismissListener(dm);
				dm.dialogList.add(alertDialog);
			}
		}
		alertDialog.show();
		return alertDialog;
	}
	
	/**
	 * 「OK」ボタンが表示されるダイアログを表示する
	 * @param context コンテキスト
	 * @param title ダイアログのタイトル
	 * @param message ダイアログのメッセージ
	 * @param listener 「OK」が押されたときの処理　何もしない場合はnullで大丈夫
	 * @return 表示されたダイアログのインスタンス
	 */
	static public Dialog showOKDialog(Context context, String title, String message, OnClickListener listener){
		return showOKDialog(context, title, message, listener, null);
	}

	/**
	 * 「OK」ボタンが表示されるダイアログを表示する
	 * @param context コンテキスト
	 * @param title ダイアログのタイトル
	 * @param message ダイアログのメッセージ
	 * @param listener 「OK」が押されたときの処理　何もしない場合はnullで大丈夫
	 * @return 表示されたダイアログのインスタンス
	 */
	static public Dialog showOKDialog(Context context, View view, String title, String message, OnClickListener listener){
		return showOKDialog(context, view, title, message, listener, null);
	}

	/**
	 * 「OK」ボタンが表示されるダイアログを表示する
	 * @param context コンテキスト
	 * @param title ダイアログのタイトル
	 * @param message ダイアログのメッセージ
	 * @param listener 「OK」が押されたときの処理　何もしない場合はnullで大丈夫
	 * @param cancelListener backボタン等でキャンセルされた時の処理 何もしない場合はnullで大丈夫
	 * @return 表示されたダイアログのインスタンス
	 */
	static public Dialog showOKDialog(Context context, String title, String message, OnClickListener listener, OnCancelListener cancelListener){
		return showOKDialog(context, null, title, message, listener, cancelListener);
	}

	/**
	 * 「OK」ボタンが表示されるダイアログを表示する
	 * @param context コンテキスト
	 * @param view セットしたいview
	 * @param title ダイアログのタイトル
	 * @param message ダイアログのメッセージ
	 * @param listener 「OK」が押されたときの処理　何もしない場合はnullで大丈夫
	 * @param cancelListener backボタン等でキャンセルされた時の処理 何もしない場合はnullで大丈夫
	 * @return 表示されたダイアログのインスタンス
	 */
	static public Dialog showOKDialog(Context context, View view, String title, String message, OnClickListener listener, OnCancelListener cancelListener){
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
		if(view != null){
			alertDialogBuilder.setView(view);
		}
		if(title != null){
			alertDialogBuilder.setTitle(title);
		}
		if(message != null){
			alertDialogBuilder.setMessage(message);
		}
		if(listener != null){
			alertDialogBuilder.setPositiveButton("OK", listener);
		} else {
			alertDialogBuilder.setPositiveButton("OK", new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {}
			});
		}
		if(cancelListener != null){
			alertDialogBuilder.setOnCancelListener(cancelListener);
		}
		AlertDialog alertDialog = alertDialogBuilder.create();
		if(dm != null){
			synchronized (dm) {
				alertDialog.setOnDismissListener(dm);
				dm.dialogList.add(alertDialog);
			}
		}
		alertDialog.show();
		return alertDialog;
	}

	/**
	 * テキストエディットと「OK」ボタンが表示されるダイアログを表示する
	 * @param context コンテキスト
	 * @param text テキストエディット
	 * @param title ダイアログのタイトル
	 * @param message ダイアログのメッセージ
	 * @param listener 「OK」が押されたときの処理　何もしない場合はnullで大丈夫
	 * @return 表示されたダイアログのインスタンス
	 */
	static public Dialog showEditTextDialog(Context context, EditText text, String title, String message, OnClickListener listener){
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
		if(title != null){
			alertDialogBuilder.setTitle(title);
		}
		if(message != null){
			alertDialogBuilder.setMessage(message);
		}
		
		alertDialogBuilder.setView(text);

		if(listener != null){
			alertDialogBuilder.setPositiveButton("OK", listener);
		} else {
			alertDialogBuilder.setPositiveButton("OK", new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {}
			});
		}
		AlertDialog alertDialog = alertDialogBuilder.create();
		if(dm != null){
			synchronized (dm) {
				alertDialog.setOnDismissListener(dm);
				dm.dialogList.add(alertDialog);
			}
		}
		alertDialog.show();
		return alertDialog;
	}
}
