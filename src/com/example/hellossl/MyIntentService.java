package com.example.hellossl;

import java.util.concurrent.TimeUnit;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class MyIntentService extends IntentService {

	public static final String ACTION="mycustomactionstring";
    
    public MyIntentService() {
        super("MyIntentService");
       Log.d("example","service started");
    }
 
    @Override
    protected void onHandleIntent(Intent arg0) {
        Log.d("example","onHandleIntent called");
        int tm = arg0.getIntExtra("time", 0);
        String label = arg0.getStringExtra("label");
        Log.d("example", "onHandleIntent start " + label);
        Double lat = 2.55;
        Double longit = 4.21;
        try {
        	TimeUnit.SECONDS.sleep(tm);
        	Intent in = new Intent(ACTION);
            in.putExtra("latitude", lat + tm);
            in.putExtra("longitude", longit + tm);
            Log.d("example","sending broadcast");
            LocalBroadcastManager.getInstance(this).sendBroadcast(in);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }       
    }

}
