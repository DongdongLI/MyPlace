package com.don.myplace.adapter;

import android.content.Context;
import android.location.Address;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.don.myplace.R;
import com.don.myplace.model.SavedPlace;

import java.util.List;

/**
 * Created by dli on 12/15/2016.
 */

public class AddressAdapter extends ArrayAdapter<SavedPlace> {
    List<SavedPlace> mData;
    Context mContext;
    int mResource;

    public AddressAdapter(Context context, int resource,List<SavedPlace> objects) {
        super(context, resource, objects);
        this.mContext=context;
        this.mData=objects;
        this.mResource=resource;
        //Log.d("in adapter", mData.toString());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView==null)
        {
            LayoutInflater inflater=(LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView=inflater.inflate(mResource, parent, false);
        }
        SavedPlace savedPlace=mData.get(position);


        ((TextView) convertView.findViewById(R.id.placeTitle)).setText(savedPlace.getTitle());
        ((TextView) convertView.findViewById(R.id.placeAddress)).setText(savedPlace.getAddress());

        // hide delete icon in search result
        convertView.findViewById(R.id.delete_img).setVisibility(View.GONE);


        return convertView;
    }

}
