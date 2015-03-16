package htlhallein.at.serverdatenbrille_rewritten.googlePlayServices;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import htlhallein.at.serverdatenbrille_rewritten.MainActivity;
import htlhallein.at.serverdatenbrille_rewritten.activityHandler.ActivityListener;
import htlhallein.at.serverdatenbrille_rewritten.datapoint.generator.GPS;

/**
 * Created by Alexander on 13.03.2015.
 */
public class GooglePlayService implements ActivityListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private GoogleApiClient mGoogleApiClient;

    //Classes using GooglePlayServices
    private GPS gps = new GPS();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (checkPlayServices()) {
            buildGoogleApiClient();

            gps.setGoogleApiClient(mGoogleApiClient);
            gps.onCreate(savedInstanceState);
        }
    }

    @Override
    public void onStart() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
            gps.setGoogleApiClient(mGoogleApiClient);
        }
        gps.onStart();
    }

    @Override
    public void onResume() {
        checkPlayServices();

        if (mGoogleApiClient.isConnected()) {
            gps.startLocationUpdates();
        }
    }

    @Override
    public void onPause() {
        gps.onPause();
    }

    @Override
    public void onStop() {
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
            Log.d(this.getClass().toString(), "Google Play Services disconnected");

            gps.setGoogleApiClient(mGoogleApiClient);
        }
        gps.onStop();
    }

    @Override
    public void onDestroy() {
        gps.onDestroy();
    }

    @Override
    public void showQRCode() {
        gps.showQRCode();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        gps.onActivityResult(requestCode,resultCode,data);
    }

    @Override
    public void onNewIntent(Intent intent) {
        gps.onNewIntent(intent);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(this.getClass().toString(), "Google Play Services connected");
        gps.startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
        gps.setGoogleApiClient(mGoogleApiClient);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(this.getClass().toString(), "Connection failed: "  + connectionResult.getErrorCode());
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(MainActivity.getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        Log.d(this.getClass().toString(), "Google API Client built");
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(MainActivity.getContext());
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, MainActivity.getActivity(),
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
                Log.d(this.getClass().toString(), "Google Play Services available");
            } else {
                Toast.makeText(MainActivity.getContext(),
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
                Log.e(this.getClass().toString(), "Google Play Services not available");
            }
            return false;
        }
        return true;
    }
}
