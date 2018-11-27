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
import android.view.View;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.ss.www.mqttdemo2.Bean.NewMessage;
import com.ss.www.mqttdemo2.adapter.RainfallAdapter;
import com.ss.www.mqttdemo2.utils.LogUtil;

import org.eclipse.paho.client.mqttv3.MqttClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RainfallShowActivity extends AppCompatActivity {
    private String TAG = getClass().getSimpleName();
    private final static int RECEIVE_DATA2 = 1;
    private TextView water_name;
    private TextView rainfall_MeasureTime;
    private TextView startTime;
    private TextView endTime;
    private TextView current_rainfall;
    private TextView current_vol;
    private TextView cur_water;
    private Toolbar mToolbar;
    private RecyclerView rv_rainfall;
    private RainfallAdapter adapter;
    private List<NewMessage> newMessageList;//存放雨量信息
    private List<NewMessage> show_mlist;
    private String str_imei;//得到IMEI好
    private List<String> titleList;//用来存放雨量时间标号1~24
    private List<Float> rainfallValueList;//存放24小时的雨量值
    public static final String INFORMATION_RAINFALLSHOWACTIVITY = "information_RainfallShowActivity";
    public static final String BACK_IMEI_RAINFALLSHOWACTIVITY= "BACK_IMEI_RainfallShowActivity";//送回去的IMEI号，用来标红颜色
    public static final String IMEI_NO_AINFALLSHOWACTIVITY = "imei_no_RainfallShowActivity";
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
                NewMessage newMessage = JSON.parseObject(str,NewMessage.class);
                Message msg = Message.obtain();
                msg.what = RECEIVE_DATA2;
                msg.obj = newMessage;
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
                    LogUtil.i(TAG,TAG+"收到数据-----");
                    NewMessage message = (NewMessage) msg.obj;
                    LogUtil.i(TAG,"imei------"+message.getIMEI());
                    if (!newMessageList.contains(message)){
                        newMessageList.add(message);
                    }
                    if (message.getIMEI().equals(str_imei)){//当广播接收到的传感器是雨量传感器时
                        if (!show_mlist.contains(message)&&!(message.getMeasureTime().equals(show_mlist.get(0).getMeasureTime())))
                            show_mlist.add(0,message);//没有重复就加入集合
                        if (!message.getStarTime().equals("0:0")&&!message.getEndTime().equals("0:0")){//当时间为0.0，不更改。
                            //写在这里面是因为广播也会收到其他传感器,去获取时间时为null。
                            startTime.setText(message.getStarTime());
                            endTime.setText(message.getEndTime());
                        }
                        if (message.getCurrentYL() != 0.0){
                            current_rainfall.setText(message.getCurrentYL()+"");//得到当前雨量
                        }
                        rainfall_MeasureTime.setText(message.getMeasureTime());
                        List<Float> list = show_mlist.get(0).getYL_24();//得到雨量值，24个
                        adapter.changeForRainfall(list);//将数据重新刷新入适配器
                    }

                    break;
            }
        }
    };

    /**
     * 注册广播
     */
    private void registerBroadcast(){
        intentFilter = new IntentFilter();
        intentFilter.addAction(MQTTService.GET_MESSAGE);
        intentFilter.addAction(MQTTService.CONNECTED);
        intentFilter.addAction(MQTTService.CONNECT_LOST);
        intentFilter.addAction(MQTTService.NO_WIFI);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.i(TAG,"-------------------------");
        setContentView(R.layout.activity_rainfall_show);
        initData();
        initView();
        registerBroadcast();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (show_mlist.get(0).getSensorType() == 1){
            mToolbar.setTitle("拉绳:"+str_imei);
        }
        if (show_mlist.get(0).getSensorType()==2){
            mToolbar.setTitle("倾角:"+str_imei);
        }
        if (show_mlist.get(0).getSensorType()==3){
            mToolbar.setTitle("土压力盒:"+str_imei);
        }
        if (show_mlist.get(0).getSensorType()==4){
            mToolbar.setTitle("雨量计:"+str_imei);
        }
        if (show_mlist.get(0).getSensorType() == 5){
            water_name.setVisibility(View.VISIBLE);
            cur_water.setVisibility(View.VISIBLE);
        }else {
            water_name.setVisibility(View.GONE);
            cur_water.setVisibility(View.GONE);
        }
        registerReceiver(receiver, intentFilter);
        Intent intent = new Intent(RainfallShowActivity.this,MQTTService.class);
        bindService(intent,connection,Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        unbindService(connection);
        /*if (mqttService!=null){
            mqttService.disconnect();
        }*/
    }

    private void initView() {
        mToolbar = (Toolbar) findViewById(R.id.mToolBar_rainfallShow);
        setSupportActionBar(mToolbar);
        rainfall_MeasureTime = (TextView) findViewById(R.id.rainfall_MeasureTime);
        startTime = (TextView) findViewById(R.id.rainfall_startTime);
        endTime = (TextView) findViewById(R.id.rainfall_endTime);
        rv_rainfall = (RecyclerView) findViewById(R.id.rainfall_RecyclerView);
        current_rainfall = (TextView) findViewById(R.id.cur_rain);
        current_vol = (TextView) findViewById(R.id.cur_vol);
        cur_water = (TextView) findViewById(R.id.cur_water);
        water_name = (TextView)findViewById(R.id.water_logo);
        adapter = new RainfallAdapter(this,titleList,show_mlist.get(0).getYL_24());
        //adapter = new RainfallAdapter(this,titleList,rainfallValueList);
        rv_rainfall.setLayoutManager(new LinearLayoutManager(this));
        rv_rainfall.setAdapter(adapter);
        setValue(show_mlist.get(0));
    }

    private void initData() {
        show_mlist = new ArrayList<>();
        show_mlist.clear();
        titleList = new ArrayList<>();
        //得到的是全部的传感器的数据。要进行筛选
        newMessageList  = (List<NewMessage>) getIntent().getSerializableExtra(INFORMATION_RAINFALLSHOWACTIVITY);
        LogUtil.i(TAG,"newMessageList:--"+newMessageList.size());
        //Collections.reverse(newMessageList);
        //rainfallValueList = newMessageList.get(0).getYL_24();

           // LogUtil.i(TAG,"rainfallValueList:--"+newMessageList.get(0).getYL_24().toString());


        //LogUtil.i(TAG,"rainfallValueList:--"+rainfallValueList.size());
        str_imei = getIntent().getStringExtra(IMEI_NO_AINFALLSHOWACTIVITY);
        LogUtil.i(TAG,"str_imei----"+str_imei);
        for (int i = 0; i < 24; i++) {//适配器所需要的数据，用来显示雨量各个时间段
            titleList.add((i)+"");
        }
        for (int i = 0; i < newMessageList.size(); i++) {//根据IMEI将传过来的所有传感器数据进行分类
            if (newMessageList.get(i).getIMEI().equals(str_imei)){
                LogUtil.i(TAG,"--1--");
                if (!show_mlist.contains(newMessageList.get(i))){
                    LogUtil.i(TAG,"--2--");
                    show_mlist.add(newMessageList.get(i));//放入最终要展示的集合
                }
            }
        }
        LogUtil.i(TAG,"----"+show_mlist.get(0).getEndTime());
        LogUtil.i(TAG,"----"+show_mlist.get(0).getStarTime());
        LogUtil.i(TAG,"----"+show_mlist.get(0).getMeasureTime());
        LogUtil.i(TAG,"----"+show_mlist.get(0).getYL_24().size());

    }
    private void setValue(NewMessage message){
        rainfall_MeasureTime.setText(message.getMeasureTime());
        startTime.setText(message.getStarTime());
        endTime.setText(message.getEndTime());
        current_rainfall.setText(message.getCurrentYL()+"");
        current_vol.setText(message.getVol()+"");

    }

    private void setMessage(String str) {
        mToolbar.setSubtitle(str);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(BACK_IMEI_RAINFALLSHOWACTIVITY,str_imei);
        setResult(Activity.RESULT_OK,intent);
        super.onBackPressed();

        //Main2Activity.this.finish();
    }
}
