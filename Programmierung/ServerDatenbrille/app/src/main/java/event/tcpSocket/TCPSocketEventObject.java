package event.tcpSocket;

import java.net.Socket;
import java.util.EventObject;

/**
 * Copyright 2015 (C) HTL - Hallein
 * Created on:  13.01.2015
 * Author:      Ferdinand
 */
public class TCPSocketEventObject extends EventObject {

    private Socket socket;

    public TCPSocketEventObject(Object source, Socket socket) {
        super(source);
        this.socket = socket;
    }

    public Socket getSocket() {
        return socket;
    }

}
