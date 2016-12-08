package com.don.myplace.fragment;


import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;


import com.don.myplace.ManipulateDataInFragment;
import com.don.myplace.R;
import com.don.myplace.model.SavedPlace;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.MarkerOptions;

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

    MapFragment mapFragment;
    GoogleMap googleMap;

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
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Detail");

        setPlace((SavedPlace)getArguments().getSerializable(PLACE_IN_FRAGMENT));
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.detail_fragment, null);
        titleTxt = (EditText)view.findViewById(R.id.detail_title_txt);
        addressTxt = (EditText)view.findViewById(R.id.detail_addr_txt);
        typeTxt = (EditText)view.findViewById(R.id.detail_type_txt);
        numberTxt = (EditText)view.findViewById(R.id.detail_num_txt);

        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(
                new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap googleMap) {
                        googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);

                    }
                }
        );

        builder.setView(view);

        // populate the editTexts
        titleTxt.setText(place.getTitle());
        addressTxt.setText(place.getAddress());
        typeTxt.setText(place.getType());
        numberTxt.setText(place.getTelephone());

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

}
