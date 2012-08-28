package com.wondershelf.tablib.auth;

import java.io.BufferedInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.wondershelf.tablib.misc.SharedPreferenceUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class TabAppAuthWebViewClient extends WebViewClient {
	//ページの読み込み開始
	OnAuthListener mListener = null;
	
	public void setOnAuthListener(OnAuthListener l) {
		mListener = l;
	}
	
	@Override
	public void onPageStarted(WebView view, String url, Bitmap favicon) {
		if (url.contains("error")) {
			//エラー処理
		} else if (url.contains("tab4android") && url.contains("code=")){
			String code = url.split("code=")[1];
			try {
				final List <NameValuePair> params = new ArrayList <NameValuePair>();
				params.add(new BasicNameValuePair("grant_type", "authorization_code"));
				params.add(new BasicNameValuePair("code", code));
				params.add(new BasicNameValuePair("client_id", "6546657ddd0be82f92a012ec89b8c102"));
				params.add(new BasicNameValuePair("client_secret", "50bc4b97d3c0ddd4c6cc6d7e3e08c3c0"));
				String body = postAuth("https://tab.do/api/1/oauth2/token", params, null);
				JSONObject obje = new JSONObject(body);
				String access_token = obje.getString("access_token");
				String refresh_token = obje.getString("refresh_token");
				
				SharedPreferenceUtils sutil = new SharedPreferenceUtils();

				
				sutil.setStringPreference(view.getContext(), "access_token", access_token);
				sutil.setStringPreference(view.getContext(), "refresh_token", refresh_token);
				JSONObject infojson = getResult("http://tab.do/api/1/users/me.json", access_token);
				sutil.setStringPreference(view.getContext(), "user_id", infojson.getJSONObject("user").getString("id"));
				if (mListener != null) {
					TabAccount account = new TabAccount(infojson.getJSONObject("user").getString("id"));
					mListener.onSuccesLogin(account);
				}
			} catch (Exception e) {
				e.printStackTrace();
				if (mListener != null) {
					mListener.onFailLogin();
				}
			}
		}
	}

	private String postAuth(String url, List <NameValuePair> params, String[][] headers) throws Exception {
		HttpPost method = null;
		method = new HttpPost(new URL(url).toURI());
		DefaultHttpClient client = new DefaultHttpClient();

		if (headers != null) {
			for (int i = 0; i < headers.length; i++) {
				method.setHeader(headers[i][0], headers[i][1]);
			}
		}

		if (params != null) {
			method.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
		}

		HttpResponse response = client.execute( method );
		int status = response.getStatusLine().getStatusCode();
		if ( status != HttpStatus.SC_OK )
			throw new Exception( "" );
		
		return EntityUtils.toString(response.getEntity(), "UTF-8");
	}
	
	private JSONObject getResult(String url, String access_token) throws IOException, URISyntaxException, JSONException {
		InputStream inputStream = null;
		String txt = null;
		JSONObject rootObject = null;
		try {
			URI uri = new URI(url);
			HttpClient httpClient = new DefaultHttpClient();

			HttpParams params = new BasicHttpParams();
			HttpConnectionParams.setSoTimeout(params, 10000);         //ソケット通信タイムアウト20秒
			HttpConnectionParams.setConnectionTimeout(params, 10000); //HTTP通信タイムアウト20秒

/*			if (cancel) {
				cancel = false;
				return null;
			}*/
			HttpGet httpGet = new HttpGet(uri);
			HttpResponse httpResponse = null;
	
/*			Log.d(TAG, "start get execute");
			if (mPlistener != null) {
				mPlistener.onProgress(10);
			}
			long starttime = System.currentTimeMillis();
			if (cancel) {
				cancel = false;
				return null;
			}*/
			httpGet.setHeader("Authorization", "Bearer " + access_token);
			httpResponse = httpClient.execute(httpGet);
			int status = httpResponse.getStatusLine().getStatusCode();
			if ( status != HttpStatus.SC_OK )
				throw new IOException( "" );
			
/*			Log.d(TAG, "end get execute elaped=" + Long.toString(System.currentTimeMillis() - starttime));
			if (cancel) {
				cancel = false;
				return null;
			}
			if (mPlistener != null) {
				mPlistener.onProgress(30);
			}*/
			inputStream = new BufferedInputStream(httpResponse.getEntity().getContent());
			byte[] b = new byte[1000 * 1024];
			int tmpnum = 0;
			int totalnum = 0;
			String tmptxt = null;
			txt = new String();
			while ((tmpnum = inputStream.read(b, totalnum, 10*1024)) != -1) {
				totalnum += tmpnum;
/*				if (cancel) {
					cancel = false;
					return null;
				}*/
			}
/*			Log.d(TAG, "complete read elaped=" + Long.toString(System.currentTimeMillis() - starttime));
			if (mPlistener != null) {
				mPlistener.onProgress(50);
			}*/
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
