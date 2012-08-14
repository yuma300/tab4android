package com.wondershelf.tabandroid;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;

public class TabAppSettingActivity extends Activity implements OnClickListener{
	CheckBox zoomreload = null;
	CheckBox movereload = null;
	CheckBox filter = null;

	Button mailbutton = null;
	final private static String preferenceFileName = "setting_info";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting);
		zoomreload = (CheckBox)findViewById(R.id.zoomreload);
		zoomreload.setOnClickListener(this);
		zoomreload.setChecked(getSharedPreferences(preferenceFileName, Context.MODE_PRIVATE).getBoolean("zoomreload", true));

		movereload = (CheckBox)findViewById(R.id.movereload);
		movereload.setOnClickListener(this);
		movereload.setChecked(getSharedPreferences(preferenceFileName, Context.MODE_PRIVATE).getBoolean("movereload", false));

		filter = (CheckBox)findViewById(R.id.filter);
		filter.setOnClickListener(this);
		filter.setChecked(getSharedPreferences(preferenceFileName, Context.MODE_PRIVATE).getBoolean("filter", true));
		
		mailbutton = (Button)findViewById(R.id.mailbutton);
		mailbutton.setOnClickListener(this);
	}
	@Override
	public void onClick(View arg0) {
		if (arg0.equals(zoomreload)) {
			setBooleanPreference("zoomreload", ((CheckBox)arg0).isChecked());
		} else if (arg0.equals(movereload)) {
			setBooleanPreference("movereload", ((CheckBox)arg0).isChecked());
		} else if(arg0.equals(mailbutton)) {
			Intent intent = new Intent();  
			intent.setAction(Intent.ACTION_SEND);  
			intent.setType("message/rfc822");  
			intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"ywakimoto2s@gmail.com"});
			startActivity(intent);
		} else if (arg0.equals(filter)) {
			setBooleanPreference("filter", ((CheckBox)arg0).isChecked());
		}
	
	}

	/**
	 * AndroidのsharedPrefenceにブール値をセットする
	 * @param cont Context
	 * @param itemname sharedPrefenceにセットするキー
	 * @param value キーに対応するブール値
	 */
	private void setBooleanPreference(String itemname, boolean value) {
		SharedPreferences.Editor editor = getSharedPreferences(preferenceFileName, Context.MODE_PRIVATE).edit();
		editor.putBoolean(itemname, value);
		editor.commit();
	}
}
