package com.don.myplace;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.don.myplace.model.Place;
import com.don.myplace.model.User;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{

    TextView infoText;
    final String TAG = "mainactivity";
    GoogleApiClient mGoogleApiClient;
    DatabaseReference firebaseDatabase;

    ValueEventListener valueEventListener;

    GoogleSignInAccount currentUser;

    OptionalPendingResult<GoogleSignInResult> opr;

    FirebaseListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        infoText = (TextView)findViewById(R.id.info_text);

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

        opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if(opr.isDone()) {
            infoText.setText("you are in");
            currentUser = opr.get().getSignInAccount();
        }
        else {
            infoText.setText("you are out");
        }

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
                    Toast.makeText(getApplicationContext(), "user found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        mAdapter = new FirebaseListAdapter<Place>(this, Place.class, R.layout.row_item_layout, firebaseDatabase.child("users").child(currentUser.getDisplayName()).child("places")) {
            @Override
            protected void populateView(View view, Place place, int i) {
                ((TextView)view.findViewById(R.id.placeTitle)).setText(place.getTitle());
                ((TextView)view.findViewById(R.id.placeType)).setText(place.getType());
                ((TextView)view.findViewById(R.id.placeAddress)).setText(place.getAddress());
                ((TextView)view.findViewById(R.id.placeNumber)).setText(place.getTelephone());
            }
        };
        listView.setAdapter(mAdapter);
        firebaseDatabase.addValueEventListener(valueEventListener);

    }

    @Override
    protected void onStart() {
        super.onStart();

        opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if(opr.isDone()) {
            infoText.setText("you are in");
            currentUser = opr.get().getSignInAccount();
        }
        else {
            infoText.setText("you are out");
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
}
