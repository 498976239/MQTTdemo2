package com.ss.www.mqttdemo2;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.ss.www.mqttdemo2.Bean.Person;
import com.ss.www.mqttdemo2.adapter.PersonAdapter;
import com.ss.www.mqttdemo2.utils.LogUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LoginActivity extends AppCompatActivity {
    private String TAG = getClass().getSimpleName();
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private EditText mLogin,mPassword;
    private Button mButton_login;
    private CheckBox rememberPass;
    private MQTTService mService;
    private String account;
    private long count;//用户的数量
    private String password;
    private List<Person> mList = new ArrayList<>();//用来读取存有的用户名账号信息
    private PopupWindow mSelectWindow;
    private ImageButton mImageButton;
    private List<String> title;//用来判别用户名是否相同
    private List<String> reset_number;//重新排序记录用户
    private PersonAdapter adapter;
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
                if(rememberPass.isChecked()){
                    editor.putBoolean("remember_password", true);
                    editor.putString("last_admin",account);
                    Person p = new Person();
                    p.setName(account);
                    p.setPassword(password);
                    if (!title.contains(account)){
                        try {
                            LogUtil.i(TAG,"注册成功----------------");
                            p.setNumber("username"+(mList.size()+1));
                            String person_string = serialize(p);
                            editor.putString("username"+(mList.size()+1),person_string);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                   // editor.putString("account", account);
                    //editor.putString("password", password);
                }else {
                    //editor.clear();
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
            if (MQTTService.NO_PERMISSION.equals(str)){
                Toast.makeText(LoginActivity.this,"请允许读取手机状态权限",Toast.LENGTH_SHORT).show();
                //ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, 3);
                unbindService(connection);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
        title = new ArrayList<>();
        reset_number = new ArrayList<>();
        boolean isRemember = pref.getBoolean("remember_password",false);
        String last_admin = pref.getString("last_admin","");
        LogUtil.i(TAG,"last_admin---"+last_admin);
        for (int i = 0; i < 100; i++) {//循环100次，将保存的用户信息都提取出来
            String s = pref.getString("username"+i,"");
            if (s != null){
                reset_number.add(s);//将文件里面的字符串读取
            }
            try {
                Person person = deSerialization(s);//反序列化得到对象
                mList.add(person);//放入集合，也是为了得到用户个数
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        for (int i = 0; i < mList.size(); i++) {//按得到的用户个数，对存储名重新编码排列，防止删除用户后，再
            //重新注册时有重复键名
            editor.putString("username"+i,reset_number.get(i));
            editor.commit();
        }
        LogUtil.i(TAG,"用户个数"+mList.size());
        if (mList.size()>0){
            mImageButton.setVisibility(View.VISIBLE);
            for (int i = 0; i < mList.size(); i++) {
                if (mList.get(i).getName().equals(last_admin)){
                    mLogin.setText(mList.get(i).getName());
                    mPassword.setText(mList.get(i).getPassword());
                }
                title.add(mList.get(i).getName());
                LogUtil.i(TAG,"已经注册了账号"+mList.get(i).getNumber());
            }
        }

        if(isRemember){
            if (!(mList.size()==0)){
                Collections.reverse(mList);
                rememberPass.setChecked(true);
            }
             //account = pref.getString("account", "");
             //password = pref.getString("password", "");

        }
        adapter = new PersonAdapter(LoginActivity.this,mList);
        mButton_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 account = mLogin.getText().toString();
                 password = mPassword.getText().toString();
                LogUtil.i(TAG,"account--"+account.length());
                LogUtil.i(TAG,"password--"+password.length());
                if ((account.length() != 0)&&(password.length()!=0)){
                    LogUtil.i(TAG,"在按钮里");
                    Intent intent = new Intent(LoginActivity.this,MQTTService.class);
                    bindService(intent,connection, Context.BIND_AUTO_CREATE);
                }else {
                    Toast.makeText(LoginActivity.this,"登录名或密码不能为空",Toast.LENGTH_SHORT).show();

                }

            }
        });
        registerBroadcast();
        mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mList.size()>0){
                    showAccountChoiceWindow();
                    mImageButton.setBackground(getResources().getDrawable(R.drawable.ic_keyboard_arrow_up_grey_500_24dp));
                }

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver,intentFilter);
    }

    private void initView() {
        pref = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
        editor = pref.edit();
        mLogin = (EditText) findViewById(R.id.login_name);
        mPassword = (EditText) findViewById(R.id.password_in);
        mButton_login = (Button) findViewById(R.id.login_btn);
        rememberPass = (CheckBox) findViewById(R.id.remember_flag);
        mImageButton = (ImageButton) findViewById(R.id.open_list);

    }

    private void registerBroadcast(){
        intentFilter = new IntentFilter();
        intentFilter.addAction(MQTTService.CONNECTED);
        //intentFilter.addAction(MQTTService.GET_MESSAGE);
        intentFilter.addAction(MQTTService.NO_WIFI);
        intentFilter.addAction(MQTTService.WRONG_INFOR);
        intentFilter.addAction(MQTTService.NO_PERMISSION);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (receiver!=null){
            unregisterReceiver(receiver);
        }
       if (mService!=null){
           unbindService(connection);
       }

    }

    //将得到的对象序列化，变成字符串，方便存储
    private String serialize(Person person) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(person);
        String serStr = byteArrayOutputStream.toString("ISO-8859-1");
        serStr = java.net.URLEncoder.encode(serStr, "UTF-8");
        objectOutputStream.close();
        byteArrayOutputStream.close();
        return serStr;
    }

    //发序列化，将字符串变成对象
    private Person deSerialization(String str) throws IOException, ClassNotFoundException {
        String redStr = java.net.URLDecoder.decode(str, "UTF-8");
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(redStr.getBytes("ISO-8859-1"));
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
        Person person = (Person) objectInputStream.readObject();
        objectInputStream.close();
        byteArrayInputStream.close();
        return person;
    }

    private void showAccountChoiceWindow(){
        View view = LayoutInflater.from(this).inflate(R.layout.layout_popwindow_list,null);
        RelativeLayout contentview  = view.findViewById(R.id.popWindow);//拿到布局
        ListView userlist  = view.findViewById(R.id.popWindow_list);
        userlist.setAdapter(adapter);
        mSelectWindow = new PopupWindow(contentview,mLogin.getMeasuredWidth(), RelativeLayout.LayoutParams.WRAP_CONTENT,true);
        mSelectWindow.setOutsideTouchable(true);
        mSelectWindow.setFocusable(true);
        mSelectWindow.showAsDropDown(mLogin, 0, 0);
        mSelectWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                mImageButton.setBackground(getResources().getDrawable(R.drawable.ic_keyboard_arrow_down_grey_500_24dp));
            }
        });
        adapter.setOnItemClickListener(new PersonAdapter.OnItemClickListener() {
            @Override
            public void onItemClicked(int position) {
                Person person = mList.get(position);
                mLogin.setText(person.getName());
                mPassword.setText(person.getPassword());
                rememberPass.setChecked(true);
                mSelectWindow.dismiss();
            }
        });
        adapter.setOnDelBtnClickListener(new PersonAdapter.OnDelBtnClickListener() {
            @Override
            public void onDelBtnClicked(final int position) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(LoginActivity.this);
                dialog.setTitle("重要提醒");
                dialog.setMessage("确定删除该账号");
                dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String s = mList.get(position).getNumber();
                        Log.i("main","删除了--"+s);
                        try {

                            Person person = deSerialization(pref.getString(s,""));
                            Log.i(TAG,"删除了-person-"+person.getName());
                            Log.i(TAG,"删除了-mLogin-"+mLogin.getText().toString());
                            if (person.getName().equals(mLogin.getText().toString())){
                                mLogin.setText("");
                                mPassword.setText("");
                                rememberPass.setChecked(false);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                        mList.remove(position);
                        adapter.notifyDataSetChanged();
                        //pref = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
                        //editor = pref.edit();
                        editor.remove(s);
                        editor.commit();
                        if (mList.size()==0){
                            mLogin.setText("");
                            mPassword.setText("");
                            rememberPass.setChecked(false);
                            mImageButton.setVisibility(View.INVISIBLE);
                        }
                    }
                });
                dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                dialog.show();
            }
        });
    }

}
