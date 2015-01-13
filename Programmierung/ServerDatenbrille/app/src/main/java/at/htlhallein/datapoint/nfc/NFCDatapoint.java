package at.htlhallein.datapoint.nfc;

import at.htlhallein.datapoint.SuperDatapoint;
import at.htlhallein.datapoint.Validator;
import at.htlhallein.event.datapoint.DatapointEventHandler;
import at.htlhallein.event.datapoint.DatapointEventObject;

/**
 * Copyright 2015 (C) HTL - Hallein
 * Created on:  13.01.2015
 * Author:      Ferdinand
 */
public class NFCDatapoint extends SuperDatapoint {

    public NFCDatapoint(Validator validator) {
        super(validator);
    }

    @Override
    public void run() {
        // TODO implement run for NFC
    }

    @Override
    protected void fireEvent(Object... objects) {
        DatapointEventObject eventObject = this.validator.validate(objects);
        if (eventObject != null) {
            DatapointEventHandler.fireDatapointEvent(eventObject);
        }
    }
}
