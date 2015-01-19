package event.tcpSocket;

import java.util.EventListener;

/**
 * Copyright 2015 (C) HTL - Hallein
 * Created on:  13.01.2015
 * Author:      Ferdinand
 */
public interface TCPSocketEventListener extends EventListener {
    public void scrollEventOccurred(TCPSocketEventObject eventObject);
}
