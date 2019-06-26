package com.netxeon.newprobox2.fragment;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.netxeon.newprobox2.R;
import com.netxeon.newprobox2.activity.CleanActivity;
import com.netxeon.newprobox2.activity.EditorActivity;
import com.netxeon.newprobox2.activity.MainActivity;
import com.netxeon.newprobox2.adapter.ShortcutsAdapter;
import com.netxeon.newprobox2.bean.Shortcut;
import com.netxeon.newprobox2.utils.AnimUtil;
import com.netxeon.newprobox2.utils.DBHelper;
import com.netxeon.newprobox2.utils.Data;
import com.netxeon.newprobox2.utils.GridViewTV;
import com.netxeon.newprobox2.utils.Util;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

public class HomeFragment extends Fragment implements AdapterView.OnItemClickListener, View.OnClickListener,
        View.OnFocusChangeListener, AdapterView.OnItemSelectedListener {

    private ShortcutsAdapter mAdapter;
    private GridViewTV gridView;
    private PackageManager pm;
    private List<Shortcut> mShortcut;
    private String mCurrentCategory = Data.HOME;
    private final String ADDITIONAL = "additional";
    private RelativeLayout kodi, movie, favourites, music, googleplay, stream, internet, game, taskkiller, photo, social, files;

    //shortcut列数限制
    public static final int columns = 11;
    //shortcut行数限制
    private int rows = 1;
    //放大倍数
    private float scale = new Float(1.2);

    public RelativeLayout lastR = null;
    private boolean isPaused = false;
    private boolean isFirst = true;
    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mShortcut = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewInit(view);
        setListener();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activityInit();
        sharedPreferences = getActivity().getSharedPreferences("isFirst", Context.MODE_PRIVATE);
        sharedPreferences.edit().putBoolean("isFirst", true).apply();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        if (isPaused) {
            mAdapter.refreshApps();
            isPaused = false;
        }
        getData();
        super.onResume();
    }

    @Override
    public void onPause() {
        isPaused = true;
        super.onPause();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (hidden) {
            gridView.setFocusable(false);
        } else {
            gridView.setFocusable(true);
//            getData();
            if (lastR != null)
                lastR.requestFocus();
        }
        super.onHiddenChanged(hidden);
    }

    private void activityInit() {
        pm = getActivity().getPackageManager();
        mAdapter = new ShortcutsAdapter(getActivity(), mShortcut, pm, false);
        gridView.setAdapter(mAdapter);
        gridView.setNumColumns(columns);
        //gridView.setFocusable(false);//不自动获取焦点
    }

    private void viewInit(View view) {
        kodi = (RelativeLayout) view.findViewById(R.id.home_icon_left_row1);
        movie = (RelativeLayout) view.findViewById(R.id.home_icon_movie);
        favourites = (RelativeLayout) view.findViewById(R.id.home_icon_favourites);
        music = (RelativeLayout) view.findViewById(R.id.home_icon_music);
        googleplay = (RelativeLayout) view.findViewById(R.id.home_icon_left_row2);
        stream = (RelativeLayout) view.findViewById(R.id.home_icon_stream);
        internet = (RelativeLayout) view.findViewById(R.id.home_icon_internet);
        game = (RelativeLayout) view.findViewById(R.id.home_icon_game);
        taskkiller = (RelativeLayout) view.findViewById(R.id.home_icon_left_row3);
        photo = (RelativeLayout) view.findViewById(R.id.home_icon_photo);
        social = (RelativeLayout) view.findViewById(R.id.home_icon_social);
        files = (RelativeLayout) view.findViewById(R.id.home_icon_files);
        gridView = (GridViewTV) view.findViewById(R.id.fragment_home_gridview);
    }

    private void setListener() {
        kodi.setOnClickListener(this);
        kodi.setOnFocusChangeListener(this);
        movie.setOnClickListener(this);
        movie.setOnFocusChangeListener(this);
        favourites.setOnClickListener(this);
        favourites.setOnFocusChangeListener(this);
        music.setOnClickListener(this);
        music.setOnFocusChangeListener(this);
        googleplay.setOnClickListener(this);
        googleplay.setOnFocusChangeListener(this);
        stream.setOnClickListener(this);
        stream.setOnFocusChangeListener(this);
        internet.setOnClickListener(this);
        internet.setOnFocusChangeListener(this);
        game.setOnClickListener(this);
        game.setOnFocusChangeListener(this);
        taskkiller.setOnClickListener(this);
        taskkiller.setOnFocusChangeListener(this);
        photo.setOnClickListener(this);
        photo.setOnFocusChangeListener(this);
        social.setOnClickListener(this);
        social.setOnFocusChangeListener(this);
        files.setOnClickListener(this);
        files.setOnFocusChangeListener(this);
        gridView.setOnItemClickListener(this);
        gridView.setOnItemSelectedListener(this);
        gridView.setOnFocusChangeListener(this);
    }

    private void getData() {
        List<ResolveInfo> allApps = Util.getAllApps(pm);
        for (ResolveInfo app : allApps) {
            Log.d(TAG, "getData: " + app.activityInfo.packageName + "----" + app.isDefault + "----" + app.activityInfo.name);
        }
        if (sharedPreferences.getBoolean("isFirst", true)) {
            //模拟被选中事件，初始化第一次
            // HomeFragment默认选中Google Search,Netflix,Youtube.
            // Category MOVIE 默认选中　netflix youtube
            // Category PHOTO 默认选中 gallery
            // Category MUSIC 默认选中　music
            // Category INTERNET 默认选中　chrome
            for (int i = 0; i < allApps.size(); i++) {
                ResolveInfo resolveInfo = allApps.get(i);
                String packageName = resolveInfo.activityInfo.packageName;
                if (packageName.equals(Data.google)) {
                    Util.insertShortcut(getActivity(), new ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name).toString(), Data.HOME);
                }
                if (packageName.equals(Data.netflix)) {
                    Util.insertShortcut(getActivity(), new ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name).toString(), Data.HOME);
                    Util.insertShortcut(getActivity(), new ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name).toString(), Data.MOVIE);
                }
                if (packageName.equals(Data.youtube)) {
                    Util.insertShortcut(getActivity(), new ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name).toString(), Data.HOME);
                    Util.insertShortcut(getActivity(), new ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name).toString(), Data.MOVIE);
                }
                if (packageName.equals(Data.gallery)) {
                    Util.insertShortcut(getActivity(), new ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name).toString(), Data.PHOTO);
                }
                if (packageName.equals(Data.music)) {
                    Util.insertShortcut(getActivity(), new ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name).toString(), Data.MUSIC);
                }
                if (packageName.equals(Data.chrome)) {
                    Util.insertShortcut(getActivity(), new ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name).toString(), Data.INTERNET);
                }
            }
            sharedPreferences.edit().putBoolean("isFirst", false).apply();
        }
        mShortcut = DBHelper.getInstance(getActivity()).queryByCategory(mCurrentCategory);
        Shortcut forAddItem = new Shortcut();
        forAddItem.setComponentName(ADDITIONAL);
        mShortcut.add(forAddItem);
        //L.d("zzy", "notifyDataSetChanged");
        mAdapter.notifyDataSetChanged(mShortcut);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ShortcutsAdapter.ShortcutHolder holder = (ShortcutsAdapter.ShortcutHolder) view.getTag();
        ComponentName componentName = holder.componentName;
        if (ADDITIONAL.equals(componentName.getPackageName())) {//点击了加号
            Intent editIntent = new Intent(getActivity(), EditorActivity.class);
            editIntent.putExtra(Util.CATEGORY, mCurrentCategory);
            startActivity(editIntent);
        } else {//否则点击跳转
            try {
                Intent mainintent = new Intent(Intent.ACTION_MAIN, null);
                mainintent.addCategory(Intent.CATEGORY_LAUNCHER);
                mainintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                mainintent.setComponent(componentName);
                startActivity(mainintent);
            } catch (Exception e) {
                Log.e("error", "FolderActivity.ItemClickListener.onItemClick() startActivity failed: " + componentName);
            }
        }
    }

    /**
     * 通过包名，类名跳转
     *
     * @param pkg
     * @param cls
     */
    private void startThirdApp(String pkg, String cls) {
        Intent startintent = new Intent();
        //"com.android.tv.settings", "com.android.tv.settings.MainSettings"
        ComponentName mComp = new ComponentName(pkg, cls);
        startintent.setComponent(mComp);
        startActivity(startintent);
    }

    /**
     * 通过包名跳转
     *
     * @param pkg
     */
    private void startThirdAppforPM(String pkg) {
        try {
            startActivity(pm.getLaunchIntentForPackage(pkg));
        } catch (Exception e) {
            Toast.makeText(getActivity(), getResources().getString(R.string.noapp), Toast.LENGTH_SHORT).show();
        }

    }


    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.home_icon_left_row1:
                startThirdAppforPM(getResources().getString(R.string.kodi_media));
                break;
            case R.id.home_icon_left_row2:
                startThirdAppforPM(getResources().getString(R.string.google_play));
                break;
            case R.id.home_icon_left_row3:
                Intent cleanapp = new Intent(getActivity(), CleanActivity.class);
                startActivity(cleanapp);
                break;
            case R.id.home_icon_movie:
                startCateFragment(Data.MOVIE);
//                intent = new Intent(getActivity(), CategoryActivity.class);
//                intent.putExtra(Util.CATEGORY, Data.Cate.movie.name());
//                startActivity(intent);
                break;
            case R.id.home_icon_favourites:
                startCateFragment(Data.FAVOURITES);
//                intent = new Intent(getActivity(), CategoryActivity.class);
//                intent.putExtra(Util.CATEGORY, Data.Cate.favourites.name());
//                startActivity(intent);
                break;
            case R.id.home_icon_music:
                startCateFragment(Data.MUSIC);
//                intent = new Intent(getActivity(), CategoryActivity.class);
//                intent.putExtra(Util.CATEGORY, Data.Cate.music.name());
//                startActivity(intent);
                break;
            case R.id.home_icon_stream:
                startCateFragment(Data.STREAM);
//                intent = new Intent(getActivity(), CategoryActivity.class);
//                intent.putExtra(Util.CATEGORY, Data.Cate.stream.name());
//                startActivity(intent);
                break;
            case R.id.home_icon_internet:
                startCateFragment(Data.INTERNET);
//                intent = new Intent(getActivity(), CategoryActivity.class);
//                intent.putExtra(Util.CATEGORY, Data.Cate.internet.name());
//                startActivity(intent);
                break;
            case R.id.home_icon_game:
                startCateFragment(Data.GAME);
//                intent = new Intent(getActivity(), CategoryActivity.class);
//                intent.putExtra(Util.CATEGORY, Data.Cate.game.name());
//                startActivity(intent);
                break;
            case R.id.home_icon_photo:
                startCateFragment(Data.PHOTO);
//                intent = new Intent(getActivity(), CategoryActivity.class);
//                intent.putExtra(Util.CATEGORY, Data.Cate.photo.name());
//                startActivity(intent);
                break;
            case R.id.home_icon_social:
                startCateFragment(Data.SOCIAL);
//                intent = new Intent(getActivity(), CategoryActivity.class);
//                intent.putExtra(Util.CATEGORY, Data.Cate.social.name());
//                startActivity(intent);
                break;
            case R.id.home_icon_files:
                startThirdAppforPM(getResources().getString(R.string.file_brower));
                break;
        }
        lastR = (RelativeLayout) v;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            switch (v.getId()) {
                case R.id.home_icon_left_row1:
                    v.bringToFront();
                    AnimUtil.setViewScale(v, scale);
                    break;
                case R.id.home_icon_left_row2:
                    v.bringToFront();
                    AnimUtil.setViewScale(v, scale);
                    break;
                case R.id.home_icon_left_row3:
                    v.bringToFront();
                    AnimUtil.setViewScale(v, scale);
                    break;
                case R.id.home_icon_movie:
                    v.bringToFront();
                    AnimUtil.setViewScale(v, scale);
                    break;
                case R.id.home_icon_favourites:
                    v.bringToFront();
                    AnimUtil.setViewScale(v, scale);
                    break;
                case R.id.home_icon_music:
                    v.bringToFront();
                    AnimUtil.setViewScale(v, scale);
                    break;
                case R.id.home_icon_stream:
                    v.bringToFront();
                    AnimUtil.setViewScale(v, scale);
                    break;
                case R.id.home_icon_internet:
                    v.bringToFront();
                    AnimUtil.setViewScale(v, scale);
                    break;
                case R.id.home_icon_game:
                    v.bringToFront();
                    AnimUtil.setViewScale(v, scale);
                    break;
                case R.id.home_icon_photo:
                    v.bringToFront();
                    AnimUtil.setViewScale(v, scale);
                    break;
                case R.id.home_icon_social:
                    v.bringToFront();
                    AnimUtil.setViewScale(v, scale);
                    break;
                case R.id.home_icon_files:
                    v.bringToFront();
                    AnimUtil.setViewScale(v, scale);
                    break;
                case R.id.fragment_home_gridview:
//                    Log.i("bo", "gridview has focus");
                    new Thread(run).start();
                    break;

            }
        } else {
            switch (v.getId()) {
                case R.id.home_icon_left_row1:
                    AnimUtil.setViewScaleDefault(v);
                    break;
                case R.id.home_icon_left_row2:
                    AnimUtil.setViewScaleDefault(v);
                    break;
                case R.id.home_icon_left_row3:
                    AnimUtil.setViewScaleDefault(v);
                    break;
                case R.id.home_icon_movie:
                    AnimUtil.setViewScaleDefault(v);
                    break;
                case R.id.home_icon_favourites:
                    AnimUtil.setViewScaleDefault(v);
                    break;
                case R.id.home_icon_music:
                    AnimUtil.setViewScaleDefault(v);
                    break;
                case R.id.home_icon_stream:
                    AnimUtil.setViewScaleDefault(v);
                    break;
                case R.id.home_icon_internet:
                    AnimUtil.setViewScaleDefault(v);
                    break;
                case R.id.home_icon_game:
                    AnimUtil.setViewScaleDefault(v);
                    break;
                case R.id.home_icon_photo:
                    AnimUtil.setViewScaleDefault(v);
                    break;
                case R.id.home_icon_social:
                    AnimUtil.setViewScaleDefault(v);
                    break;
                case R.id.home_icon_files:
                    AnimUtil.setViewScaleDefault(v);
                    break;
                case R.id.fragment_home_gridview:
                    if (mOldView != null) {
                        AnimUtil.setViewScaleDefault(mOldView);
                    }
                    isSelect = false;
                    break;
            }
        }
    }

    Runnable run = new Runnable() {

        @Override
        public void run() {
            try {
                Thread.sleep(50);
                handler.sendEmptyMessage(0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };
    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
//            Log.i("bo", "handle get msg");
            if (isSelect) {
                isSelect = false;
            } else {
                // 如果是第一次进入该gridView，则进入第一个item，如果不是第一次进去，则选择上次出来的item
                if (mOldView == null) {
                    mOldView = gridView.getChildAt(0);
                }
                AnimUtil.setViewScale(mOldView, scale);
//                Log.i("bo", "heyheyhey");
            }
        }

        ;
    };


    private View mOldView;
    private boolean isSelect = false;

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//        Log.i("bo", "gridView.isFocused()=" + gridView.isFocused());
        if (view != null && gridView.hasFocus()) {
//            Log.d("bo", view.toString() + " bringToFront");
            view.bringToFront();
            isSelect = true;
            AnimUtil.setViewScale(view, scale);
            if (mOldView != null)
                AnimUtil.setViewScaleDefault(mOldView);
        }
        mOldView = view;

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    private void startCateFragment(String cate) {
        CategoryFragment categoryFragment = ((MainActivity) getActivity()).getCategoryFragment();
        categoryFragment.setCurrentCategory(cate);
        ((MainActivity) getActivity()).changeFragment(categoryFragment);
    }
}
