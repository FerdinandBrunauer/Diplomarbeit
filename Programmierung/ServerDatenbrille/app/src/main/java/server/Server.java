package server;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiConfiguration;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

import event.datapoint.DatapointEventHandler;
import event.datapoint.DatapointEventListener;
import event.datapoint.DatapointEventObject;
import event.scroll.ScrollEventDirection;
import event.scroll.ScrollEventHandler;
import event.scroll.ScrollEventListener;
import event.scroll.ScrollEventObject;
import event.tcpSocket.TCPSocketEventHandler;
import event.tcpSocket.TCPSocketEventListener;
import event.tcpSocket.TCPSocketEventObject;
import htlhallein.at.serverdatenbrille1.R;
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

        clients = new ArrayList<Socket>();
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
        try {
            JSONObject object = new JSONObject();
            object.put("operationType", "HTML");
            object.put("HTML", eventObject.getHtmlText());
            sendMessageToAllClients(object.toString());
        } catch (Exception e) {
            Log.v("Client send", "JSON Object Error - Datapoint", e);
        }
    }

    @Override
    public void scrollEventOccurred(ScrollEventObject eventObject) {
        try {
            JSONObject object = new JSONObject();
            object.put("operationType", "SCROLL");
            if(eventObject.getDirection() == ScrollEventDirection.UP)
                object.put("direction", "UP");
            else
                object.put("direction", "DOWN");
            object.put("percent", eventObject.getPercent());
            sendMessageToAllClients(object.toString());
        } catch (Exception e) {
            Log.v("Client send", "JSON Object Error - Scrollevent", e);
        }
    }

    private void sendMessageToAllClients(String jsonMessage) {
        Log.v("Clients", "Sending Data to \"" + clients.size() + "\" Clients");
        for (Socket actualSocket : clients) {
            try {
                OutputStream stream = actualSocket.getOutputStream();
                stream.write(jsonMessage.getBytes("UTF-8"));
                stream.flush();
            } catch (Exception e) {
                Log.v("Client", "Error sending data", e);
                try {
                    actualSocket.getInputStream().close();
                    actualSocket.getOutputStream().close();
                    actualSocket.close();
                } catch (Exception e1) {
                    Log.v("Client", "Error closing Socket", e1);
                } finally {
                    clients.remove(actualSocket); // i dont know if this works
                }
            }
        }
    }

    @Override
    public void TCPSocketEventOccurred(TCPSocketEventObject eventObject) {
        Log.v("Client", "connected");

        final Socket socket = eventObject.getSocket();
        try {
            socket.setKeepAlive(true);
        } catch (SocketException e) {
            // i can't do anything against that ...
        }

        new Thread() {
            @Override
            public void run() {
                try {
                    socket.setSoTimeout(5000);
                    DataInputStream inputStream = new DataInputStream(socket.getInputStream());
                    String data = inputStream.readUTF();
                    if(data.compareTo("Datenbrille-Handshake") == 0) {
                        // SUCCESSFULL
                        socket.setSoTimeout(0);
                        Server.this.clients.add(socket);
                    } else {
                        // NOT SUCCESSFULL
                        socket.close();
                        inputStream.close();
                        throw new UnsupportedOperationException("Wrong Handshake Message");
                    }
                } catch (Exception e) {
                    Log.v("Client handshake", "Fail", e);
                }
            }
        }.start();
    }
}
