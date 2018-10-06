package com.ss.www.mqttdemo2;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.ss.www.mqttdemo2.utils.LogUtil;
import com.ss.www.mqttdemo2.utils.MacSignature;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MQTTService extends Service implements MqttCallback{
    public final static  String TAG = MQTTService.class.getSimpleName();
    private int count;//用来计算第一次打开软件收到的数据，只收最新的100条
    private boolean first,first2;
    // 线程池的大小
    private static int threadSize = 5;
    // 创建一个可重用固定线程数的线程池，以共享的无界队列方式来运行这些线程
    private static ExecutorService threadPool = Executors.newFixedThreadPool(threadSize);
    public final static  String CONNECTED ="com.ss.www.mqttdemo2.service.CONNECTED";
    public final static String CONNECT_LOST = "com.ss.www.mqttdemo2.service.CONNECTED_LOST";
    public final static String GET_MESSAGE = "com.ss.www.mqttdemo2.service.GET_MESSAGE";
    public final static String MESSAGE = "com.ss.www.mqttdemo2.service.MESSAGE";
    public final static String WRONG_INFOR = "com.ss.www.mqttdemo2.service.WRONG_INFOR";
    public final static String NO_WIFI = "com.ss.www.mqttdemo2.service.no_wifi";
    private MqttClient client;//client
    private MqttConnectOptions options;//配置
    private  String TelephonyIMEI;
    private String sign;
    private String host = "tcp://120.77.246.251:1883";
    private String Local = "tcp://mq.tongxinmao.com:18831";//通讯猫测试
    private String userName ;
    private static String myTopic = "iot/nb/";      //要订阅的主题
    private static String Topic = "ss";//通讯猫测试
    private String clientId ;//客户端标识
    private Timer mTimer;
   /* private TimerTask timerTask = new TimerTask(){

        @Override
        public void run() {
            if (first == true && !first2){
                try {
                    client.subscribe(myTopic +userName,2);
                    LogUtil.i(TAG,"在定时器里开启订阅");

                    first2 = true;
                } catch (MqttException e) {
                    LogUtil.i(TAG,"定时器里报的异常--"+e.toString());

                }
            }
        }
    };*/
    private IGetMessageCallBack iGetMessageCallBack;
    public class CustomBinder extends Binder {
        public MQTTService getService(){
            return MQTTService.this;
        }
    }
    public MQTTService() {
    }
    public void setIGetMessageCallBack(IGetMessageCallBack iGetMessageCallBack){
        this.iGetMessageCallBack = iGetMessageCallBack;

    }

    @Override
    public void onCreate() {
        super.onCreate();
       // mTimer = new Timer();
        //mTimer.schedule(timerTask,1,1000*2);
        Notification.Builder builder = new Notification.Builder(this);
        /*PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);
        builder.setContentIntent(contentIntent);*/
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setTicker("Foreground Service Start");
        builder.setContentTitle("Foreground Service");
        builder.setContentText("正在采集...");
        Notification notification = builder.build();
        startForeground(1, notification);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        //mTimer.cancel();
        try {
            client.unsubscribe(myTopic +userName);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    //封装的用来发送广播的方法
    private void broadcastUpdate(String intentAction) {
        Intent intent = new Intent(intentAction);
        sendBroadcast(intent);
    }
    private void broadcastUpdate2(String intentAction,String str) {
        Intent intent = new Intent(intentAction);
        intent.putExtra(MESSAGE,str);
        sendBroadcast(intent);
    }

    public void connect() {
        if (threadPool == null || threadPool.isShutdown()){
            threadPool = Executors.newFixedThreadPool(threadSize);
        }
           threadPool.submit(new Runnable() {
               @Override
               public void run() {
                   try {
                       client.connect(options);//连接服务器,连接不上会阻塞在这
                       LogUtil.i(TAG,"连接成功");
                       String intentAction = CONNECTED;
                       broadcastUpdate(intentAction);
                       LogUtil.i(TAG,"-----"+myTopic +userName+"/+");
                   } catch (MqttException e) {
                       e.printStackTrace();
                       if (e.toString().contains("错误的用户名或密码")){
                           String s = MQTTService.WRONG_INFOR;
                           broadcastUpdate(s);
                       }
                       if (e.toString().contains("无法连接至服务器")|e.toString().contains("Network is unreachable")){
                           String s = MQTTService.NO_WIFI;
                           broadcastUpdate(s);
                       }
                       LogUtil.i(TAG,"connect异常---"+e.toString());
                   }
               }
           });

    }

    public void subscribe(){
        try {
            client.subscribe(myTopic +userName+"/+",2);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void init(String userName,String passWord) {
        this.userName = userName;
        TelephonyManager mTm = (TelephonyManager)this.getSystemService(TELEPHONY_SERVICE);
        TelephonyIMEI = mTm.getDeviceId();
        int currentapiVersion=android.os.Build.VERSION.SDK_INT;
        clientId = userName +  "@Android@" + currentapiVersion+"@"+  TelephonyIMEI;
        LogUtil.i(TAG,"clientId----"+clientId);
        LogUtil.i(TAG,"userName----"+userName);
        LogUtil.i(TAG,"c----"+clientId.split("@")[0]);
        try {
            sign =  MacSignature.macSignature(clientId.split("@")[0], passWord);
            Log.i(TAG,"sign----"+sign);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try {
            client = new MqttClient(host,clientId,new MemoryPersistence());
        } catch (MqttException e) {
            e.printStackTrace();
            LogUtil.i(TAG,"new client---"+e.toString());
        }

        options = new MqttConnectOptions();//MQTT的连接设置

        options.setCleanSession(true);//设置是否清空session,这里如果设置为false表示服务器会保留客户端的连接记录，这里设置为true表示每次连接到服务器都以新的身份连接

        options.setUserName(userName);//设置连接的用户名(自己的服务器没有设置用户名)

        options.setPassword(sign.toCharArray());//设置连接的密码(自己的服务器没有设置密码)

        options.setConnectionTimeout(10);// 设置连接超时时间 单位为秒

        options.setKeepAliveInterval(20);// 设置会话心跳时间 单位为秒 服务器会每隔1.5*20秒的时间向客户端发送个消息判断客户端是否在线，但这个方法并没有重连的机制

        client.setCallback(this);// 设置MQTT监听并且接受消息

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return new CustomBinder();
    }

    @Override
    public void connectionLost(Throwable cause) {
        LogUtil.i(TAG,"connectionLost"+cause.toString());
        String str = CONNECT_LOST;
        broadcastUpdate(str);
        if (threadPool == null || threadPool.isShutdown()){
            threadPool = Executors.newFixedThreadPool(threadSize);
        }
        threadPool.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    client.connect(options);//连接服务器,连接不上会阻塞在这
                    //LogUtil.i(TAG,"连接成功");
                    String intentAction = CONNECTED;
                    broadcastUpdate(intentAction);
                    subscribe();
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        });


    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
            LogUtil.i(TAG,"arrived------"+message.toString());
            String str2 = GET_MESSAGE;
            broadcastUpdate2(str2,message.toString());
            if (!first){
            //client.unsubscribe(myTopic +userName);
            first = true;
            }
           /* if (iGetMessageCallBack != null){
                String str = message.toString();
                LogUtil.i(TAG,"str------"+message.toString());
                iGetMessageCallBack.getMessage(str);
                LogUtil.i(TAG,"first------"+first);
                if (!first){

                    first = true;
                }
                LogUtil.i(TAG,"count--"+count);
            }*/


    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }

    public MqttClient getClient(){
        return client;
    }
    public interface IGetMessageCallBack{
       void getMessage(String str1);
    }


}
