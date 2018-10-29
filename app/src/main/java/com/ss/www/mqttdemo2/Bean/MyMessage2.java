package com.ss.www.mqttdemo2.Bean;

import java.io.Serializable;

/**
 * Created by 小松松 on 2018/10/28.
 */

public class MyMessage2 implements Serializable {
    public byte ChannelNO;
    public float D1;
    public float D2;
    public float D3;

    public byte getChannelNO() {
        return ChannelNO;
    }

    public void setChannelNO(byte channelNO) {
        ChannelNO = channelNO;
    }

    public float getD1() {
        return D1;
    }

    public void setD1(float d1) {
        D1 = d1;
    }

    public float getD2() {
        return D2;
    }

    public void setD2(float d2) {
        D2 = d2;
    }

    public float getD3() {
        return D3;
    }

    public void setD3(float d3) {
        D3 = d3;
    }
}
