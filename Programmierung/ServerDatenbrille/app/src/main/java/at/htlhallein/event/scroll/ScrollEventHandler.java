package at.htlhallein.event.scroll;

import java.util.ArrayList;

/**
 * Copyright 2015 (C) HTL - Hallein
 * Created on:  13.01.2015
 * Author:      Ferdinand
 */
public class ScrollEventHandler {
    private static ArrayList<ScrollEventListener> eventListener = new ArrayList<ScrollEventListener>();

    public static void addListener(ScrollEventListener eventListener1) {
        eventListener.add(eventListener1);
    }

    public static void removeListener(ScrollEventListener eventListener1) {
        eventListener.remove(eventListener1);
    }

    public static void fireScrollEvent(ScrollEventObject eventObject) {
        for (ScrollEventListener listener : eventListener) {
            listener.scrollEventOccurred(eventObject);
        }
    }
}
