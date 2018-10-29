package com.ss.www.mqttdemo2.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ss.www.mqttdemo2.Bean.NewMessage;
import com.ss.www.mqttdemo2.R;
import com.ss.www.mqttdemo2.utils.LogUtil;

import java.util.List;

/**
 * Created by 小松松 on 2018/10/28.
 */

public class RainfallAdapter extends RecyclerView.Adapter {
    private Context mContext;
    private List<String> time_chain;//用来存放展示的序列，如1,2,3,4,5...
    private List<Float> mList;//用来展示下雨量

    public RainfallAdapter(Context mContext, List<String> time_chain, List<Float> mList) {
        this.mContext = mContext;
        this.time_chain = time_chain;
        this.mList = mList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_rainfall_item,parent,false);
        return new RainfallViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof RainfallViewHolder){
                RainfallViewHolder viewHolder = (RainfallViewHolder) holder;
                viewHolder.tv_time_chain.setText(time_chain.get(position));
                viewHolder.tv_vaule.setText(mList.get(position)+"");
                float result = getMaxValue();
                int value = (int) (result*10);
                viewHolder.show_value.setMax(value+1000);
                int value2 = (int) (mList.get(position)*10);
                viewHolder.show_value.setProgress(value2);

            }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }
    class RainfallViewHolder extends RecyclerView.ViewHolder {
        TextView tv_time_chain;
        TextView tv_vaule;
        ProgressBar show_value;
        public RainfallViewHolder(View itemView) {
            super(itemView);
            tv_time_chain = itemView.findViewById(R.id.time_chain_rain);
            tv_vaule = itemView.findViewById(R.id.value_rain);
            show_value = itemView.findViewById(R.id.ProgressBar_rain);
        }
    }

    public float getMaxValue(){
        float[] temp = new float[mList.size()];
        for (int i = 0; i < mList.size(); i++) {
            temp[i] = mList.get(i);
        }
        float a = temp[0];
        for (int i = 0; i < temp.length; i++) {
            if (temp[i] > a){
                a = temp[i];
            }
        }
        return a;
    }
    public void changeForRainfall(List<Float> list){
        this.mList = list;
        notifyDataSetChanged();
    }
}
