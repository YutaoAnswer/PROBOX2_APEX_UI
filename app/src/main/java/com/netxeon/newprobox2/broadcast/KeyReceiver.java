package com.netxeon.newprobox2.broadcast;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.netxeon.newprobox2.activity.MainActivity;
import com.netxeon.newprobox2.fragment.AppsFragment;

public class KeyReceiver extends BroadcastReceiver {


	 AppsFragment appsFragment;
     MainActivity mainActivity;

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("test", "波波");
		if (appsFragment == null) {
			appsFragment = new AppsFragment();
		}
        Log.d("test", "波波接收到之后了");

}
	
 

}
