package com.ss.www.mqttdemo2.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ss.www.mqttdemo2.Bean.Person;
import com.ss.www.mqttdemo2.R;

import java.util.List;

/**
 * Created by 小松松 on 2018/10/11.
 */

public class PersonAdapter extends BaseAdapter {
    private Context mContext;
    private List<Person> mList;

    public PersonAdapter(Context mContext, List<Person> mList) {
        this.mContext = mContext;
        this.mList = mList;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null){
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.layout_popwindow_item,null);
            viewHolder.mRelativeLayout = convertView.findViewById(R.id.popWindow_item);
            viewHolder.mTextView = convertView.findViewById(R.id.input_select_item_account);
            viewHolder.mImageView = convertView.findViewById(R.id.input_select_item_delete);
            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.mTextView.setText(mList.get(position).getName());
        viewHolder.mRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemClickListener.onItemClicked(position);
            }
        });
        viewHolder.mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnDelBtnClickListener.onDelBtnClicked(position);
            }
        });
        return convertView;
    }
    class ViewHolder{
        RelativeLayout mRelativeLayout;
        TextView mTextView;
        ImageView mImageView;
    }
    private OnItemClickListener mOnItemClickListener;
    public interface OnItemClickListener{
        void onItemClicked(int position);
    }
    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        mOnItemClickListener = onItemClickListener;
    }
    private OnDelBtnClickListener mOnDelBtnClickListener;
    public interface OnDelBtnClickListener{
        void onDelBtnClicked(int position);
    }
    public void setOnDelBtnClickListener(OnDelBtnClickListener onDeleteBtnClickListener){
        mOnDelBtnClickListener = onDeleteBtnClickListener;
    }
}
