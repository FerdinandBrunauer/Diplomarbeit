package at.htlhallein.datapoint;

import java.util.EventListener;

/**
 * Copyright 2015 (C) HTL - Hallein
 * Created on:  13.01.2015
 * Author:      Ferdinand
 */
public interface DatapointEventListener extends EventListener {
    public void fireDatapointEvent(DatapointEventObject eventObject);
}
