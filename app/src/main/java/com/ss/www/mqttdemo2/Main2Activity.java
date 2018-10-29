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

import com.alibaba.fastjson.JSON;
import com.ss.www.mqttdemo2.Bean.MyMessage;
import com.ss.www.mqttdemo2.Bean.MyMessage2;
import com.ss.www.mqttdemo2.Bean.NewMessage;
import com.ss.www.mqttdemo2.adapter.ImeiAdpter;
import com.ss.www.mqttdemo2.adapter.MainAdapter;
import com.ss.www.mqttdemo2.utils.JsonUtil;
import com.ss.www.mqttdemo2.utils.LogUtil;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class Main2Activity extends AppCompatActivity {
    private String TAG = getClass().getSimpleName();
    private final static int RECEIVE_DATA2 = 1;
    public static final String IMEI_NO = "imei_no";
    public static final String INFORMATION = "information";
    public static final String BACK_IMEI = "BACK_IMEI";//送回去的IMEI号，用来标红颜色
    private List<NewMessage> mList ;
    private List<NewMessage>show_mlist;
    private List<MyMessage> theEndShow;
    private String str_imei;
    private RecyclerView rv;
    private MainAdapter adapter;
    private Toolbar mToolbar;
    private MQTTService mqttService;
    private MqttClient client;
    private IntentFilter intentFilter;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mqttService = ((MQTTService.CustomBinder)service).getService();
            client = mqttService.getClient();
            if (client.isConnected()){
                setMessage("连接成功");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    private BroadcastReceiver receiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
            if (MQTTService.GET_MESSAGE.equals(action)){
                String str = intent.getStringExtra(MQTTService.MESSAGE);
                NewMessage message = JSON.parseObject(str,NewMessage.class);
                Message msg = Message.obtain();
                msg.what = RECEIVE_DATA2;
                msg.obj = message;
                mHandler.sendMessage(msg);
            }
            if (MQTTService.CONNECTED.equals(action)){
                LogUtil.i(TAG,"main---连接成功");
                setMessage("连接成功");
            }
            if (MQTTService.CONNECT_LOST.equals(action)){
                setMessage("连接断开");
            }
            if (MQTTService.NO_WIFI.equals(action)){
                setMessage("信号不稳，正在重连...");

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
                    NewMessage s = (NewMessage) msg.obj;
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
        intentFilter.addAction(MQTTService.NO_WIFI);
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
        theEndShow = new ArrayList<>();
        show_mlist.clear();
        str_imei = getIntent().getStringExtra(IMEI_NO);
        mList = (List<NewMessage>) getIntent().getSerializableExtra(INFORMATION);
        for (int i = 0; i < mList.size(); i++) {
            if (mList.get(i).getIMEI().equals(str_imei)){
                if (!show_mlist.contains(mList.get(i))){
                    show_mlist.add(mList.get(i));
                }
            }
        }
        Collections.reverse(show_mlist);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (show_mlist.get(0).getSensorType()==1){
            mToolbar.setTitle("拉绳:"+str_imei);
        }
        if (show_mlist.get(0).getSensorType()==2){
            mToolbar.setTitle("倾角:"+str_imei);
        }
        if (show_mlist.get(0).getSensorType()==3){
            mToolbar.setTitle("土压力盒:"+str_imei);
        }
        registerReceiver(receiver, intentFilter);
        Intent intent = new Intent(Main2Activity.this,MQTTService.class);
        bindService(intent,connection,Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtil.i(TAG,"被销毁了");
        unregisterReceiver(receiver);
        unbindService(connection);
        /*if (mqttService != null){
            mqttService.disconnect();
        }*/
    }

    private void setMessage(String str) {
        mToolbar.setSubtitle(str);
    }
    private void getInformation(NewMessage message){
            if (mList.size()==0){
                mList.add(message);
            }
            if (!mList.contains(message)) {
                mList.add(message);
            }
            if (message.getIMEI().equals(show_mlist.get(0).getIMEI())){
                if (!show_mlist.contains(message)) {
                    show_mlist.add(0,message);
                }
            }

            adapter.notifyDataSetChanged();

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(BACK_IMEI,str_imei);
        setResult(Activity.RESULT_OK,intent);
        super.onBackPressed();

        //Main2Activity.this.finish();
    }
}
