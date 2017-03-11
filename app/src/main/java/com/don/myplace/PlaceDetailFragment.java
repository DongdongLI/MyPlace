package com.don.myplace;


import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.don.myplace.model.SavedPlace;
import com.don.myplace.parser.PlaceParser;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

/**
 * Created by dli on 12/7/2016.
 */

public class PlaceDetailFragment extends DialogFragment{

    private static final String PLACE_IN_FRAGMENT = "placeInFragment";
    String TAG = "placedetail";
    private static SavedPlace place;

    EditText titleTxt;
    EditText addressTxt;
    TextView distanceTxt;
    EditText numberTxt;
    ImageView callBtnImg;
    ImageView driveBtnImg;
    Button typeDropDown;

    MapFragment mapFragment;
    GoogleMap googleMap;

    private static View view;

    static String APIKEY = null;

    private ManipulateDataInFragment listener;

    public static PlaceDetailFragment newInstance(SavedPlace place) {
        PlaceDetailFragment fragment = new PlaceDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(PLACE_IN_FRAGMENT, place);
        fragment.setArguments(bundle);
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
        distanceTxt = (TextView)view.findViewById(R.id.distance_txt);
        numberTxt = (EditText)view.findViewById(R.id.detail_num_txt);
        callBtnImg = (ImageView)view.findViewById(R.id.call_btn);
        driveBtnImg = (ImageView)view.findViewById(R.id.drive_btn);
        typeDropDown = (Button)view.findViewById(R.id.typeDropDownBtn);

        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(
                new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap googleMap) {
                        googleMap.clear();
                        new GeoTask(getActivity(), 1).execute(place.getAddress().toString());
                    }
                }
        );


        builder.setView(view);

        // populate the editTexts
        titleTxt.setText(place.getTitle());
        addressTxt.setText(place.getAddress());
        //TODO: create a asyncronized function to calculate the distance and time of driving there
        new DistanceMatrixTask().execute(place.getAddress());
        numberTxt.setText(place.getTelephone());
        callBtnImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+place.getTelephone()));
                startActivity(callIntent);
            }
        });
        driveBtnImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("waze://?q="+place.getAddress()+"&navigate=yes")));
            }
        });

        typeDropDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(getActivity(), typeDropDown);
                //Inflating the Popup using xml file
                popup.getMenuInflater()
                        .inflate(R.menu.type_drop_down, popup.getMenu());

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        Toast.makeText(
                                getActivity(),
                                "You Clicked : " + item.getTitle(),
                                Toast.LENGTH_SHORT
                        ).show();
                        return true;
                    }
                });

                popup.show(); //showing popup menu
            }
        });


        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                place.setAddress(addressTxt.getText().toString());
                place.setTitle(titleTxt.getText().toString());
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

    private void addMarkerWhenReady(final List<Address> addresses) {
        mapFragment.getMapAsync(
                new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap googleMap) {
                        googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                        if (addresses != null) {
                            Address address = addresses.get(0);
                            // this is the marker itself
                            Marker marker = googleMap.addMarker((new MarkerOptions()
                                    .position(new LatLng(address.getLatitude(), address.getLongitude()))
                                    .draggable(false)
                            ));

                            // this will move the camera to certain area and zoom in
                            CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(new LatLng(address.getLatitude(), address.getLongitude()), 16F);
                            googleMap.moveCamera(cu);
                        }
                        else {
                            CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(new LatLng(0, 0), 16F);
                            googleMap.moveCamera(cu);
                        }
                    }

                });
    }

    class GeoTask extends AsyncTask<String, Void, List<Address>>
    {
        Context mContext;
        int limit;

        public GeoTask(Context context, int limit)
        {
            this.mContext=context;
            this.limit = limit;
        }


        @Override
        protected List<Address> doInBackground(String... params) {
            List<Address> addressList=null;
            Geocoder geocoder=new Geocoder(mContext);

            try {
                addressList = geocoder.getFromLocationName(params[0], limit);
                if(addressList == null || addressList.size()==0)
                    return null;
                return addressList;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(List<Address> result) {
            if(result==null || result.size()==0)
            {
                Log.d("GeoTask","No result found");
                addMarkerWhenReady(null);
            }
            else
            {
                Log.d("GeoTask",result.toString());
                addMarkerWhenReady(result);
            }
        }
    }

    class DistanceMatrixTask extends AsyncTask<String, Void, Map<String, String>>
    {
        BufferedReader br;

        @Override
        protected Map doInBackground(String... params) {
            Log.d(TAG, "in distance background");
            try {
                while (MainActivity.currLocation == null) {
                    Thread.sleep(500);
                    //Log.d(TAG, "still waiting for location");
                }
            }catch (InterruptedException e){
                Log.d(TAG, e.getMessage());
            }

            try {
                String getPlaceIdUrl = "https://maps.googleapis.com/maps/api/distancematrix/json?units=imperial"
                        + "&origins="+ MainActivity.currLocation.getLatitude() + "," + MainActivity.currLocation.getLongitude()
                        + "&destinations="+URLEncoder.encode(params[0], "UTF-8")
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
                return PlaceParser.parseDistanceAndTime(sb.toString());
            } catch (Exception e) {
                Log.d(TAG, e.getMessage());
            }
            return null;

        }

        @Override
        protected void onPostExecute(Map<String, String> res) {
            super.onPostExecute(res);
            distanceTxt.setText("Distance: "+res.get("distance")+"  "+"Time: "+res.get("duration"));
        }
    }
}
