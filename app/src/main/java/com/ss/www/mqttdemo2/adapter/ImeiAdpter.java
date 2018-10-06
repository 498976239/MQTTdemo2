package com.ss.www.mqttdemo2.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ss.www.mqttdemo2.Bean.HeadInfo;
import com.ss.www.mqttdemo2.Bean.MyMessage;
import com.ss.www.mqttdemo2.R;

import java.util.List;

/**
 * Created by 小松松 on 2018/9/24.
 */

public class ImeiAdpter extends RecyclerView.Adapter {
    private static final int VIEW_TYPE = -1;
    private Context mContext;
    private List<HeadInfo> mList;

    public ImeiAdpter(Context mContext, List<HeadInfo> mList) {
        this.mContext = mContext;
        this.mList = mList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (VIEW_TYPE == viewType ){
            View v = LayoutInflater.from(mContext).inflate(R.layout.layout__empty,parent,false);
            return new EmptyViewHoldr(v);
        }
        View v = LayoutInflater.from(mContext).inflate(R.layout.layout_item2,parent,false);
        return new ImeiViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
            if (holder instanceof ImeiViewHolder){
                final ImeiViewHolder holder1 = (ImeiViewHolder) holder;
                holder1.tv.setText(mList.get(position).getName());
                holder1.count.setText(mList.get(position).getCount());
                holder1.checkBox.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            holder1.tv.setTextColor(Color.WHITE);//让字体变回白色
                            holder1.count.setTextColor(Color.WHITE);
                            mList.get(position).setMark(false);//让信息没有被标记
                            holder1.checkBox.setClickable(false);//让选择框不能被点击
                        }
                    });

                    if (mList.get(position).isMark()){//如果信息被标记
                        holder1.checkBox.setChecked(true);//选择框勾选
                        holder1.tv.setTextColor(Color.GREEN);//字体变绿
                        holder1.count.setTextColor(Color.GREEN);//字体变绿
                        holder1.checkBox.setClickable(true);//选择框可以被点击
                    }else {
                        holder1.checkBox.setChecked(false);
                        holder1.tv.setTextColor(Color.WHITE);
                        holder1.count.setTextColor(Color.WHITE);
                        holder1.checkBox.setClickable(false);
                    }

                if (onItemClickListener != null){
                    holder1.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onItemClickListener.onItemClick(v,position);

                        }
                    });
                }
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
    class EmptyViewHoldr extends RecyclerView.ViewHolder{
        ProgressBar empty;
        public EmptyViewHoldr(View itemView) {
            super(itemView);
            empty = itemView.findViewById(R.id.empty);
        }
    }

     class ImeiViewHolder extends RecyclerView.ViewHolder{
        TextView tv;
         CheckBox checkBox;
         TextView count;
        public ImeiViewHolder(View itemView) {
            super(itemView);
            tv = itemView.findViewById(R.id.imei_info);
            checkBox = itemView.findViewById(R.id.mark);
            count = itemView.findViewById(R.id.count);
        }
    }
    public interface OnItemClickListener{
        void onItemClick(View view, int position);
    }
    private OnItemClickListener onItemClickListener;
    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }
    public void setFilter(List<HeadInfo> list){
        mList = list;
        notifyDataSetChanged();
    }
}
