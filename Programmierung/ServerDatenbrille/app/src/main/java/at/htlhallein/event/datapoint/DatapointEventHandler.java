package at.htlhallein.event.datapoint;

import java.util.ArrayList;

/**
 * Copyright 2015 (C) HTL - Hallein
 * Created on:  13.01.2015
 * Author:      Ferdinand
 */
public class DatapointEventHandler {
    private static ArrayList<DatapointEventListener> eventListener = new ArrayList<DatapointEventListener>();

    public static void addListener(DatapointEventListener eventListener1) {
        eventListener.add(eventListener1);
    }

    public static void removeListener(DatapointEventListener eventListener1) {
        eventListener.remove(eventListener1);
    }

    public static void fireDatapointEvent(DatapointEventObject eventObject) {
        for (DatapointEventListener listener : eventListener) {
            listener.datapointEventOccurred(eventObject);
        }
    }
}
