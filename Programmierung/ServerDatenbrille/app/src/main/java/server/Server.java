package server;

import android.content.Context;
import android.net.wifi.WifiManager;

import java.net.Socket;
import java.util.ArrayList;

import datapoint.NFC_QRValidator;
import datapoint.gps.GPSDatapoint;
import datapoint.gps.GPSValidator;
import datapoint.nfc.NFCDatapoint;
import datapoint.qr.QRDatapoint;
import event.datapoint.DatapointEventHandler;
import event.datapoint.DatapointEventListener;
import event.datapoint.DatapointEventObject;
import event.scroll.ScrollEventHandler;
import event.scroll.ScrollEventListener;
import event.scroll.ScrollEventObject;

/**
 * Copyright 2015 (C) HTL - Hallein
 * Created on:  13.01.2015
 * Author:      Ferdinand
 */
public class Server implements DatapointEventListener, ScrollEventListener, Runnable {
    private Context context;

    private GPSDatapoint gpsDatapoint;
    private NFCDatapoint nfcDatapoint;
    private QRDatapoint qrDatapoint;

    private ArrayList<Socket> clients;
    private WifiManager myWifiManager;

    public Server(Context context) {
        this.context = context;

        DatapointEventHandler.addListener(this);
        ScrollEventHandler.addListener(this);

        gpsDatapoint = new GPSDatapoint(new GPSValidator());
        nfcDatapoint = new NFCDatapoint(new NFC_QRValidator());
        qrDatapoint = new QRDatapoint(new NFC_QRValidator());

        clients = new ArrayList<Socket>();
        this.myWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }

    @Override
    public void run() {
        if (!myWifiManager.isWifiEnabled())
            myWifiManager.setWifiEnabled(true);

//        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
//
//        Method[] wmMethod = myWifiManager.getClass().getDeclaredMethods();
//        boolean methodFound = false;
//        for (Method method : wmMethod) {
//            if (method.getName().equals("setWifiApEnabled")) {
//                methodFound = true;
//                WifiConfiguration myWifiConfiguration = new WifiConfiguration();
//                myWifiConfiguration.SSID = "\"" + preferences.getString(context.getString(R.string.preferences_preference_wifihotspot_name), context.getString(R.string.preferences_preference_wifihotspot_name_default)) + "\"";
//                myWifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
//                myWifiConfiguration.preSharedKey = "\"" + preferences.getString(context.getString(R.string.preferences_preference_wifihotspot_password), context.getString(R.string.preferences_preference_wifihotspot_password_default)) + "\"";
//                try {
//                    boolean apstatus = (Boolean) method.invoke(myWifiManager, myWifiConfiguration, true);
//                    for (Method isWifiApEnabledmethod : wmMethod) {
//                        if (isWifiApEnabledmethod.getName().equals("isWifiApEnabled")) {
//                            while (!(Boolean) isWifiApEnabledmethod.invoke(myWifiManager)) {
//                            }
//                            ;
//                            for (Method method1 : wmMethod) {
//                                if (method1.getName().equals("getWifiApState")) {
//                                    int apstate;
//                                    apstate = (Integer) method1.invoke(myWifiManager);
//                                    //                    netConfig=(WifiConfiguration)method1.invoke(wifi);
//                                    //statusView.append("\nSSID:"+netConfig.SSID+"\nPassword:"+netConfig.preSharedKey+"\n");
//                                }
//                            }
//                        }
//                    }
//                    if (apstatus) {
//                        Log.v("Hotspot", "SUCCESS");
//                    } else {
//                        Log.v("Hotspot", "FAILED");
//                    }
//                } catch (IllegalArgumentException e) {
//                    e.printStackTrace();
//                } catch (IllegalAccessException e) {
//                    e.printStackTrace();
//                } catch (InvocationTargetException e) {
//                    e.printStackTrace();
//                }
//            }
//        }

// TODO create WIFI Hotspot

        // TODO start TCP Server

//        myWifiManager.setWifiEnabled(false);
    }

    @Override
    public void datapointEventOccurred(DatapointEventObject eventObject) {
        // TODO implement sending HTML
    }

    @Override
    public void scrollEventOccurred(ScrollEventObject eventObject) {
        // TODO implement sending Scroll
    }

    // TODO methods to enable and disable the Datapoint Event Generator (gps, nfc, qr)
}
