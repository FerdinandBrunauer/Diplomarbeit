package server;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiConfiguration;
import android.preference.PreferenceManager;

import java.net.Socket;
import java.util.ArrayList;

import event.datapoint.DatapointEventHandler;
import event.datapoint.DatapointEventListener;
import event.datapoint.DatapointEventObject;
import event.scroll.ScrollEventHandler;
import event.scroll.ScrollEventListener;
import event.scroll.ScrollEventObject;
import htlhallein.at.serverdatenbrille.R;
import server.wifiHotspotUtils.WifiApManager;

/**
 * Copyright 2015 (C) HTL - Hallein
 * Created on:  13.01.2015
 * Author:      Ferdinand
 */
public class Server implements DatapointEventListener, ScrollEventListener, Runnable {
    private Context context;

    private SharedPreferences preferences;

    private ArrayList<Socket> clients;
    private WifiApManager myWifiManager;

    public Server(Context context) {
        this.context = context;

        DatapointEventHandler.addListener(this);
        ScrollEventHandler.addListener(this);

        preferences = PreferenceManager.getDefaultSharedPreferences(context);

        clients = new ArrayList<Socket>();
        this.myWifiManager = new WifiApManager(context);
    }

    @Override
    public void run() {
        enableWifiAP();

        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // TODO start TCP Server

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
        // TODO implement sending HTML
    }

    @Override
    public void scrollEventOccurred(ScrollEventObject eventObject) {
        // TODO implement sending Scroll
    }
}
