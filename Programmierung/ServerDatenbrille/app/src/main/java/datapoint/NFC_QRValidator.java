package datapoint;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.Map;

import database.DatabaseConnection;
import database.Location;
import event.datapoint.DatapointEventObject;

/**
 * Copyright 2015 (C) HTL - Hallein
 * Created on:  13.01.2015
 * Author:      Ferdinand
 */
public class NFC_QRValidator  {

    public DatapointEventObject validate(Context context, Object... objects) {
        if (objects.length == 1) {
            if (objects[0] instanceof String) {
                Map<String, Object> json = new Gson().fromJson((String) objects[0], Map.class);
                if (json.containsKey("datapointtype")) {
                    String datapointType = (String) json.get("datapointtype");
                    switch (datapointType) {
                        case "location": {
                            if (json.containsKey("latitude") && json.containsKey("longitude")) {
                                try {
                                    Location location = new Location(Double.parseDouble((String) json.get("latitude")), Double.parseDouble((String) json.get("longitude")));
                                    String html = DatabaseConnection.getDatapointByLocation(location)[1];
                                    return new DatapointEventObject(this, html);
                                } catch (Exception e) {
                                    Toast.makeText(context, "Datenpunkt wurde in der Datenbank nicht gefunden!", Toast.LENGTH_LONG).show();
                                    Log.v("Validator", "Datapoint by Location", e);
                                    return null;
                                }
                            }
                        }
                        default: {
                            return null;
                        }
                    }
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } else if (objects.length > 1) {
            return null;
        } else {
            return null;
        }
    }

}