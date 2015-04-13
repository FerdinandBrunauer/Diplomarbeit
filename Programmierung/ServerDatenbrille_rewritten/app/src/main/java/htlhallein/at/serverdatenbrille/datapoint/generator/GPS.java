package htlhallein.at.serverdatenbrille.datapoint.generator;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import htlhallein.at.serverdatenbrille.MainActivity;
import htlhallein.at.serverdatenbrille.R;
import htlhallein.at.serverdatenbrille.activityHandler.ActivityListener;
import htlhallein.at.serverdatenbrille.datapoint.gps.GPSValidator;
import htlhallein.at.serverdatenbrille.event.datapoint.DatapointEventHandler;
import htlhallein.at.serverdatenbrille.event.datapoint.DatapointEventObject;

public class GPS implements ActivityListener, com.google.android.gms.location.LocationListener {
    private static int updateInterval;
    private static int fastestInterval;
    private static int displacement;
    private LocationRequest mLocationRequest;
    private SensorManager sensorManager;
    private float currentDegree = 0.0f;
    private SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            currentDegree = Math.round(event.values[0]);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };
    private SharedPreferences preferences;
    private GoogleApiClient mGoogleApiClient;
    public static Location mLastLocation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.getContext());
        updateInterval = preferences.getInt("gps_update_interval", 5000);
        fastestInterval = preferences.getInt("gps_fastest_interval", 2000);
        displacement = preferences.getInt("gps_displacement", 1);
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
        return preferences.getBoolean(MainActivity.getActivity().getString(R.string.preferences_preference_gps_enabled),
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
            this.sensorManager = (SensorManager) MainActivity.getContext().getSystemService(Context.SENSOR_SERVICE);
            this.sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_NORMAL);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            Log.d(this.getClass().toString(), "Periodic location updates started");
        }
    }

    public void stopLocationUpdates() {
        if (mGoogleApiClient.isConnected()) {
            this.sensorManager = (SensorManager) MainActivity.getContext().getSystemService(Context.SENSOR_SERVICE);
            this.sensorManager.unregisterListener(sensorEventListener);
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            Log.d(this.getClass().toString(), "Periodic location updates stopped");
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(this.getClass().toString(), "Location update, lat: " + location.getLatitude() + ", long: " + location.getLongitude());
        mLastLocation = location;

        DatapointEventObject datapointEventObject = GPSValidator.validate(mLastLocation, currentDegree);
        if (datapointEventObject != null) {
            DatapointEventHandler.fireDatapointEvent(datapointEventObject);
        }
    }

    public void setGoogleApiClient(GoogleApiClient mGoogleApiClient) {
        this.mGoogleApiClient = mGoogleApiClient;
    }
}
