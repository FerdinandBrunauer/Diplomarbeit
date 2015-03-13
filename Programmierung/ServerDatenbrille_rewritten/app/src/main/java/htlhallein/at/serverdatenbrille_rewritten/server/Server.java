package htlhallein.at.serverdatenbrille_rewritten.server;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiConfiguration;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

import htlhallein.at.serverdatenbrille_rewritten.R;
import htlhallein.at.serverdatenbrille_rewritten.event.datapoint.DatapointEventHandler;
import htlhallein.at.serverdatenbrille_rewritten.event.datapoint.DatapointEventListener;
import htlhallein.at.serverdatenbrille_rewritten.event.datapoint.DatapointEventObject;
import htlhallein.at.serverdatenbrille_rewritten.event.scroll.ScrollEventDirection;
import htlhallein.at.serverdatenbrille_rewritten.event.scroll.ScrollEventHandler;
import htlhallein.at.serverdatenbrille_rewritten.event.scroll.ScrollEventListener;
import htlhallein.at.serverdatenbrille_rewritten.event.scroll.ScrollEventObject;
import htlhallein.at.serverdatenbrille_rewritten.event.tcpSocket.TCPSocketEventHandler;
import htlhallein.at.serverdatenbrille_rewritten.event.tcpSocket.TCPSocketEventListener;
import htlhallein.at.serverdatenbrille_rewritten.event.tcpSocket.TCPSocketEventObject;
import htlhallein.at.serverdatenbrille_rewritten.server.tcpService.TcpServer;
import htlhallein.at.serverdatenbrille_rewritten.server.tcpService.TcpServerState;
import htlhallein.at.serverdatenbrille_rewritten.server.wifiHotspotUtils.WifiApManager;

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

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                disableWifiAP();
            }
        });

        this.tcpServer.start();
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
        Log.v("Server", "started ...");
        while (this.tcpServer.getState() != TcpServerState.STOPPED) {
            try {
                Thread.sleep(500);
            } catch (Exception e) {
                this.tcpServer.stop();
            }
        }
        Log.v("Server", "stopped ...");

        disableWifiAP();
    }

    public void enableWifiAP() {
        Log.v("Server", "Enabling AP ...");

        WifiConfiguration wifiConfiguration = new WifiConfiguration();
        wifiConfiguration.SSID = preferences.getString(context.getString(R.string.preferences_preference_wifihotspot_name), context.getString(R.string.preferences_preference_wifihotspot_name_default));
        wifiConfiguration.preSharedKey = preferences.getString(context.getString(R.string.preferences_preference_wifihotspot_password), context.getString(R.string.preferences_preference_wifihotspot_password_default));
        wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        myWifiManager.setWifiApEnabled(wifiConfiguration, true);

        Log.v("Server", "AP enabled ...");
    }

    public void disableWifiAP() {
        Log.v("Server", "Disable AP ...");

        myWifiManager.setWifiApEnabled(null, false);

        Log.v("Server", "AP disabled ...");
    }

    @Override
    public void datapointEventOccurred(DatapointEventObject eventObject) {
        try {
            JSONObject object = new JSONObject();
            object.put("operationType", "HTML");
            byte[] jsonMessageBase64;
            try {
                jsonMessageBase64 = Base64.encode(eventObject.getHtmlText().getBytes(), Base64.NO_WRAP);
            } catch (Exception e) {
                Log.v("Server", "Failed to base64 encode the message ...", e);
                return;
            }
            String htmlTextEncoded = new String(jsonMessageBase64);
            object.put("HTML", htmlTextEncoded);
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
            if (eventObject.getDirection() == ScrollEventDirection.UP)
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
        jsonMessage += (char) 13;

        Log.v("Clients", "Sending Data to \"" + clients.size() + "\" Clients");
        Socket[] sockets = clients.toArray(new Socket[clients.size()]);

        for (Socket actualSocket : sockets) {
            try {
                actualSocket.getOutputStream().write(jsonMessage.getBytes("UTF-8"));
                actualSocket.getOutputStream().flush();
            } catch (Exception e) {
                Log.v("Client", "Error sending data", e);
                try {
                    actualSocket.close();
                } catch (Exception e1) {
                    Log.v("Client", "Error closing Socket", e1);
                }

                Log.v("Server", "Removing Client from Arraylist start. Count: \"" + clients.size() + "\"");
                clients.remove(actualSocket); // i dont know if this works
                Log.v("Server", "Removing Client from Arraylist finish. Count: \"" + clients.size() + "\"");
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
                    // DataInputStream inputStream = new DataInputStream(socket.getInputStream());
                    BufferedReader inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String data = inputStream.readLine();
                    Log.v("Handshake", "Data recieved: \"" + data + "\"");
                    if (data.compareTo("Datenbrille-Handshake") == 0) {
                        // SUCCESSFULL
                        socket.setSoTimeout(0);
                        Server.this.clients.add(socket);
                        Looper.prepare();
                        Toast.makeText(context, context.getText(R.string.new_client_connected), Toast.LENGTH_LONG).show();
                    } else {
                        // NOT SUCCESSFULL
                        socket.close();
                        inputStream.close();
                        throw new UnsupportedOperationException("Wrong Handshake Message");
                    }
                } catch (EOFException e) {
                    // Socket closed
                } catch (NullPointerException e) {
                    // Socket closed or something like that
                } catch (Exception e) {
                    Log.v("Client handshake", "Fail with undefined Exception", e);
                }
            }
        }.start();
    }
}
