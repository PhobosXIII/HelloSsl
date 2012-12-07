package com.example.hellossl;



import java.util.LinkedList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.commonsware.cwac.endless.EndlessAdapter;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class MainSslActivity extends Activity {

	LinkedList<String> places, tempList;
	PullToRefreshListView pullToRefreshView;
	DemoAdapter adapter;
	ListView actualListView;
	Intent intent;
	private int mLastOffset = 0;
	private boolean mEndList = true;
	static final int BATCH_SIZE = 7;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_ssl);
        
        intent = new Intent(this, MyIntentService.class);
        tempList = new LinkedList<String>();
        
        pullToRefreshView = (PullToRefreshListView) findViewById(R.id.pull_to_refresh_listview);
        pullToRefreshView.setOnRefreshListener(new OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
            	pullToRefreshView.setLastUpdatedLabel(DateUtils.formatDateTime(getApplicationContext(),
						System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE
								| DateUtils.FORMAT_ABBREV_ALL));
                startService(intent.putExtra("url", "http://172.31.0.120:8000/v2/places.json"));
            }
        });
        pullToRefreshView.setMode(Mode.PULL_DOWN_TO_REFRESH);
        pullToRefreshView.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {
            @Override
            public void onLastItemVisible() {

            }
        });

		actualListView = pullToRefreshView.getRefreshableView();
        adapter = (DemoAdapter) actualListView.getAdapter();
        if (adapter == null) { 
        	places = new LinkedList<String>();
        	startService(intent.putExtra("url", "http://172.31.0.120:8000/v2/places.json?limit=" + String.valueOf(BATCH_SIZE) + "&offset=" + String.valueOf(getLastOffset())));
        	adapter = new DemoAdapter(this, places);
        }
        else {
            adapter.startProgressAnimation();
          }
        actualListView.setAdapter(adapter);
    }
	
	private void setLastOffset(int i) {
	    mLastOffset = i;
	}

	private int getLastOffset() {
	    return mLastOffset;
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
            tempList.clear();
            String message = intent.getStringExtra("message");
            try {
	            JSONArray jPlaces = new JSONArray(message);
	            if (jPlaces.length() != 0) {
	            	mEndList = false;
		            for (int i = 0; i < jPlaces.length(); i++) {
		                JSONObject jPlace = jPlaces.getJSONObject(i);
		                if (!places.contains(jPlace.getString("name"))) {
		                	places.add(jPlace.getString("name"));
		                	tempList.add(jPlace.getString("name"));
		                }
		                Log.d("example", jPlace.getString("name"));
		            }
	            }
	            else {
	            	mEndList = true;
	            }
            } catch (JSONException e) {}
            adapter.notifyDataSetChanged();
			pullToRefreshView.onRefreshComplete();
			setLastOffset(getLastOffset() + BATCH_SIZE);
			Log.d("example", String.valueOf(mEndList));
			Log.d("example", String.valueOf(getLastOffset()));
			Log.d("example", message);
        }
    };
    
    public void btnNextClick(View v) {
    	Intent i = new Intent(this, EndlessAdapterDemo.class);
    	startActivity(i);
    }
    
    class DemoAdapter extends EndlessAdapter {
    	  private RotateAnimation rotate=null;
    	  private View pendingView=null;
    	  
    	  DemoAdapter(Context ctxt, LinkedList<String> list) {
    		  super(new ArrayAdapter<String>(ctxt,
    	              R.layout.row,
    	              android.R.id.text1,
    	              list));
    		  rotate=new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF,
    	              0.5f, Animation.RELATIVE_TO_SELF,
    	              0.5f);
    		  rotate.setDuration(600);
    		  rotate.setRepeatMode(Animation.RESTART);
    		  rotate.setRepeatCount(Animation.INFINITE);
    	  }
    	  
    	  @Override
    	  protected View getPendingView(ViewGroup parent) {
    	    View row=LayoutInflater.from(parent.getContext()).inflate(R.layout.row, null);
    	    
    	    pendingView=row.findViewById(android.R.id.text1);
    	    pendingView.setVisibility(View.GONE);
    	    pendingView=row.findViewById(R.id.throbber);
    	    pendingView.setVisibility(View.VISIBLE);
    	    startProgressAnimation();
    	    
    	    return(row);
    	  }
    	  
    		@Override
    		protected void appendCachedData() {
    			if (!mEndList) {
    			      @SuppressWarnings("unchecked")
    			      ArrayAdapter<String> a=(ArrayAdapter<String>)getWrappedAdapter();
    			      int listLen = tempList.size();
    			      for (int i = 0; i < listLen; i++) {
    			          a.add(tempList.get(i));
    			      }
    			}
    		}
    		
    		@Override
    		protected boolean cacheInBackground() throws Exception {
    			if (!mEndList) {
    				startService(intent.putExtra("url", "http://172.31.0.120:8000/v2/places.json" + String.valueOf(BATCH_SIZE) + "&offset=" + String.valueOf(getLastOffset())));
    				return true;
    			}
    			else {
    				return false;
    			}
    		}
    		
    		void startProgressAnimation() {
    			if (pendingView!=null) {
    			      pendingView.startAnimation(rotate);
    			}
    		}
    	}

}
