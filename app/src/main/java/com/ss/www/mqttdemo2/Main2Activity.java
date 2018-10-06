package com.ss.www.mqttdemo2;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;
import android.widget.Toast;

import com.ss.www.mqttdemo2.Bean.MyMessage;
import com.ss.www.mqttdemo2.adapter.ImeiAdpter;
import com.ss.www.mqttdemo2.adapter.MainAdapter;
import com.ss.www.mqttdemo2.utils.JsonUtil;
import com.ss.www.mqttdemo2.utils.LogUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Main2Activity extends AppCompatActivity {
    private String TAG = getClass().getSimpleName();
    private final static int RECEIVE_DATA2 = 1;
    public static final String IMEI_NO = "imei_no";
    public static final String INFORMATION = "information";
    private List<MyMessage> mList;
    private List<MyMessage>show_mlist;
    private String str_imei;
    private RecyclerView rv;
    private MainAdapter adapter;
    private Toolbar mToolbar;
    private MQTTService mqttService;
    private IntentFilter intentFilter;
    private BroadcastReceiver receiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
            if (MQTTService.GET_MESSAGE.equals(action)){
                String str = intent.getStringExtra(MQTTService.MESSAGE);
                String s = str.substring(str.indexOf("[{"),str.indexOf("}]")+2);
                Message msg = Message.obtain();
                msg.what = RECEIVE_DATA2;
                msg.obj = s;
                mHandler.sendMessage(msg);
            }
            if (MQTTService.CONNECTED.equals(action)){
                LogUtil.i(TAG,"main---连接成功");
                setMessage("连接成功");
            }
            if (MQTTService.CONNECT_LOST.equals(action)){
                setMessage("连接断开");
            }
        }
    };
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case RECEIVE_DATA2 :
                    LogUtil.i(TAG,"main2收到数据");
                    String s = (String) msg.obj;
                    //initJSON(s);
                    getInformation(s);
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initData();
        initView();
        registerBroadcast();//注册广播

    }
    private void registerBroadcast(){
        intentFilter = new IntentFilter();
        intentFilter.addAction(MQTTService.GET_MESSAGE);
        intentFilter.addAction(MQTTService.CONNECTED);
        intentFilter.addAction(MQTTService.CONNECT_LOST);
    }
    private void initView() {
        rv = (RecyclerView) findViewById(R.id.main_recycler);
        mToolbar = (Toolbar) findViewById(R.id.mToolBar2);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        adapter = new MainAdapter(this,show_mlist);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void initData() {
        show_mlist = new ArrayList<>();
        str_imei = getIntent().getStringExtra(IMEI_NO);
        mList = (List<MyMessage>) getIntent().getSerializableExtra(INFORMATION);
        Collections.reverse(mList);
        LogUtil.i(TAG,"得到的信息数::"+mList.size());
        for (int i = 0; i < mList.size(); i++) {
            if (mList.get(i).getIMEI().equals(str_imei)){
                show_mlist.add(mList.get(i));
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (show_mlist.get(0).getSensorType().equals("1")){
            //type.setText("拉绳");
            mToolbar.setTitle("拉绳:"+str_imei);
        }
        if (show_mlist.get(0).getSensorType().equals("2")){
            //type.setText("倾角");
            mToolbar.setTitle("倾角:"+str_imei);
        }
        if (show_mlist.get(0).getSensorType().equals("3")){
            //type.setText("土压力盒");
            mToolbar.setTitle("土压力盒:"+str_imei);
        }
        registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtil.i(TAG,"被销毁了");
        unregisterReceiver(receiver);
    }

    private void setMessage(String str) {
        mToolbar.setSubtitle(str);
    }
    private void getInformation(String s){
        try {
            MyMessage message = JsonUtil.dealWithJson(s);
            mList.add(message);
            if (message.getIMEI().equals(str_imei)){
                show_mlist.add(0,message);
            }
            adapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
