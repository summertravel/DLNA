package com.zx.zpush;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.teleal.cling.model.meta.Device;

/**
 * Created by kangxiangtao on 2016/5/19.
 */
public class Text extends Activity {

    Device device;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MainActivity mainActivity = (MainActivity)AppManager.getAppManager().getActivity(MainActivity.class);
        device = mainActivity.getDevice();

        DeviceDisplay deviceDisplay = new DeviceDisplay(device);

        Toast.makeText(getApplicationContext(),deviceDisplay.toString()+"==",Toast.LENGTH_SHORT).show();

        Log.e("kk", deviceDisplay.toString());



    }
}
