package com.ss.www.mqttdemo2.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;

import com.ss.www.mqttdemo2.R;

/**
 * Created by 小松松 on 2018/10/14.
 */

public class MyViewDialog extends Dialog {
    private Context mContext;

    public MyViewDialog(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View v= LayoutInflater.from(mContext).inflate(R.layout.layout_dialog,null);
        this.setContentView(v);
    }
}
