package com.netxeon.newprobox2.broadcast;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.ImageView;

import com.netxeon.newprobox2.R;

import java.util.Objects;

/**
 * 监听蓝牙广播
 */
public class BluetoothChangeReceiver extends BroadcastReceiver {

    ImageView imageView;

    public BluetoothChangeReceiver(ImageView imageView) {
        this.imageView = imageView;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        if (Objects.requireNonNull(intent.getAction()).equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
            String stateExtra = BluetoothAdapter.EXTRA_STATE;
            int state = intent.getIntExtra(stateExtra, -1);

            switch (state) {
                case BluetoothAdapter.STATE_TURNING_ON:
                    break;
                case BluetoothAdapter.STATE_ON:
                    imageView.setImageResource(R.mipmap.bt_on);
                    break;
                case BluetoothAdapter.STATE_TURNING_OFF:
                    break;
                case BluetoothAdapter.STATE_OFF:
                    imageView.setImageResource(R.mipmap.bt_off);
                    break;
            }
        }
    }
}
