package com.ss.www.mqttdemo2;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ss.www.mqttdemo2.Bean.HeadInfo;
import com.ss.www.mqttdemo2.Bean.MyMessage;
import com.ss.www.mqttdemo2.adapter.ImeiAdpter;
import com.ss.www.mqttdemo2.adapter.MainAdapter;
import com.ss.www.mqttdemo2.utils.JsonUtil;
import com.ss.www.mqttdemo2.utils.LogUtil;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private String TAG = getClass().getSimpleName();
    private final static int RECEIVE_DATA = 1;
    public  final static String ACCOUNT = "com.ss.www.mqttdemo.ACCOUNT";
    public  final static String PASSWORD = "com.ss.www.mqttdemo.PASSWORD";
    public  final static int COME_BACK = 3;
    private MQTTService mqttService;
    private MqttClient client;
    private Toolbar mToolbar;
    private int count;
    private List<MyMessage> back_list;
    private SearchView mSearchView;
    private RecyclerView imei_rv;
    private ImeiAdpter imei_adapter;
    private List<MyMessage> mList_show;
    private List<MyMessage> mList;
    private List<String> compare;//用来查看里面是否有重复的IMEI
    private List<HeadInfo> mList_imei;
    private List<HeadInfo> filter_string;
    public  String userName2 ;
    public  String passWord2 ;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mqttService = ((MQTTService.CustomBinder)service).getService();
            client = mqttService.getClient();
            if (client.isConnected()){
                setMessage("连接成功");
            }
            //mqttService.setPassager(userName,passWord);
            //mqttService.init(userName2,passWord2);
            //mqttService.connect();
            mqttService.subscribe();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private IntentFilter intentFilter;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (MQTTService.CONNECTED.equals(action)){
                LogUtil.i(TAG,"main---连接成功");
                Toast.makeText(MainActivity.this,"连接成功",Toast.LENGTH_SHORT).show();
                setMessage("连接成功");

            }
            if (MQTTService.GET_MESSAGE.equals(action)){
                String str = intent.getStringExtra(MQTTService.MESSAGE);
                LogUtil.i(TAG,"get--"+str);
                String s = str.substring(str.indexOf("[{"),str.indexOf("}]")+2);
                Message msg = Message.obtain();
                msg.what = RECEIVE_DATA;
                msg.obj = s;
                mHandler.sendMessage(msg);
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
                case RECEIVE_DATA :
                    LogUtil.i(TAG,"main收到数据");
                    String s = (String) msg.obj;
                    getInformation(s);
                    break;

            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        userName2 = getIntent().getStringExtra(ACCOUNT);
        passWord2 = getIntent().getStringExtra(PASSWORD);
        initData();//初始化数据
        initView();//初始化UI
        registerBroadcast();//注册广播
        mSearchView.setQueryHint("请输入关键字");
        mSearchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
            }
        });
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                    filter_string = Filter(mList_imei,newText);

                    imei_adapter.setFilter(filter_string);
                    LogUtil.i(TAG,"-filter_string.size()--"+filter_string.size());
                if (filter_string.size() == 0){
                    //imei_adapter.setFilter(mList_imei);
                }

                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(this, MQTTService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
        registerReceiver(receiver, intentFilter);
        if (mList_imei != null){
            //imei_adapter.setFilter(mList_imei);
            imei_adapter.notifyDataSetChanged();
        }
        LogUtil.i(TAG,"filter_string.size()------------------------"+filter_string.size()+"");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
        unregisterReceiver(receiver);
        LogUtil.i(TAG,"被销毁了");
        try {
            client.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
            LogUtil.i(TAG,"Mainactivity里面disconnect异常"+e.toString());
        }
    }
    private void registerBroadcast(){
        intentFilter = new IntentFilter();
        intentFilter.addAction(MQTTService.CONNECTED);
        intentFilter.addAction(MQTTService.GET_MESSAGE);
        intentFilter.addAction(MQTTService.CONNECT_LOST);
    }

    private List<HeadInfo> Filter(List<HeadInfo> list, String str){
        if (filter_string != null){
            filter_string.clear();
        }
        for (HeadInfo headInfo : list){
            if ((headInfo.getName()).contains(str)) {
                if (!filter_string.contains(headInfo)){
                    filter_string.add(headInfo);
                }

            }

        }
        return filter_string;
    }

    private void initView(){
        mToolbar = (Toolbar) findViewById(R.id.mToolBar);
        imei_rv = (RecyclerView) findViewById(R.id.imei_title);
        mSearchView = (SearchView) findViewById(R.id.search);
        // mSearchView.setIconifiedByDefault(false);
        setSupportActionBar(mToolbar);
        imei_adapter = new ImeiAdpter(this,mList_imei);
        imei_rv.setLayoutManager(new LinearLayoutManager(this));
        imei_rv.setAdapter(imei_adapter);
        imei_adapter.setOnItemClickListener(new ImeiAdpter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (filter_string.size() > 0){
                    String str2 = filter_string.get(position).getName();
                    filter_string.get(position).setMark(true);
                    filter_string.get(position).setLast(true);
                    Intent intent = new Intent(MainActivity.this,Main2Activity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString(Main2Activity.IMEI_NO,str2);
                    bundle.putSerializable(Main2Activity.INFORMATION, (Serializable) mList);
                    intent.putExtras(bundle);
                   // startActivity(intent);
                    startActivityForResult(intent,COME_BACK);
                }else {
                    String str = mList_imei.get(position).getName();
                    mList_imei.get(position).setMark(true);
                    mList_imei.get(position).setLast(true);
                    Intent intent = new Intent(MainActivity.this,Main2Activity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString(Main2Activity.IMEI_NO,str);
                    LogUtil.i(TAG,"得到的mList总数："+mList.size());
                    bundle.putSerializable(Main2Activity.INFORMATION, (Serializable) mList);
                    intent.putExtras(bundle);
                    startActivityForResult(intent,COME_BACK);
                    //startActivity(intent);
                }

            }
        });
        if (mList_imei != null){
            imei_adapter.notifyDataSetChanged();
        }
    }
    private void initData(){
        mList = new ArrayList<>();
        mList_imei = new ArrayList<>();
        mList_show = new ArrayList<>();
        filter_string = new ArrayList<>();
        compare = new ArrayList<>();
    }

    private void setMessage(String str) {
        mToolbar.setSubtitle(str);
    }

    private long firstTime;//用来定义再按一次退出的变量
    @Override
    public void onBackPressed() {
        //super.onBackPressed();
            if (firstTime + 2000 > System.currentTimeMillis()) {
                super.onBackPressed();
                System.exit(0);
            } else {
                Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show();
            }
            firstTime = System.currentTimeMillis();

    }
    private void getInformation(String s){
        try {
            MyMessage message = JsonUtil.dealWithJson(s);
            mList.add(message);
            if (!compare.contains(message.getIMEI())){
                count++;
                HeadInfo headInfo = new HeadInfo();
                headInfo.setName(message.getIMEI());
                headInfo.setCount(count+"");
                compare.add(message.getIMEI());
                mList_imei.add(headInfo);
                if (filter_string.size()!= 0){//加入这一条是为了，在最初的时候没有得到IMEI号，过了
                    //一段时间后才来的，这时候adapter关联的数据时filter_string
                    if (!filter_string.contains(headInfo)){//filter_string没有改对象才添加进去
                        filter_string.add(headInfo);
                    }

                }
            }
            imei_adapter.notifyDataSetChanged();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtil.i(TAG,"requestCode----"+requestCode);
        LogUtil.i(TAG,"resultCode----"+resultCode);
        LogUtil.i(TAG,"运行到返回-1---");
        switch (requestCode){
            case COME_BACK:
                LogUtil.i(TAG,"运行到返回--2--");
                if (resultCode == Activity.RESULT_OK){
                    LogUtil.i(TAG,"运行到返回--3--");
                    String str = data.getStringExtra(Main2Activity.BACK_IMEI);
                    for (int i = 0; i < mList_imei.size(); i++) {
                        if (mList_imei.get(i).getName().equals(str)){
                            mList_imei.get(i).setLast(true);
                        }else {
                            mList_imei.get(i).setLast(false);
                        }
                    }
                    for (int i = 0; i < filter_string.size(); i++) {
                        if (filter_string.get(i).getName().equals(str)){
                            filter_string.get(i).setLast(true);
                        }else {
                            filter_string.get(i).setLast(false);
                        }
                    }
                    imei_adapter.notifyDataSetChanged();
                }
                break;
        }
    }
}
