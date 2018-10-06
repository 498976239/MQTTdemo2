package com.ss.www.mqttdemo2.utils;

import com.ss.www.mqttdemo2.Bean.MyMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by 小松松 on 2018/9/30.
 */

public class JsonUtil {
    public static MyMessage dealWithJson(String s) throws JSONException {
        JSONArray array = new JSONArray(s);
        MyMessage message = new MyMessage();
        for (int i = 0; i <array.length() ; i++){
            JSONObject jsonObject = array.getJSONObject(i);
            message.setIMEI(jsonObject.getString("IMEI"));
            message.setVoltage(String.valueOf(jsonObject.get("Vol")));
            message.setSensorType(String.valueOf(jsonObject.get("SensorType")));
            message.setChannelNO(String.valueOf(jsonObject.get("ChannelNO")));
            message.setMeasureTime(jsonObject.getString("MeasureTime"));
            message.setD1(String.valueOf(jsonObject.get("D1")) );
            message.setD2(String.valueOf(jsonObject.get("D2")) );
            message.setD3(String.valueOf(jsonObject.get("D3")) );
        }
        return message;
    }
}
