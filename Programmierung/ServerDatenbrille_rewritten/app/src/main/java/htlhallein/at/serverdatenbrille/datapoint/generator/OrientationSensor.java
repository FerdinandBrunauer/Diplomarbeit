package htlhallein.at.serverdatenbrille.datapoint.generator;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import htlhallein.at.serverdatenbrille.MainActivity;
import htlhallein.at.serverdatenbrille.R;
import htlhallein.at.serverdatenbrille.activityHandler.ActivityListener;

public class OrientationSensor implements ActivityListener, SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor mSensorAccelerometer;
    private Sensor mSensorMagneticField;

    private float[] mValuesAccelerometer;
    private float[] mValuesMagneticField;
    private float[] mMatrixR;
    private float[] mMatrixI;
    private float[] mMatrixValues;

    private static double mAzimuth = Double.NaN;

    long lastupdate = 0;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        mSensorManager = (SensorManager) MainActivity.getContext().getSystemService(Context.SENSOR_SERVICE);
        mSensorAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorMagneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        mValuesAccelerometer = new float[3];
        mValuesMagneticField = new float[3];

        mMatrixR = new float[9];
        mMatrixI = new float[9];
        mMatrixValues = new float[3];

    }

    @Override
    public void onStart() {
        mSensorManager.registerListener(this, mSensorAccelerometer, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, mSensorMagneticField, SensorManager.SENSOR_DELAY_UI);


    }

    @Override
    public void onResume() {
    }

    @Override
    public void onPause() {

    }

    @Override
    public void onStop() {
        mSensorManager.unregisterListener(this, mSensorAccelerometer);
        mSensorManager.unregisterListener(this, mSensorMagneticField);
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

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                System.arraycopy(event.values, 0, mValuesAccelerometer, 0, 3);
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                System.arraycopy(event.values, 0, mValuesMagneticField, 0, 3);
                break;
        }

        boolean success = SensorManager.getRotationMatrix(mMatrixR, mMatrixI,
                mValuesAccelerometer,
                mValuesMagneticField);

        if (success) {
            float[] outR = new float[9];
            SensorManager.remapCoordinateSystem(mMatrixR, SensorManager.AXIS_X, SensorManager.AXIS_Z, outR);
            SensorManager.getOrientation(outR, mMatrixValues);
            mAzimuth = Math.toDegrees(mMatrixValues[0]);


            //debug
            double currentAngle = 0;
            if(mAzimuth < 0){
                currentAngle = 180 + Math.abs(180+mAzimuth);
            }else{
                currentAngle = mAzimuth;
            }
            if(lastupdate + 1000 < System.currentTimeMillis()) {
                lastupdate = System.currentTimeMillis();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    public static double getmAzimuth() {
        return mAzimuth;
    }
}
