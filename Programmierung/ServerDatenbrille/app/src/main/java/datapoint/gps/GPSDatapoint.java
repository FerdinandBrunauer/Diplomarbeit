package datapoint.gps;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

import datapoint.Validator;
import event.datapoint.DatapointEventHandler;
import event.datapoint.DatapointEventObject;
import htlhallein.at.serverdatenbrille.R;

/**
 * Copyright 2015 (C) HTL - Hallein
 * Created on:  13.01.2015
 * Author:      Ferdinand
 */
public class GPSDatapoint {

    private Context context;
    private LocationManager locationManager;
    private SensorManager sensorManager;
    private long TIME_BETWEEN_UPDATES;
    private long DISTANCE_BETWEEN_UPDATES;
    private boolean GPSEnabled = false;
    private float currentDegree = 0.0f;
    private LocationListener gpsListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.v("GPS Datapoint", "New Location recieved! Latitude: \"" + location.getLatitude() + "\", Longitude: \"" + location.getLongitude() + "\"");

            fireEvent(context, location.getLatitude(), location.getLongitude(), currentDegree);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {
            GPSDatapoint.this.GPSEnabled = true;
        }

        @Override
        public void onProviderDisabled(String provider) {
            GPSDatapoint.this.GPSEnabled = false;
        }
    };
    private SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            float degree = Math.round(event.values[0]);
            currentDegree = -degree;
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };
    private boolean alreadyInitialized = false;
    private boolean initialized = false;
    private SharedPreferences preferences;
    private SharedPreferences.OnSharedPreferenceChangeListener listener; // must be a local variable, otherwise the garbage collector will remove it

    public GPSDatapoint(final Context context) {
        this.context = context;
        setConstants(5L, 1000 * 10 * 1L); //5SEC between updates and 10METERS

        this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.compareTo(context.getString(R.string.preferences_preference_gps_enabled)) == 0) {
                    if (gpsPreferenceEnabled()) {
                        startTracking();
                    } else {
                        stopTracking();
                    }
                }
            }
        };
        this.preferences.registerOnSharedPreferenceChangeListener(this.listener);

        initialize();
    }

    private boolean gpsPreferenceEnabled() {
        return preferences.getBoolean(this.context.getString(R.string.preferences_preference_gps_enabled), this.context.getResources().getBoolean(R.bool.preferences_preference_gps_enabled_default));
    }

    private void initialize() {
        Log.v("GPS Datapoint", "Initialize ...");

        if(!gpsPreferenceEnabled())
            return;

        if (!initialized) {
            this.sensorManager = (SensorManager) this.context.getSystemService(Context.SENSOR_SERVICE);
            this.locationManager = (LocationManager) this.context.getSystemService(Context.LOCATION_SERVICE);
            this.GPSEnabled = this.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (!canGetLocation()) {
                if (!GPSDatapoint.this.alreadyInitialized) {
                    AlertDialog.Builder changeSettingsDialog = new AlertDialog.Builder(this.context);
                    changeSettingsDialog.setTitle("Standorteinstellungen");
                    changeSettingsDialog.setMessage("Standorteinstellugen sind deaktiviert. Um diese Anwendung nutzen zu können, müssen sie diese aktivieren.");
                    changeSettingsDialog.setPositiveButton("Einstellungen", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            GPSDatapoint.this.context.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                            GPSDatapoint.this.alreadyInitialized = true;
                            GPSDatapoint.this.initialize();
                        }
                    });
                    changeSettingsDialog.setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            initialized = false;
                            return;
                        }
                    });
                    changeSettingsDialog.show();
                }
            }
            alreadyInitialized = true;
            initialized = true;
        }
    }

    public void startTracking() {
        Log.v("GPS Datapoint", "Start Tracking ...");

        if (!initialized) {
            initialize();
        }
        if (canGetLocation()) {
            if (this.GPSEnabled) {
                this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, this.TIME_BETWEEN_UPDATES, this.DISTANCE_BETWEEN_UPDATES, gpsListener);
                this.sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_NORMAL);
            }
        }
    }

    public void stopTracking() {
        Log.v("GPS Datapoint", "Stop Tracking ...");

        if (!initialized) {
            initialize();
        }

        this.locationManager.removeUpdates(gpsListener);
        this.sensorManager.unregisterListener(sensorEventListener);
    }

    private void setConstants(long timeBetweenUpdates, long distanceBetweenUpdates) {
        this.TIME_BETWEEN_UPDATES = timeBetweenUpdates;
        this.DISTANCE_BETWEEN_UPDATES = distanceBetweenUpdates;
    }

    private boolean canGetLocation() {
        return this.GPSEnabled;
    }

    protected void fireEvent(Context context, Object... objects) {
        Validator validator = new GPSValidator();
        DatapointEventObject eventObject = validator.validate(context, objects);
        if (eventObject != null) {
            DatapointEventHandler.fireDatapointEvent(eventObject);
        }
    }
}
