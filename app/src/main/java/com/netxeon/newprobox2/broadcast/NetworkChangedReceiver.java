package com.netxeon.newprobox2.broadcast;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.widget.ImageView;

import com.netxeon.newprobox2.R;
import com.netxeon.newprobox2.utils.L;
import com.netxeon.newprobox2.utils.Util;
import com.netxeon.newprobox2.weather.WeatherUtils;
import com.netxeon.newprobox2.weather.WeatherUtilsV2;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class NetworkChangedReceiver extends BroadcastReceiver {

    private Activity mContext;
    private Handler mUpdateHandler;
    private WeatherUtilsV2 mWeatherUtilsV2;
    // update weather info per 3 hours
    private static final long UPDATE_WEATHER_PER_TIME = 1000 * 60 * 60 * 3;
    private long mLastWeatherUpdate = 0;

    public NetworkChangedReceiver(Activity context, Handler updateHandler, WeatherUtilsV2 weatherUtilsV2) {
        mContext = context;
        mUpdateHandler = updateHandler;
        mWeatherUtilsV2 = weatherUtilsV2;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Objects.equals(intent.getAction(), WifiManager.RSSI_CHANGED_ACTION) || Objects.equals(intent.getAction(), ConnectivityManager.CONNECTIVITY_ACTION)) {
            boolean updateWeather = intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION);//网络状态改变
            initNetwork(updateWeather);
        }
        //Util.updateDateDisplay(mContext);
    }

    /*
     * update networkIcon while start app or network changed
     */
    private void initNetwork(boolean updateWeather) {
        ConnectivityManager connManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connManager.getActiveNetworkInfo();
        boolean ethernet = false;
        boolean available = false;
        if (info != null) {
            ethernet = info.getType() == ConnectivityManager.TYPE_ETHERNET;
            available = info.isConnected();//网络是否可用
        }
        if (updateWeather && available) {
            if (System.currentTimeMillis() - mLastWeatherUpdate > UPDATE_WEATHER_PER_TIME) {
                mLastWeatherUpdate = System.currentTimeMillis();
                initWeather();
            }
        }
        ImageView wifi_image = (ImageView) mContext.findViewById(R.id.main_foot_wifi_states);
        ImageView ethernat = (ImageView) mContext.findViewById(R.id.main_foot_ethernet);

        if (!available) {
            ethernat.setImageResource(R.mipmap.ethernet_off);
            wifi_image.setImageResource(R.mipmap.icon_wifi_0);
            L.i("NetworkChangedReceiver:networkstate_off");
        } else {
            if (ethernet) {
                ethernat.setImageResource(R.mipmap.ethernet_on);
                wifi_image.setImageResource(R.mipmap.icon_wifi_0);
                L.i("NetworkChangedReceiver:networkstate_on");
            } else {
                ethernat.setImageResource(R.mipmap.ethernet_off);
                WifiManager manager = (WifiManager) mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                int level = WifiManager.calculateSignalLevel(manager.getConnectionInfo().getRssi(), 5);
                L.i(level + "level");
                wifi_image.setImageResource(R.drawable.wifi_signl);
                switch (level) {
                    case 1:
                        wifi_image.setImageLevel(level);
                        break;
                    case 2:
                        wifi_image.setImageLevel(level);
                        break;
                    case 3:
                        wifi_image.setImageLevel(level);
                        break;
                    case 4:
                    case 5:
                        wifi_image.setImageLevel(4);
                        break;
                    default:
                        wifi_image.setImageLevel(1);
                }
            }
        }
    }

    int count = 0;

    private void initWeather() {
        count++;
        Timer mTimer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
//                WeatherUtilsV2.updateWeather(mContext, mUpdateHandler, Util.getString(mContext, WeatherUtils.WEATHER_CITY));
                mWeatherUtilsV2.updateWeatherByGPS();
                //               L.d("MainActivity.initWeather() start Timer weatherUpdater. count:" + count);
            }
        };
        try {
            mTimer.schedule(task, 100, UPDATE_WEATHER_PER_TIME);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
