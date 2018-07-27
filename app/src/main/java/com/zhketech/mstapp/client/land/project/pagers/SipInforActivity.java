package com.zhketech.mstapp.client.land.project.pagers;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.zhketech.mstapp.client.land.project.R;
import com.zhketech.mstapp.client.land.project.base.BaseActivity;
import com.zhketech.mstapp.client.land.project.beans.SipBean;
import com.zhketech.mstapp.client.land.project.beans.SipClient;
import com.zhketech.mstapp.client.land.project.callbacks.BatteryAndWifiCallback;
import com.zhketech.mstapp.client.land.project.callbacks.BatteryAndWifiService;
import com.zhketech.mstapp.client.land.project.callbacks.RequestSipSourcesThread;
import com.zhketech.mstapp.client.land.project.global.AppConfig;
import com.zhketech.mstapp.client.land.project.utils.Logutils;
import com.zhketech.mstapp.client.land.project.utils.SharedPreferencesUtils;
import com.zhketech.mstapp.client.land.project.utils.SipHttpUtils;
import com.zhketech.mstapp.client.land.project.utils.ToastUtils;
import com.zhketech.mstapp.client.land.project.utils.WriteLogToFile;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SipInforActivity extends BaseActivity {

    //电量图标
    @BindView(R.id.icon_electritity_show)
    ImageView batteryIcon;
    //网络图标
    @BindView(R.id.icon_network)
    ImageView networkIcon;

    Timer timer = null;
    //gridview
    @BindView(R.id.gridview)
    public GridView gridview;

    Context mContext;
    List<SipClient> mList = new ArrayList<>();
    List<SipBean> sipListResources = new ArrayList<>();
    List<SipClient> adapterList = new ArrayList<>();
    SipInforAdapter ada = null;

    int selected = -1;

    @Override
    public int intiLayout() {
        return R.layout.activity_sip_infor;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        mContext = this;
    }

    @Override
    public void initData() {
        //获取组id
        int groupID = getIntent().getIntExtra("group_id", 0);
        if (groupID != 0) {
            RequestSipSourcesThread requestSipSourcesThread = new RequestSipSourcesThread(mContext, groupID + "", new RequestSipSourcesThread.SipListern() {
                @Override
                public void getDataListern(List<SipBean> sipList) {
                    if (sipList != null && sipList.size() > 0) {
                        sipListResources = sipList;
                        getHttpdata();
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtils.showShort("No get SipGroup infor !!!");
                            }
                        });
                        WriteLogToFile.info("No get SipGroup infor !!!");
                    }
                }
            });
            requestSipSourcesThread.start();
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.showShort("No get GroupID!!!");
                    WriteLogToFile.info("No Get GroupID");
                }
            });
        }

        //定时器每三秒刷新一下数据
         timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                getHttpdata();
            }
        };
        timer.schedule(timerTask, 0, 3000);
    }

    /**
     * 获取sip服务器上的sip信息
     */
    public void getHttpdata() {
        if (mList != null && mList.size() > 0) {
            mList.clear();
        }
        SipHttpUtils sipHttpUtils = new SipHttpUtils(AppConfig.sipServerDataUrl, new SipHttpUtils.GetHttpData() {
            @Override
            public void httpData(String result) {
                if (!TextUtils.isEmpty(result)) {
                    if (!result.contains("Execption") && !result.contains("code != 200")) {
                        try {
                            JSONArray jsonArray = new JSONArray(result);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                String username = jsonObject.getString("usrname");
                                String description = jsonObject.getString("description");
                                String dispname = jsonObject.getString("dispname");
                                String addr = jsonObject.getString("addr");
                                String state = jsonObject.getString("state");
                                String userAgent = jsonObject.getString("userAgent");
                                SipClient sipClient = new SipClient(username, description, dispname, addr, state, userAgent);
                                mList.add(sipClient);
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (adapterList != null && adapterList.size() > 0) {
                                        adapterList.clear();
                                    }
                                    for (int i = 0; i < mList.size(); i++) {
                                        for (int j = 0; j < sipListResources.size(); j++) {
                                            if (mList.get(i).getUsrname().equals(sipListResources.get(j).getNumber())) {
                                                SipClient sipClient = new SipClient();
                                                sipClient.setState(mList.get(i).getState());
                                                sipClient.setUsrname(mList.get(i).getUsrname());
                                                adapterList.add(sipClient);
                                            }
                                        }
                                    }
                                    List<SipClient> dd = adapterList;
                                //    Logutils.i("ss:" + dd.toString());
                                    if (adapterList != null && adapterList.size() > 0) {
                                        if (ada != null) {
                                            ada = null;
                                        }
                                        ada = new SipInforAdapter(mContext);
                                        gridview.setAdapter(ada);
                                        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                            @Override
                                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                ada.setSeclection(position);
                                                ada.notifyDataSetChanged();
                                                Logutils.i("Position:" + position);
                                                selected = position;
                                            }
                                        });
                                    }
                                }
                            });
                        } catch (JSONException e) {
                            WriteLogToFile.info("SIpInfor Json Resolve Error !!!");
                            e.printStackTrace();
                        }
                    }
                } else {
                    //showNoData();
                }
            }
        });
        sipHttpUtils.start();
    }

    /**
     * 展示数据的适配器
     *
     */
    class SipInforAdapter extends BaseAdapter {
        private int clickTemp = -1;
        private LayoutInflater layoutInflater;

        public SipInforAdapter(Context context) {
            layoutInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return adapterList.size();
        }

        @Override
        public Object getItem(int position) {
            return adapterList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public void setSeclection(int position) {
            clickTemp = position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = layoutInflater.inflate(R.layout.sipstatus_item, null);
                viewHolder.item_name = (TextView) convertView.findViewById(R.id.item_name);
                viewHolder.linearLayout = (LinearLayout) convertView.findViewById(R.id.item_layout);
                viewHolder.main_layout = convertView.findViewById(R.id.sipstatus_main_layout);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            String native_name = (String) SharedPreferencesUtils.getObject(SipInforActivity.this, "sipNum", "");

            if (!TextUtils.isEmpty(native_name)) {
                if (adapterList.get(position).getUsrname().equals(native_name)) {
                    viewHolder.item_name.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG); //中间横线
                    viewHolder.item_name.setTextColor(0xffDC143C);
                }
            }
            if (adapterList.get(position).getState().equals("0")) {
                viewHolder.item_name.setText("哨位名:" + adapterList.get(position).getUsrname());
                viewHolder.linearLayout.setBackgroundResource(R.mipmap.btn_lixian);
            } else if (adapterList.get(position).getState().equals("1")) {
                viewHolder.item_name.setText("哨位名:" + adapterList.get(position).getUsrname());
                viewHolder.linearLayout.setBackgroundResource(R.drawable.sip_call_select_bg);
            } else if (adapterList.get(position).getState().equals("2")) {
                viewHolder.item_name.setText("哨位名:" + adapterList.get(position).getUsrname());
                viewHolder.linearLayout.setBackgroundResource(R.mipmap.btn_zhenling);
            } else if (adapterList.get(position).getState().equals("3")) {
                viewHolder.item_name.setText("哨位名:" + adapterList.get(position).getUsrname());
                viewHolder.linearLayout.setBackgroundResource(R.mipmap.btn_tonghua);
            }

            if (clickTemp == position) {
                if (adapterList.get(position).getState().equals("1") && !adapterList.get(position).getUsrname().equals(native_name))
                    viewHolder.main_layout.setBackgroundResource(R.drawable.sip_selected_bg);
            } else {
                viewHolder.main_layout.setBackgroundColor(Color.TRANSPARENT);
            }
            return convertView;
        }

        class ViewHolder {
            TextView item_name;
            LinearLayout main_layout;
            LinearLayout linearLayout;
        }
    }


    @OnClick({R.id.voice_intercom_icon_layout,R.id.video_intercom_layout,R.id.sip_group_lastpage_layout,R.id.sip_group_nextpage_layout,R.id.sip_group_refresh_layout,R.id.sip_group_back_layout})
    public void onclickEvent(View view){
        Intent intent = new Intent();
        switch (view.getId()){
            case R.id.voice_intercom_icon_layout:

                if (adapterList != null && adapterList.size() > 0) {
                    intent.putExtra("isCall", true);
                    if (selected != -1) {
                        if (adapterList.get(selected) != null) {
                            intent.setClass(SipInforActivity.this,SingleCallActivity.class);
                            intent.putExtra("userName", adapterList.get(selected).getUsrname());
                        }
                    }
                }
                startActivity(intent);
                break;
            case R.id.video_intercom_layout:
                if (adapterList != null && adapterList.size() > 0) {
                    intent.putExtra("isCall", true);
                    if (selected != -1) {
                        if (adapterList.get(selected) != null) {
                            intent.setClass(SipInforActivity.this,SingleCallActivity.class);
                            intent.putExtra("userName", adapterList.get(selected).getUsrname());
                            intent.putExtra("isVideo", true);
                            startActivity(intent);
                        }
                    }
                }
                break;
            case R.id.sip_group_lastpage_layout:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        toastShort("无数据");
                    }
                });
                break;
            case R.id.sip_group_nextpage_layout:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        toastShort("无数据");
                    }
                });
                break;
            case R.id.sip_group_refresh_layout:
                getHttpdata();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        toastShort("已更新");
                    }
                });
                break;
            case R.id.sip_group_back_layout:
                timer.cancel();
                timer = null;
                SipInforActivity.this.finish();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        BatteryAndWifiService.addBatterCallback(new BatteryAndWifiCallback() {
            @Override
            public void getBatteryData(final int level) {

                if (level >= 75 && level <= 100) {
                    updateUi(batteryIcon, R.mipmap.icon_electricity_a);
                }
                if (level >= 50 && level < 75) {
                    updateUi(batteryIcon, R.mipmap.icon_electricity_b);
                }
                if (level >= 25 && level < 50) {
                    updateUi(batteryIcon, R.mipmap.icon_electricity_c);
                }
                if (level >= 0 && level < 25) {
                    updateUi(batteryIcon, R.mipmap.icon_electricity_disable);
                }

            }

            @Override
            public void getWifiData(int rssi) {
                if (rssi > -50 && rssi < 0) {
                    updateUi(networkIcon, R.mipmap.icon_network);
                } else if (rssi > -70 && rssi <= -50) {
                    updateUi(networkIcon, R.mipmap.icon_network_a);
                } else if (rssi < -70) {
                    updateUi(networkIcon, R.mipmap.icon_network_b);
                } else if (rssi == -200) {
                    updateUi(networkIcon, R.mipmap.icon_network_disable);
                }
            }
        });
    }
    /**
     * 更新UI
     */
    public void updateUi(final ImageView imageView, final int n) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imageView.setBackgroundResource(n);
            }
        });
    }
}
