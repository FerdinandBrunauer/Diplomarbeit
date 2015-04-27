package htlhallein.at.serverdatenbrille.datapoint.generator;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import htlhallein.at.serverdatenbrille.MainActivity;
import htlhallein.at.serverdatenbrille.R;
import htlhallein.at.serverdatenbrille.activityHandler.ActivityHandler;
import htlhallein.at.serverdatenbrille.activityHandler.ActivityListener;
import htlhallein.at.serverdatenbrille.datapoint.gps.GPSValidator;
import htlhallein.at.serverdatenbrille.event.datapoint.DatapointEventHandler;
import htlhallein.at.serverdatenbrille.event.datapoint.DatapointEventObject;

public class GPS implements ActivityListener, com.google.android.gms.location.LocationListener {
    private static int updateInterval;
    private static int fastestInterval;
    private static int displacement;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    public static Location mLastLocation;
    private int gpsState = 1;
    private static final int RUNNING = 0;
    private static final int STOPPED = 0;

    private SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.getContext());

    private SharedPreferences.OnSharedPreferenceChangeListener mChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.compareTo(MainActivity.getContext().getString(R.string.preferences_preference_nfc_enabled)) == 0) {
                Resources resources = MainActivity.getContext().getResources();
                boolean gpsEnabled = mSharedPreferences.getBoolean(MainActivity.getContext().getString(R.string.preferences_preference_nfc_enabled), resources.getBoolean(R.bool.preferences_preference_nfc_enabled_default));

                if (gpsEnabled && gpsState == STOPPED) {
                    ActivityHandler.removeListenerClass(GPS.class);
                }else if(!gpsEnabled && (gpsState == RUNNING)){
                    ActivityHandler.addListener(new GPS());
                    ActivityHandler.onCreate(null,GPS.class);
                    ActivityHandler.onResume(GPS.class);
                }
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.getContext());
        updateInterval = mSharedPreferences.getInt("gps_update_interval", 5000);
        fastestInterval = mSharedPreferences.getInt("gps_fastest_interval", 2000);
        displacement = mSharedPreferences.getInt("gps_displacement", 1);

        createLocationRequest();
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onResume() {
        startLocationUpdates();
    }

    @Override
    public void onPause() {
    }

    @Override
    public void onStop() {
        stopLocationUpdates();
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public void showQRCode() {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

    }

    @Override
    public void onNewIntent(Intent intent) {

    }

    private boolean gpsPreferenceEnabled() {
        return mSharedPreferences.getBoolean(MainActivity.getActivity().getString(R.string.preferences_preference_gps_enabled),
                MainActivity.getContext().getResources().getBoolean(R.bool.preferences_preference_gps_enabled_default));
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(updateInterval);
        mLocationRequest.setFastestInterval(fastestInterval);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(displacement);
    }

    @SuppressWarnings("deprecation")
    public void startLocationUpdates() {
        if (gpsPreferenceEnabled()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            Log.d(this.getClass().toString(), "Periodic location updates started");
        }
    }

    public void stopLocationUpdates() {
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            Log.d(this.getClass().toString(), "Periodic location updates stopped");
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(this.getClass().toString(), "Location update, lat: " + location.getLatitude() + ", long: " + location.getLongitude());
        mLastLocation = location;

        DatapointEventObject datapointEventObject = GPSValidator.validate(mLastLocation);
        if (datapointEventObject != null) {
            DatapointEventHandler.fireDatapointEvent(datapointEventObject);
        }
    }

    public void setGoogleApiClient(GoogleApiClient mGoogleApiClient) {
        this.mGoogleApiClient = mGoogleApiClient;
    }

}
