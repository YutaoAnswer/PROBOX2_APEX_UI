package com.netxeon.newprobox2.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;

import com.netxeon.newprobox2.R;
import com.netxeon.newprobox2.utils.MemoryCleaner;

/**
 * 清理UI
 *  2016/7/2.
 */
public class CleanActivity extends Activity  {

    private int percent = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_clean);

        MemoryCleaner memoryCleaner = new MemoryCleaner(this);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode()==KeyEvent.KEYCODE_BACK){
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
}
