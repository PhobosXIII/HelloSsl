package com.example.hellossl;

import android.app.ListActivity;
import android.os.Bundle;
import java.util.ArrayList;

public class EndlessAdapterDemo extends ListActivity {
  @Override
  public void onCreate(Bundle icicle) {
    super.onCreate(icicle);
    setContentView(R.layout.list_activity);
    
    DemoAdapter2 adapter=(DemoAdapter2)getLastNonConfigurationInstance();
    
    if (adapter==null) {
      ArrayList<Integer> items=new ArrayList<Integer>();
      
      for (int i=0;i<25;i++) { items.add(i); }
      
      adapter = new DemoAdapter2(this, items);
    }
    else {
      adapter.startProgressAnimation();
    }
    
    setListAdapter(adapter);
  }
  
  @Override
  public Object getLastNonConfigurationInstance() {
    return(getListAdapter());
  }
}