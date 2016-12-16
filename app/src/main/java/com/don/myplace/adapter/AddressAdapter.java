package com.don.myplace.adapter;

import android.content.Context;
import android.location.Address;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.don.myplace.R;

import java.util.List;

/**
 * Created by dli on 12/15/2016.
 */

public class AddressAdapter extends ArrayAdapter<Address> {
    List<Address> mData;
    Context mContext;
    int mResource;

    public AddressAdapter(Context context, int resource,List<Address> objects) {
        super(context, resource, objects);
        this.mContext=context;
        this.mData=objects;
        this.mResource=resource;
        Log.d("in adapter", mData.toString());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView==null)
        {
            LayoutInflater inflater=(LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView=inflater.inflate(mResource, parent, false);
        }
        Address address=mData.get(position);


        TextView addressDetail = (TextView) convertView.findViewById(R.id.placeTitle);
        String addrStr = "";
        int indexMax = address.getMaxAddressLineIndex();
        for(int i=1;i<=indexMax;i++)
            addrStr+=(address.getAddressLine(i))+", ";
        addressDetail.setText(addrStr.substring(0, addrStr.length()-2));
        return convertView;
    }

}
