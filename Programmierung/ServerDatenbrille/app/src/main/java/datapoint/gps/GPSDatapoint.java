package datapoint.gps;

import datapoint.SuperDatapoint;
import datapoint.Validator;
import event.datapoint.DatapointEventHandler;
import event.datapoint.DatapointEventObject;

/**
 * Copyright 2015 (C) HTL - Hallein
 * Created on:  13.01.2015
 * Author:      Ferdinand
 */
public class GPSDatapoint extends SuperDatapoint {

    public GPSDatapoint(Validator validator) {
        super(validator);
    }

    @Override
    public void run() {
        // TODO implement run for GPS
    }

    @Override
    protected void fireEvent(Object... objects) {
        DatapointEventObject eventObject = this.validator.validate(objects);
        if (eventObject != null) {
            DatapointEventHandler.fireDatapointEvent(eventObject);
        }
    }
}
