package com.zhketech.mstapp.client.land.project.pagers;

import android.app.AlertDialog;
import android.bluetooth.BluetoothGatt;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.zhketech.mstapp.client.land.project.R;
import com.zhketech.mstapp.client.land.project.base.BaseActivity;
import com.zhketech.mstapp.client.land.project.beans.AlarmBen;
import com.zhketech.mstapp.client.land.project.beans.SipBean;
import com.zhketech.mstapp.client.land.project.beans.VideoBen;
import com.zhketech.mstapp.client.land.project.callbacks.AmmoRequestCallBack;
import com.zhketech.mstapp.client.land.project.callbacks.ReceiveServerMess;
import com.zhketech.mstapp.client.land.project.callbacks.ReceiverServerAlarm;
import com.zhketech.mstapp.client.land.project.callbacks.RequestSipSourcesThread;
import com.zhketech.mstapp.client.land.project.callbacks.RequestVideoSourcesThread;
import com.zhketech.mstapp.client.land.project.callbacks.SendheartService;
import com.zhketech.mstapp.client.land.project.db.DatabaseHelper;
import com.zhketech.mstapp.client.land.project.global.AppConfig;
import com.zhketech.mstapp.client.land.project.onvif.Device;
import com.zhketech.mstapp.client.land.project.onvif.Onvif;
import com.zhketech.mstapp.client.land.project.taking.Linphone;
import com.zhketech.mstapp.client.land.project.taking.PhoneCallback;
import com.zhketech.mstapp.client.land.project.taking.RegistrationCallback;
import com.zhketech.mstapp.client.land.project.taking.SipService;
import com.zhketech.mstapp.client.land.project.utils.GsonUtils;
import com.zhketech.mstapp.client.land.project.utils.Logutils;
import com.zhketech.mstapp.client.land.project.utils.SharedPreferencesUtils;
import com.zhketech.mstapp.client.land.project.utils.ToastUtils;
import com.zhketech.mstapp.client.land.project.utils.WriteLogToFile;

import org.linphone.core.LinphoneCall;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends BaseActivity {
    //handler时间标识
    public static int timeFlage = 10001;

    public static final int FLAGE = 1000;
    boolean threadIsRun = true;

    List<Device> dataSources = new ArrayList<>();
    int num = -1;
    //时间
    @BindView(R.id.main_incon_time)
    TextView timeTextView;
    //日期
    @BindView(R.id.main_icon_date)
    TextView dateTextView;


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //主页面时间显示
            if (msg.what == timeFlage) {
                long time = System.currentTimeMillis();
                Date date = new Date(time);
                SimpleDateFormat timeD = new SimpleDateFormat("HH:mm:ss");
                timeTextView.setText(timeD.format(date).toString());
                SimpleDateFormat dateD = new SimpleDateFormat("MM月dd日 EEE");
                dateTextView.setText(dateD.format(date).toString());
            } else if (msg.what == FLAGE) {
                //onvif数据处理
                Bundle bundle = msg.getData();
                Device device = (Device) bundle.getSerializable("device");
                dataSources.add(device);
                if (dataSources.size() == num) {
                    Logutils.i(dataSources.toString());
                    Log.i("TAG", dataSources.size() + "");
                    Logutils.i("Date:" + new Date().toString());
                    String json = GsonUtils.getGsonInstace().list2String(dataSources);
                    if (TextUtils.isEmpty(json)) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtils.showShort("Json not to String !!! ");
                            }
                        });
                        Logutils.i("为空了");
                        return;
                    }
                    AppConfig.data = json;
                    SharedPreferencesUtils.putObject(MainActivity.this, "result", json);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toastShort("init data fished !!!");
                        }
                    });
                }
            }
        }
    };

    @Override
    public int intiLayout() {
        return R.layout.activity_main;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        BleManager.getInstance()
                .enableLog(false)
                .setReConnectCount(1, 5000)
                .setSplitWriteNum(20)
                .setConnectOverTime(10000)
                .setOperateTimeout(5000);

    }

    @Override
    public void initData() {
        ReceiveServerMess receiveServerMess = new ReceiveServerMess(new ReceiveServerMess.GetSmsListern() {
            @Override
            public void getSmsContent(final String ms) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                        alert.setTitle("短消息:").setMessage(ms).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create().show();
                    }
                });

                DatabaseHelper databaseHelper = new DatabaseHelper(MainActivity.this);
                databaseHelper.insertMessData(new Date().toString(), "true", ms, "receivermess");
                databaseHelper.close();

            }
        });
        new Thread(receiveServerMess).start();

        ReceiverServerAlarm receiverServerAlarm = new ReceiverServerAlarm(new ReceiverServerAlarm.GetAlarmFromServerListern() {
            @Override
            public void getListern(final AlarmBen alarmBen, final String flage) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("报警报文:").setMessage("是否成功接收:" + flage + "\n" + alarmBen.toString()).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create().show();
                    }
                });
                DatabaseHelper databaseHelper = new DatabaseHelper(MainActivity.this);
                databaseHelper.insertMessData(new Date().toString(), flage + "", alarmBen.toString(), "receiveralarm");
                databaseHelper.close();
            }
        });
        new Thread(receiverServerAlarm).start();


        startService(new Intent(this, SendheartService.class));
        TimeThread timeThread = new TimeThread();
        new Thread(timeThread).start();

        getNativeSipInformation();

        getAllVideoResoucesInformation();

    }

    /**
     * 获取所有的video资源并解析rtsp
     */
    private void getAllVideoResoucesInformation() {
        Logutils.i("Date:" + new Date().toString());
        RequestVideoSourcesThread requestVideoSourcesThread = new RequestVideoSourcesThread(MainActivity.this, new RequestVideoSourcesThread.GetDataListener() {
            @Override
            public void getResult(final List<VideoBen> mList) {
                if (mList != null && mList.size() > 0) {
                    num = mList.size();
                    for (int i = 0; i < mList.size(); i++) {
                        String deviceType = mList.get(i).getDevicetype();

                        String ip = mList.get(i).getIp();
                        final Device device = new Device();
                        device.setVideoBen(mList.get(i));
                        device.setServiceUrl("http://" + ip + "/onvif/device_service");
                        Onvif onvif = new Onvif(device, new Onvif.GetRtspCallback() {
                            @Override
                            public void getDeviceInfoResult(String rtsp, boolean isSuccess, Device mDevice) {
                                Logutils.i("isdddddddd:" + isSuccess);
                                Message message = new Message();
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("device", mDevice);
                                message.setData(bundle);
                                message.what = FLAGE;
                                handler.sendMessage(message);
                            }
                        });
                        new Thread(onvif).start();
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtils.showShort("No get Video Resources Data !!!");
                            WriteLogToFile.info("No get Video Resources Data !!!");
                        }
                    });
                }
            }
        });
        requestVideoSourcesThread.start();
    }

    /**
     * 获取本机的sip信息
     */
    private void getNativeSipInformation() {
        RequestSipSourcesThread sipThread = new RequestSipSourcesThread(MainActivity.this, "0", new RequestSipSourcesThread.SipListern() {
            @Override
            public void getDataListern(List<SipBean> mList) {
                String nativeIp = (String) SharedPreferencesUtils.getObject(MainActivity.this, "nativeIp", "");
                if (mList != null && mList.size() > 0) {
                    for (SipBean s : mList) {
                        if (s.getIp().equals(nativeIp)) {
                            String sipName = s.getName();
                            String sipNum = s.getNumber();
                            String sipPwd = s.getSippass();
                            String sipServer = s.getSipserver();
                            if (!TextUtils.isEmpty(sipNum) && !TextUtils.isEmpty(sipPwd) && !TextUtils.isEmpty(sipServer)) {
                                SharedPreferencesUtils.putObject(MainActivity.this, "sipName", sipName);
                                SharedPreferencesUtils.putObject(MainActivity.this, "sipNum", sipNum);
                                SharedPreferencesUtils.putObject(MainActivity.this, "sipPwd", sipPwd);
                                SharedPreferencesUtils.putObject(MainActivity.this, "sipServer", sipServer);
                                registerSipIntoServer(sipNum, sipPwd, sipServer);
                            }
                            break;
                        }
                    }
                }
            }
        });
        sipThread.start();
    }

    @OnClick({R.id.button_intercom, R.id.button_video, R.id.button_applyforplay})
    public void onclickEvent(View view) {
        Intent intent = new Intent();
        switch (view.getId()) {
            case R.id.button_intercom:
                intent.setClass(MainActivity.this, SipGroupActivity.class);
                startActivity(intent);
                break;
            case R.id.button_video:
                intent.setClass(MainActivity.this, MutilScreenActivity.class);
                startActivity(intent);
                break;
            case R.id.button_applyforplay:
                applyForUnpacking();
                break;
        }
    }

    /**
     * 开箱申请
     */
    private void applyForUnpacking() {
        AmmoRequestCallBack ammoRequestCallBack = new AmmoRequestCallBack(new AmmoRequestCallBack.GetDataListern() {
            @Override
            public void getDataInformation(final String result) {
                if (!TextUtils.isEmpty(result)){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setTitle("开箱申请:").setMessage("result:\n"+result).setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).create().show();
                        }
                    });
                }
            }
        });
        ammoRequestCallBack.start();
    }


    //显示时间的线程
    class TimeThread extends Thread {
        @Override
        public void run() {
            super.run();
            do {
                try {
                    Thread.sleep(1000);
                    Message msg = new Message();
                    msg.what = timeFlage;
                    handler.sendMessage(msg);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (threadIsRun);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        threadIsRun = false;
    }

    private void registerSipIntoServer(String sipNum, String sipPwd, String sipServer) {

        if (!SipService.isReady()) {
            Linphone.startService(this);
        }
        Linphone.setAccount(sipNum, sipPwd, sipServer);
        Linphone.login();
    }


    @Override
    protected void onResume() {
        super.onResume();

        Linphone.addCallback(new RegistrationCallback() {
            @Override
            public void registrationProgress() {
                Logutils.i("registering");
            }

            @Override
            public void registrationOk() {
                Logutils.i("register ok");
                SharedPreferencesUtils.putObject(MainActivity.this, "registerStatus", true);
            }

            @Override
            public void registrationFailed() {
                Logutils.i("register fail");
                SharedPreferencesUtils.putObject(MainActivity.this, "registerStatus", false);
            }
        }, new PhoneCallback() {
            @Override
            public void incomingCall(LinphoneCall linphoneCall) {
                Logutils.i("来电");
            }

            @Override
            public void outgoingInit() {
                Logutils.i("拨打");
            }

            @Override
            public void callConnected() {
                Logutils.i("接通");
            }

            @Override
            public void callEnd() {
                Logutils.i("结束");
            }

            @Override
            public void callReleased() {
                Logutils.i("释放");
            }

            @Override
            public void error() {
                Logutils.i("出错");
            }
        });
    }
}
