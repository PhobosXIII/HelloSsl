package com.example.hellossl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import oauth.signpost.OAuth;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class MyIntentService extends IntentService {

	public static final String ACTION="mycustomactionstring";
	private static final String CONSUMER_KEY = "taxi-ios";
	private static final String CONSUMER_SECRET = "502c1763-b6c1-41b5-8ed8-0dcec15f";
	private static final String REQUEST_TOKEN_ENDPOINT_URL = "http://api.ktovputi.ru/oauth/request_token/";
	private static final String ACCESS_TOKEN_ENDPOINT_URL = "http://api.ktovputi.ru/oauth/access_token/";
	private static final String AUTHORIZE_WEBSITE_URL = "http://api.ktovputi.ru/oauth/authorize/";
	private static final String TAG = "myLog";
	
	CommonsHttpOAuthConsumer consumer = null;
	CommonsHttpOAuthProvider provider = null;
	
	String message = "";
	String encode = "";
    
    public MyIntentService() {
        super("MyIntentService");
    }
 
    @Override
    protected void onHandleIntent(Intent arg0) {
    	String url = arg0.getStringExtra("url");
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String authToken = prefs.getString(OAuth.OAUTH_TOKEN, "");
        String authTokenSecret = prefs.getString(OAuth.OAUTH_TOKEN_SECRET, "");
    	
    	HttpClient client = new DefaultHttpClient();
        
        consumer = new CommonsHttpOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
        
        if (authToken.length() == 0 && authTokenSecret.length() == 0) {
	        provider = new MyOauthProvider(REQUEST_TOKEN_ENDPOINT_URL, ACCESS_TOKEN_ENDPOINT_URL,
	        			AUTHORIZE_WEBSITE_URL, client);        
	        try {
	        	provider.setOAuth10a(true);
	        	String authUrl = provider.retrieveRequestToken(consumer, OAuth.OUT_OF_BAND);
	        	Log.d(TAG, authUrl);
				Log.d(TAG, "Request token: " + consumer.getToken());
				Log.d(TAG, "Token secret: " + consumer.getTokenSecret());
			} catch (Exception e) {
				Log.e(TAG, "Error retrieveRequestToken",e);
			}        
	        try {
				provider.retrieveAccessToken(consumer, null);
				saveAccessToken(prefs, consumer.getToken(), consumer.getTokenSecret());
				Log.d(TAG, "Access token: " + consumer.getToken());
				Log.d(TAG, "Token secret: " + consumer.getTokenSecret());
	        } catch (Exception e) {
				Log.e(TAG, "Error retrieveAccessToken",e);
			} 
        }
        else {
        	consumer.setTokenWithSecret(authToken, authTokenSecret);
        }

        HttpUriRequest request = new HttpPost(url);
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("role", "passenger");
        params.put("places", "2015,2014");
        params.put("departure_time", "2013-04-18T05:56:21.746Z");
        params.put("waiting_time_span", "15");
        params.put("cost", "0.00");
        params.put("passengers_count", "1");
        params.put("distance_extension", "1.20000004768372");
        params.put("walking_distance", "500");
        
		Map<String, String> map = (Map<String, String>) params;
		List<NameValuePair> kwargs = new ArrayList<NameValuePair>();
		for (Map.Entry<String, String> entry : map.entrySet()) {
			String key = entry.getKey();
			String val = entry.getValue();
			kwargs.add(new BasicNameValuePair(key, val));
		}
		try {
			((HttpEntityEnclosingRequestBase) request).setEntity(new UrlEncodedFormEntity(kwargs, "utf-8"));
			encode = ((HttpEntityEnclosingRequestBase) request).getEntity().toString();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

        try {
			consumer.sign(request);
		} catch (OAuthMessageSignerException e1) {
			e1.printStackTrace();
		} catch (OAuthExpectationFailedException e1) {
			e1.printStackTrace();
		} catch (OAuthCommunicationException e1) {
			e1.printStackTrace();
		}

        try {
        	HttpResponse response = client.execute(request);
        	message = String.valueOf(convertStreamToString(response.getEntity().getContent()));   
		} catch (ClientProtocolException e) {
			message = "Что-то не так";
		} catch (IOException e) {
			message = e.toString();
		}
    	
    	Intent in = new Intent(ACTION);
    	in.putExtra("message", message);
    	in.putExtra("encode", encode);
        LocalBroadcastManager.getInstance(this).sendBroadcast(in);
     
    }
    
    public static void saveAccessToken(SharedPreferences prefs, String token, String tokenSecret) {
		final Editor edit = prefs.edit();
		edit.putString(OAuth.OAUTH_TOKEN, token);
		edit.putString(OAuth.OAUTH_TOKEN_SECRET, tokenSecret);
		edit.commit();
    }
    
    public String convertStreamToString(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8);
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line + "\n");
        }
        is.close();
        String message = sb.toString();
                return message;
        }

}
