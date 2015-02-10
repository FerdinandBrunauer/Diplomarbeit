package datapoint.gps;

import datapoint.Validator;
import event.datapoint.DatapointEventHandler;
import event.datapoint.DatapointEventObject;

/**
 * Copyright 2015 (C) HTL - Hallein
 * Created on:  13.01.2015
 * Author:      Ferdinand
 */
public class GPSDatapoint {

    public GPSDatapoint() {
        // TODO
    }

    protected void fireEvent(Object... objects) {
        Validator validator = new GPSValidator();
        DatapointEventObject eventObject = validator.validate(null, objects);
        if (eventObject != null) {
            DatapointEventHandler.fireDatapointEvent(eventObject);
        }
    }
}
