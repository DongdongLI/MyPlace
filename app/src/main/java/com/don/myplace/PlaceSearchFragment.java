package com.don.myplace;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.database.DataSetObserver;
import android.location.Address;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.don.myplace.adapter.AddressAdapter;
import com.don.myplace.model.SavedPlace;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by dli on 12/15/2016.
 */

public class PlaceSearchFragment extends DialogFragment {

    String TAG = "PlaceSearchFragment";
    private static View view;
    ListView searchResultListView;
    EditText searchEditText;
    Button searchBtn;

    List<Address> searchResultList;

    private ManipulateDataInFragment listener;

    public static PlaceSearchFragment newInstance() {
        PlaceSearchFragment placeSearchFragment = new PlaceSearchFragment();
        Bundle bundle = new Bundle();

        placeSearchFragment.setArguments(bundle);
        return placeSearchFragment;
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

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        if(view != null && view.getParent()!= null)
            ((ViewGroup)view.getParent()).removeView(view);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Search");

        //setPlace((SavedPlace)getArguments().getSerializable(PLACE_IN_FRAGMENT));
        LayoutInflater inflater = getActivity().getLayoutInflater();

        searchResultList = new ArrayList<>();

        try {
            view = inflater.inflate(R.layout.place_search_fragment, null);
        } catch (InflateException e){

        }
        searchEditText = (EditText) view.findViewById(R.id.search_txt);
        searchBtn = (Button)view.findViewById(R.id.search_btn);
        searchResultListView = (ListView)view.findViewById(R.id.search_result_list);
        ArrayAdapter<Address> adapter = new AddressAdapter(getActivity(), R.layout.row_item_layout, searchResultList);

        searchResultListView.setAdapter(adapter);
        adapter.setNotifyOnChange(true);

        searchBtn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(searchEditText.getText().length()==0 || searchEditText.getText().toString().length()==0) {
                            Toast.makeText((Context)listener, "Search text should not be empty. ",Toast.LENGTH_SHORT).show();
                        }
                        else{
                            String keyword = searchEditText.getText().toString().trim();
                            List<Address> temp = new PlaceDetailFragment.GeoTask(getActivity(), 10).doInBackground(keyword);


                            searchResultList.clear();
                            searchResultList.addAll(temp) ;

                            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                        }
                    }
                }
        );



        builder.setView(view);
        return builder.create();
    }
}
