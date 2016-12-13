package com.don.myplace.fragment;


import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;


import com.don.myplace.ManipulateDataInFragment;
import com.don.myplace.R;
import com.don.myplace.model.SavedPlace;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

/**
 * Created by dli on 12/7/2016.
 */

public class PlaceDetailFragment extends DialogFragment{

    private static final String PLACE_IN_FRAGMENT = "placeInFragment";
    String TAG = "placedetail";
    private static SavedPlace place;

    EditText titleTxt;
    EditText addressTxt;
    EditText typeTxt;
    EditText numberTxt;
    ImageView callBtnImg;

    MapFragment mapFragment;
    GoogleMap googleMap;

    private static View view;

    private ManipulateDataInFragment listener;

    public static PlaceDetailFragment newInstance(SavedPlace place) {
        PlaceDetailFragment fragment = new PlaceDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(PLACE_IN_FRAGMENT, place);
        fragment.setArguments(bundle);
        //setPlace(place);
        return fragment;
    }

    public static void setPlace(SavedPlace place) {
        PlaceDetailFragment.place = place;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof ManipulateDataInFragment)
            listener = (ManipulateDataInFragment) context;
    }

    @Override
    public void onDetach() {
        listener = null;
        super.onDetach();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        if(view != null && view.getParent()!= null)
            ((ViewGroup)view.getParent()).removeView(view);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Detail");

        setPlace((SavedPlace)getArguments().getSerializable(PLACE_IN_FRAGMENT));
        LayoutInflater inflater = getActivity().getLayoutInflater();

        try {
            view = inflater.inflate(R.layout.detail_fragment, null);
        } catch (InflateException e){

        }
        titleTxt = (EditText)view.findViewById(R.id.detail_title_txt);
        addressTxt = (EditText)view.findViewById(R.id.detail_addr_txt);
        typeTxt = (EditText)view.findViewById(R.id.detail_type_txt);
        numberTxt = (EditText)view.findViewById(R.id.detail_num_txt);
        callBtnImg = (ImageView)view.findViewById(R.id.call_btn);

        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(
                new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap googleMap) {
                        Address address = new GeoTask(getActivity()).doInBackground(place.getAddress().toString());
                        // this is the marker itself
                        Marker marker = googleMap.addMarker((new MarkerOptions()
                                .position(new LatLng(address.getLatitude(), address.getLongitude()))
                                .draggable(false)
                        ));
                        googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                        // this will move the camera to certain area and zoom in
                        CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(new LatLng(address.getLatitude(), address.getLongitude()), 16F);
                        googleMap.moveCamera(cu);
                    }
                }
        );

        builder.setView(view);

        // populate the editTexts
        titleTxt.setText(place.getTitle());
        addressTxt.setText(place.getAddress());
        typeTxt.setText(place.getType());
        numberTxt.setText(place.getTelephone());
        callBtnImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+place.getTelephone()));
                startActivity(callIntent);
            }
        });

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                place.setAddress(addressTxt.getText().toString());
                place.setTitle(titleTxt.getText().toString());
                place.setType(typeTxt.getText().toString());
                place.setTelephone(numberTxt.getText().toString());
                listener.saveData(place);
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });



        return builder.create();
    }


    class GeoTask extends AsyncTask<String, Void, Address>
    {
        Context mContext;
        public GeoTask(Context context)
        {
            this.mContext=context;
        }
        @Override
        protected Address doInBackground(String... params) {
            List<Address> addressList=null;

            Geocoder geocoder=new Geocoder(mContext);

            try {
                addressList = geocoder.getFromLocationName(params[0], 5);
                if(addressList == null || addressList.size()==0)
                    return null;
                return addressList.get(0);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(Address result) {
            if(result==null)
            {
                Log.d(TAG,"No result found");
            }
            else
            {
                Log.d(TAG,result.toString());

            }
        }

    }
}
