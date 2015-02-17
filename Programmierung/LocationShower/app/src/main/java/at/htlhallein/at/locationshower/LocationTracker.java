package at.htlhallein.at.locationshower;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;

public class LocationTracker {
    private final Context context;
    private LocationManager locationManager;
    private long TIME_BETWEEN_UPDATES;
    private long DISTANCE_BETWEEN_UPDATES;
    private boolean GPSEnabled = false;
    private boolean NetworkEnabled = false;
    private Location lastLocationGPS = null;
    private long locationGPSUpdate = 0L;
    private Location lastLocationNetwork = null;
    private long locationNetworkUpdate = 0L;
    private boolean alreadyInitialized = false;

    private LocationListener gpsListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            LocationTracker.this.lastLocationGPS = location;
            LocationTracker.this.locationGPSUpdate = System.currentTimeMillis();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {
            LocationTracker.this.GPSEnabled = true;
        }

        @Override
        public void onProviderDisabled(String provider) {
            LocationTracker.this.GPSEnabled = false;
        }
    };

    private LocationListener networkListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            LocationTracker.this.lastLocationNetwork = location;
            LocationTracker.this.locationNetworkUpdate = System.currentTimeMillis();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {
            LocationTracker.this.NetworkEnabled = true;
        }

        @Override
        public void onProviderDisabled(String provider) {
            LocationTracker.this.NetworkEnabled = false;
        }
    };

    public LocationTracker(Context context) {
        this.context = context;
        setConstants(5L, 1000 * 20 * 1L);
        initialize();
    }

    public LocationTracker(Context context, long timeBetweenUpdates, long distanceBetweenUpdates) {
        this.context = context;
        setConstants(timeBetweenUpdates, distanceBetweenUpdates);
        initialize();
    }

    private void initialize() {
        this.locationManager = (LocationManager) this.context.getSystemService(Context.LOCATION_SERVICE);
        this.GPSEnabled = this.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        this.NetworkEnabled = this.locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (!canGetLocation()) {
            if (!LocationTracker.this.alreadyInitialized) {
                AlertDialog.Builder changeSettingsDialog = new AlertDialog.Builder(this.context);
                changeSettingsDialog.setTitle("Standorteinstellungen");
                changeSettingsDialog.setMessage("Standorteinstellugen sind deaktiviert. Um diese Anwendung nutzen zu können, müssen sie diese aktivieren.");
                changeSettingsDialog.setPositiveButton("Einstellungen", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        LocationTracker.this.context.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        LocationTracker.this.alreadyInitialized = true;
                        LocationTracker.this.initialize();
                    }
                });
                changeSettingsDialog.setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                changeSettingsDialog.show();
            }
        }
        alreadyInitialized = true;
    }

    public void startTracking() {
        if (canGetLocation()) {
            if (this.GPSEnabled) {
                this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, this.TIME_BETWEEN_UPDATES, this.DISTANCE_BETWEEN_UPDATES, gpsListener);
            }
            if (this.NetworkEnabled) {
                this.locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, this.TIME_BETWEEN_UPDATES, this.DISTANCE_BETWEEN_UPDATES, networkListener);
            }
        }
    }

    public void stopTracking() {
        this.locationManager.removeUpdates(gpsListener);
        this.locationManager.removeUpdates(networkListener);
    }

    public Location getLastLocation() {
        long currTime = System.currentTimeMillis();
        boolean canReturnGPS = false;
        boolean canReturnNetwork = false;
        if ((currTime - this.locationGPSUpdate) < (1000 * 50 * 1)) {
            if (this.lastLocationGPS != null) {
                canReturnGPS = true;
            }
        }
        if ((currTime - this.locationNetworkUpdate) < (1000 * 50 * 1)) {
            if (this.lastLocationNetwork != null) {
                canReturnNetwork = true;
            }
        }

        if (!canReturnGPS && !canReturnNetwork) {
            throw new NullPointerException("No Location Update since 50seconds");
        }

        if (canReturnGPS && canReturnNetwork) {
            if ((currTime - this.locationGPSUpdate) < (currTime - this.locationNetworkUpdate)) {
                return this.lastLocationGPS;
            } else {
                return this.lastLocationNetwork;
            }
        } else if (canReturnGPS) {
            return this.lastLocationGPS;
        } else {
            return this.lastLocationNetwork;
        }
    }

    private void setConstants(long timeBetweenUpdates, long distanceBetweenUpdates) {
        this.TIME_BETWEEN_UPDATES = timeBetweenUpdates;
        this.DISTANCE_BETWEEN_UPDATES = distanceBetweenUpdates;
    }

    private boolean canGetLocation() {
        return (this.GPSEnabled && this.NetworkEnabled);
    }
}
