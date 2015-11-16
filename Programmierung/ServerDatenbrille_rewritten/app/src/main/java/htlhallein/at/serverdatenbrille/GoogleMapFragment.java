package htlhallein.at.serverdatenbrille;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import htlhallein.at.serverdatenbrille.database.DatabaseHelper;
import htlhallein.at.serverdatenbrille.datapoint.Validator;
import htlhallein.at.serverdatenbrille.datapoint.generator.GPS;
import htlhallein.at.serverdatenbrille.datapoint.gps.GPSDatapointObject;
import htlhallein.at.serverdatenbrille.datapoint.gps.GPSValidator;
import htlhallein.at.serverdatenbrille.event.datapoint.DatapointEventHandler;
import htlhallein.at.serverdatenbrille.event.datapoint.DatapointEventListener;
import htlhallein.at.serverdatenbrille.event.datapoint.DatapointEventObject;
import htlhallein.at.serverdatenbrille.memoryObjects.DataPackage;
import htlhallein.at.serverdatenbrille.server.DatenbrillenServer;

public class GoogleMapFragment extends Fragment implements LocationListener {
    private static Location currentLoc;
    private Polygon polygon = null;
    private GoogleMap map;
    private static View view;
    private float currentDegree = 0.0f;
    private LocationManager locationManager;
    private static final long MIN_TIME = 400;
    private static final float MIN_DISTANCE = 1000;
    int viewAngleTolerance;
    int maxDistance;
    long lastTimestamp = 0;
    HashMap<Integer, Integer> meters;
    private SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {

            currentDegree = Math.round(event.values[0]);
            if(lastTimestamp + 5000 < System.currentTimeMillis()){
                lastTimestamp = System.currentTimeMillis();
                currentLoc = GPS.mLastLocation;
                //drawTriangle();
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };
    private SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.getContext());
    private boolean gpsEnabled;


    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        /*meters = new HashMap<>();
        meters.put(0, 110570);
        meters.put(10, 110610);
        meters.put(20, 110700);
        meters.put(30, 110850);
        meters.put(40, 111040);
        meters.put(50, 111230);
        meters.put(60, 111470);
        meters.put(70, 111560);
        meters.put(80, 111660);
        meters.put(90, 111690);*/
        SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                gpsEnabled = preferences.getBoolean(MainActivity.getContext().getString(R.string.preferences_preference_gps_enabled), MainActivity.getContext().getResources().getBoolean(R.bool.preferences_preference_gps_enabled_default));
                if(gpsEnabled){
                    map.setMyLocationEnabled(true);
                }else{
                    map.setMyLocationEnabled(false);
                }
            }
        };
        preferences.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
        gpsEnabled = preferences.getBoolean(MainActivity.getContext().getString(R.string.preferences_preference_gps_enabled), MainActivity.getContext().getResources().getBoolean(R.bool.preferences_preference_gps_enabled_default));

        SensorManager sensorManager = (SensorManager) MainActivity.getContext().getSystemService(Context.SENSOR_SERVICE);
        sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_NORMAL);


        viewAngleTolerance = Integer.parseInt(preferences.getString(
                MainActivity.getContext().getString(R.string.preferences_preference_gps_viewangletolerance),
                MainActivity.getContext().getString(R.string.preferences_preference_gps_viewangletolerance_default)));
        maxDistance = Integer.parseInt(
                preferences.getString(
                        MainActivity.getContext().getString(R.string.preferences_preference_gps_viewmaxdistance),
                        MainActivity.getContext().getString(R.string.preferences_preference_gps_viewmaxdistance_default)));


        locationManager = (LocationManager) MainActivity.getContext().getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this);

        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null)
                parent.removeView(view);
        }
        try {
            view = inflater.inflate(R.layout.fragment_map, container, false);
        } catch (InflateException ignored) {
        }

        map =  ((MapFragment)getChildFragmentManager().findFragmentById(R.id.map)).getMap();


        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        if(gpsEnabled){
            map.setMyLocationEnabled(true);
        }else{
            map.setMyLocationEnabled(false);
        }
        map.clear();

       map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
           @Override
           public void onInfoWindowClick(Marker marker) {
                String html = DatabaseHelper.getDatapointcontentFromLocation(marker.getPosition().latitude,marker.getPosition().longitude);
               if(html != null){
                   DatapointEventHandler.fireDatapointEvent(new DatapointEventObject(html));

                   getFragmentManager()
                           .beginTransaction()
                           .replace(R.id.flContent, new ControllerFragment())
                           .commit();
               }else{
                   Toast.makeText(getActivity(),getString(R.string.google_map_marker_description_null),Toast.LENGTH_SHORT).show();
               }
           }
       });

        List<GPSDatapointObject> datapointObjects = DatabaseHelper.getAllDatapoints();
        List<DataPackage> dataPackages = DatabaseHelper.getDataPackages();

            for (GPSDatapointObject datapointObject : datapointObjects) {
                for(DataPackage dataPackage:dataPackages) {
                    if(dataPackage.getId()==datapointObject.getODId()){
                        if(dataPackage.getDisplayed() == 1) {
                            int markerColor = dataPackage.getColor();
                            int red = Color.red(markerColor);
                            int green = Color.green(markerColor);
                            int blue = Color.blue(markerColor);
                            float[] hsv = new float[3];
                            android.graphics.Color.RGBToHSV(red, green, blue, hsv);

                            String title = getString(R.string.google_map_marker_no_title);
                            if(datapointObject.getTitle() != null && datapointObject.getTitle().compareTo("") != 0){
                                title = datapointObject.getTitle();
                            }
                            map.addMarker(new MarkerOptions().position(new LatLng(datapointObject.getLatitude(), datapointObject.getLongitude()))
                                    .title(title).icon(BitmapDescriptorFactory.defaultMarker(hsv[0])).snippet(getString(R.string.google_map_marker_description)));
                        }
                    }

                }
            }

        return view;

    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
        map.animateCamera(cameraUpdate);
        //drawTriangle();
        locationManager.removeUpdates(this);
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

    //TODO: dodalalala
    private void drawTriangle(){
        if(currentLoc != null) {
            float angle = currentDegree;
            double lat = currentLoc.getLatitude();
            int latround = (int) (lat / 10);

            if (meters.containsKey(latround * 10)) {
                int meter = meters.get(latround * 10);
                double factor = ((double)1) / meter;
                float angleToLocB = angle - (viewAngleTolerance / 2);
                float angleToLocC = angle + (viewAngleTolerance / 2);

                double Ba = 0;
                double Bb = 0;

                if (angleToLocB >= 0 && angleToLocB <= 90) {


                    Ba = Math.sin(Math.toRadians(angleToLocB)) * maxDistance * factor;
                    Bb = Math.sin(Math.toRadians(90 - angleToLocB)) * maxDistance * factor;

                } else if (angleToLocB > 90 && angleToLocB <= 180) {
                    Ba = Math.sin(Math.toRadians(angleToLocB - 90)) * maxDistance * factor;
                    Bb = Math.sin(Math.toRadians(90 - (angleToLocB - 90))) * maxDistance * factor;

                } else if (angleToLocB > 180 && angleToLocB <= 270) {
                    Ba = Math.sin(Math.toRadians(angleToLocB - 180)) * maxDistance * factor;
                    Bb = Math.sin(Math.toRadians(90 - (angleToLocB - 180))) * maxDistance * factor;

                } else if (angleToLocB > 270 && angleToLocB <= 360) {
                    Ba = Math.sin(Math.toRadians(angleToLocB - 270)) * maxDistance * factor;
                    Bb = Math.sin(Math.toRadians(90 - (angleToLocB - 270))) * maxDistance * factor;
                }

                double Ca = 0;
                double Cb = 0;

                if (angleToLocC >= 0 && angleToLocC <= 90) {
                    Ca = Math.sin(Math.toRadians(angleToLocC)) * maxDistance * factor;
                    Cb = Math.sin(Math.toRadians(90 - angleToLocC)) * maxDistance * factor;

                } else if (angleToLocC > 90 && angleToLocC <= 180) {
                    Ca = Math.sin(Math.toRadians(angleToLocC - 90)) * maxDistance * factor;
                    Cb = Math.sin(Math.toRadians(90 - (angleToLocC - 90))) * maxDistance * factor;

                } else if (angleToLocC > 180 && angleToLocC <= 270) {
                    Ca = Math.sin(Math.toRadians(angleToLocC - 180)) * maxDistance * factor;
                    Cb = Math.sin(Math.toRadians(90 - (angleToLocC - 180))) * maxDistance * factor;

                } else if (angleToLocC > 270 && angleToLocC <= 360) {
                    Ca = Math.sin(Math.toRadians(angleToLocC - 270)) * maxDistance * factor;
                    Cb = Math.sin(Math.toRadians(90 - (angleToLocC - 270))) * maxDistance * factor;
                }
                Location B = new Location("");
                Location C = new Location("");

                if (angleToLocB > 90 && angleToLocB <= 270) {
                    B.setLatitude(currentLoc.getLatitude() - Bb);
                } else if (angleToLocB > 0 && angleToLocB <= 90) {
                    B.setLatitude(currentLoc.getLatitude() + Bb);
                } else if (angleToLocB > 270 && angleToLocB <= 360) {
                    B.setLatitude(currentLoc.getLatitude() + Bb);
                }

                if (angleToLocC > 90 && angleToLocC <= 270) {
                    C.setLatitude(currentLoc.getLatitude() - Cb);
                } else if (angleToLocC > 0 && angleToLocC <= 90) {
                    C.setLatitude(currentLoc.getLatitude() + Cb);
                } else if (angleToLocC > 270 && angleToLocC <= 360) {
                    C.setLatitude(currentLoc.getLatitude() + Cb);
                }

                if (angleToLocB > 0 && angleToLocB <= 180) {
                    B.setLongitude(currentLoc.getLongitude() + Ba);
                } else if (angleToLocB > 180 && angleToLocB <= 360) {
                    B.setLongitude(currentLoc.getLongitude() - Ba);
                }

                if (angleToLocC > 0 && angleToLocC <= 180) {
                    C.setLongitude(currentLoc.getLongitude() + Ca);
                } else if (angleToLocC > 180 && angleToLocC <= 360) {
                    C.setLongitude(currentLoc.getLongitude() - Ca);
                }


                PolygonOptions polygonOptions = new PolygonOptions()
                        .add(new LatLng(currentLoc.getLatitude(), currentLoc.getLongitude()),
                                new LatLng(B.getLatitude(), B.getLongitude()),
                                new LatLng(C.getLatitude(), C.getLongitude()),
                                new LatLng(currentLoc.getLatitude(), currentLoc.getLongitude()))
                        .strokeColor(Color.BLACK)
                        .fillColor(Color.GRAY);

                if(polygon != null) {
                    if (polygon.isVisible()) {
                        polygon.remove();
                    }
                }
                polygon = map.addPolygon(polygonOptions);

            }
        }
    }
}
