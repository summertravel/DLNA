package com.zx.zpush;

import android.util.Log;

import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.model.types.UnsignedIntegerFourBytes;
import org.teleal.cling.support.avtransport.callback.Play;

/**
 * Created by kangxiangtao on 2016/5/10.
 */
public class PalyCallBack extends Play {

    public PalyCallBack(Service service) {
        super(service);
    }


    @Override
    public void failure(ActionInvocation actionInvocation, UpnpResponse upnpResponse, String s) {
        Log.e("play failure", "play failure");
    }

    @Override
    public void run() {
        super.run();
    }

    @Override
    public void success(ActionInvocation invocation) {
        super.success(invocation);
        Log.e("play success", "play success");
    }
}
