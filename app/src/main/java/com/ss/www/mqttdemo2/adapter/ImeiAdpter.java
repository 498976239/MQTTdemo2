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
 * Created by 小松松 on 2018/9/24.
 */

public class ImeiAdpter extends RecyclerView.Adapter {
    private static final int VIEW_TYPE = -1;
    private Context mContext;
    private List<String> mList;

    public ImeiAdpter(Context mContext, List<String> mList) {
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
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            if (holder instanceof ImeiViewHolder){
                ImeiViewHolder holder1 = (ImeiViewHolder) holder;
                holder1.tv.setText(mList.get(position));

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
         TextView tv2;
        public ImeiViewHolder(View itemView) {
            super(itemView);
            tv = itemView.findViewById(R.id.imei_info);

        }
    }
    public interface OnItemClickListener{
        void onItemClick(View view, int position);
    }
    private OnItemClickListener onItemClickListener;
    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }
    public void setFilter(List<String> list){
        mList = list;
        notifyDataSetChanged();
    }
}
