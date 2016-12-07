package com.don.myplace.fragment;


import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;


import com.don.myplace.R;
import com.don.myplace.model.Place;

/**
 * Created by dli on 12/7/2016.
 */

public class PlaceDetailFragment extends DialogFragment{

    private static final String PLACE_IN_FRAGMENT = "placeInFragment";
    String TAG = "placedetail";
    private static Place place;

    EditText titleTxt;
    EditText addressTxt;
    EditText typeTxt;
    EditText numberTxt;

    public static PlaceDetailFragment newInstance(Place place) {
        PlaceDetailFragment fragment = new PlaceDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(PLACE_IN_FRAGMENT, place);
        fragment.setArguments(bundle);
        //setPlace(place);
        return fragment;
    }

    public static void setPlace(Place place) {
        PlaceDetailFragment.place = place;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Detail");

        setPlace((Place)getArguments().getSerializable(PLACE_IN_FRAGMENT));
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.detail_fragment, null);
        titleTxt = (EditText)view.findViewById(R.id.detail_title_txt);
        addressTxt = (EditText)view.findViewById(R.id.detail_addr_txt);
        typeTxt = (EditText)view.findViewById(R.id.detail_type_txt);
        numberTxt = (EditText)view.findViewById(R.id.detail_num_txt);

        builder.setView(view);

        titleTxt.setText(place.getTitle());
        addressTxt.setText(place.getAddress());
        typeTxt.setText(place.getType());
        numberTxt.setText(place.getTelephone());

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        return builder.create();
    }
}
