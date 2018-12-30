package com.ushikun.musicplayer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by user on 2018/10/07.
 */

public class CustomAdapter extends BaseAdapter {
    private final Context mContext;
    private final LayoutInflater mInflater;
    private final int mResource;
    private List<MusicItem> mObject;


    CustomAdapter(Context context, int resource, List<MusicItem> objects) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mResource = resource;
        mObject = objects;
    }


    @Override
    public int getCount() {
        return mObject.size();
    }

    @Override
    public Object getItem(int position) {
        return mObject.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mObject.get(position).id;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final View view;

        if (convertView == null) {
            view = mInflater.inflate(mResource, parent, false);
        } else {
            view = convertView;
        }


        ((TextView) view.findViewById(R.id.title)).setText(mObject.get(position).title);
        ((TextView) view.findViewById(R.id.artist)).setText(mObject.get(position).artist);
        String str="";
        if((int)(mObject.get(position).duration/1000 )% 60<10){
            str="0";
        }
        ((TextView) view.findViewById(R.id.duration)).setText(String.valueOf((int) (mObject.get(position).duration / 60000)) + ":" +str+String.valueOf(mObject.get(position).duration/1000 % 60));

        return view;


    }

}
