package com.example.hellossl;



import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

public class MainSslActivity extends Activity {

	TextView tv;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_ssl);
        tv = (TextView) findViewById(R.id.tvHello);
        
        Intent intent = new Intent(this, MyIntentService.class);
        startService(intent.putExtra("url", "https://api.v7taxi.ru/v2/places.json"));
        startService(intent.putExtra("url", "https://api.v7taxi.ru/v2/account.json"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_ssl, menu);
        return true;
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
            String message = intent.getStringExtra("message");
            tv.setText("Broadcast received! " + message);
        }
    };

}
