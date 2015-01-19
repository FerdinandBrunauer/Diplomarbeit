package event.tcpSocket;

import java.util.ArrayList;

/**
 * Copyright 2015 (C) HTL - Hallein
 * Created on:  13.01.2015
 * Author:      Ferdinand
 */
public class TCPSocketEventHandler {
    private static ArrayList<TCPSocketEventListener> eventListener = new ArrayList<>();

    public static void addListener(TCPSocketEventListener eventListener1) {
        eventListener.add(eventListener1);
    }

    public static void removeListener(TCPSocketEventListener eventListener1) {
        eventListener.remove(eventListener1);
    }

    public static void fireScrollEvent(TCPSocketEventObject eventObject) {
        for (TCPSocketEventListener listener : eventListener) {
            listener.scrollEventOccurred(eventObject);
        }
    }
}
