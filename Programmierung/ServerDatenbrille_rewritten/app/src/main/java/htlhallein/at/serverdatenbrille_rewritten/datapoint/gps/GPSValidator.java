package htlhallein.at.serverdatenbrille_rewritten.datapoint.gps;

import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

import htlhallein.at.serverdatenbrille_rewritten.MainActivity;
import htlhallein.at.serverdatenbrille_rewritten.R;
import htlhallein.at.serverdatenbrille_rewritten.database.DatabaseHelper;
import htlhallein.at.serverdatenbrille_rewritten.event.datapoint.DatapointEventHandler;
import htlhallein.at.serverdatenbrille_rewritten.event.datapoint.DatapointEventObject;

/**
 * Created by Alexander on 14.03.2015.
 */
public class GPSValidator {
    public static DatapointEventObject validate(Location location, double currentDegree){
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
            if (distance < maxDistance) {
                Toast.makeText(MainActivity.getContext(), "Datapoint is in reach.", Toast.LENGTH_SHORT).show();
                Log.d("GPS Validator", "Datapoint " + destination.getLatitude() + ":"
                        + destination.getLongitude() + " is in reach");

                double coarseAngle = GPSCalculationMethods.getCourseAngle(location,destination);
                double angleDeviation = Math.abs(currentDegree - coarseAngle);

                if (angleDeviation <= viewAngleTolerance) {
                    Log.d("GPS Validator", "Found datapoint " + destination.getLatitude() + ":"
                            + destination.getLongitude() + " in angle");
                    Toast.makeText(MainActivity.getContext(), "Found datapoint in angle", Toast.LENGTH_LONG).show();

                    String html = DatabaseHelper.getDatapointcontentFromLocation(destination.getLatitude(),destination.getLongitude());
                    return new DatapointEventObject(html);
                }
            }
        }
        return null;
    }
}
