package com.example.hellossl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import oauth.signpost.OAuth;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class MyIntentService extends IntentService {

	public static final String ACTION="mycustomactionstring";
	private static final String CONSUMER_KEY = "sweettea";
	private static final String CONSUMER_SECRET = "topsecretsweettea248";
	private static final String REQUEST_TOKEN_ENDPOINT_URL = "https://api.v7taxi.ru/oauth/request_token/";
	private static final String ACCESS_TOKEN_ENDPOINT_URL = "https://api.v7taxi.ru/oauth/access_token/?username=God&password=holly";
	private static final String AUTHORIZE_WEBSITE_URL = "https://api.v7taxi.ru/oauth/authorize/";
	private static final String TAG = "myLog";
	
	CommonsHttpOAuthConsumer consumer = null;
	CommonsHttpOAuthProvider provider = null;
	
	String message = "";
    
    public MyIntentService() {
        super("MyIntentService");
    }
 
    @Override
    protected void onHandleIntent(Intent arg0) {
    	String url = arg0.getStringExtra("url");
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String authToken = prefs.getString(OAuth.OAUTH_TOKEN, "");
        String authTokenSecret = prefs.getString(OAuth.OAUTH_TOKEN_SECRET, "");
    	
    	HttpClient client = HttpUtils.getNewHttpClient();
        
        consumer = new CommonsHttpOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
        
        if (authToken.length() == 0 && authTokenSecret.length() == 0) {
	        provider = new MyOauthProvider(REQUEST_TOKEN_ENDPOINT_URL, ACCESS_TOKEN_ENDPOINT_URL,
	        			AUTHORIZE_WEBSITE_URL, client);        
	        try {
	        	provider.setOAuth10a(true);
	        	String authUrl = provider.retrieveRequestToken(consumer, OAuth.OUT_OF_BAND);
	        	Log.d(TAG, authUrl);
				Log.d(TAG, "Access token: " + consumer.getToken());
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

        HttpGet request = new HttpGet(url);
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
        	message = String.valueOf(response.getStatusLine().getStatusCode() + " " + convertStreamToString(response.getEntity().getContent()));   
		} catch (ClientProtocolException e) {
			message = "Что-то не так";
		} catch (IOException e) {
			message = e.toString();
		}
    	
    	Intent in = new Intent(ACTION);
    	in.putExtra("message", message);
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
