package com.wondershelf.tablib;

import java.io.BufferedInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import com.wondershelf.tablib.auth.TabAppAuthWebViewClient;
import com.wondershelf.tablib.misc.NotLoginException;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.webkit.WebView;

public class TabLib {

	private OnProgressListener mPlistener = null;
	private final String TAG = "TabLib";
	HttpGet httpGet = null;
	boolean cancel = false;
	final private static String preferenceFileName = "user_info";

	public TabLib() {
		
	}

	public void cancel() {
		cancel = true;
		if (httpGet != null) {
			httpGet.abort();
		}
	}


	public void setOnProgressListener(OnProgressListener l) {
		mPlistener = l;
	}

	public TabPagenatedItems getItemsNearby(double lat, double lon, double rad) throws JSONException, IOException, URISyntaxException {
		JSONObject obj = getResult("http://tab.do/api/1/items/nearby.json?lon="+lon+"&lat="+lat+"&radius="+rad);
		if (obj == null) {
			return null;
		}
		return new TabPagenatedItems(obj);
	}

	public TabPagenatedItems getItemsNearby(double lat, double lon, String fil, double rad) throws JSONException, IOException, URISyntaxException {
		JSONObject obj = getResult("http://tab.do/api/1/items/nearby.json?lon="+lon+"&lat="+lat+"&radius="+rad+"&q="+fil);
		if (obj == null) {
			return null;
		}
		return new TabPagenatedItems(obj);
	}

	public TabPagenatedItems getItemsSearch(String query) throws JSONException, IOException, URISyntaxException {
		JSONObject obj = getResult("http://tab.do/api/1/items/search.json?q="+query);
		if (obj == null) {
			return null;
		}
		return new TabPagenatedItems(obj);
	}

	public TabPagenatedItems getMyFollowingItems(Context cont) throws JSONException, IOException, URISyntaxException, NotLoginException {
		String userid = cont.getSharedPreferences(preferenceFileName, Context.MODE_PRIVATE).getString("user_id", null);
		if (userid != null) {
			JSONObject obj = getResult("http://tab.do/api/1/users/" + userid + "/items.json");
			if (obj == null) {
				return null;
			}
			return new TabPagenatedItems(obj);
		} else {
			throw new NotLoginException();
		}
	}

/*	public TabPagenatedItems getMyFollowingItems(Context cont) throws JSONException, IOException, URISyntaxException {
		String userid = cont.getSharedPreferences(preferenceFileName, Context.MODE_PRIVATE).getString("user_id", null);
		if (userid != null) {
			JSONObject obj = getResult("http://tab.do/api/1/users/" + userid + "/items.json");
			if (obj == null) {
				return null;
			}
			return new TabPagenatedItems(obj);
		} else {
			return null;
		}
	}*/

	public TabBasicList getMyOwnTabs(Context cont) throws JSONException, IOException, URISyntaxException, NotLoginException {
		String userid = cont.getSharedPreferences(preferenceFileName, Context.MODE_PRIVATE).getString("user_id", null);
		if (userid != null) {
			JSONObject obj = getResult("http://tab.do/api/1/users/" + userid + "/streams.json");
			if (obj == null) {
				return null;
			}
			return new TabBasicList(obj);
		} else {
			throw new NotLoginException();
		}
	}
	
	public TabPagenatedComments getComments(String itemid) throws IOException, URISyntaxException, JSONException {
		JSONObject obj = getResult("http://tab.do/api/1/items/"+itemid+"/comments.json");
		if (obj == null) {
			return null;
		}
		return new TabPagenatedComments(obj);
	}
	
	
	public TabBasicItem getAnItem(String id) throws Exception {
		JSONObject obj = getResult("http://tab.do/api/1/items/"+ id +".json");
		if (obj == null) {
			return null;
		}
		return new TabBasicItem(obj);
	}
	
	private JSONObject getResult(String url) throws IOException, URISyntaxException, JSONException {
		InputStream inputStream = null;
		String txt = null;
		JSONObject rootObject = null;
		try {
			URI uri = new URI(url);
			HttpClient httpClient = new DefaultHttpClient();

			HttpParams params = new BasicHttpParams();
			HttpConnectionParams.setSoTimeout(params, 10000);         //ソケット通信タイムアウト20秒
			HttpConnectionParams.setConnectionTimeout(params, 10000); //HTTP通信タイムアウト20秒

			if (cancel) {
				cancel = false;
				return null;
			}
			HttpGet httpGet = new HttpGet(uri);
			HttpResponse httpResponse = null;
	
			Log.d(TAG, "start get execute");
			if (mPlistener != null) {
				mPlistener.onProgress(10);
			}
			long starttime = System.currentTimeMillis();
			if (cancel) {
				cancel = false;
				return null;
			}
			httpResponse = httpClient.execute(httpGet);
			int status = httpResponse.getStatusLine().getStatusCode();
			if ( status != HttpStatus.SC_OK )
				throw new IOException( "" );
			
			Log.d(TAG, "end get execute elaped=" + Long.toString(System.currentTimeMillis() - starttime));
			if (cancel) {
				cancel = false;
				return null;
			}
			if (mPlistener != null) {
				mPlistener.onProgress(30);
			}
			inputStream = new BufferedInputStream(httpResponse.getEntity().getContent());
			byte[] b = new byte[1000 * 1024];
			int tmpnum = 0;
			int totalnum = 0;
			String tmptxt = null;
			txt = new String();
			while ((tmpnum = inputStream.read(b, totalnum, 10*1024)) != -1) {
				totalnum += tmpnum;
				if (cancel) {
					cancel = false;
					return null;
				}
			}
			Log.d(TAG, "complete read elaped=" + Long.toString(System.currentTimeMillis() - starttime));
			if (mPlistener != null) {
				mPlistener.onProgress(50);
			}
			txt = new String(b, "Shift-JIS");
			rootObject = new JSONObject(txt);
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				inputStream = null;
			}
		}
		return rootObject;
	}
}
