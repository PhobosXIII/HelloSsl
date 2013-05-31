package com.example.hellossl;

import oauth.signpost.OAuth;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class AuthActivity extends Activity {
	private String CALLBACK_URL = "vputiapp://connect";
	private static final String TAG = "myLog";
	private static final String CONSUMER_KEY = "taxi-ios";
	private static final String CONSUMER_SECRET = "502c1763-b6c1-41b5-8ed8-0dcec15f";
	private static final String REQUEST_TOKEN_ENDPOINT_URL = "http://api.ktovputi.ru/oauth/request_token/";
	private static final String ACCESS_TOKEN_ENDPOINT_URL = "http://api.ktovputi.ru/oauth/access_token/";
	private static final String AUTHORIZE_WEBSITE_URL = "http://api.ktovputi.ru/oauth/authorize/";
	
	CommonsHttpOAuthConsumer consumer = null;
	CommonsHttpOAuthProvider provider = null;
	SharedPreferences prefs;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auth);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
	}
	
	public void onAuthClick(View v) {
		
        String authToken = prefs.getString(OAuth.OAUTH_TOKEN, "");
        String authTokenSecret = prefs.getString(OAuth.OAUTH_TOKEN_SECRET, "");
        consumer = new CommonsHttpOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
        HttpClient client = new DefaultHttpClient();
        if (authToken.length() == 0 && authTokenSecret.length() == 0) {
	        provider = new MyOauthProvider(REQUEST_TOKEN_ENDPOINT_URL, ACCESS_TOKEN_ENDPOINT_URL,
	        			AUTHORIZE_WEBSITE_URL, client);        
	        try {
	        	provider.setOAuth10a(true);
	        	String authUrl = provider.retrieveRequestToken(consumer, CALLBACK_URL);
	        	Toast.makeText(this, "Please authorize this app!", Toast.LENGTH_LONG).show();
	            this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(authUrl)));
	        	Log.d(TAG, authUrl);
				Log.d(TAG, "Request token: " + consumer.getToken());
				Log.d(TAG, "Token secret: " + consumer.getTokenSecret());
			} catch (Exception e) {
				Log.e(TAG, "Error retrieveRequestToken",e);
			}        
        }
        else {
        	consumer.setTokenWithSecret(authToken, authTokenSecret);
        	startActivity(new Intent(this, MainSslActivity.class));
        }
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
	    super.onNewIntent(intent);

	    Uri uri = intent.getData();
	    if (uri != null && uri.toString().startsWith(CALLBACK_URL)) {
	        String verifier = uri.getQueryParameter(oauth.signpost.OAuth.OAUTH_VERIFIER);
	        Log.d(TAG, verifier);
	        Log.d(TAG, uri.toString());
	        try {
	            provider.retrieveAccessToken(consumer, verifier);
	            saveAccessToken(prefs, consumer.getToken(), consumer.getTokenSecret());
	            Log.d(TAG, "Access token: " + consumer.getToken());
				Log.d(TAG, "Token secret: " + consumer.getTokenSecret());
				startActivity(new Intent(this, MainSslActivity.class));
	        } catch (Exception e) {
	        	Log.e(TAG, "Error retrieveAccessToken",e);
	        }

	    }
	}
	
	public static void saveAccessToken(SharedPreferences prefs, String token, String tokenSecret) {
		final Editor edit = prefs.edit();
		edit.putString(OAuth.OAUTH_TOKEN, token);
		edit.putString(OAuth.OAUTH_TOKEN_SECRET, tokenSecret);
		edit.commit();
    }
}
