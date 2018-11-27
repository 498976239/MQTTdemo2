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
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.ss.www.mqttdemo2.Bean.HeadInfo;
import com.ss.www.mqttdemo2.Bean.NewMessage;
import com.ss.www.mqttdemo2.adapter.ImeiAdpter;
import com.ss.www.mqttdemo2.utils.JsonUtil;
import com.ss.www.mqttdemo2.utils.LogUtil;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.json.JSONException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RainFallActivity extends AppCompatActivity {
    private String TAG = getClass().getSimpleName();
    private final static int RECEIVE_DATA = 1;
    public  final static int COME_BACK = 3;
    public  final static int COME_BACK_MAIN2ACTIVITY = 4;
    private MQTTService mqttService;
    private MqttClient client;
    private Toolbar mToolbar;
    private SearchView mSearchView;
    private int count;
    private RecyclerView imei_rv;
    private ImeiAdpter imei_adapter;
    private List<NewMessage> mList;
    private List<String> compare;//用来查看里面是否有重复的IMEI
    private List<HeadInfo> mList_imei;//用来做界面显示的IMEI对象
    private List<HeadInfo> filter_string;//用来做查找的IMEI对象
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mqttService = ((MQTTService.CustomBinder)service).getService();
            client = mqttService.getClient();
            if (client.isConnected()){
                setMessage("连接成功");
            }
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
                setMessage("连接成功");

            }
            if (MQTTService.GET_MESSAGE.equals(action)){
                String str = intent.getStringExtra(MQTTService.MESSAGE);
                LogUtil.i(TAG,"get--"+str);
                NewMessage newMessage = JSON.parseObject(str,NewMessage.class);
                /*LogUtil.i(TAG,"mei-----"+newMessage.getIMEI());
                LogUtil.i(TAG,"vol-----"+newMessage.getVol());
                LogUtil.i(TAG,"SensorType-----"+newMessage.getSensorType());
                LogUtil.i(TAG,"MeasureTime-----"+newMessage.getMeasureTime());
                LogUtil.i(TAG,"PDataList-----"+newMessage.getPDataList());
                LogUtil.i(TAG,"StarTime-----"+newMessage.getStarTime());
                LogUtil.i(TAG,"EndTime-----"+newMessage.getEndTime());
                LogUtil.i(TAG,"CurrentYL-----"+newMessage.getCurrentYL());
                LogUtil.i(TAG,"yl_24-----"+newMessage.getYL_24());
                List<Float> list = newMessage.getYL_24();
                LogUtil.i(TAG,"list.size()-----"+list.size());
                LogUtil.i(TAG,"PDataList().size()-----"+newMessage.getPDataList().size());*/
                Message msg = Message.obtain();
                msg.what = RECEIVE_DATA;
                msg.obj = newMessage;
                mHandler.sendMessage(msg);
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
                case RECEIVE_DATA :
                    LogUtil.i(TAG,"main收到数据");
                    NewMessage newMessage = (NewMessage) msg.obj;
                    getInformation(newMessage);
                    break;

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rain_fall);
        initData();//初始化数据
        initView();//初始化UI
        registerBroadcast();//注册广播
        mSearchView.setQueryHint("请输入关键字");
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                filter_string = Filter(mList_imei,newText);//查找所需要的对象
                imei_adapter.setFilter(filter_string);//将适配器的内容更换
                LogUtil.i(TAG,"-filter_string.size()--"+filter_string.size());
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
        unregisterReceiver(receiver);
        if (mqttService != null){
            mqttService.disconnect();
        }

    }

    private void registerBroadcast() {
        intentFilter = new IntentFilter();
        intentFilter.addAction(MQTTService.CONNECTED);
        intentFilter.addAction(MQTTService.GET_MESSAGE);
        intentFilter.addAction(MQTTService.CONNECT_LOST);
        intentFilter.addAction(MQTTService.NO_WIFI);
    }

    private void initView() {
        mToolbar = (Toolbar) findViewById(R.id.mToolBar_rainfall);
        mSearchView = (SearchView) findViewById(R.id.search_rainfall);
        imei_rv = (RecyclerView) findViewById(R.id.imei_title_railfall);
        setSupportActionBar(mToolbar);
        imei_adapter = new ImeiAdpter(this,mList_imei);
        imei_rv.setLayoutManager(new LinearLayoutManager(this));
        imei_rv.setAdapter(imei_adapter);
        imei_adapter.setOnItemClickListener(new ImeiAdpter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                byte a = 0 ;//用来记录传感器类型，4为雨量
                if (filter_string.size() > 0){//当已经使用过查找数据
                    String str2 = filter_string.get(position).getName();//得到IMEI号
                    filter_string.get(position).setMark(true);//单击，做是一个标记
                    filter_string.get(position).setLast(true);//同时标记为最新一个受到单击的对象
                    for (int i = 0; i < mList.size(); i++) {
                        if (str2.equals(mList.get(i).getIMEI())){
                            a = mList.get(i).getSensorType();//根据IMEI号得到相应的传感器数据类型
                            LogUtil.i(TAG,"a-----------------------------:"+a);
                        }
                    }

                  //  LogUtil.i(TAG,"a:"+a);
                    if (a == 4 || a == 5){
                        //开启雨量传感器界面
                        Intent intent = new Intent(RainFallActivity.this,RainfallShowActivity.class);
                        Bundle bundle = new Bundle();
                        //将点击的传感器IMEI传入
                        bundle.putString(RainfallShowActivity.IMEI_NO_AINFALLSHOWACTIVITY,str2);
                        //将已经缓存的传感器数据传入
                        bundle.putSerializable(RainfallShowActivity.INFORMATION_RAINFALLSHOWACTIVITY, (Serializable) mList);
                        intent.putExtras(bundle);
                        startActivityForResult(intent,COME_BACK);
                    }else if (a < 4){
                        //开启除雨量以外的界面
                        Intent intent = new Intent(RainFallActivity.this,Main2Activity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString(Main2Activity.IMEI_NO,str2);
                        bundle.putSerializable(Main2Activity.INFORMATION, (Serializable) mList);
                        intent.putExtras(bundle);
                        startActivityForResult(intent,COME_BACK_MAIN2ACTIVITY);
                    }else {
                        Toast.makeText(RainFallActivity.this,"该型号传感器暂不显示",Toast.LENGTH_SHORT).show();
                    }
                }else {
                    //这部分是没有使用查找滤波的操作
                    String str = mList_imei.get(position).getName();
                    mList_imei.get(position).setMark(true);
                    mList_imei.get(position).setLast(true);
                    for (int i = 0; i < mList.size(); i++) {
                        if (str.equals(mList.get(i).getIMEI())){
                            a = mList.get(i).getSensorType();
                            LogUtil.i(TAG,"a-----------------------------:"+a);
                        }
                    }
                    if (a == 4 || a == 5){
                        Intent intent = new Intent(RainFallActivity.this,RainfallShowActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString(RainfallShowActivity.IMEI_NO_AINFALLSHOWACTIVITY,str);
                        LogUtil.i(TAG,"得到的mList总数："+mList.size());
                        bundle.putSerializable(RainfallShowActivity.INFORMATION_RAINFALLSHOWACTIVITY, (Serializable) mList);
                        intent.putExtras(bundle);
                        startActivityForResult(intent,COME_BACK);
                    }else if(a < 4){
                        Intent intent = new Intent(RainFallActivity.this,Main2Activity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString(Main2Activity.IMEI_NO,str);
                        LogUtil.i(TAG,"得到的mList总数："+mList.size());
                        bundle.putSerializable(Main2Activity.INFORMATION, (Serializable) mList);
                        intent.putExtras(bundle);
                        startActivityForResult(intent,COME_BACK_MAIN2ACTIVITY);
                    }else {
                        Toast.makeText(RainFallActivity.this,"该型号传感器暂不显示",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }) ;

    }

    private void initData() {
        mList = new ArrayList<>();
        mList_imei = new ArrayList<>();
        filter_string = new ArrayList<>();
        compare = new ArrayList<>();
    }

    /**查找过滤IMEI
     * @param list
     * @param str
     * @return
     */
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

    private void getInformation(NewMessage newMessage){

            LogUtil.i(TAG,"得到的数据---"+newMessage.getIMEI());
            if (!mList.contains(newMessage)){
                mList.add(newMessage);
            }
            if (!compare.contains(newMessage.getIMEI())){
                count++;//HeadInfo对象，用来记录有多少个传感器对象
                HeadInfo headInfo = new HeadInfo();
                headInfo.setName(newMessage.getIMEI());
                headInfo.setCount(count+"");
                compare.add(newMessage.getIMEI());
                mList_imei.add(headInfo);
                if (filter_string.size()!= 0){//加入这一条是为了，在最初的时候没有得到IMEI号，过了
                    //一段时间后才来的，这时候adapter关联的数据是filter_string
                    if (!filter_string.contains(headInfo)){//filter_string没有该对象才添加进去
                        filter_string.add(headInfo);
                    }

                }
            }
            LogUtil.i(TAG,"count-----"+count);
            LogUtil.i(TAG,"mList_imei.size()-----"+mList_imei.size());
        for (int i = 0; i < mList_imei.size(); i++) {
            LogUtil.i(TAG,"-----"+mList_imei.get(i).getName());
        }
            imei_adapter.notifyDataSetChanged();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case COME_BACK://从雨量界面返回
                if (resultCode == Activity.RESULT_OK) {
                   //带回刚刚查看的传感器的IMEI号
                    String str = data.getStringExtra(RainfallShowActivity.BACK_IMEI_RAINFALLSHOWACTIVITY);
                    for (int i = 0; i < mList_imei.size(); i++) {
                        if (mList_imei.get(i).getName().equals(str)) {
                            mList_imei.get(i).setLast(true);//在缓存里根据IMEI找到对应的传感器，设置为最新查看的
                        } else {
                            mList_imei.get(i).setLast(false);
                        }
                    }
                    for (int i = 0; i < filter_string.size(); i++) {
                        if (filter_string.get(i).getName().equals(str)) {
                            filter_string.get(i).setLast(true);
                        } else {
                            filter_string.get(i).setLast(false);
                        }
                    }
                    imei_adapter.notifyDataSetChanged();
                }
                break;
            case COME_BACK_MAIN2ACTIVITY://从其他界面回来，作用和上面一样
                if (resultCode == Activity.RESULT_OK){
                    String str = data.getStringExtra(Main2Activity.BACK_IMEI);
                    for (int i = 0; i < mList_imei.size(); i++) {
                        if (mList_imei.get(i).getName().equals(str)) {
                            mList_imei.get(i).setLast(true);
                        } else {
                            mList_imei.get(i).setLast(false);
                        }
                    }
                    for (int i = 0; i < filter_string.size(); i++) {
                        if (filter_string.get(i).getName().equals(str)) {
                            filter_string.get(i).setLast(true);
                        } else {
                            filter_string.get(i).setLast(false);
                        }
                    }
                    imei_adapter.notifyDataSetChanged();
                }
                break;
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.back_logn:
                Intent i = new Intent(RainFallActivity.this,LoginActivity.class);
                startActivity(i);
                this.finish();
                break;
        }
        return true;
    }
}
