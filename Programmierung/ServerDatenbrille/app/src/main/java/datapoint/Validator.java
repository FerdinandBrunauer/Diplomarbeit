package datapoint;

import android.content.Context;

import event.datapoint.DatapointEventObject;

/**
 * Copyright 2015 (C) HTL - Hallein
 * Created on:  13.01.2015
 * Author:      Ferdinand
 */
public interface Validator {
    /**
     * @param objects Coordinates or whatever
     * @return NULL when not a valid Point and an DatapointEventObject if it is a valid Point
     */
    public DatapointEventObject validate(Context context, Object... objects);
}