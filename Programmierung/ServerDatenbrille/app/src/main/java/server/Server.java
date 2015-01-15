package server;

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
    private GPSDatapoint gpsDatapoint;
    private NFCDatapoint nfcDatapoint;
    private QRDatapoint qrDatapoint;

    private ArrayList<Socket> clients;

    public Server() {
        DatapointEventHandler.addListener(this);
        ScrollEventHandler.addListener(this);

        this.gpsDatapoint = new GPSDatapoint(new GPSValidator());
        this.nfcDatapoint = new NFCDatapoint(new NFC_QRValidator());
        this.qrDatapoint = new QRDatapoint(new NFC_QRValidator());

        this.clients = new ArrayList<Socket>();
    }

    @Override
    public void run() {
        // TODO create WIFI Hotspot

        // TODO start TCP Server
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
