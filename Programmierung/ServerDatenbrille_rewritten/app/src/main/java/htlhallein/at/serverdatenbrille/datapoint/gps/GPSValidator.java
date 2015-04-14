package htlhallein.at.serverdatenbrille.datapoint.gps;

import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import htlhallein.at.serverdatenbrille.MainActivity;
import htlhallein.at.serverdatenbrille.R;
import htlhallein.at.serverdatenbrille.database.DatabaseHelper;
import htlhallein.at.serverdatenbrille.datapoint.generator.OrientationSensor;
import htlhallein.at.serverdatenbrille.event.datapoint.DatapointEventObject;

/**
 * Created by Alexander on 14.03.2015.
 */
public class GPSValidator {
    public static DatapointEventObject validate(Location location) {
        List<GPSDatapointObject> datapointObjects = DatabaseHelper.getAllDatapoints();
        Log.d("GPS Validator", "Datapoints in database: " + datapointObjects.size());

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.getContext());

        int viewAngleTolerance = Integer.parseInt(preferences.getString(
                MainActivity.getContext().getString(R.string.preferences_preference_gps_viewangletolerance),
                MainActivity.getContext().getString(R.string.preferences_preference_gps_viewangletolerance_default)));
        Log.d("GPS Validator", "View angle tolerance: " + viewAngleTolerance);

        int maxDistance = Integer.parseInt(
                preferences.getString(
                        MainActivity.getContext().getString(R.string.preferences_preference_gps_viewmaxdistance),
                        MainActivity.getContext().getString(R.string.preferences_preference_gps_viewmaxdistance_default)));
        Log.d("GPS Validator", "Max distance between datapooint and current location: " + maxDistance);

        for (GPSDatapointObject currentDatapointObject : datapointObjects) {
            Location destination = new Location("" + currentDatapointObject.getId());
            destination.setLatitude(currentDatapointObject.getLatitude());
            destination.setLongitude(currentDatapointObject.getLongitude());

            double distance = location.distanceTo(destination);

            ArrayList<Location> locationsInRange = new ArrayList<>();

            if (distance < maxDistance) {
                Log.d("GPS Validator", "Datapoint " + destination.getLatitude() + ":"
                        + destination.getLongitude() + " is in reach");

                locationsInRange.add(destination);
            }

            if(locationsInRange.size() != 0){
                Toast.makeText(MainActivity.getContext(), "Datapoint is in reach.", Toast.LENGTH_SHORT).show();

                for(Location locationInRange:locationsInRange){
                    double currentAngle = 0;
                    double az = OrientationSensor.getmAzimuth();
                    if(az < 0){
                        currentAngle = 180 + Math.abs(180+az);
                    }else{
                        currentAngle = az;
                    }

                    double coarseAngle = GPSCalculationMethods.getCourseAngle(location, locationInRange);
                    double angleDeviation = Math.abs(currentAngle - coarseAngle);

                    if (angleDeviation <= viewAngleTolerance) {
                        Log.d("GPS Validator", "Found datapoint " + locationInRange.getLatitude() + ":"
                                + locationInRange.getLongitude() + " in angle");
                        Toast.makeText(MainActivity.getContext(), "Found datapoint in angle", Toast.LENGTH_LONG).show();

                        String html = DatabaseHelper.getDatapointcontentFromLocation(locationInRange.getLatitude(), locationInRange.getLongitude());
                        return new DatapointEventObject(html);
                    }
                }
            }
        }
        return null;
    }
}
