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

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

public class MainSslActivity extends Activity {
    private static final String CONSUMER_KEY = "sweettea";
	private static final String CONSUMER_SECRET = "topsecretsweettea248";
	private static final String REQUEST_TOKEN_ENDPOINT_URL = "https://api.v7taxi.ru/oauth/request_token/";
	private static final String ACCESS_TOKEN_ENDPOINT_URL = "https://api.v7taxi.ru/oauth/access_token/?username=God&password=holly";
	private static final String AUTHORIZE_WEBSITE_URL = "https://api.v7taxi.ru/oauth/authorize/";
	private static final String TAG = "myLog";
	
	CommonsHttpOAuthConsumer consumer = null;
	CommonsHttpOAuthProvider provider = null;
	
	TextView tv;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_ssl);
        
        tv = (TextView) findViewById(R.id.tvHello);
        
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy); 
        
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String authToken = prefs.getString(OAuth.OAUTH_TOKEN, "");
        String authTokenSecret = prefs.getString(OAuth.OAUTH_TOKEN_SECRET, "");
        
        HttpClient client = HttpUtils.getNewHttpClient();
        
        // create a consumer object and configure it with the access
        // token and token secret obtained from the service provider
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

        // create an HTTP request to a protected resource
        HttpGet request = new HttpGet("https://api.v7taxi.ru/v2/places.json");

        // sign the request
        try {
			consumer.sign(request);
		} catch (OAuthMessageSignerException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (OAuthExpectationFailedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (OAuthCommunicationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

        
        // send the request 
        try {
        	HttpResponse response = client.execute(request);
			Toast.makeText(getApplicationContext(), String.valueOf(response.getStatusLine().getStatusCode()), Toast.LENGTH_LONG).show();
			Log.d(TAG, convertStreamToString(response.getEntity().getContent()));
			Log.d("example","starting service");
            Intent intent = new Intent(this, MyIntentService.class);
            startService(intent.putExtra("time", 3).putExtra("label", "Call 1") );
            startService(intent.putExtra("time", 7).putExtra("label", "Call 2") );
            startService(intent.putExtra("time", 5).putExtra("label", "Call 3") );
		} catch (ClientProtocolException e) {
			Toast.makeText(getApplicationContext(), "Что-то не так", Toast.LENGTH_LONG).show();
		} catch (IOException e) {
			Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
		}

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_ssl, menu);
        return true;
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
    
    public static void saveAccessToken(SharedPreferences prefs, String token, String tokenSecret) {
		final Editor edit = prefs.edit();
		edit.putString(OAuth.OAUTH_TOKEN, token);
		edit.putString(OAuth.OAUTH_TOKEN_SECRET, tokenSecret);
		edit.commit();
    }
    
    @Override
    protected void onPause() {
        super.onPause();  
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onNotice);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter iff= new IntentFilter(MyIntentService.ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(onNotice, iff);
    }
     
    private BroadcastReceiver onNotice= new BroadcastReceiver() {    
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("example","onReceive called");
            Double currentLatitude = intent.getDoubleExtra("latitude", 0);
            Double currentLongitude = intent.getDoubleExtra("longitude", 0);
            tv.setText("Broadcast received! Lat: " + String.valueOf(currentLatitude) + ", Long: " + String.valueOf(currentLongitude));
        }
    };

}
