package com.netxeon.newprobox2.weather;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.netxeon.newprobox2.newyahooweather.NewYahooWeatherHandler;
import com.netxeon.newprobox2.utils.Logger;
import com.netxeon.newprobox2.utils.Util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import zh.wang.android.apis.yweathergetter4a.NetworkUtils;
import zh.wang.android.apis.yweathergetter4a.WeatherInfo;
import zh.wang.android.apis.yweathergetter4a.YahooWeather;
import zh.wang.android.apis.yweathergetter4a.YahooWeatherExceptionListener;
import zh.wang.android.apis.yweathergetter4a.YahooWeatherInfoListener;

/**
 * Created by Administrator on 2018/3/5 0005.
 */
public class WeatherUtilsV2 implements YahooWeatherInfoListener,
        YahooWeatherExceptionListener {
    public static final String WEATHER_CITY = "locationCity";
    public static final int MSG_WEATHER_OK_NEW = 2;
    public static final int MSG_WEATHER_OK = 1;
    public static final int MSG_WEATHER_NO_CITY = 0;
    public static final int MSG_WEATHER_FAILED = -1;
    public static final int MSG_WEATHER_PARSE_CITY_FAILED = -2;
    public static final int MSG_WEATHER_NETWORK_DISCONNECTED = -3;
    public static final long UPDATE_WEATHER_PER_TIME = 3;
    private Context context;
    private Handler weatherHandler;
    //访问次数
    private static int currAccessCount = 0;
    //访问最大次数
    private static final int maxAccessCount = 10;
    //网络是否连接
    private boolean netwokConnecting = false;
    //是否正在访问
    private boolean isAccessing = false;

    private String cityNameTmp = "empty";

    public WeatherUtilsV2(Context context, Handler weatherHandler) {
        this.context = context;
        this.weatherHandler = weatherHandler;
        cityNameTmp = getCityName();
        accessWeatherServer("WeatherUtilsV2 constructor");
    }

    /**
     * 网络变化
     */
    public void netWorkChanged() {
        if (Logger.DEBUG_WEATHER) Log.i("WeatherUtilsV2", "netWorkChanged");
        boolean lastAvailable = netwokConnecting;
        checkNextWork();
        //网络从断开--连接
        if (lastAvailable != netwokConnecting && netwokConnecting) {
            if (Logger.DEBUG_WEATHER) Log.i("WeatherUtilsV2", "netWorkChanged-connection");
            accessWeatherServer("netWorkChanged");
        }
    }

    /**
     * 检查网络
     */
    private void checkNextWork() {
        if (Logger.DEBUG_WEATHER) Log.i("WeatherUtilsV2", "checkNextWork");
        netwokConnecting = NetworkUtils.isConnected(context);
    }

    public void changeCityName(String cityName) {
        if (Logger.DEBUG_WEATHER) Log.i("WeatherUtilsV2", "changeCityName:" + cityName);
        cityNameTmp = cityName;
        currAccessCount = 0;
        accessWeatherServer("changeCityName");
    }

    public String getCityName() {
        if (Logger.DEBUG_WEATHER) Log.i("WeatherUtilsV2", "getCityName");
        return Util.getString(context, WEATHER_CITY);
    }

    /**
     * 设置当前城市名称
     */
    public void saveCityName() {
        if (Logger.DEBUG_WEATHER) Log.i("WeatherUtilsV2", "setCityName:" + cityNameTmp);
        Util.setString(context, WEATHER_CITY, cityNameTmp);
        startAccessTimer();
    }

    public void saveCityName(String mWeatherRegion) {
        if (Logger.DEBUG_WEATHER)
            Log.i("WeatherUtilsV2", "setCityName:" + cityNameTmp + " ; mWeatherRegion:" + mWeatherRegion);
        Util.setString(context, WEATHER_CITY, cityNameTmp + "," + mWeatherRegion.replace(" ", ""));
        startAccessTimer();
    }

    /**
     * 设置是否访问中
     */
    public void setAccessing(boolean isAccessing) {
        if (Logger.DEBUG_WEATHER) Log.i("WeatherUtilsV2", "setAccessing:" + isAccessing);
        this.isAccessing = isAccessing;
        String currSaveCityName = getCityName();
        if (!currSaveCityName.equals(cityNameTmp) && !currSaveCityName.startsWith(cityNameTmp)) {
            changeCityName(cityNameTmp);
        }
    }

    /**
     * 停止访问
     */
    public void stopAccessWeather() {
        weatherHandler.removeCallbacks(delayAccessRunnale);
    }

    /**
     * 访问服务器
     */
    public void accessWeatherServer(String reason) {
        if (Logger.DEBUG_WEATHER)
            Log.i("WeatherUtilsV2", "reason:" + reason + ";accessWeatherServer isAccessing:" + isAccessing);
        if (isAccessing) return;
        weatherHandler.removeCallbacks(delayAccessRunnale);
        isAccessing = true;
        checkNextWork();
        if (!netwokConnecting) {
            if (Logger.DEBUG_WEATHER) Log.i("WeatherUtilsV2", "netwokConnecting");
            weatherHandler.sendEmptyMessage(MSG_WEATHER_NETWORK_DISCONNECTED);
            return;
        }
        if (!cityNameTmp.equals("empty")) {
            if (Logger.DEBUG_WEATHER)
                Log.i("WeatherUtilsV2", "cityNameTmp is not empty : " + cityNameTmp);
            try {
                //YahooWeather mYahooWeather = YahooWeather.getInstance(5000, 5000, Logger.DEBUG_WEATHER);//Logger.DEBUG_WEATHER
                //mYahooWeather.setNeedDownloadIcons(false);
                //mYahooWeather.setSearchMode(YahooWeather.SEARCH_MODE.PLACE_NAME);
                //mYahooWeather.queryYahooWeatherByPlaceName(context,
                //		cityNameTmp, this);
                new NewYahooWeatherHandler(weatherHandler, cityNameTmp).execute();
            } catch (Exception e) {
                weatherHandler.sendEmptyMessage(WeatherUtilsV2.MSG_WEATHER_FAILED);
            }
        } else {
            if (Logger.DEBUG_WEATHER)
                Log.i("WeatherUtilsV2", "cityNameTmp is empty : " + cityNameTmp);
            weatherHandler.sendEmptyMessage(MSG_WEATHER_NO_CITY);
        }
    }

    Runnable delayAccessRunnale = new Runnable() {
        @Override
        public void run() {
            currAccessCount++;
            if (Logger.DEBUG_WEATHER)
                Log.i("WeatherUtilsV2", "currAccessCount : " + currAccessCount);
            if (currAccessCount <= maxAccessCount) {
                accessWeatherServer("delayAccessRunnale");
            } else {
                currAccessCount = 0;
                startAccessTimer();
            }
        }
    };

    /**
     * 没有查询到结果,延迟10s重新查询
     */
    public void accessFailed() {
        weatherHandler.removeCallbacks(delayAccessRunnale);
        weatherHandler.postDelayed(delayAccessRunnale, 10000);
    }

    /**
     * 查询到结果
     */
    public void getCityInfo() {
        if (Logger.DEBUG_WEATHER) Log.i("WeatherUtilsV2", "getCityInfo : " + cityNameTmp);
        currAccessCount = 0;
        startAccessTimer();
    }

    /**
     * onResume
     */
    public void onResume() {
        accessWeatherServer("onResume");
    }

    private static boolean isStartAccessTimer = false;

    /**
     * 启动定时连接
     */
    public void startAccessTimer() {
        if (isStartAccessTimer) {
            return;
        }
        isStartAccessTimer = true;
        if (Logger.DEBUG_WEATHER)
            Log.i("WeatherUtilsV2", "startAccessTimer : " + UPDATE_WEATHER_PER_TIME);
        ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(1);
        scheduledThreadPool.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                accessWeatherServer("scheduleAtFixedRate");
            }
        }, UPDATE_WEATHER_PER_TIME, UPDATE_WEATHER_PER_TIME, TimeUnit.HOURS);
    }

    @Override
    public void onFailConnection(Exception arg0) {
        weatherHandler.sendEmptyMessage(MSG_WEATHER_NETWORK_DISCONNECTED);
        Logger.log(Logger.TAG_WEATHER, "WeatherUpdater.gotWeatherInfo() onFailConnection");

    }

    @Override
    public void onFailFindLocation(Exception arg0) {
        weatherHandler.sendEmptyMessage(MSG_WEATHER_PARSE_CITY_FAILED);
        Logger.log(Logger.TAG_WEATHER, "WeatherUpdater.gotWeatherInfo() onFailFindLocation");

    }

    @Override
    public void onFailParsing(Exception arg0) {
        weatherHandler.sendEmptyMessage(MSG_WEATHER_FAILED);
        Logger.log(Logger.TAG_WEATHER, "WeatherUpdater.gotWeatherInfo() onFailParsing");

    }

    @Override
    public void gotWeatherInfo(WeatherInfo info) {
        Message msg = new Message();
        msg.what = MSG_WEATHER_OK;
        msg.obj = info;
        if (info == null) {
            weatherHandler.sendEmptyMessage(MSG_WEATHER_PARSE_CITY_FAILED);
            Logger.log(Logger.TAG_WEATHER, "WeatherUpdater.gotWeatherInfo() weather info is null");
        } else {
            Logger.log(Logger.TAG_WEATHER, "WeatherUpdater.gotWeatherInfo() weather info get succes");
            weatherHandler.sendMessage(msg);
        }
    }


    public void updateWeatherByGPS() {
        YahooWeather mYahooWeather = YahooWeather.getInstance(5000, 5000, Logger.DEBUG_WEATHER);
        mYahooWeather.setNeedDownloadIcons(false);
        mYahooWeather.setSearchMode(YahooWeather.SEARCH_MODE.GPS);
        mYahooWeather.queryYahooWeatherByGPS(context, this);
    }

}
