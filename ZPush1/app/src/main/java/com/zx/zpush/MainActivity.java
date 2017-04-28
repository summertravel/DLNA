package com.zx.zpush;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.teleal.cling.Main;
import org.teleal.cling.android.AndroidUpnpService;
import org.teleal.cling.controlpoint.ActionCallback;
import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.meta.LocalDevice;
import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.model.types.ServiceId;
import org.teleal.cling.model.types.UDAServiceId;
import org.teleal.cling.model.types.UDAServiceType;
import org.teleal.cling.registry.DefaultRegistryListener;
import org.teleal.cling.registry.Registry;
import org.teleal.cling.registry.RegistryListener;
import org.teleal.cling.support.avtransport.callback.GetPositionInfo;
import org.teleal.cling.support.avtransport.callback.Pause;
import org.teleal.cling.support.avtransport.callback.Play;
import org.teleal.cling.support.avtransport.callback.Seek;
import org.teleal.cling.support.avtransport.callback.SetAVTransportURI;
import org.teleal.cling.support.connectionmanager.callback.GetProtocolInfo;
import org.teleal.cling.support.connectionmanager.callback.PrepareForConnection;
import org.teleal.cling.support.model.PositionInfo;
import org.teleal.cling.support.model.ProtocolInfos;
import org.teleal.cling.support.renderingcontrol.callback.GetMute;
import org.teleal.cling.support.renderingcontrol.callback.GetVolume;
import org.teleal.cling.support.renderingcontrol.callback.SetVolume;

import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends Activity {

    private String s = "AVTransport";
    private String s1 = "ConnectionManager";
    private String ACTION_SEEKBAR = "seekbarupdate";
    private String VOLUME = "RenderingControl";
    int l;
    private static Device device;
    private static final int GETVOLUME = 1;
    private static final int GETPOSITION = 2;
    private String TAG = "kk";
    Timer timer;
    String realTime;
    String allTime;
//	ProtocolInfo sink;

    /**
     * 接收读取到视频的实时信息
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case GETVOLUME:
                    l = msg.getData().getInt("currentVolume");
                    break;
                case GETPOSITION:
                    String real = msg.getData().getString("realTime");
                    String all = msg.getData().getString("allTime");
                    tv_start.setText(real);
                    tv_end.setText(all);
                    int i = Utils.getRealTime(real);
                    int j = Utils.getRealTime(all);
                    seekBar.setMax(j);
                    seekBar.setProgress(i);


            }

        }
    };

    private Dialog listdialog;
    private Button btn, play, stop, read, add_voc, cut_voc, mute, forword, backword, getCurrentVolume;
    private TextView tv_start, tv_end;
    private SeekBar seekBar;
    private ListView devicelist;
    private ArrayAdapter<DeviceDisplay> listAdapter;


    private AndroidUpnpService upnpService;

    private RegistryListener registryListener = new BrowseRegistryListener();

    private ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {

            upnpService = (AndroidUpnpService) service;

            // Refresh the list with all known devices
            listAdapter.clear();
            for (Device device : upnpService.getRegistry().getDevices()) {
                ((BrowseRegistryListener) registryListener).deviceAdded(device);
            }
            // Getting ready for future device advertisements
            upnpService.getRegistry().addListener(registryListener);
            // Search asynchronously for all devices
            upnpService.getControlPoint().search();
        }

        public void onServiceDisconnected(ComponentName className) {
            upnpService = null;
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);


        btn = (Button) findViewById(R.id.start_btn);
        play = (Button) findViewById(R.id.play);
        stop = (Button) findViewById(R.id.stop);
        read = (Button) findViewById(R.id.read);
        add_voc = (Button) findViewById(R.id.add_voc);
        cut_voc = (Button) findViewById(R.id.cut_voc);
        mute = (Button) findViewById(R.id.mute);
        forword = (Button) findViewById(R.id.forward);
        backword = (Button) findViewById(R.id.backword);
        tv_start = (TextView) findViewById(R.id.tv_start);
        tv_end = (TextView) findViewById(R.id.tv_end);
        seekBar = (SeekBar) findViewById(R.id.seekbar);
        getCurrentVolume = (Button) findViewById(R.id.getCurrentVolume);

        seekBar.setOnSeekBarChangeListener(new PlaySeekBarListener());

        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (upnpService != null) {
                    upnpService.getRegistry().removeAllRemoteDevices();
                    upnpService.getControlPoint().search();
                }
                showDialog();
            }
        });

        getCurrentVolume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCurrentVol();
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listAdapter != null) {
                    Service localservice = device.findService(new UDAServiceType(s));
                    if (localservice != null) {
                        upnpService.getControlPoint().execute(new Pause(localservice) {
                            @Override
                            public void failure(ActionInvocation actionInvocation, UpnpResponse upnpResponse, String s) {
                            }
                        });
                    }
                }
            }
        });

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listAdapter != null) {
                    Service localservice = device.findService(new UDAServiceType(s));
                    if (localservice != null) {
                        upnpService.getControlPoint().execute(new PalyCallBack(localservice));
                    }
                }
            }
        });

        cut_voc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cutVolume();
            }
        });

        add_voc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addVolume();
            }
        });

        mute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMute();
            }
        });

        forword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int t1 = Utils.getRealTime(realTime);
                int t2 = Utils.getRealTime(allTime);
                if (t1 + 15 >= t2) {
                    stepForword(Utils.secToTime(t1));
                } else {
                    stepForword(Utils.secToTime(t1 + 15));
                }
            }
        });
        backword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int t1 = Utils.getRealTime(realTime);
                if (t1 - 15 >= 0) {

                    stepForword(Utils.secToTime(t1 - 15));
                } else {
                    stepForword(Utils.secToTime(t1));
                }
            }
        });

    }





    class PlaySeekBarListener implements SeekBar.OnSeekBarChangeListener {

        PlaySeekBarListener() {

        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            String str = Utils.secToTime(seekBar.getProgress());
            stepForword(str);
        }
    }

    /**
     * 快进
     */
    public void stepForword(String string) {
        if (listAdapter != null) {

            Service localservice = device.findService(new UDAServiceType(s));
            if (localservice != null) {
                upnpService.getControlPoint().execute(new Seek(localservice, string) {
                    @Override
                    public void failure(ActionInvocation actionInvocation, UpnpResponse upnpResponse, String s) {
                    }

                    @Override
                    public void success(ActionInvocation invocation) {
                        super.success(invocation);
                        Log.e("kk", "快进  success");
                        getPosition();
                    }
                });

            }
        }
    }

    /**
     * 设置静音
     */
    public void setMute() {
        if (listAdapter != null) {
            Service localservice = device.findService(new UDAServiceType("RenderingControl"));
            if (localservice != null) {
                upnpService.getControlPoint().execute(new GetMute(localservice) {
                    @Override
                    public void received(ActionInvocation actionInvocation, boolean currentMute) {

                        Log.e("kk", "currentMute====" + currentMute);
                        if (currentMute) {
                            Service service = device.findService(new UDAServiceType("RenderingControl"));
                            upnpService.getControlPoint().execute(new SetVolume(service, 0) {
                                @Override
                                public void failure(ActionInvocation actionInvocation, UpnpResponse upnpResponse, String s) {

                                }

                                @Override
                                public void success(ActionInvocation invocation) {
                                    super.success(invocation);
                                    Log.e(TAG, "setVolume0 success");
                                }
                            });
                        }
                    }

                    @Override
                    public void failure(ActionInvocation actionInvocation, UpnpResponse upnpResponse, String s) {
                    }
                });
            }
        }
    }

    /**
     * 读取视频实时信息
     */
    public void getPosition() {
        if (listAdapter != null) {
            Service localservice = device.findService(new UDAServiceType(s));
            if (localservice != null) {
                upnpService.getControlPoint().execute(new GetPositionInfo(localservice) {
                    @Override
                    public void received(ActionInvocation invocation, PositionInfo positionInfo) {
                        Log.e(TAG, positionInfo.toString());

                        Message message = Message.obtain();
                        message.what = GETPOSITION;
                        Bundle bundle = new Bundle();
                        realTime = positionInfo.getRelTime();
                        allTime = positionInfo.getTrackDuration();
                        bundle.putString("realTime", realTime);
                        bundle.putString("allTime", allTime);
                        message.setData(bundle);
                        handler.sendMessage(message);
                    }

                    @Override
                    public void failure(ActionInvocation actionInvocation, UpnpResponse upnpResponse, String s) {

                    }
                });
            }
        }
    }

    /**
     * 获得当前音量
     */
    private void getCurrentVol() {
        Service localservice = device.findService(new UDAServiceType("RenderingControl"));
        if (localservice != null) {
            upnpService.getControlPoint().execute(new GetVolume(localservice) {
                @Override
                public void received(ActionInvocation actionInvocation, int currentVolume) {
                    Log.e(TAG, "当前的音量" + currentVolume);
                    Message message = Message.obtain();
                    message.what = GETVOLUME;
                    Bundle bundle = new Bundle();
                    bundle.putInt("currentVolume", currentVolume);
                    message.setData(bundle);
                    handler.sendMessage(message);

                }

                @Override
                public void failure(ActionInvocation actionInvocation, UpnpResponse upnpResponse, String s) {

                }
            });
        }
    }

    /**
     * 减音量
     */
    public void cutVolume() {
        if (listAdapter != null) {
            Service localservice = device.findService(new UDAServiceType(VOLUME));
            if (localservice != null) {
                if (l >= 0) {
                    l -= 6;
                    if (l < 0) {
                        l = 0;
                    }
                }
                upnpService.getControlPoint().execute(new SetVolume(localservice, l) {
                    @Override
                    public void failure(ActionInvocation actionInvocation, UpnpResponse upnpResponse, String s) {
                    }
                });
            }
        }
    }

    /**
     * 加音量
     */
    public void addVolume() {
        if (listAdapter != null) {
            Service localservice = device.findService(new UDAServiceType(VOLUME));
            l += 6;
            if (l > 100) {
                l = 100;
            }
            if (localservice != null) {
                upnpService.getControlPoint().execute(new SetVolume(localservice, l) {
                    @Override
                    public void failure(ActionInvocation actionInvocation, UpnpResponse upnpResponse, String s) {
                    }
                });
            }
        }
    }

    public void showDialog() {
        Builder builder = new Builder(this);
        builder.setTitle("可选择设备……");
        final LayoutInflater inflater = LayoutInflater.from(this);
        View v = inflater.inflate(R.layout.listview, null);

        getApplicationContext().bindService(
                new Intent(this, MyUpnpService.class),
                serviceConnection,
                Context.BIND_AUTO_CREATE
        );
        devicelist = (ListView) v.findViewById(R.id.devicelist);
        listAdapter = new ArrayAdapter<DeviceDisplay>(this, android.R.layout.simple_list_item_1);
        devicelist.setAdapter(listAdapter);

        builder.setView(v);
        builder.setNegativeButton("取消", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton("搜索", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
//				if (upnpService != null) {
//			        upnpService.getRegistry().removeAllRemoteDevices();
//			        upnpService.getControlPoint().search();
//			    }
            }
        });
        listdialog = builder.create();
        listdialog.show();

        devicelist.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Toast.makeText(getApplicationContext(), "即将转到第" + position + "项播放", Toast.LENGTH_SHORT).show();
                DeviceDisplay devicePlay = listAdapter.getItem(position);
                device = devicePlay.getDevice();
//	    		String url="http://stream2.ahtv.cn/ahws/cd/live.m3u8";
                String url = "http://10.0.1.43/wovideo/tra.mp4";
                Uri.parse(url);
                Log.e("URL", url);
                GetInfo(device);
                executeAVTransportURI(device, url);
                executePlay(device);
                listdialog.dismiss();

                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        getPosition();
                    }
                }, 1000, 1000);


                startActivity(new Intent(MainActivity.this,Text.class));
            }
        });
    }


    protected void onDestroy() {
        super.onDestroy();
        if (upnpService != null) {
            upnpService.getRegistry().removeListener(registryListener);
        }
//		getApplicationContext().unbindService(serviceConnection);
        if (timer != null) {
            timer.cancel();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, R.string.search)
                .setIcon(android.R.drawable.ic_menu_search);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 0 && upnpService != null) {
            upnpService.getRegistry().removeAllRemoteDevices();
            upnpService.getControlPoint().search();
        }
        return false;
    }


    class BrowseRegistryListener extends DefaultRegistryListener {

        @Override
        public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) {
            deviceAdded(device);
        }

        @Override
        public void remoteDeviceDiscoveryFailed(Registry registry, final RemoteDevice device, final Exception ex) {

            deviceRemoved(device);
        }

        @Override
        public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
            deviceAdded(device);
        }

        @Override
        public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
            deviceRemoved(device);
        }

        @Override
        public void localDeviceAdded(Registry registry, LocalDevice device) {
            deviceAdded(device);
        }

        @Override
        public void localDeviceRemoved(Registry registry, LocalDevice device) {
            deviceRemoved(device);
        }

        public void deviceAdded(final Device device) {
            runOnUiThread(new Runnable() {
                public void run() {
                    if (device.isFullyHydrated()) {

                        DeviceDisplay d = new DeviceDisplay(device);
                        int position = listAdapter.getPosition(d);
                        if (position >= 0) {
                            // Device already in the list, re-set new value at same position
                            listAdapter.remove(d);
                            listAdapter.insert(d, position);
                        } else {
                            listAdapter.add(d);
                        }
//	                listAdapter.sort(DISPLAY_COMPARATOR);
                        listAdapter.notifyDataSetChanged();
                    }
                }
            });
        }

        public void deviceRemoved(final Device device) {
            runOnUiThread(new Runnable() {
                public void run() {
                    listAdapter.remove(new DeviceDisplay(device));
                }
            });
        }
    }

    public void executeAVTransportURI(Device device, String uri) {

        ServiceId AVTransportId = new UDAServiceId(s);
        Service service = device.findService(AVTransportId);
        ActionCallback callback = new SetAVTransportURI(service, uri) {

            @Override
            public void failure(ActionInvocation arg0, UpnpResponse arg1,
                                String arg2) {
                // TODO Auto-generated method stub
                Log.e("SetAVTransportURI", "failed^^^^^^^");
            }

        };
        upnpService.getControlPoint().execute(callback);

    }

    public void executePlay(Device device) {
        ServiceId AVTransportId = new UDAServiceId(s);
        Service service = device.findService(AVTransportId);
        ActionCallback playcallback = new Play(service) {

            @Override
            public void failure(ActionInvocation arg0, UpnpResponse arg1,
                                String arg2) {
                // TODO Auto-generated method stub
                Log.e("Play", "failed^^^^^^^");
            }

        };
        upnpService.getControlPoint().execute(playcallback);

    }

    public void GetInfo(Device device) {
        ServiceId AVTransportId = new UDAServiceId(s1);
        Service service = device.findService(AVTransportId);
        ActionCallback getInfocallback = new GetProtocolInfo(service) {

            @Override
            public void received(ActionInvocation actionInvocation,
                                 ProtocolInfos sinkProtocolInfos,
                                 ProtocolInfos sourceProtocolInfos) {
                // TODO Auto-generated method stub
                Log.v("sinkProtocolInfos", sinkProtocolInfos.toString());
                Log.v("sourceProtocolInfos", sourceProtocolInfos.toString());
            }

            @Override
            public void failure(ActionInvocation arg0, UpnpResponse arg1,
                                String arg2) {
                // TODO Auto-generated method stub
                Log.v("GetProtocolInfo", "failed^^^^^^^");
            }

        };
        upnpService.getControlPoint().execute(getInfocallback);
    }

    public void PrepareConn(Device device) {
        ServiceId AVTransportId = new UDAServiceId(s1);
        Service service = device.findService(AVTransportId);
        ActionCallback prepareConncallback = new PrepareForConnection(service, null, null, -1, null) {

            @Override
            public void received(ActionInvocation invocation, int connectionID,
                                 int rcsID, int avTransportID) {
                // TODO Auto-generated method stub
                Log.v("avTransportID", Integer.toString(avTransportID));
            }

            @Override
            public void failure(ActionInvocation arg0, UpnpResponse arg1,
                                String arg2) {
                // TODO Auto-generated method stub
                Log.v("PrepareForConnection", "failed^^^^^^^");
            }

        };
        upnpService.getControlPoint().execute(prepareConncallback);
    }

    public static Device getDevice() {
        return device;
    }


}
