package com.don.myplace;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.don.myplace.fragment.PlaceDetailFragment;
import com.don.myplace.model.SavedPlace;
import com.don.myplace.model.User;
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
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, ManipulateDataInFragment{

    TextView infoText;
    Button addPlaceBtn;

    final String TAG = "mainactivity";
    GoogleApiClient mGoogleApiClient;
    DatabaseReference firebaseDatabase;

    ValueEventListener valueEventListener;

    GoogleSignInAccount currentUser;

    OptionalPendingResult<GoogleSignInResult> opr;

    FirebaseListAdapter mAdapter;

    final int PLACE_PICKER_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        infoText = (TextView)findViewById(R.id.info_text);
        addPlaceBtn = (Button)findViewById(R.id.add_new_place_btn);
        ListView listView = (ListView)findViewById(R.id.placeList);

        // google sign in stuff
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        firebaseDatabase = FirebaseDatabase.getInstance().getReference();

        // attempt to sign in silently
        opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if(opr.isDone()) {
            currentUser = opr.get().getSignInAccount();
            infoText.setText(currentUser.getDisplayName());
            if(currentUser == null)
                Log.d(TAG, "what the hell?");
        }
        else {
            infoText.setText("you are out");
        }

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

        ((Button)findViewById(R.id.mock)).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        firebaseDatabase.child("users").child(currentUser.getDisplayName()).child("places").child("mocknew").setValue(new SavedPlace("","","",""));
                    }
                }
        );
    }

    @Override
    protected void onStart() {
        super.onStart();

        opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if(opr.isDone()) {
            currentUser = opr.get().getSignInAccount();
            infoText.setText(currentUser.getDisplayName());
        }
        else {
            infoText.setText("you are out");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == PLACE_PICKER_REQUEST
                && resultCode == Activity.RESULT_OK) {

            final Place place = PlacePicker.getPlace(this, data);
            final CharSequence name = place.getName();
            final CharSequence address = place.getAddress();
            String attributions = (String) place.getAttributions();

            SavedPlace newPlace = new SavedPlace(place.getName().toString(), place.getPlaceTypes().toString(), place.getAddress().toString(), place.getPhoneNumber().toString());
            newPlace.setPlaceId(place.getId());

            saveData(newPlace);
            if (attributions == null) {
                attributions = "";
            }

            Log.d(TAG, place.toString());

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //firebaseDatabase.removeEventListener(valueEventListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //firebaseDatabase.addValueEventListener(valueEventListener);
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


}
