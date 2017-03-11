package com.don.myplace;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.don.myplace.adapter.SavedPlaceAdapter;
import com.don.myplace.model.SavedPlace;
import com.don.myplace.model.User;
import com.don.myplace.parser.PlaceParser;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.GoogleMap;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, ManipulateDataInFragment{


    TextView infoText;
    Button addPlaceBtn;
    ListView listView;

    final String TAG = "mainactivity";
    GoogleApiClient mGoogleApiClient;


    static DatabaseReference firebaseDatabase;

    ValueEventListener valueEventListener;

    static GoogleSignInAccount currentUser;

    OptionalPendingResult<GoogleSignInResult> opr;

    FirebaseListAdapter mAdapter;
    SavedPlaceAdapter filteredAdapter;
    List<SavedPlace> filteredSavedPlace;

    LocationManager locationManager;
    LocationListener locationListener;

    static Location currLocation;

    final int PLACE_PICKER_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        infoText = (TextView)findViewById(R.id.info_text);
        addPlaceBtn = (Button)findViewById(R.id.add_new_place_btn);
        listView = (ListView)findViewById(R.id.placeList);

        filteredSavedPlace = new ArrayList<>();

        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        locationListener = new MyLocationListener();

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 111);
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 1, locationListener);
        }
        catch (SecurityException e){
            Log.d(TAG, "No permission. ");
        }

        currentUser = getIntent().getExtras().getParcelable("currentUser");
        infoText.setText("Hi "+currentUser.getDisplayName());
        /*
        * Note the sign in user will be passed in from the previous activity*/
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        firebaseDatabase = FirebaseDatabase.getInstance().getReference();


        // in case data change in fire base
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // If user does not exist, create him
                if (dataSnapshot.child("users").child(currentUser.getDisplayName()).getValue()==null) {
                    Toast.makeText(getApplicationContext(), "user not found, creating one...", Toast.LENGTH_SHORT).show();
                    // create user
                    createUser();
                }
                else{
                    //Toast.makeText(getApplicationContext(), "user found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        mAdapter = new FirebaseListAdapter<SavedPlace>(this, SavedPlace.class, R.layout.row_item_layout_simple, firebaseDatabase.child("users").child(currentUser.getDisplayName()).child("places")) {
            @Override
            protected void populateView(View view, final SavedPlace place, int i) {
                ((TextView)view.findViewById(R.id.placeTitle)).setText(place.getTitle());
                ((TextView)view.findViewById(R.id.placeAddress)).setText(place.getAddress());

                // the deleteIcon
                view.findViewById(R.id.delete_img).setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // find which savedPlace is in this row
                                Log.d(TAG,"image is hit");
                                firebaseDatabase.child("users").child(currentUser.getDisplayName()).child("places").child(place.getPlaceId()).setValue(null);
                            }
                        });
            }
        };
        listView.setAdapter(mAdapter);
        firebaseDatabase.addValueEventListener(valueEventListener);

        // this adapter is used for the filtered subset of all of the places
        filteredAdapter = new SavedPlaceAdapter(this, R.layout.row_item_layout_simple, filteredSavedPlace);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SavedPlace place = (SavedPlace)parent.getItemAtPosition(position);
                Log.d(TAG, "I'm clicked... "+place);

                DialogFragment newFragment = PlaceDetailFragment.newInstance(place);
                newFragment.show(getFragmentManager(), "detail");
            }
        });



        addPlaceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    PlacePicker.IntentBuilder intentBuilder =
                            new PlacePicker.IntentBuilder();
                    Intent intent = intentBuilder.build(MainActivity.this);
                    startActivityForResult(intent, PLACE_PICKER_REQUEST);

                } catch (GooglePlayServicesRepairableException
                        | GooglePlayServicesNotAvailableException e) {
                    Log.d(TAG, "fail to build place picker intent ");
                    Log.d(TAG, e.getMessage());
                    e.printStackTrace();
                }
            }
        });

        findViewById(R.id.logout_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                        new ResultCallback<Status>() {
                            @Override
                            public void onResult(@NonNull Status status) {
                                finish();
                            }
                        }
                );
            }
        });

        findViewById(R.id.add_new_place_withGoogleAPI_btn).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DialogFragment newFragment = new PlaceSearchFragment();
                        newFragment.show(getFragmentManager(), "search");
                    }
                }
        );

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.sort_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item1:
                //Log.d("demo1","item 1 clicked");
                showEntryBasedOnCategory("all");
                break;

            case R.id.menu_item2:
                Log.d("demo1","item 2 clicked");
                showEntryBasedOnCategory("restaurant");
                break;

            case R.id.menu_item3:
                showEntryBasedOnCategory("people's place");
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showEntryBasedOnCategory(final String category) {
        if(category.equals("all")){
            listView.setAdapter(mAdapter);
            firebaseDatabase.addValueEventListener(valueEventListener);
            return;
        }

        listView.setAdapter(filteredAdapter);
        filteredAdapter.setNotifyOnChange(true);

        firebaseDatabase.child("users").child(currentUser.getDisplayName()).child("places").orderByChild("title").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot item: dataSnapshot.getChildren()){
                        if(item.child("type").toString().contains(category+ "") || item.child("type").toString().contains(Place.TYPE_RESTAURANT+ "")) {
                            filteredAdapter.add( new SavedPlace(item.getValue().toString()) );
                            //Log.d(TAG, filteredSavedPlace.toString());
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == PLACE_PICKER_REQUEST
                && resultCode == Activity.RESULT_OK) {

            final Place place = PlacePicker.getPlace(this, data);
            SavedPlace newPlace = new SavedPlace(place.getName().toString(), place.getPlaceTypes().toString(), place.getAddress().toString(), place.getPhoneNumber().toString());
            newPlace.setPlaceId(place.getId());

            saveData(newPlace);

            Log.d(TAG, place.toString());

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "login fail: "+connectionResult);
    }

    private void createUser() {
        User user = new User(currentUser.getDisplayName(), currentUser.getEmail(), null);
        firebaseDatabase.child("users").child(user.getDisplayName()).setValue(user);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAdapter.cleanup();
    }

    @Override
    public void saveData(SavedPlace place) {
        firebaseDatabase.child("users").child(currentUser.getDisplayName()).child("places").child(place.getPlaceId()).setValue(place);
    }

    public static void staticSaveData(SavedPlace place) {
        firebaseDatabase.child("users").child(currentUser.getDisplayName()).child("places").child(place.getPlaceId()).setValue(place);
    }

    class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            Log.d(TAG, location.toString());
            if(currLocation == null)
                currLocation = location;
            else {
                currLocation.setLatitude(location.getLatitude());
                currLocation.setLongitude(location.getLongitude());
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            //Log.d(TAG, "changed!!");
        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }

    }


    static class SavePlaceByInjectingPhonenumber extends AsyncTask<SavedPlace, Void, SavedPlace> {
        BufferedReader br;

        @Override
        protected SavedPlace doInBackground(SavedPlace... params) {
            try {
                String getPlaceIdUrl = "https://maps.googleapis.com/maps/api/place/details/json?"
                        + "placeid="+params[0].getPlaceId()
                        + "&key=" + PlaceSearchFragment.APIKEY;
                URL url = new URL(getPlaceIdUrl);
                Log.d("PlaceSearchFragment", "url is: "+getPlaceIdUrl);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");//POST

                br = new BufferedReader(new InputStreamReader(con.getInputStream()));

                StringBuilder sb = new StringBuilder();
                String line = "";
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }

                params[0].setTelephone(PlaceParser.parsePhoneNumber(sb.toString()));

                return params[0];
            } catch (Exception e) {
                Log.d("PlaceSearchFragment", e.getMessage());
            }
            return null;

        }

        @Override
        protected void onPostExecute(SavedPlace res) {
            super.onPostExecute(res);
            staticSaveData(res);
            //Log.d("PlaceSearchFragment", "phone number is: "+ res);
        }
    }
}
