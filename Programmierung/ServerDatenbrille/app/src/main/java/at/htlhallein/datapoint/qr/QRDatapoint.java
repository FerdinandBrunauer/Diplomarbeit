package at.htlhallein.datapoint.qr;

import at.htlhallein.datapoint.SuperDatapoint;
import at.htlhallein.datapoint.Validator;
import at.htlhallein.event.datapoint.DatapointEventHandler;
import at.htlhallein.event.datapoint.DatapointEventObject;

/**
 * Copyright 2015 (C) HTL - Hallein
 * Created on:  13.01.2015
 * Author:      Ferdinand
 */
public class QRDatapoint extends SuperDatapoint {

    public QRDatapoint(Validator validator) {
        super(validator);
    }

    @Override
    public void run() {
        // TODO implement run for QR
    }

    @Override
    protected void fireEvent(Object... objects) {
        DatapointEventObject eventObject = this.validator.validate(objects);
        if (eventObject != null) {
            DatapointEventHandler.fireDatapointEvent(eventObject);
        }
    }
}
