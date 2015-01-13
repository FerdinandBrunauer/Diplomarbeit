package at.htlhallein.server;

import java.net.Socket;
import java.util.ArrayList;

import at.htlhallein.datapoint.NFC_QRValidator;
import at.htlhallein.datapoint.gps.GPSDatapoint;
import at.htlhallein.datapoint.gps.GPSValidator;
import at.htlhallein.datapoint.nfc.NFCDatapoint;
import at.htlhallein.datapoint.qr.QRDatapoint;
import at.htlhallein.event.datapoint.DatapointEventHandler;
import at.htlhallein.event.datapoint.DatapointEventListener;
import at.htlhallein.event.datapoint.DatapointEventObject;
import at.htlhallein.event.scroll.ScrollEventHandler;
import at.htlhallein.event.scroll.ScrollEventListener;
import at.htlhallein.event.scroll.ScrollEventObject;

/**
 * Copyright 2015 (C) HTL - Hallein
 * Created on:  13.01.2015
 * Author:      Ferdinand
 */
public class Server implements DatapointEventListener, ScrollEventListener {
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

        // TODO create WIFI Hotspot

        this.clients = new ArrayList<Socket>();
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
