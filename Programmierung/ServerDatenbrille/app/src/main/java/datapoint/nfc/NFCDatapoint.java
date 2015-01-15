package datapoint.nfc;

import datapoint.SuperDatapoint;
import datapoint.Validator;
import event.datapoint.DatapointEventHandler;
import event.datapoint.DatapointEventObject;

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
