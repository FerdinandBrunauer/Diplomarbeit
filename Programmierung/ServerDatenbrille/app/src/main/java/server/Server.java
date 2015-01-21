package server;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiConfiguration;
import android.preference.PreferenceManager;
import android.util.Log;

import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

import event.datapoint.DatapointEventHandler;
import event.datapoint.DatapointEventListener;
import event.datapoint.DatapointEventObject;
import event.scroll.ScrollEventHandler;
import event.scroll.ScrollEventListener;
import event.scroll.ScrollEventObject;
import event.tcpSocket.TCPSocketEventHandler;
import event.tcpSocket.TCPSocketEventListener;
import event.tcpSocket.TCPSocketEventObject;
import htlhallein.at.serverdatenbrille.R;
import server.tcpService.TcpServer;
import server.tcpService.TcpServerState;
import server.wifiHotspotUtils.WifiApManager;

/**
 * Copyright 2015 (C) HTL - Hallein
 * Created on:  13.01.2015
 * Author:      Ferdinand
 */
public class Server implements DatapointEventListener, ScrollEventListener, TCPSocketEventListener, Runnable {
    private Context context;

    private SharedPreferences preferences;

    private ArrayList<Socket> clients;
    private WifiApManager myWifiManager;
    private TcpServer tcpServer;

    public Server(Context context) {
        this.context = context;

        DatapointEventHandler.addListener(this);
        ScrollEventHandler.addListener(this);
        TCPSocketEventHandler.addListener(this);

        preferences = PreferenceManager.getDefaultSharedPreferences(context);

        clients = new ArrayList<>();
        this.myWifiManager = new WifiApManager(context);

        this.tcpServer = new TcpServer();
    }

    @Override
    public void run() {
        enableWifiAP();

        this.tcpServer.start();
        while (this.tcpServer.getState() != TcpServerState.STARTED) {
            try {
                Thread.sleep(500);
            } catch (Exception e) {
                // shit happens
            }
        }

        while (this.tcpServer.getState() != TcpServerState.STOPPED) {
            try {
                Thread.sleep(500);
            } catch (Exception e) {
                this.tcpServer.stop();
            }
        }

        disableWifiAP();
    }

    private void enableWifiAP() {
        WifiConfiguration wifiConfiguration = new WifiConfiguration();
        wifiConfiguration.SSID = preferences.getString(context.getString(R.string.preferences_preference_wifihotspot_name), context.getString(R.string.preferences_preference_wifihotspot_name_default));
        wifiConfiguration.preSharedKey = preferences.getString(context.getString(R.string.preferences_preference_wifihotspot_password), context.getString(R.string.preferences_preference_wifihotspot_password_default));
        wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        myWifiManager.setWifiApEnabled(wifiConfiguration, true);
    }

    private void disableWifiAP() {
        myWifiManager.setWifiApEnabled(null, false);
    }

    @Override
    public void datapointEventOccurred(DatapointEventObject eventObject) {
        sendMessageToAllClients(eventObject.getHtmlText());
    }

    @Override
    public void scrollEventOccurred(ScrollEventObject eventObject) {
        // TODO implement sending Scroll
    }

    private void sendMessageToAllClients(String message) {
        Socket[] sockets = (Socket[]) clients.toArray();

        for (int i = 0; i < sockets.length; i++) {
            // TODO client reachable? when not then remove me
        }
    }

    @Override
    public void TCPSocketEventOccurred(TCPSocketEventObject eventObject) {
        Log.v("Client", "connected");

        Socket socket = eventObject.getSocket();
        try {
            socket.setKeepAlive(true);
        } catch (SocketException e) {
            // i cant do anything against that ...
        }

        // TODO Handshake

        this.clients.add(socket);
    }
}
