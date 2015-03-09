package datapoint.gps;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import database.DatabaseConnection;
import database.Location;
import event.datapoint.DatapointEventObject;
import htlhallein.at.serverdatenbrille.R;


/**
 * Copyright 2015 (C) HTL - Hallein
 * Created on:  13.01.2015
 * Author:      Ferdinand
 */
public class GPSValidator {

    public DatapointEventObject validate(Context context, Object... objects) {
        if (objects.length >= 3) {
            Object first = objects[0];
            Object second = objects[1];
            Object third = objects[2];
            if ((first instanceof Double) && (second instanceof Double) && (third instanceof Float)) {
                final double latitude = (double) first;
                final double longitude = (double) second;
                final float compassAngle = (float) third;
                final Location actualLocation = new Location(latitude, longitude);

                List<GPSDatapointObject> datapointObjects = DatabaseConnection.getAllDatapoints();
                Collections.sort(datapointObjects, new Comparator<GPSDatapointObject>() {
                    @Override
                    public int compare(GPSDatapointObject lhs, GPSDatapointObject rhs) {
                        double distanceActualpositionLHS = GPSCalculationMethods.haversine(actualLocation, new Location(lhs.getLatitude(), lhs.getLongitude()));
                        double distanceActualpositionRHS = GPSCalculationMethods.haversine(actualLocation, new Location(rhs.getLatitude(), rhs.getLongitude()));

                        return Double.compare(distanceActualpositionLHS, distanceActualpositionRHS);
                    }
                });

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                int viewAngleTolerance = Integer.parseInt(preferences.getString(context.getString(R.string.preferences_preference_gps_viewangletolerance), context.getString(R.string.preferences_preference_gps_viewangletolerance_default)));

                for (GPSDatapointObject currentDatapointObject : datapointObjects) {
                    double coarseAngle = GPSCalculationMethods.getCourseAngle(actualLocation, new Location(currentDatapointObject.getLatitude(), currentDatapointObject.getLongitude()));
                    double angleDeviation = Math.abs(compassAngle - coarseAngle);
                    if (angleDeviation <= viewAngleTolerance) {
                        String html = DatabaseConnection.getDatapointByLocation(new Location(currentDatapointObject.getLatitude(), currentDatapointObject.getLongitude()))[1];
                        DatapointEventObject eventObject = new DatapointEventObject(this, html);
                        return eventObject;
                    }
                }
                return null;
            } else {
                Log.v("GPS Validator", "Argument one or two not from type Double or third Argument not from type Float (LATITUDE, LONGITUDE, COMPASS ANGLE)");
                return null;
            }
        } else {
            Log.v("GPS Validator", "Too less Arguments (LATITUDE, LONGITUDE, COMPASS ANGLE)");
            return null;
        }
    }
}
