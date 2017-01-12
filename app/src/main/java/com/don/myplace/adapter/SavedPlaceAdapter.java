package com.don.myplace.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.don.myplace.R;
import com.don.myplace.model.SavedPlace;

import java.util.List;

/**
 * Created by dli on 1/12/2017.
 */

public class SavedPlaceAdapter extends ArrayAdapter<SavedPlace> {
    List<SavedPlace> mData;
    Context mContext;
    int mResource;

    public SavedPlaceAdapter(Context context, int resource, List<SavedPlace> objects) {
        super(context, resource, objects);
        this.mData = objects;
        this.mContext = context;
        this.mResource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView==null)
        {
            LayoutInflater inflater=(LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView=inflater.inflate(mResource, parent, false);
        }
        SavedPlace place=mData.get(position);

        ((TextView)convertView.findViewById(R.id.placeTitle)).setText(place.getTitle());
        ((TextView)convertView.findViewById(R.id.placeAddress)).setText(place.getAddress());

        return convertView;
    }
}
