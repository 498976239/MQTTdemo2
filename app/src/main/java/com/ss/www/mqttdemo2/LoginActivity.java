package com.ss.www.mqttdemo2;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.ss.www.mqttdemo2.utils.LogUtil;

public class LoginActivity extends AppCompatActivity {
    private String TAG = getClass().getSimpleName();
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private EditText mLogin,mPassword;
    private Button mButton_login;
    private CheckBox rememberPass;
    private MQTTService mService;
    private String account;
    private String password;
    private ServiceConnection  connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = ((MQTTService.CustomBinder)service).getService();
            mService.init(account,password);
            mService.connect();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private IntentFilter intentFilter;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
                String str = intent.getAction();
            if (MQTTService.CONNECTED.equals(str)){
                editor = pref.edit();
                if(rememberPass.isChecked()){
                    editor.putBoolean("remember_password", true);
                    editor.putString("account", account);
                    editor.putString("password", password);
                }else {
                    editor.clear();
                }
                editor.commit();
                Intent intent1 = new Intent(LoginActivity.this,MainActivity.class);
                intent1.putExtra(MainActivity.ACCOUNT,account);
                intent1.putExtra(MainActivity.PASSWORD,password);
                startActivity(intent1);
                finish();
            }
            if (MQTTService.WRONG_INFOR.equals(str)){
                Toast.makeText(LoginActivity.this,"用户名密码错误",Toast.LENGTH_SHORT).show();
                unbindService(connection);
            }
            if (MQTTService.NO_WIFI.equals(str)){
                Toast.makeText(LoginActivity.this,"无信号，请检查网络",Toast.LENGTH_SHORT).show();
                unbindService(connection);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
        boolean isRemember = pref.getBoolean("remember_password",false);
        if(isRemember){
             account = pref.getString("account", "");
             password = pref.getString("password", "");
            mLogin.setText(account);
            mPassword.setText(password);
            rememberPass.setChecked(true);
        }
        mButton_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                  account = mLogin.getText().toString();
                  password = mPassword.getText().toString();
                LogUtil.i(TAG,"account--"+account);
                LogUtil.i(TAG,"password--"+password);
                Intent intent = new Intent(LoginActivity.this,MQTTService.class);
                bindService(intent,connection, Context.BIND_AUTO_CREATE);
            }
        });
        registerBroadcast();

    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver,intentFilter);
    }

    private void initView() {
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        mLogin = (EditText) findViewById(R.id.login_name);
        mPassword = (EditText) findViewById(R.id.password_in);
        mButton_login = (Button) findViewById(R.id.login_btn);
        rememberPass = (CheckBox) findViewById(R.id.remember_flag);
    }

    private void registerBroadcast(){
        intentFilter = new IntentFilter();
        intentFilter.addAction(MQTTService.CONNECTED);
        //intentFilter.addAction(MQTTService.GET_MESSAGE);
        intentFilter.addAction(MQTTService.NO_WIFI);
        intentFilter.addAction(MQTTService.WRONG_INFOR);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        unbindService(connection);
    }
}
