package com.wondershelf.tabandroid;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.wondershelf.tablib.TabPagenatedItem;

public class TabPagenatedItemAdapter extends ArrayAdapter {  
	private ArrayList<TabPagenatedItem> items;  
	private ArrayList<Bitmap> bitmaps;
	private LayoutInflater inflater;
	private Context mCont = null;
	public TabPagenatedItemAdapter(Context context, int textViewResourceId, ArrayList<TabPagenatedItem> items) {
		super(context, textViewResourceId, items);
		mCont = context;
		this.items = items;
		this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);  
	}
	
	public void setList(ArrayList<TabPagenatedItem> items) {
		this.items = items;
	}

	public void setBitmap(ArrayList<Bitmap> picts) {
		bitmaps = picts;
	}

	
	public TabPagenatedItem getTabPagenatedItem(int index) {
		return items.get(index);
	}

	@Override  
	public View getView(int position, View convertView, ViewGroup parent) { 
		//Log.d(TAG, "getView position="+Integer.toString(position));
		// ビューを受け取る  
		View view = convertView;
		if (view == null) {  
			// 受け取ったビューがnullなら新しくビューを生成  
			view = inflater.inflate(R.layout.searchlistitem, null);  
			// 背景画像をセットする  
			//view.setBackgroundResource(R.drawable.loading);  
		}
		TextView title = (TextView)view.findViewById(R.id.searchitemtitle);  
		title.setText(items.get(position).getTitle());
		
		ImageView image = (ImageView)view.findViewById(R.id.searchitemimage);
		if (bitmaps != null && position < bitmaps.size()) {
			image.setImageBitmap(bitmaps.get(position));
		} else {
			image.setImageResource(R.drawable.sloading);
		}
		return view;  
	}
	
	@Override
	public int getCount() {
		return items.size();
	}

}