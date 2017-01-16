package com.don.myplace;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.don.myplace.adapter.AddressAdapter;
import com.don.myplace.model.SavedPlace;
import com.don.myplace.parser.PlaceParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dli on 12/15/2016.
 */

public class PlaceSearchFragment extends DialogFragment {

    String TAG = "PlaceSearchFragment";
    private static View view;
    ListView searchResultListView;
    EditText searchEditText;
    Button searchBtn;

    List<SavedPlace> searchResultList;

    ArrayAdapter<SavedPlace> adapter;

    static String APIKEY = null;

    private ManipulateDataInFragment listener;

    // TODO: will be useful when saving the new places
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

        if(APIKEY == null){
            try {
                ApplicationInfo ai = ((Context) listener).getPackageManager().getApplicationInfo(((Context) listener).getPackageName(), PackageManager.GET_META_DATA);
                APIKEY = ai.metaData.getString("com.google.android.geo.API_KEY");
            }catch (PackageManager.NameNotFoundException e){

            }
        }
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

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Search");

        LayoutInflater inflater = getActivity().getLayoutInflater();

        searchResultList = new ArrayList<>();

        try {
            view = inflater.inflate(R.layout.place_search_fragment, null);
        } catch (InflateException e){

        }
        searchEditText = (EditText) view.findViewById(R.id.search_txt);
        searchBtn = (Button)view.findViewById(R.id.search_btn);
        searchResultListView = (ListView)view.findViewById(R.id.search_result_list);
        adapter = new AddressAdapter(getActivity(), R.layout.row_item_layout_simple, searchResultList);

        searchResultListView.setAdapter(adapter);
        adapter.setNotifyOnChange(true);

        searchBtn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(searchEditText.getText().length()==0 || searchEditText.getText().toString().length()==0) {
                            Toast.makeText((Context)listener, "Search text should not be empty. ",Toast.LENGTH_SHORT).show();
                        }
                        else if(MainActivity.currLocation == null)
                            Toast.makeText((Context)listener, "Still trying to find your location. One sec... ",Toast.LENGTH_SHORT).show();
                        else{
                            String keyword = searchEditText.getText().toString().trim();
                            new GetData().execute(keyword);

                            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

                        }
                    }
                }
        );

        searchResultListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        new MainActivity.SavePlaceByInjectingPhonenumber().execute(searchResultList.get(position));
                        getDialog().dismiss();
                    }
                }
        );

        builder.setView(view);
        return builder.create();
    }

    class GetData extends AsyncTask<String, Void, List<SavedPlace>> {
        BufferedReader br;

        @Override
        protected List<SavedPlace> doInBackground(String... params) {
            try {
                String getPlaceIdUrl = "https://maps.googleapis.com/maps/api/place/textsearch/json?query="
                        + URLEncoder.encode(params[0], "UTF-8")
                        +"&location="+ MainActivity.currLocation.getLatitude() + "," + MainActivity.currLocation.getLongitude()
                        + "&radius=50000"
                        + "&key=" + APIKEY;
                URL url = new URL(getPlaceIdUrl);
                Log.d(TAG, "url is: "+getPlaceIdUrl);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");//POST

                br = new BufferedReader(new InputStreamReader(con.getInputStream()));

                StringBuilder sb = new StringBuilder();
                String line = "";
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                return PlaceParser.parsePlace(sb.toString());
            } catch (Exception e) {
                Log.d(TAG, e.getMessage());
            }
            return null;

        }

        @Override
        protected void onPostExecute(List<SavedPlace> savedPlaces) {
            super.onPostExecute(savedPlaces);
            searchResultList.clear();
            if(savedPlaces != null) {
                searchResultList.addAll(savedPlaces);
                adapter.notifyDataSetChanged();
            }
            else
                Log.d(TAG, "it is null...");
        }
    }
}
