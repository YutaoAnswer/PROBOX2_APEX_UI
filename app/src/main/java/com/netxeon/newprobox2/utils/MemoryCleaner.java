package com.netxeon.newprobox2.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.netxeon.newprobox2.R;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class MemoryCleaner {

    private Activity mActivity;
    private MySinkingView mySinkingView;

    private int percent;
    private boolean isUpFinish = false;

    private static final int cleanFinish = 0;
    private static final int cleanStart = 1;
    private static final int upFinish = 2;
    private static final int downFinish = 3;
    private static final int delayTime = 1000;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case cleanFinish://清理完成
                    downUi();
                    break;
                case cleanStart:
                    updateMemory();
                    cleanMemory();
                    break;
                case upFinish://动画上去完成
                    isUpFinish = true;
                    break;
                case downFinish:
                    Toast.makeText(mActivity, R.string.clear_memory_done, Toast.LENGTH_LONG).show();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mActivity.finish();
                        }
                    }, delayTime);
                    break;
            }
        }
    };

    public MemoryCleaner(Activity activity) {
        mActivity = activity;
        initViews();
        handler.sendEmptyMessage(cleanStart);
    }

    private void initViews() {
        mySinkingView = mActivity.findViewById(R.id.sinking);
    }

    private void downUi() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (isUpFinish) {
                        long totalMemory = getTotalMemory();
                        long available = getAvailMemory();

                        float result = (float) (totalMemory - available) / (float) (totalMemory);

//                        Log.i("bo", "tatal=" + tatal + "availale=" + available + "result=" + result);

                        if (percent == 100) {
                            //              Log.i("bo", "100percent:" + percent);
                            while (percent >= (result * 100)) {
                                //                  Log.i("bo", percent + "result*100");
                                mySinkingView.setPercent((float) percent / 100);
                                percent -= 1;
                                try {
                                    Thread.sleep(20);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        break;
                    } else {
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                handler.sendEmptyMessage(downFinish);
            }
        }).start();
    }

    private void updateMemory() {
        long total = getTotalMemory();
        long available = getAvailMemory();

        float result = (float) (total - available) / (float) (total);

//        Log.i("bo", "tatal=" + tatal + "availale=" + available + "result=" + result);
        if (mySinkingView != null) {
            mySinkingView.setPercent(result);
        }
        percent = (int) result * 100;
        loadUI();
    }

    /**
     * 向上动画
     */
    private void loadUI() {
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {

                while (percent < 100) {
                    //       Log.i("bo", percent + "");
                    percent += 1;
                    mySinkingView.setPercent((float) percent / 100);
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
                handler.sendEmptyMessage(upFinish);
            }
        });
        thread.start();
    }

    private void cleanMemory() {
        new Thread() {
            @Override
            public void run() {
                ActivityManager activityManger = (ActivityManager) mActivity.getSystemService(Context.ACTIVITY_SERVICE);
                List<ActivityManager.RunningAppProcessInfo> list = activityManger.getRunningAppProcesses();
                if (list != null) {
                    for (int i = 0; i < list.size(); i++) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        ActivityManager.RunningAppProcessInfo apinfo = list.get(i);
                        String[] pkgList = apinfo.pkgList;
                        if (apinfo.importance > ActivityManager.RunningAppProcessInfo.IMPORTANCE_SERVICE) {
                            for (String s : pkgList) {
                                activityManger.killBackgroundProcesses(s);
                            }
                        }
                    }
                }
                handler.sendEmptyMessage(cleanFinish);
            }
        }.start();
    }


    /**
     * 总可用空间
     *
     * @return 总可用空间
     */
    private long getTotalMemory() {
        String str1 = "/proc/meminfo";
        String str2;
        String[] arrayOfString;
        long initial_memory = 0;

        try {
            FileReader localFileReader = new FileReader(str1);
            BufferedReader localBufferedReader = new BufferedReader(localFileReader, 8192);
            str2 = localBufferedReader.readLine();
            arrayOfString = str2.split("\\s+");
            initial_memory = Integer.valueOf(arrayOfString[1]);
            localBufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return initial_memory / (1024);
    }

    /**
     * 可用空间
     *
     * @return 可用空间量
     */
    private long getAvailMemory() {
        ActivityManager am = (ActivityManager) mActivity.getSystemService(Context.ACTIVITY_SERVICE);
        MemoryInfo mi = new MemoryInfo();
        am.getMemoryInfo(mi);
        return mi.availMem / (1024 * 1024);
    }

}
