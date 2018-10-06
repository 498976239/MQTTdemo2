package com.ss.www.mqttdemo2.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ss.www.mqttdemo2.Bean.MyMessage;
import com.ss.www.mqttdemo2.R;

import java.util.List;

/**
 * Created by 小松松 on 2018/9/20.
 */

public class MainAdapter extends RecyclerView.Adapter {
    private static final int VIEW_TYPE = -1;
    private Context mContext;
    private List<MyMessage> mList;

    public MainAdapter(Context mContext, List<MyMessage> mList) {
        this.mContext = mContext;
        this.mList = mList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (VIEW_TYPE == viewType ){
            View v = LayoutInflater.from(mContext).inflate(R.layout.layout__empty,parent,false);
            return new MainEmptyViewHoldr(v);
        }
        View v = LayoutInflater.from(mContext).inflate(R.layout.layout_item,parent,false);
        return new MainViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MainViewHolder){
            MainViewHolder viewHolder = (MainViewHolder) holder;
            viewHolder.t2.setText(mList.get(position).getVoltage());
            viewHolder.t4.setText(mList.get(position).getChannelNO());
            if (mList.get(position).getD1().length()>6){
                viewHolder.t5.setText((mList.get(position).getD1()).substring(0,6));
            }else {
                viewHolder.t5.setText((mList.get(position).getD1()));
            }
            if (mList.get(position).getD2().length()>6){
                viewHolder.t6.setText((mList.get(position).getD2()).substring(0,6));
            }else {
                viewHolder.t6.setText((mList.get(position).getD2()));
            }
            if (mList.get(position).getD3().length()>6){
                viewHolder.t7.setText((mList.get(position).getD3()).substring(0,6));
            }else {
                viewHolder.t7.setText((mList.get(position).getD3()));
            }

            viewHolder.t8.setText(mList.get(position).getMeasureTime());
        }

    }

    @Override
    public int getItemCount() {
        return mList.size() > 0 ? mList.size() : 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (mList.size() <= 0) {
            return VIEW_TYPE;
        }
        return super.getItemViewType(position);
    }

    class MainViewHolder extends RecyclerView.ViewHolder{

        TextView t2;

        TextView t4;
        TextView t5;
        TextView t6;
        TextView t7;
        TextView t8;
        public MainViewHolder(View itemView) {
            super(itemView);
            t2 = itemView.findViewById(R.id.vol);
            t4 = itemView.findViewById(R.id.channel);
            t5 = itemView.findViewById(R.id.data1);
            t6 = itemView.findViewById(R.id.data2);
            t7 = itemView.findViewById(R.id.data3);
            t8 = itemView.findViewById(R.id.time);
        }
    }
    class MainEmptyViewHoldr extends RecyclerView.ViewHolder{
        ProgressBar empty;
        public MainEmptyViewHoldr(View itemView) {
            super(itemView);
            empty = itemView.findViewById(R.id.empty);
        }
    }
}
