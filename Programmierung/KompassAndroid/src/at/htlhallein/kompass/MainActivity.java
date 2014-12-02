package at.htlhallein.kompass;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import at.htlhallein.gps.GPSTracker;
import at.htlhallein.gps.GPSUtils;

public class MainActivity extends Activity implements SensorEventListener {

	private ImageView myImage;
	private TextView tvHeading;
	private TextView tvLatitude;
	private TextView tvLongtitude;
	private TextView tvDistanceNoth;
	private TextView tvDistanceSouth;
	private float currentDegree = 0f;
	private SensorManager sensorManager;
	private GPSTracker gpsTracker;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		this.myImage = (ImageView) findViewById(R.id.imageViewCompass);
		this.tvHeading = (TextView) findViewById(R.id.tvHeading);
		this.sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		this.tvLatitude = (TextView) findViewById(R.id.tvLatitude);
		this.tvLongtitude = (TextView) findViewById(R.id.tvLongtitude);
		this.tvDistanceNoth = (TextView) findViewById(R.id.tvDistanceNorth);
		this.tvDistanceSouth = (TextView) findViewById(R.id.tvDistanceSouth);

		this.gpsTracker = new GPSTracker(this);
		if (this.gpsTracker.canGetLocation()) {
			TimerTask task = new TimerTask() {
				@Override
				public void run() {
					double latitude = MainActivity.this.gpsTracker.getLatitude();
					double longtitude = MainActivity.this.gpsTracker.getLongitude();
					double distanceNorth = GPSUtils.distanceBetweenPoints(latitude, longtitude, 84.05, -174.85);
					double distanceSouth = GPSUtils.distanceBetweenPoints(latitude, longtitude, -85.83, 65.78);
					final double hundred = 100.0;
					
					final String stringLatitude = "Latitude: " + String.valueOf(latitude) + "°";
					final String stringLongtitude = "Longtitude: " + String.valueOf(longtitude) + "°";
					final String stringDistanceNoth = "Distanz Nordpol: " + (Math.round(distanceNorth * hundred) / hundred) + "km";
					final String stringDistanceSouth = "Distanz Südpol: " + (Math.round(distanceSouth * hundred) / hundred) + "km";

					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							MainActivity.this.tvLatitude.setText(stringLatitude);
							MainActivity.this.tvLongtitude.setText(stringLongtitude);
							MainActivity.this.tvDistanceNoth.setText(stringDistanceNoth);
							MainActivity.this.tvDistanceSouth.setText(stringDistanceSouth);
						}
					});
				}
			};
			new Timer().schedule(task, 1000, 1000);
		} else {
			this.gpsTracker.showSettingsAlert();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		this.sensorManager.registerListener(this, this.sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_GAME);
	}

	@Override
	protected void onPause() {
		super.onPause();

		this.sensorManager.unregisterListener(this);
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
}
