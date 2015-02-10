package datapoint;

import android.content.Context;

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
public class NFC_QRValidator implements Validator {

    @Override
    public DatapointEventObject validate(Context context, Object... objects) {
        if(objects.length == 1) {
            if(objects[0] instanceof String) {
                Map<String, Object> json = new Gson().fromJson((String) objects[0], Map.class);
                // TODO
                if(json.containsKey("datapointtype")) {
                    String datapointType = (String) json.get("datapointtype");
                    switch (datapointType) {
                        case "location": {
                            if(json.containsKey("latitude") && json.containsKey("longitude")) {
                                String html = DatabaseConnection.getInstance(context).getDatapointByLocation(new Location(Double.parseDouble((String)json.get("latitude")), Double.parseDouble((String)json.get("longitude"))))[1]; // TODO from database if exist in database
                                return new DatapointEventObject(this, html);
                            }
                        }
                        default:
                        {
                            return null;
                        }
                    }
                }else {
                    return null;
                }
            } else {
                return null;
            }
        } else if(objects.length > 1) {
            return null;
        } else {
            return null;
        }
    }

}