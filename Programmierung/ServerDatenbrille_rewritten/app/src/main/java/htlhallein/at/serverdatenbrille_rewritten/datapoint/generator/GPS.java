package htlhallein.at.serverdatenbrille_rewritten.datapoint.generator;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.List;

import htlhallein.at.serverdatenbrille_rewritten.MainActivity;
import htlhallein.at.serverdatenbrille_rewritten.R;
import htlhallein.at.serverdatenbrille_rewritten.activityHandler.ActivityListener;
import htlhallein.at.serverdatenbrille_rewritten.datapoint.Validator;
import htlhallein.at.serverdatenbrille_rewritten.datapoint.gps.GPSCalculationMethods;
import htlhallein.at.serverdatenbrille_rewritten.datapoint.gps.GPSDatapointObject;
import htlhallein.at.serverdatenbrille_rewritten.datapoint.gps.GPSValidator;
import htlhallein.at.serverdatenbrille_rewritten.event.datapoint.DatapointEventHandler;
import htlhallein.at.serverdatenbrille_rewritten.event.datapoint.DatapointEventObject;

public class GPS implements ActivityListener, LocationListener {
    private LocationRequest mLocationRequest;
    private SensorManager sensorManager;
    private float currentDegree = 0.0f;
    private SharedPreferences preferences;
    private GoogleApiClient mGoogleApiClient;

    private Location mLastLocation;

    private static int updateInterval;
    private static int fastestInterval;
    private static int displacement;

    private SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            float degree = Math.round(event.values[0]);
            currentDegree = degree;
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

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

    public void startLocationUpdates() {
        if(gpsPreferenceEnabled()) {
            this.sensorManager = (SensorManager) MainActivity.getContext().getSystemService(Context.SENSOR_SERVICE);
            this.sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_NORMAL);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, (com.google.android.gms.location.LocationListener) this);
            Log.d(this.getClass().toString(), "Periodic location updates started");
        }
    }

    public void stopLocationUpdates() {
        this.sensorManager = (SensorManager) MainActivity.getContext().getSystemService(Context.SENSOR_SERVICE);
        this.sensorManager.unregisterListener(sensorEventListener);
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, (com.google.android.gms.location.LocationListener)this);
        Log.d(this.getClass().toString(), "Periodic location updates stopped");
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(this.getClass().toString(), "Location update");
        mLastLocation = location;

        DatapointEventObject datapointEventObject = GPSValidator.validate(mLastLocation, currentDegree);
        if(datapointEventObject != null) {
            DatapointEventHandler.fireDatapointEvent(datapointEventObject);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public void setGoogleApiClient(GoogleApiClient mGoogleApiClient){
        this.mGoogleApiClient = mGoogleApiClient;
    }
}
