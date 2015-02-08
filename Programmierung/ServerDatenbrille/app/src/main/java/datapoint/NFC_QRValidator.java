package datapoint;

import android.util.Log;

import event.datapoint.DatapointEventObject;

/**
 * Copyright 2015 (C) HTL - Hallein
 * Created on:  13.01.2015
 * Author:      Ferdinand
 */
public class NFC_QRValidator implements Validator {

    @Override
    public DatapointEventObject validate(Object... objects) {
        // TODO implement validation for NFC and QR

        for(int i = 0; i < objects.length; i++)
            Log.v("Validator Object " + i, objects[i].toString());

        return null;
    }

}