package com.ss.www.mqttdemo2.utils;

import com.ss.www.mqttdemo2.Bean.MyMessage;
import com.ss.www.mqttdemo2.Bean.NewMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

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

   /* public static NewMessage getNewMessageByjson(String s)throws JSONException{
        JSONArray array = new JSONArray(s);
        NewMessage newMessage = new NewMessage();
        for (int i = 0; i < array.length(); i++) {
            JSONObject jsonObject = array.getJSONObject(i);
            newMessage.setIMEI(jsonObject.getString("IMEI"));
            newMessage.setVoltage(String.valueOf(jsonObject.get("Vol")));
            newMessage.setSensorType(String.valueOf(jsonObject.get("SensorType")));
            newMessage.setAverage_sensor((List<Object>) jsonObject.get("PDataList"));
            newMessage.setStartTime(jsonObject.getString("StarTime"));
            newMessage.setEndTime(jsonObject.getString("EndTime"));
            newMessage.setRailfall_current(String.valueOf(jsonObject.get("CurrentYL")));
            newMessage.setRailfall_24((List<Float>) jsonObject.get("YL_24"));
        }
        return newMessage;
    }*/
}
