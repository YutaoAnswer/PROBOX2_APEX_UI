package com.netxeon.newprobox2.activity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.netxeon.newprobox2.R;
import com.netxeon.newprobox2.broadcast.BluetoothChangeReceiver;
import com.netxeon.newprobox2.broadcast.NetworkChangedReceiver;
import com.netxeon.newprobox2.broadcast.UsbChangeReceiver;
import com.netxeon.newprobox2.fragment.AppsFragment;
import com.netxeon.newprobox2.fragment.CategoryFragment;
import com.netxeon.newprobox2.fragment.HomeFragment;
import com.netxeon.newprobox2.utils.AnimUtil;
import com.netxeon.newprobox2.utils.Data;
import com.netxeon.newprobox2.utils.DateUtil;
import com.netxeon.newprobox2.utils.ListPopupwindow;
import com.netxeon.newprobox2.utils.SPUtils;
import com.netxeon.newprobox2.utils.T;
import com.netxeon.newprobox2.utils.Util;
import com.netxeon.newprobox2.weather.WeatherUtils;

import java.util.List;
import java.util.Map;

import zh.wang.android.apis.yweathergetter4a.WeatherInfo;

public class MainActivity extends Activity implements View.OnClickListener, View.OnFocusChangeListener
        , View.OnLongClickListener {

    //顶部tab
    private TextView tab1, tab2, tab3;
    //天气，日期相关
    private LinearLayout weather;
    private TextView weather_city;
    private TextView weathrer_temperature;
    private ImageView weather_image;
    private static int mWeatherCode = 3200;
    private TextView systemDate;
    private ImageView externalStorage, switcher, btStatus, setting;

    public HomeFragment homeFragment;
    public AppsFragment appsFragment;
    private CategoryFragment categoryFragment;

    public static Integer FRAGMENT = R.id.main_content_fragment;
    public static Integer CODE_FOR_ACCESS_COARSE_LOCATION = 923;
    private IntentFilter mFilter;

    private NetworkChangedReceiver mNetworkChangedReceiver;
    private UsbChangeReceiver mUsbChangeReceiver;
    private BluetoothChangeReceiver mBluetoothChangeReceiver;

    private Fragment mCurrentFragment, mLastFragment = null;
    public Context mContext;

    private TextView lastTag;
    private Map<String, String> volumes;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //db
        int lastDatabaseVersion = Util.getInt(this, Data.PRE_DB_VERSION);//获取上一版本数据库如果第一次装是0
        final int newDatabaseVersion = Util.getNewDatabaseVersion(this, Data.PRE_DB_VERSION);//获取xml里设置的数据库版本
        if (lastDatabaseVersion < newDatabaseVersion) {
            new Thread() {
                public void run() {
                    Util.copyDatabaseFromAssert(MainActivity.this, newDatabaseVersion);//顺便把数据库版本set进去
                    Util.copyWallpaperFromAssert(MainActivity.this);
                }
            }.start();
        }
        mContext = this;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);//壁纸
        setContentView(R.layout.activity_main);
        initView();
        initNetworkDisplay();
        initUsbDispaly();
        initbtDispaly();
        setListener();
        setDefaultFragment(savedInstanceState != null);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        lastTag.requestFocus();
        registerHomeKeyReceiver(this);

        registerReceiver(mNetworkChangedReceiver, mFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterHomeKeyReceiver(this);
        unregisterReceiver(mNetworkChangedReceiver);
    }

    private void initNetworkDisplay() {
        mFilter = new IntentFilter();
        mFilter.addAction(Intent.ACTION_TIME_CHANGED);//当用户更改了设备上的时间时发送
//        mFilter.addAction(Intent.ACTION_DATE_CHANGED);//日期改变
//        mFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);//时区
        mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);//监听网络状态的action，提供几种extra，wifi 3g等等
        mFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);//Wi-Fi连接状态改变时广播这个intent
        mFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);//标识Wi-Fi硬件状态改变，可能在enabling、enabled、disabling、disabled和unknown之间改变
        mFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        //监听这个intent可以使应用程序监控当前Wi-Fi连接的信号强度。包含一个额外键值EXTRA_NEW_RSSI，包含了当前信号强度。使用这个信号强度，需要使用静态函数calculateSignalLevel将这个值按指定的缩放转换为整型值。
        mNetworkChangedReceiver = new NetworkChangedReceiver(this, mUpdateWeatherHandler);// 监听网络状态改变，顺便初始化天气定时更新
        registerReceiver(mNetworkChangedReceiver, mFilter);
    }

    private void initUsbDispaly() {

        //注册外置存储广播
        IntentFilter Filter = new IntentFilter();
        Filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        Filter.addAction(Intent.ACTION_MEDIA_EJECT);
        Filter.addDataScheme("file");//不添加接收不到广播
        mUsbChangeReceiver = new UsbChangeReceiver(externalStorage);
        registerReceiver(mUsbChangeReceiver, Filter);
    }


    private void initbtDispaly() {

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        mBluetoothChangeReceiver = new BluetoothChangeReceiver(btStatus);
        registerReceiver(mBluetoothChangeReceiver, filter);
    }


    private void initView() {
        tab1 = (TextView) findViewById(R.id.main_content_tag1);
        tab2 = (TextView) findViewById(R.id.main_content_tag2);
        tab3 = (TextView) findViewById(R.id.main_content_tag_category);
        weather = (LinearLayout) findViewById(R.id.main_header_weather);
        weathrer_temperature = (TextView) findViewById(R.id.title_weather_temperature);
        weather_city = (TextView) findViewById(R.id.title_weather_city);
        weather_image = (ImageView) findViewById(R.id.title_weather_image);
        systemDate = (TextView) findViewById(R.id.main_header_date);
        externalStorage = (ImageView) findViewById(R.id.main_foot_external_storage);
//        switcher = (ImageView) findViewById(R.id.main_foot_launcher_switcher);
        btStatus = (ImageView) findViewById(R.id.main_foot_bluetooth_states);
        setting = (ImageView) findViewById(R.id.main_foot_setting);
        timeHandle.post(timeRun);
        BluetoothAdapter blueadapter = BluetoothAdapter.getDefaultAdapter();
        if (blueadapter != null) {
            if (blueadapter.isEnabled()) {
                btStatus.setImageResource(R.mipmap.bt_on);
            }
        }
    }

    private void setListener() {
        tab1.setOnFocusChangeListener(this);
        tab2.setOnFocusChangeListener(this);
        tab1.setOnClickListener(this);
        tab2.setOnClickListener(this);
        weather.setOnFocusChangeListener(this);
        externalStorage.setOnFocusChangeListener(this);
//        switcher.setOnFocusChangeListener(this);
        setting.setOnClickListener(this);
        weather.setOnClickListener(this);
//        switcher.setOnClickListener(this);
        setting.setOnFocusChangeListener(this);
        externalStorage.setOnClickListener(this);
//        switcher.setOnLongClickListener(this);
        //判断是否有外置存储
        volumes = Util.getPublicVolumes(this);
        if (volumes.size() > 0) {
            externalStorage.setImageResource(R.mipmap.usb_on);
            externalStorage.setClickable(true);
            externalStorage.setFocusable(true);
        } else {
            externalStorage.setClickable(false);
        }
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        if (homeFragment == null && fragment instanceof HomeFragment) {
            homeFragment = (HomeFragment) fragment;
        } else if (appsFragment == null && fragment instanceof AppsFragment) {
            appsFragment = (AppsFragment) fragment;
        } else if (categoryFragment == null && fragment instanceof CategoryFragment) {
            categoryFragment = (CategoryFragment) fragment;
        }
    }

    private void setDefaultFragment(boolean isSave) {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        if (isSave) {
            fragmentTransaction.hide(appsFragment).hide(categoryFragment).show(homeFragment);
        } else {
            appsFragment = new AppsFragment();
            categoryFragment = new CategoryFragment();
            homeFragment = new HomeFragment();
            fragmentTransaction.add(FRAGMENT, appsFragment).hide(appsFragment).
                    add(FRAGMENT, categoryFragment).hide(categoryFragment).add(FRAGMENT, homeFragment);
        }
//        Log.d("zzy", "setDefaultFragment,saveState:" + isSave);
        fragmentTransaction.commit();
        mCurrentFragment = homeFragment;
        lastTag = tab1;
        tab1.requestFocus();
    }

    public CategoryFragment getCategoryFragment() {
        return categoryFragment;
    }

    public void changeFragment(Fragment fragment) {
        if (fragment == mCurrentFragment)//如果是当前不做变化
            return;
        if (fragment == categoryFragment) {
            tab1.setVisibility(View.GONE);
            tab2.setVisibility(View.GONE);
            tab3.setVisibility(View.VISIBLE);
            tab3.setText(categoryFragment.getmCurrentCategoryString());
        } else {
            tab3.setVisibility(View.GONE);
            tab1.setVisibility(View.VISIBLE);
            tab2.setVisibility(View.VISIBLE);
        }
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        //  transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.setCustomAnimations(R.anim.main_test, 0, 0, 0);
        transaction.hide(mCurrentFragment).show(fragment).commitAllowingStateLoss();
        mLastFragment = mCurrentFragment;
        mCurrentFragment = fragment;
    }

    public void onSetWallpaper() {
        //生成一个设置壁纸的请求
        final Intent pickWallpaper = new Intent(Intent.ACTION_SET_WALLPAPER);
        Intent chooser = Intent.createChooser(pickWallpaper, "chooser_wallpaper");
        //发送设置壁纸的请求
        startActivity(chooser);
    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        Log.i("bo", "dispatchKeyEvent" + "");
        return super.dispatchKeyEvent(event);

    }

    /**
     * 按键监听
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && event.getRepeatCount() == 0) {
//            if (tab2.isFocused()) {
//                GridView gridView = (GridView) findViewById(R.id.fragment_apps_gridview);
//                if (gridView != null) {
//                    gridView.setFocusable(true);
//                }
//                return false;
//            }
//            if (tab1.isFocused()) {
//                GridView gridView = (GridView) findViewById(R.id.fragment_home_gridview);
//                if (gridView != null) {
//                    gridView.setFocusable(true);
//                }
//                return false;
//            }
            if (weather.isFocused()) {
                weather.setNextFocusDownId(lastTag.getId());
            }
        }

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!categoryFragment.isHidden()) {
                changeFragment(homeFragment);
            }
            return false;
        }

        if (keyCode == KeyEvent.KEYCODE_MENU) {

            onSetWallpaper();

            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            if (setting.isFocused()) {
                setting.setNextFocusLeftId(externalStorage.isFocusable() ? R.id.main_foot_external_storage : R.id.main_foot_setting);
            }
        }

        if (keyCode == KeyEvent.KEYCODE_HOME) {

            changeFragment(homeFragment);
            return true;
    }

        return super.onKeyDown(keyCode, event);
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        if (requestCode == CODE_FOR_ACCESS_COARSE_LOCATION) {
//            if (permissions[0].equals(Manifest.permission.ACCESS_COARSE_LOCATION)
//                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                //用户同意
//                showEditCityForWeatherDialog();
//            } else {
//                //用户不同意，自行处理即可
//                T.showLong(mContext, "无法获取位置");
//            }
//        }
//    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_header_weather:
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                    if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                        int CODE_FOR_WRITE_PERMISSION;
//                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
//                                CODE_FOR_ACCESS_COARSE_LOCATION);
//                    } else {
//                        showEditCityForWeatherDialog();
//                    }
//                } else {
//                    showEditCityForWeatherDialog();
//                }
                showEditCityForWeatherDialog();
                break;
            case R.id.main_foot_external_storage:
                if (externalStorage.isClickable()) {
                    ListPopupwindow usbWindow = new ListPopupwindow(this, ListPopupwindow.LEFTPOPUP);
                    usbWindow.showOrDismissPopupWindow(externalStorage);
                }
                break;
//            case R.id.main_foot_launcher_switcher:
//                if (!SPUtils.contains(this, "default_launcher")) {
//                    ListPopupwindow launcherWindow = new ListPopupwindow(this, ListPopupwindow.RIGHTPOPUP);
//                    launcherWindow.showOrDismissPopupWindow(switcher);
//                } else {
//                    doStartApplicationWithPackageName((String) SPUtils.get(this, "default_launcher", getPackageName()));
//                }

//模拟系统home键
//                Intent setlauncher = new Intent(Intent.ACTION_MAIN);
//                setlauncher.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                setlauncher.addCategory(Intent.CATEGORY_HOME);
//                startActivity(setlauncher);

//取消系统默认launcher
//                getPackageManager().queryIntentActivities()
//                Intent intent = new Intent(Intent.ACTION_MAIN);
//                intent.addCategory(Intent.CATEGORY_HOME);
//                ResolveInfo resolveInfo = getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
//                String currentHomePackage = resolveInfo.activityInfo.packageName;
//                getPackageManager().clearPackagePreferredActivities(currentHomePackage);
//                break;
            case R.id.main_foot_setting:
                Intent settingIntent = new Intent();
                ComponentName mComp = new ComponentName(getString(R.string.setting_pkg), getString(R.string.setting_main_activity));
                settingIntent.setComponent(mComp);
                try {
                    startActivity(settingIntent);
                } catch (Exception e) {
                    //              L.i("netexon setting not found");
                    settingIntent = new Intent(Settings.ACTION_SETTINGS);
                    startActivity(settingIntent);
                }
                break;
            case R.id.main_content_tag1:
                if (homeFragment == null) {
                    homeFragment = new HomeFragment();
                }
                changeFragment(homeFragment);
                if (lastTag.equals(tab2)) {
                    tab2.setBackgroundColor(getResources().getColor(R.color.none));
                }
                tab1.setBackgroundColor(getResources().getColor(R.color.blue));
                lastTag = tab1;
                break;
            case R.id.main_content_tag2:
                if (appsFragment == null) {
                    appsFragment = new AppsFragment();
                }
                changeFragment(appsFragment);
                if (lastTag.equals(tab1)) {
                    tab1.setBackgroundColor(getResources().getColor(R.color.none));
                }
                lastTag = tab2;
                tab2.setBackgroundColor(getResources().getColor(R.color.blue));
                break;
        }
    }

    @Override
    public boolean onLongClick(View v) {
//        switch (v.getId()) {
//            case R.id.main_foot_launcher_switcher:
//                if (SPUtils.contains(mContext, "default_launcher")) {
//                    SPUtils.remove(mContext, "default_launcher");
//                    T.showLong(mContext, getString(R.string.launcher_default_cancel));
//                }
//                break;
//        }
        return false;
    }

    //wifi,weather
    @SuppressLint("HandlerLeak")
    private Handler mUpdateWeatherHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

//            L.i("MainActivity.UpdateWeatherHandler msg.what : " + msg.what);
            switch (msg.what) {
                case WeatherUtils.MSG_WEATHER_NO_CITY: {
                    if (weather_city != null) weather_city.setText(R.string.weather_no_city);
                    Util.setString(getApplicationContext(), WeatherUtils.WEATHER_CITY, "empty");
                    break;
                }
                case WeatherUtils.MSG_WEATHER_OK: {
                    WeatherInfo weatherInfo = (WeatherInfo) msg.obj;

                    if (weather_city != null && weather_image != null && weatherInfo != null) {
                        mWeatherCode = weatherInfo.getCurrentCode();
                        int temp = (int) ((weatherInfo.getCurrentTemp() - 32) / 1.8);
                        weathrer_temperature.setText(temp + "ºC");
                        weather_city.setText(weatherInfo.getLocationCity());
                        weathrer_temperature.setVisibility(View.VISIBLE);
                        if (mWeatherCode >= 0 && mWeatherCode <= 47) {
                            weather_image.setImageResource(Data.getWeatherIcon(mWeatherCode));//设置通过weathercode设置已经在本地的天气图片
                        } else {
                            weather_image.setImageResource(R.mipmap.weather3200);
                        }

                        Util.setString(getApplicationContext(), WeatherUtils.WEATHER_CITY, weatherInfo.getLocationCity());
//                        L.i("MainActivity.UpdateWeatherHandler display weather info !");
                    } else {
                        if (weather_city != null && weather_image != null) {
                            weather_city.setText(R.string.weather_no_city);
                            weathrer_temperature.setText("");
                            weathrer_temperature.setVisibility(View.GONE);
                            weather_image.setImageBitmap(null);
                        }
                        Util.setString(getApplicationContext(), WeatherUtils.WEATHER_CITY, "empty");
                        Toast.makeText(getApplicationContext(), R.string.weather_edit_city_error, Toast.LENGTH_LONG).show();
//                        L.i("MainActivity.UpdateWeatherHandler weather views are null !");
                    }
                    break;
                }
            }
        }
    };

    private void showEditCityForWeatherDialog() {
        final EditText editor = new EditText(this);
        editor.setSingleLine();
        new AlertDialog.Builder(this).setTitle(R.string.weather_edit_city_dialog_tips).setIcon(android.R.drawable.ic_dialog_info).setView(editor).setPositiveButton(R.string.weather_edit_city_dialog_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String _location = editor.getText().toString();
                if (!TextUtils.isEmpty(_location)) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(editor.getWindowToken(), 0);
                    WeatherUtils.updateWeather(MainActivity.this, mUpdateWeatherHandler, _location);
                } else {
                    Toast.makeText(getApplicationContext(), R.string.weather_edit_city_dialog_not_empty, Toast.LENGTH_LONG).show();
                }
            }
        }).show();
    }

    //时间
    private Handler timeHandle = new Handler();
    private Runnable timeRun = new Runnable() {
        public void run() {
            systemDate.setText(DateUtil.getTime());
            timeHandle.postDelayed(this, 5000);
        }

    };


    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            switch (v.getId()) {
                case R.id.main_content_tag1:
                    if (homeFragment == null) {
//                        Log.i("bo", "homeFragment == null");
                        homeFragment = new HomeFragment();
                    }
                    homeFragment.lastR = null;
                    changeFragment(homeFragment);
                    if (lastTag.equals(tab2)) {
                        tab2.setBackgroundColor(getResources().getColor(R.color.none));
                    }
                    tab1.setBackgroundColor(getResources().getColor(R.color.blue));
                    lastTag = tab1;
                    break;
                case R.id.main_content_tag2:
                    if (appsFragment == null) {
                        appsFragment = new AppsFragment();
                    }

                    changeFragment(appsFragment);
                    if (lastTag.equals(tab1)) {
                        tab1.setBackgroundColor(getResources().getColor(R.color.none));
                    }
                    lastTag = tab2;
                    tab2.setBackgroundColor(getResources().getColor(R.color.blue));
                    break;
                case R.id.main_header_weather:
                    AnimUtil.setViewScale(v, new Float(1.5));
                    break;
                case R.id.main_foot_external_storage:
                    AnimUtil.setViewScale(v, new Float(1.5));
                    break;
                case R.id.main_foot_setting:
                    setting.setImageResource(R.mipmap.settings_on);
                    break;
//                case R.id.main_foot_launcher_switcher:
//                    switcher.setImageResource(R.mipmap.switch_on);
//                    break;
            }
        } else {
            switch (v.getId()) {
                case R.id.main_content_tag1:
                    break;
                case R.id.main_content_tag2:
                    break;
                case R.id.main_header_weather:
                    AnimUtil.setViewScaleDefault(v);
                    break;
                case R.id.main_foot_external_storage:
                    AnimUtil.setViewScaleDefault(v);
                    break;
                case R.id.main_foot_setting:
                    setting.setImageResource(R.mipmap.settings_off);
                    break;
//                case R.id.main_foot_launcher_switcher:
//                    switcher.setImageResource(R.mipmap.switch_off);
//                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {

        unregisterReceiver(mUsbChangeReceiver);
        unregisterReceiver(mBluetoothChangeReceiver);
        super.onDestroy();
    }

    public void doStartApplicationWithPackageName(String packagename) {
        // 通过包名获取此APP详细信息，包括Activities、services、versioncode、name等等
        PackageInfo packageinfo = null;
        try {
            packageinfo = getPackageManager().getPackageInfo(packagename, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageinfo == null) {
            return;
        }
        // 创建一个类别为CATEGORY_LAUNCHER的该包名的Intent
        Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
        resolveIntent.addCategory(Intent.CATEGORY_DEFAULT);
        resolveIntent.setPackage(packageinfo.packageName);

        // 通过getPackageManager()的queryIntentActivities方法遍历
        List<ResolveInfo> resolveinfoList = getPackageManager()
                .queryIntentActivities(resolveIntent, 0);
        ResolveInfo resolveinfo = resolveinfoList.iterator().next();
        if (resolveinfo != null) {
            // packagename = 参数packname
            String packageName = resolveinfo.activityInfo.packageName;
            // 这个就是我们要找的该APP的LAUNCHER的Activity[组织形式：packagename.mainActivityname]
            String className = resolveinfo.activityInfo.name;
            // LAUNCHER Intent
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            // 设置ComponentName参数1:packagename参数2:MainActivity路径
            ComponentName cn = new ComponentName(packageName, className);
            intent.setComponent(cn);
            startActivity(intent);
        }
    }

    private HomeWatcherReceiver mHomeKeyReceiver = new HomeWatcherReceiver();

    private void registerHomeKeyReceiver(Context context) {
        final IntentFilter homeFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        context.registerReceiver(mHomeKeyReceiver, homeFilter);
    }

    private void unregisterHomeKeyReceiver(Context context) {
        if (null != mHomeKeyReceiver) {
            context.unregisterReceiver(mHomeKeyReceiver);
        }
    }

    private class HomeWatcherReceiver extends BroadcastReceiver {
        private static final String SYSTEM_DIALOG_REASON_KEY = "reason";
        private static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                // android.intent.action.CLOSE_SYSTEM_DIALOGS
                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                if (SYSTEM_DIALOG_REASON_HOME_KEY.equals(reason)) {
                    // 短按Home键
                    changeFragment(homeFragment);
                    tab1.requestFocus();
                }
            }
        }
    }
}
