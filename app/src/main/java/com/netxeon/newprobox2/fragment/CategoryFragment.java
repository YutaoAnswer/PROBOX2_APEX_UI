package com.netxeon.newprobox2.fragment;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.netxeon.newprobox2.R;
import com.netxeon.newprobox2.activity.EditorActivity;
import com.netxeon.newprobox2.adapter.ShortcutsAdapter;
import com.netxeon.newprobox2.bean.Shortcut;
import com.netxeon.newprobox2.utils.DBHelper;
import com.netxeon.newprobox2.utils.Data;
import com.netxeon.newprobox2.utils.L;
import com.netxeon.newprobox2.utils.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * 分类页
 */
public class CategoryFragment extends Fragment {

    private String mCurrentCategory = null;
    private PackageManager pm;
    private List<Shortcut> mShortcut = new ArrayList<>();
    private ShortcutsAdapter mAdapter;
    private final String ADDITIONAL = "additional";
    private PackageChangedReceiver mUpdateShortcutsReceiver;
    private IntentFilter mIntentFilter;
    private GridView gridview;
    private int columns = 8;
    private boolean isPaused = false;
    private boolean isFirst = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_category, container, false);
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
    public void onDestroy() {
        getActivity().unregisterReceiver(mUpdateShortcutsReceiver);
        super.onDestroy();
    }

    public void setCurrentCategory(String category) {
        mCurrentCategory = category;
    }

    public String getmCurrentCategory() {
        return mCurrentCategory;
    }

    public String getmCurrentCategoryString() {
        String str = "";
        switch (mCurrentCategory) {
            case Data.MOVIE:
                str = getActivity().getString(R.string.icon_label_movie);
                break;
            case Data.FAVOURITES:
                str = getActivity().getString(R.string.icon_label_favourites);
                break;
            case Data.MUSIC:
                str = getActivity().getString(R.string.icon_label_music);
                break;
            case Data.STREAM:
                str = getActivity().getString(R.string.icon_label_stream);
                break;
            case Data.INTERNET:
                str = getActivity().getString(R.string.icon_label_internet);
                break;
            case Data.GAME:
                str = getActivity().getString(R.string.icon_label_game);
                break;
            case Data.PHOTO:
                str = getActivity().getString(R.string.icon_label_photo);
                break;
            case Data.SOCIAL:
                str = getActivity().getString(R.string.icon_label_social);
                break;
        }
        return str;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (hidden) {

        } else {
            mAdapter = new ShortcutsAdapter(getActivity(), mShortcut, pm, true);
            gridview.setAdapter(mAdapter);
            getData();
        }
        super.onHiddenChanged(hidden);
    }

    private void viewInit(View view) {
        gridview = (GridView) view.findViewById(R.id.folder_id_gridview);
    }

    private void setListener() {
        gridview.setOnItemClickListener(new ItemClickListener());
    }

    private void activityInit() {
        L.d("zzy", "cate:" + mCurrentCategory);
        mUpdateShortcutsReceiver = new PackageChangedReceiver();
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(Data.ACTION_UPDATE_SHORTCUTS);
        pm = getActivity().getPackageManager();
        gridview.setNumColumns(columns);
        mAdapter = new ShortcutsAdapter(getActivity(), mShortcut, pm, true);
        gridview.setAdapter(mAdapter);
        getActivity().registerReceiver(mUpdateShortcutsReceiver, mIntentFilter);
    }

    private void getData() {
        if (mCurrentCategory == null) {
            return;
        }
        if (mShortcut == null) {
            mShortcut = new ArrayList<>();
        }
        mShortcut = DBHelper.getInstance(getActivity()).queryByCategory(mCurrentCategory);
        if (isFirst) {
            switch (mCurrentCategory) {
                case Data.MOVIE:
                    Shortcut netflix = new Shortcut();
                    netflix.setComponentName(Data.netflix);
                    mShortcut.add(netflix);
                    Shortcut youtube = new Shortcut();
                    youtube.setComponentName(Data.youtube);
                    mShortcut.add(youtube);
                    isFirst = false;
                    break;
                case Data.MUSIC:
                    
                    isFirst = false;
                    break;
                case Data.INTERNET:

                    isFirst = false;
                    break;
                case Data.PHOTO:

                    isFirst = false;
                    break;
                default:
                    break;
            }
        }
        Shortcut forAddItem = new Shortcut();
        forAddItem.setComponentName(ADDITIONAL);
        mShortcut.add(forAddItem);
        mAdapter.notifyDataSetChanged(mShortcut);
        gridview.requestFocus();
    }

    /**
     * handle the icon click event
     */
    private class ItemClickListener implements AdapterView.OnItemClickListener {
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

            ShortcutsAdapter.ShortcutHolder holder = (ShortcutsAdapter.ShortcutHolder) arg1.getTag();
            ComponentName componentName = holder.componentName;
            if (ADDITIONAL.equals(componentName.getPackageName())) {
                Intent editIntent = new Intent(getActivity(), EditorActivity.class);
                editIntent.putExtra(Util.CATEGORY, mCurrentCategory);
                startActivity(editIntent);
            } else {
                try {
                    Intent mainintent = new Intent(Intent.ACTION_MAIN, null);
                    mainintent.addCategory(Intent.CATEGORY_LAUNCHER);
                    mainintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    mainintent.setComponent(componentName);
                    startActivity(mainintent);
                } catch (Exception e) {
                    L.e("error", "FolderActivity.ItemClickListener.onItemClick() startActivity failed: " + componentName);
                }
            }

        }
    }

    private class PackageChangedReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            getData();
        }

    }

}
