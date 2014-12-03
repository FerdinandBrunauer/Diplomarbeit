package at.htlhallein.kompass;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import at.htlhallein.gps.GPSUtils;

public class MainActivity extends Activity implements SensorEventListener, LocationListener {

	private ImageView myImage;
	private TextView tvHeading;
	private TextView tvLatitude;
	private TextView tvLongtitude;
	private TextView tvDistanceNoth;
	private TextView tvDistanceSouth;
	private float currentDegree = 0f;
	private SensorManager sensorManager;
	private LocationManager locationManager;
	private String provider;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		this.myImage = (ImageView) findViewById(R.id.imageViewCompass);
		this.tvHeading = (TextView) findViewById(R.id.tvHeading);
		this.tvLatitude = (TextView) findViewById(R.id.tvLatitude);
		this.tvLongtitude = (TextView) findViewById(R.id.tvLongtitude);
		this.tvDistanceNoth = (TextView) findViewById(R.id.tvDistanceNorth);
		this.tvDistanceSouth = (TextView) findViewById(R.id.tvDistanceSouth);

		this.sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		this.locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		boolean enabled = this.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

		if (!enabled) {
			AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

			// Setting Dialog Title
			alertDialog.setTitle("GPS Einstellungen");

			// Setting Dialog Message
			alertDialog.setMessage("GPS ist nicht aktiviert. Wollen sie es jetzt einschalten?");

			// On Pressing Setting button
			alertDialog.setPositiveButton("Einstellungen", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
					MainActivity.this.startActivity(intent);
				}
			});

			// On pressing cancel button
			alertDialog.setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});

			alertDialog.show();
		}

		Criteria criteria = new Criteria();
		provider = locationManager.getBestProvider(criteria, false);
		Location location = locationManager.getLastKnownLocation(provider);
		Log.wtf("Provider", provider);
		if (location != null) {
			System.out.println("Provider " + provider + " has been selected.");
			onLocationChanged(location);
		} else {
			this.tvLatitude.setText("Location not available");
			this.tvLongtitude.setText("Location not available");
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		this.sensorManager.registerListener(this, this.sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_GAME);
		this.locationManager.requestLocationUpdates(provider, 400, 1, this);
	}

	@Override
	protected void onPause() {
		super.onPause();

		this.sensorManager.unregisterListener(this);
		this.locationManager.removeUpdates(this);
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		float degree = Math.round(event.values[0] * 100) / 100;

		this.tvHeading.setText("Grad: " + degree + "°");

		RotateAnimation ra = new RotateAnimation(this.currentDegree, -degree, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		ra.setDuration(100);
		ra.setFillAfter(true);

		this.myImage.startAnimation(ra);
		currentDegree = -degree;
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// not needed
	}

	@Override
	public void onLocationChanged(Location location) {
		Toast.makeText(this, "Update", Toast.LENGTH_LONG).show();
		double latitude = location.getLatitude();
		double longtitude = location.getLongitude();
		double distanceNorth = GPSUtils.distanceBetweenPoints(latitude, longtitude, 84.05, -174.85);
		double distanceSouth = GPSUtils.distanceBetweenPoints(latitude, longtitude, -85.83, 65.78);
		final double hundred = 100.0;

		Log.wtf("Update", "Update");
		Log.wtf("Latitude", String.valueOf(latitude));
		Log.wtf("Longtitude", String.valueOf(longtitude));
		Log.wtf("Distance Nort", String.valueOf(distanceNorth));
		Log.wtf("Distance South", String.valueOf(distanceSouth));

		final String stringLatitude = "Latitude: " + String.valueOf(latitude) + "°";
		final String stringLongtitude = "Longtitude: " + String.valueOf(longtitude) + "°";
		final String stringDistanceNoth = "Distanz Nordpol: " + (Math.round(distanceNorth * hundred) / hundred) + "km";
		final String stringDistanceSouth = "Distanz Südpol: " + (Math.round(distanceSouth * hundred) / hundred) + "km";

		this.tvLatitude.setText(stringLatitude);
		this.tvLongtitude.setText(stringLongtitude);
		this.tvDistanceNoth.setText(stringDistanceNoth);
		this.tvDistanceSouth.setText(stringDistanceSouth);
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		Toast.makeText(this, "Enabled new provider " + provider, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onProviderDisabled(String provider) {
		Toast.makeText(this, "Disabled provider " + provider, Toast.LENGTH_SHORT).show();
	}
}
