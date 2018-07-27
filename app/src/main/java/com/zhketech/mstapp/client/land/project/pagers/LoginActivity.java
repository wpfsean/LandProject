package com.zhketech.mstapp.client.land.project.pagers;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.zhketech.mstapp.client.land.project.R;
import com.zhketech.mstapp.client.land.project.base.BaseActivity;
import com.zhketech.mstapp.client.land.project.callbacks.BatteryAndWifiService;
import com.zhketech.mstapp.client.land.project.db.DatabaseHelper;
import com.zhketech.mstapp.client.land.project.utils.Logutils;
import com.zhketech.mstapp.client.land.project.utils.CpuAndRam;
import com.zhketech.mstapp.client.land.project.utils.PhoneUtils;
import com.zhketech.mstapp.client.land.project.utils.SharedPreferencesUtils;
import com.zhketech.mstapp.client.land.project.utils.WriteLogToFile;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends BaseActivity {


    //整个项目可能用到的权限
    String[] permissions = new String[]{
            Manifest.permission.USE_SIP,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    //存放未授权的权限
    List<String> mPermissionList = new ArrayList<>();


    @BindView(R.id.login_progressBar)
    ProgressBar loginPr;

    @Override
    public int intiLayout() {
        return R.layout.activity_login;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        verifyPermissions();

    }

    @Override
    public void initData() {
        String nativeIP = PhoneUtils.displayIpAddress(LoginActivity.this);
        if (!TextUtils.isEmpty(nativeIP)) {
            SharedPreferencesUtils.putObject(LoginActivity.this, "nativeIp", nativeIP);
        } else {
            SharedPreferencesUtils.putObject(LoginActivity.this, "nativeIp", "127.0.0.1");
        }
        startService(new Intent(this, BatteryAndWifiService.class));

    }

    @OnClick({R.id.userlogin_button_layout, R.id.userlogin_button_cancel_layout})
    public void loginOrCancel(View view) {
        switch (view.getId()) {
            case R.id.userlogin_button_layout:
                LoginToCMS();
                break;
            case R.id.userlogin_button_cancel_layout:
                LoginCancel();
                break;
        }
    }

    private void LoginCancel() {


//        DatabaseHelper databaseHelper = new DatabaseHelper(LoginActivity.this);
//        Cursor c= databaseHelper.searchAllData("receiveralarm");
//        while (c.moveToNext()){
//            String time = c.getString(1);
//            String flage = c.getString(2);
//            String data = c.getString(3);
//
//            Logutils.i("time:"+time+"\nflage:"+flage+"\ndata:"+data);
//        }

//        int a = (int) SharedPreferencesUtils.getObject(LoginActivity.this, "battery", 0);
//        int b = (int) SharedPreferencesUtils.getObject(LoginActivity.this, "wifi", 0);
//        Logutils.i(a + "\n" + b);
//        // LoginActivity.this.finish();
//
//        Logutils.i(a + "\n" + b);
//

        CpuAndRam.getInstance().init(getApplicationContext(), 5 * 1000L);
        CpuAndRam.getInstance().start();


//        final String name = userName.getText().toString().trim();
//        final String pass = userPwd.getText().toString().trim();
//        final String server_IP = serverIp.getText().toString().trim();
//        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(pass) && !TextUtils.isEmpty(server_IP)) {
//
//            LoginParameters loginParameters = new LoginParameters();
//            loginParameters.setUsername(name);
//            loginParameters.setPass(pass);
//            loginParameters.setServer_ip(server_IP);
//            String localIp = (String) SharedPreferencesUtils.getObject(LoginPager.this, "nativeIp", "");
//            if (TextUtils.isEmpty(localIp)) {
//                Logutils.e("Local_Ip is empty !!!");
//                return;
//            }
//            loginParameters.setNative_ip(localIp);
//            LoginIntoServerThread loginThread = new LoginIntoServerThread(LoginPager.this, loginParameters, new LoginIntoServerThread.IsLoginListern() {
//                @Override
//                public void loginStatus(String status) {
//                    if (!TextUtils.isEmpty(status)) {
//
//                        String result = status;
//                        Logutils.i(result);
//                        if (status.equals("success")) {
        try {
            Thread.sleep(2 * 1000);
            openActivityAndCloseThis(MainActivity.class);
        } catch (InterruptedException e) {
            Logutils.e("Execption:" + e.getMessage());
        }
//                        } else if (status.contains("fail")) {
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    toastShort("Login Filed !!\n Please enter the correct informatiom !!!");
//                                }
//                            });
//                        }
//                    }
//                }
//            });
//            loginThread.start();
//        } else {
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    Toast.makeText(LoginPager.this, "Can not be empty !!!", Toast.LENGTH_SHORT).show();
//                }
//            });
//        }


    }


    private void LoginToCMS() {


        getLocation();
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        LoginActivity.this.finish();

        WriteLogToFile.info("成功登录：" + new Date().toString());

    }

    /**
     * 权限申请
     */
    private void verifyPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermission();
        } else {
            initThisPageData();
        }
    }

    private void initThisPageData() {
        Log.i("TAG", "can't request permission !!!");
    }

    /**
     * 申请权限
     */
    private void requestPermission() {
        mPermissionList.clear();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(LoginActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permission);
            }
        } /** * 判断存储委授予权限的集合是否为空 */
        if (!mPermissionList.isEmpty()) {
            String[] permissions = mPermissionList.toArray(new String[mPermissionList.size()]);//将List转为数组
            ActivityCompat.requestPermissions(LoginActivity.this, permissions, 1);
        } else {
            //未授予的权限为空，表示都授予了 // 后续操作...
            Log.i("TAG", "权限已全部给予");
            initThisPageData();
        }
    }

    boolean mShowRequestPermission = true;//用户是否禁止权限

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        //判断是否勾选禁止后不再询问
                        boolean showRequestPermission = ActivityCompat.shouldShowRequestPermissionRationale(LoginActivity.this, permissions[i]);
                        if (showRequestPermission) {//
                            requestPermission();//重新申请权限
                            return;
                        } else {
                            mShowRequestPermission = false;//已经禁止
                            String permisson = permissions[i];
                            android.util.Log.w("TAG", "permisson:" + permisson);
                        }
                    }
                }
                initThisPageData();
                break;
            default:
                break;
        }
    }


    public void openPermissionPager(View view) {
        String sdk = android.os.Build.VERSION.SDK; // SDK号
        String model = android.os.Build.MODEL; // 手机型号
        String release = android.os.Build.VERSION.RELEASE; // android系统版本号
        String brand = Build.BRAND;//手机厂商

        Log.i("TAG", "");
    }


    @SuppressLint("MissingPermission")
    public void getLocation() {
        LocationManager mLocationManager = (LocationManager) getSystemService(getApplicationContext().LOCATION_SERVICE);

        if (mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5 * 60 * 1000, 1, mLocationListener);
        }

    }

    LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {

            double lat = location.getLatitude();
            double log = location.getLongitude();
            Logutils.i("Lat:" + lat + "\n" + log);
            SharedPreferencesUtils.putObject(LoginActivity.this, "lat", lat);
            SharedPreferencesUtils.putObject(LoginActivity.this, "long", log);

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };


}
