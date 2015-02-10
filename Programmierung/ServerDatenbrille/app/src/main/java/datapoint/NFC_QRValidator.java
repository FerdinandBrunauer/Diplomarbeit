package datapoint;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import event.datapoint.DatapointEventObject;

/**
 * Copyright 2015 (C) HTL - Hallein
 * Created on:  13.01.2015
 * Author:      Ferdinand
 */
public class NFC_QRValidator implements Validator {

    @Override
    public DatapointEventObject validate(Object... objects) {
        if(objects.length == 1) {
            if(objects[0] instanceof String) {
                Map<String, Object> json = new Gson().fromJson((String) objects[0], Map.class);
                // TODO
            } else {
                return null;
            }
        } else if(objects.length > 1) {
            return null;
        } else {
            return null;
        }

        return null;
    }

}