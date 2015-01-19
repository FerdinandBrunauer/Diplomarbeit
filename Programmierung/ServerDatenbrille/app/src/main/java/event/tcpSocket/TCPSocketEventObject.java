package event.tcpSocket;

import java.net.Socket;
import java.util.EventObject;

import server.tcpService.TcpServer;

/**
 * Copyright 2015 (C) HTL - Hallein
 * Created on:  13.01.2015
 * Author:      Ferdinand
 */
public class TCPSocketEventObject extends EventObject {

    private TcpServer tcpServer;

    public TCPSocketEventObject(Object source, TcpServer server) {
        super(source);
        this.tcpServer = server;
    }

    public TcpServer.State getServerState() {
        return tcpServer.getState();
    }

    public Socket getSocket() {
        return tcpServer.getSocket();
    }

}
