package at.htlhallein.at.locationshower;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity implements LocationListener, SensorEventListener {
    private LocationManager locationManager;
    private boolean gpsAvailable, networkAvailable;

    private TextView tvLatitude;
    private TextView tvLongitude;
    private ImageView ivKompassRose;
    private TextView tvDegree;

    private float currentDegree = 0.0f;
    private SensorManager sensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.tvLatitude = (TextView) findViewById(R.id.latitude);
        this.tvLatitude.setText("Latitude: Nicht verfügbar!");
        this.tvLongitude = (TextView) findViewById(R.id.longitude);
        this.tvLongitude.setText("Longitude: Nicht verfügbar!");
        this.tvDegree = (TextView) findViewById(R.id.degree);
        this.tvDegree.setText("Grad: Nicht verfügbar!");
        this.ivKompassRose = (ImageView) findViewById(R.id.ivKompassRose);

        this.locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        try {
            this.gpsAvailable = this.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }
        try {
            this.networkAvailable = this.locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }

        if (!this.gpsAvailable && !this.networkAvailable) {
            Toast.makeText(this, "Kein Provider verfügbar! Bitte Einstellungen vornehmen.", Toast.LENGTH_LONG).show();
            return;
        }

        if (this.gpsAvailable)
            this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, this);
        if (this.networkAvailable)
            this.locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 0, this);

        this.sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.locationManager.removeUpdates(this);
        this.sensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (this.gpsAvailable)
            this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, this);
        if (this.networkAvailable)
            this.locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 0, this);

        this.sensorManager.registerListener(this, this.sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onLocationChanged(Location location) {
        Toast.makeText(this, "Location Update", Toast.LENGTH_SHORT).show();
        double latitude = Math.round(location.getLatitude() * 100000.0d) / 100000.0d;
        double longitude = Math.round(location.getLongitude() * 100000.0d) / 100000.0d;

        this.tvLatitude.setText("Latitude: " + String.valueOf(latitude) + "°");
        this.tvLongitude.setText("Longitude: " + String.valueOf(longitude) + "°");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {    }

    @Override
    public void onProviderEnabled(String provider) {    }

    @Override
    public void onProviderDisabled(String provider) {    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float degree = Math.round(event.values[0]);
        this.tvDegree.setText("Aktuelle Ausrichtung: " + degree + "°");
        RotateAnimation ra = new RotateAnimation(this.currentDegree, -degree, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        ra.setDuration(20);
        ra.setFillAfter(true);
        this.ivKompassRose.startAnimation(ra);
        this.currentDegree = -degree;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {  }
}
