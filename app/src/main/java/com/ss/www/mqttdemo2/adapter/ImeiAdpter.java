package com.ss.www.mqttdemo2.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
    private boolean change;//用来改变空界面的显示

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
                if (onSettingItemClickListener != null){
                    holder1.set_para.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onSettingItemClickListener.onSettingItemClick(v,position);
                        }
                    });
                }

                holder1.checkBox.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            holder1.tv.setTextColor(Color.WHITE);//让字体变回白色
                            holder1.count.setTextColor(Color.WHITE);
                            mList.get(position).setMark(false);//让信息没有被标记
                            mList.get(position).setLast(false);
                            holder1.checkBox.setClickable(false);//让选择框不能被点击
                        }
                    });
                holder1.checkBox2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        holder1.tv.setTextColor(Color.WHITE);//让字体变回白色
                        holder1.count.setTextColor(Color.WHITE);
                        mList.get(position).setMark(false);//让信息没有被标记
                        mList.get(position).setLast(false);
                        holder1.checkBox.setClickable(false);//让选择框不能被点击
                        holder1.checkBox.setChecked(false);
                        holder1.checkBox.setVisibility(View.VISIBLE);
                        holder1.checkBox2.setVisibility(View.GONE);
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
                    if (mList.get(position).isMark()&&mList.get(position).isLast()){
                        //当是最新点击的对象checkBox2要显现并且勾选，checkBox1消失
                        holder1.checkBox2.setChecked(true);//选择框勾选
                        holder1.tv.setTextColor(Color.RED);//字体变绿
                        holder1.checkBox2.setVisibility(View.VISIBLE);
                        holder1.checkBox.setVisibility(View.INVISIBLE);
                        holder1.count.setTextColor(Color.RED);//字体变绿
                        //holder1.checkBox.setClickable(true);//选择框可以被点击
                    }
                    if (!mList.get(position).isLast()){//当不是最新的点击对象时，让checkBox2消失，checkBox显示
                        holder1.checkBox2.setVisibility(View.GONE);
                        holder1.checkBox.setVisibility(View.VISIBLE);
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
            if (holder instanceof EmptyViewHoldr){
                EmptyViewHoldr viewHoldr = (EmptyViewHoldr) holder;
                if (change){
                    viewHoldr.empty.setVisibility(View.GONE);
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
         CheckBox checkBox2;
         TextView count;
         Button set_para;
        public ImeiViewHolder(View itemView) {
            super(itemView);
            tv = itemView.findViewById(R.id.imei_info);
            checkBox = itemView.findViewById(R.id.mark);
            checkBox2 = itemView.findViewById(R.id.mark2);
            count = itemView.findViewById(R.id.count);
            set_para = itemView.findViewById(R.id.setting_para);
        }
    }
    public interface OnItemClickListener{
        void onItemClick(View view, int position);
    }
    private OnItemClickListener onItemClickListener;
    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }


    public interface OnSettingItemClickListener{
         void onSettingItemClick(View view, int position);
    }

    private OnSettingItemClickListener onSettingItemClickListener;

    public void setSettingOnItemClickListener(OnSettingItemClickListener onSettingItemClickListener){
        this.onSettingItemClickListener = onSettingItemClickListener;
    }
    public void setFilter(List<HeadInfo> list){
        mList = list;
        change = true;
        notifyDataSetChanged();
    }
}
