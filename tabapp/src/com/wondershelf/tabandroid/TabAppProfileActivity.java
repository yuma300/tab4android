package com.wondershelf.tabandroid;

import java.io.IOException;

import java.net.URISyntaxException;
import java.util.ArrayList;

import org.json.JSONException;

import com.wondershelf.tablib.TabBasicList;
import com.wondershelf.tablib.TabLib;
import com.wondershelf.tablib.TabPagenatedItem;
import com.wondershelf.tablib.TabPagenatedItems;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class TabAppProfileActivity extends Activity {
	private ListView itemlist = null;
	private TabStreamAdapter adapter = null;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.profile);
		
		itemlist = (ListView)findViewById(R.id.itemlist);
		//itemlist.setOnItemClickListener(this);
		
		
		TabLib lib = new TabLib();
		try {
			TabBasicList items = lib.getMyOwnTabs(this);

			adapter = new TabStreamAdapter(this, R.layout.searchlistitem, items.getItems());
			itemlist.setAdapter((ArrayAdapter)adapter);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
