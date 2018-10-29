package com.ss.www.mqttdemo2.Bean;

import java.io.Serializable;
import java.util.List;

/**
 * Created by 小松松 on 2018/10/27.
 */

public class NewMessage implements Serializable {
    public String IMEI;
    public int Vol ;
    public byte SensorType;//传感器类型，1：拉绳位移；2：倾角
    public List<MyMessage2> PDataList ;//普通传感器
    public String MeasureTime;//采集时间
    public String StarTime ;//雨量开始时间
    public String EndTime ;//雨量结束时间
    public float CurrentYL ;//当前雨量数据
    public List<Float> YL_24;//24小时的雨量

    public String getIMEI() {
        return IMEI;
    }

    public void setIMEI(String IMEI) {
        this.IMEI = IMEI;
    }

    public int getVol() {
        return Vol;
    }

    public void setVol(int vol) {
        Vol = vol;
    }

    public byte getSensorType() {
        return SensorType;
    }

    public void setSensorType(byte sensorType) {
        SensorType = sensorType;
    }

    public List<MyMessage2> getPDataList() {
        return PDataList;
    }

    public void setPDataList(List<MyMessage2> PDataList) {
        this.PDataList = PDataList;
    }

    public String getMeasureTime() {
        return MeasureTime;
    }

    public void setMeasureTime(String measureTime) {
        MeasureTime = measureTime;
    }

    public String getStarTime() {
        return StarTime;
    }

    public void setStarTime(String starTime) {
        StarTime = starTime;
    }

    public String getEndTime() {
        return EndTime;
    }

    public void setEndTime(String endTime) {
        EndTime = endTime;
    }

    public float getCurrentYL() {
        return CurrentYL;
    }

    public void setCurrentYL(float currentYL) {
        CurrentYL = currentYL;
    }

    public List<Float> getYL_24() {
        return YL_24;
    }

    public void setYL_24(List<Float> YL_24) {
        this.YL_24 = YL_24;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NewMessage)) return false;

        NewMessage that = (NewMessage) o;

        if (!IMEI.equals(that.IMEI)) return false;
        return MeasureTime.equals(that.MeasureTime);

    }

    @Override
    public int hashCode() {
        int result = IMEI.hashCode();
        result = 31 * result + MeasureTime.hashCode();
        return result;
    }
}
