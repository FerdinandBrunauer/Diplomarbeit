package server;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiConfiguration;
import android.preference.PreferenceManager;
import android.util.Log;

import java.net.Socket;
import java.util.ArrayList;

import event.datapoint.DatapointEventHandler;
import event.datapoint.DatapointEventListener;
import event.datapoint.DatapointEventObject;
import event.scroll.ScrollEventHandler;
import event.scroll.ScrollEventListener;
import event.scroll.ScrollEventObject;
import event.tcpSocket.TCPSocketEventListener;
import event.tcpSocket.TCPSocketEventObject;
import htlhallein.at.serverdatenbrille.R;
import server.tcpService.State;
import server.tcpService.TCPServer;
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
    private TCPServer tcpServer;

    public Server(Context context) {
        this.context = context;

        DatapointEventHandler.addListener(this);
        ScrollEventHandler.addListener(this);

        preferences = PreferenceManager.getDefaultSharedPreferences(context);

        clients = new ArrayList<>();
        this.myWifiManager = new WifiApManager(context);

        this.tcpServer = new TCPServer();
    }

    @Override
    public void run() {
        enableWifiAP();

        this.tcpServer.start();
        while(this.tcpServer.getMyState() != State.STARTED) {
            try {
                Thread.sleep(500);
            } catch (Exception e) {
                // shit happens
            }
        }

        while (this.tcpServer.getMyState() != State.STOPPED) {
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
        Log.v("Client", "connected");

        sendMessageToAllClients(eventObject.getHtmlText());
    }

    @Override
    public void scrollEventOccurred(ScrollEventObject eventObject) {
        // TODO implement sending Scroll
    }

    private void sendMessageToAllClients(String message) {
        for (int i = 0; i < clients.size(); i++) {
            if (i < clients.size()) { // Threadsafe
                // TODO client reachable? when not then remove me
                // TODO when client removed, then i-=1;
            }
        }
    }

    @Override
    public void scrollEventOccurred(TCPSocketEventObject eventObject) {
        this.clients.add(eventObject.getSocket());
    }
}
