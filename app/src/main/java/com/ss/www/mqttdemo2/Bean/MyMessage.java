package com.ss.www.mqttdemo2.Bean;

import java.io.Serializable;
import java.util.List;

/**
 * Created by 小松松 on 2018/9/17.
 */

public class MyMessage implements Serializable {
    public String IMEI;
    public String Voltage;
    public String SensorType;//传感器类型，1：拉绳位移；2：倾角
    public String ChannelNO;//通道号
    public String MeasureTime;//采集时间
    public String D1;//数据1
    public String D2;//数据2
    public String D3;//数据3
    public String getIMEI() {
        return IMEI;
    }

    public void setIMEI(String IMEI) {
        this.IMEI = IMEI;
    }

    public String getVoltage() {
        return Voltage;
    }

    public void setVoltage(String voltage) {
        Voltage = voltage;
    }

    public String getSensorType() {
        return SensorType;
    }

    public void setSensorType(String sensorType) {
        SensorType = sensorType;
    }

    public String getChannelNO() {
        return ChannelNO;
    }

    public void setChannelNO(String channelNO) {
        ChannelNO = channelNO;
    }

    public String getMeasureTime() {
        return MeasureTime;
    }

    public void setMeasureTime(String measureTime) {
        MeasureTime = measureTime;
    }

    public String getD1() {
        return D1;
    }

    public void setD1(String d1) {
        D1 = d1;
    }

    public String getD2() {
        return D2;
    }

    public void setD2(String d2) {
        D2 = d2;
    }

    public String getD3() {
        return D3;
    }

    public void setD3(String d3) {
        D3 = d3;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MyMessage)) return false;

        MyMessage message = (MyMessage) o;

        if (!IMEI.equals(message.IMEI)) return false;
        return MeasureTime.equals(message.MeasureTime);

    }

    @Override
    public int hashCode() {
        int result = IMEI.hashCode();
        result = 31 * result + MeasureTime.hashCode();
        return result;
    }
}
