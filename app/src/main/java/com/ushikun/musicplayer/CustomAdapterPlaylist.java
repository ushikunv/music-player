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
 * Created by user on 2018/12/29.
 */

public class CustomAdapterPlaylist extends BaseAdapter{
    private final Context mContext;
    private final LayoutInflater mInflater;
    private final int mResource;
    private List<Playlist> mObject;



    CustomAdapterPlaylist(Context context, int resource, List<Playlist> objects) {
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


        ((TextView) view.findViewById(R.id.title)).setText(mObject.get(position).name);

        return view;


    }
}
