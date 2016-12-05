package com.don.myplace.ListviewAdapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.don.myplace.R;
import com.don.myplace.model.Place;

import java.util.List;

/**
 * Created by dli on 12/5/2016.
 */

public class PlaceAdapter extends ArrayAdapter<Place>{
    List<Place> mData;
    Context mContext;
    int mResource;

    public PlaceAdapter(Context context, int resource, List<Place> objects) {
        super(context, resource, objects);
        this.mData = objects;
        this.mContext = context;
        this.mResource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView==null){
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(mResource,parent, false);
        }

        Place place = mData.get(position);

        TextView placeTitle = (TextView)convertView.findViewById(R.id.placeTitle);
        TextView placeType = (TextView)convertView.findViewById(R.id.placeType);
        TextView placeAddress = (TextView)convertView.findViewById(R.id.placeAddress);
        TextView placeNumber = (TextView)convertView.findViewById(R.id.placeNumber);

        placeTitle.setText(place.getTitle());
        placeType.setText(place.getType());
        placeAddress.setText(place.getAddress());
        placeNumber.setText(place.getTelephone());

        return convertView;
    }
}
