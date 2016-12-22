package com.don.myplace;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

/**
 * Created by dli on 12/5/2016.
 */

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener{

    final String TAG = "login";
    private static final int RC_SIGN_IN = 9001;

    Button signInBtn;
    Button signOutBtn;
    TextView statusTextView;


    GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        signInBtn = (Button)findViewById(R.id.signin_btn);
        signOutBtn = (Button) findViewById(R.id.signout_btn);
        statusTextView = (TextView) findViewById(R.id.statusText);

        signInBtn.setOnClickListener(this);
        signOutBtn.setOnClickListener(this);


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();

        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if(opr.isDone()) {
            Log.d(TAG, "Got cached sign-in");
            GoogleSignInResult res = opr.get();
            handleSignInResult(res);
        }
        else {
            updateUI(false);
        }
    }

    private void handleSignInResult(GoogleSignInResult res) {
        if(res.isSuccess()){
            Log.d(TAG,"sign in successfully");
            GoogleSignInAccount acct = res.getSignInAccount();
            statusTextView.setText("Welcome "+ acct.getDisplayName());
            updateUI(true);

            Intent intent = new Intent(new Intent(LoginActivity.this, MainActivity.class));
            intent.putExtra("currentUser", acct);
            startActivity(intent);
        }
        else {
            Log.d(TAG,"sign in failed");
            updateUI(false);
        }
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
            new ResultCallback<Status>() {
                @Override
                public void onResult(@NonNull Status status) {
                    updateUI(false);
                }
            }
        );
    }

    private void revokeAccess() {
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        updateUI(false);
                    }
                }
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Toast.makeText(this,"requestCode: "+ requestCode, Toast.LENGTH_SHORT).show();

        if(requestCode == RC_SIGN_IN) {
            GoogleSignInResult res = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            Log.d(TAG,"in onActivityResult");
            handleSignInResult(res);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "login fail: "+connectionResult);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.signin_btn:
                signIn();
                break;
            case R.id.signout_btn:
                signOut();
                break;
        }
    }

    private void updateUI (boolean signedIn) {
        if(signedIn){
            signInBtn.setVisibility(View.GONE);
            signOutBtn.setVisibility(View.VISIBLE);
        }
        else {
            statusTextView.setText("Not signed in");

            signInBtn.setVisibility(View.VISIBLE);
            signOutBtn.setVisibility(View.GONE);
        }
    }
}
