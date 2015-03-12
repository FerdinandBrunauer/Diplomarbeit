package at.htlhallein.clientdatenbrille;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.webkit.WebView;

import org.apache.commons.net.util.SubnetUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;

import javax.net.SocketFactory;

/**
 * Created by Alexander on 11.03.2015.
 */
public class NetworkManager {
    private static final int PORT = 6484;

    public static final int RUNNING = 2;
    public static final int STOPPED = 1;
    public static final int CLOSED = 0;

    private int serverState = STOPPED;

    private String wifiName = "Datenbrille";
    private String wifiPassword = "Passwort1!";

    private WifiManager wifiManager;
    private Context context;
    private Activity activity;
    private WebView webView;
    private SharedPreferences sharedPreferences;

    private Thread listenerThread;

    public NetworkManager(Context context, Activity activity, WebView webView) {
        this.context = context;
        this.activity = activity;
        this.webView = webView;
        wifiManager = (WifiManager) this.context.getSystemService(Context.WIFI_SERVICE);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.context);
    }

    public void startConnection(){
        setWifiAuthetification();
        enableWifi();
        connectToHotspot();
        final Socket serverConnection = getServerConnection(getRespondingHost(getPhoneIp(getSubnetMask())));

        listenerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                readServerOutput(serverConnection);
            }
        });
        listenerThread.start();
    }

    public void closeConnection(){
        if(serverState == RUNNING){
            while(serverState != CLOSED){
                stopServer();
            }
        }
        disableWifi();
    }

    private void stopServer(){
        serverState = STOPPED;
    }

    private void enableWifi(){
        if(!wifiManager.isWifiEnabled()){
            wifiManager.setWifiEnabled(true);
            Log.i("Wifi", "Wifi enabled");
        }
    }

    private void disableWifi(){
        if(wifiManager.isWifiEnabled()){
            wifiManager.setWifiEnabled(false);
            Log.i("Wifi", "Wifi disabled");
        }
    }


    private void setWifiAuthetification(){
        try {
            this.wifiName = sharedPreferences.getString("preference_wifi_name", this.wifiName);
        } catch (Exception e) {
            Log.e("Preferences", "Can not get Wifi Name: " + e);
        }
        try {
            this.wifiPassword = sharedPreferences.getString("preference_wifi_password", this.wifiPassword);
        } catch (Exception e) {
            Log.e("Preferences", "Can not get Wifi Password: " + e);
        }
    }

    private void connectToHotspot(){
        boolean isConnected = false;
        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
        for( WifiConfiguration i : list ) {
            if(i.SSID != null && i.SSID.equals("\"" + wifiName + "\"")) {
                wifiManager.disconnect();
                wifiManager.enableNetwork(i.networkId, true);
                wifiManager.reconnect();
                Log.i("Wifi", "Connected to Hotspot");
                isConnected = true;
                break;
            }
        }
        if(!isConnected){
            addNetwork();
            connectToHotspot();
        }
    }

    private void addNetwork(){
        WifiConfiguration wifiConfiguration = new WifiConfiguration();
        wifiConfiguration.SSID = "\\" + wifiName + "\\";
        wifiConfiguration.preSharedKey = "\\" + wifiPassword + "\\";
        wifiManager.addNetwork(wifiConfiguration);
        Log.i("Wifi", "Added Hotspot to configured Networks");
    }

    private String getSubnetMask(){
        int subnetRaw = this.wifiManager.getDhcpInfo().netmask;
        String subnet = ipToString(subnetRaw);

        if (subnet.compareTo("0.0.0.0") == 0) {
            Log.e("SubnetMask", "Could not get Subnet Mask");
            try{
                Thread.sleep(500);
            }catch (InterruptedException subnetInterruptException){
                Log.e("SubnetMask", "Thread interrupted: " + subnetInterruptException);
            }
            return getSubnetMask();
        } else {
            Log.i("SubnetMask", "Obtained Subnet Mask " + subnet);
        }
        return subnet;
    }

    private IP getPhoneIp(String subnetMask){
        if(subnetMask != null){
            int phoneIPRaw = this.wifiManager.getConnectionInfo().getIpAddress();
            String phoneIP = "";
            try {
                phoneIP = rawIPToString(phoneIPRaw);
                int subnetRaw = this.wifiManager.getDhcpInfo().netmask;
                String subnet = ipToString(subnetRaw);
                Log.i("IP-Adress", "IP-Adress: \"" + phoneIP + "\" and Subnetmask: \"" + subnet + "\"");
                return new IP(phoneIPRaw, phoneIP, subnetRaw, subnet);
            } catch (UnknownHostException e) {
                Log.e("IP-Adress", "Could not obtain IP-Adress");
                try{
                    Thread.sleep(500);
                }catch (InterruptedException ipInterruptException){
                    Log.e("IP-Adress", "Thread interrupted: " + ipInterruptException);
                }
                return getPhoneIp(subnetMask);
            }
        }else{
            return null;
        }
    }

    private Socket getServerConnection(String respondingHost){
        if(respondingHost != null) {
            Socket serverConnection;
            try {
                serverConnection = SocketFactory.getDefault().createSocket(respondingHost, PORT);
                if (serverConnection.isConnected()) {
                    Log.i("Server Connection", "Connected to Server");
                    OutputStream outputStream = serverConnection.getOutputStream();
                    outputStream.write("Datenbrille-Handshake".getBytes());
                    outputStream.flush();
                    outputStream.write('\r');
                    outputStream.flush();
                } else {
                    Log.e("Server Connection", "Could not connect to IP-Adress: " + respondingHost);
                    return null;
                }
                serverState = RUNNING;
                return serverConnection;
            } catch (IOException e) {
                Log.e("Server Connection", "Could not connect to IP-Adress: " + e);
                return null;
            }
        }
        else{
            return null;
        }
    }

    private void readServerOutput(Socket serverConnection){
        String data = "";
        byte[] buffer;
        while ((serverConnection != null) && this.serverState == RUNNING) {
            try {
                Thread.sleep(500);
                while (serverConnection.getInputStream().available() > 0) {
                    buffer = new byte[serverConnection.getInputStream().available()];
                    serverConnection.getInputStream().read(buffer);
                    data += new String(buffer);
                }
                if (data.compareTo("") != 0){
                    Log.i("Server Read", "Read: " + data);
                    validateServerData(data);
                }

            } catch (IOException e) {
                Log.e("Server Read", "IOError while reading Data from the Server: " + e);
                break;
            } catch (InterruptedException e){
                Log.e("Server Read", "Thread interrupted: " + e);
            } catch (Exception e) {
                Log.e("Server Read", "Undefined Error while reading Data from the Server: " + e);
            }
        }
        if(serverConnection != null){
            try {
                serverConnection.getInputStream().close();
            } catch (IOException e) {
                Log.e("Server Closing", "Error at closing Input Stream: " + e);
            }
            try {
                serverConnection.getOutputStream().close();
            } catch (IOException e) {
                Log.e("Server Closing", "Error at closing Output Stream: " + e);
            }
            try {
                serverConnection.close();
            } catch (IOException e) {
                Log.e("Server Closing", "Error at closing Server Connection: " + e);
            }
            Log.i("Server Closing", "Connection closed");
        }
        serverState = CLOSED;
    }

    private void validateServerData(String data){
        try{
            JSONObject mainObject = new JSONObject(data);
            if (mainObject.has("operationType")) {
                switch (mainObject.getString("operationType")) {
                    case "HTML": {
                        String htmlRaw = mainObject.getString("HTML");
                        String html = new String(Base64.decode(htmlRaw.getBytes(), Base64.NO_WRAP));
                        setHTML(html);
                        Log.i("Validate", "HTML detected: " + html);
                        break;
                    }
                    case "SCROLL": {
                        scrollToPosition(mainObject.getInt("percent"));
                        Log.i("Validate", "SCROLL detected");
                        break;
                    }
                    default: {
                        Log.e("Validate", "Undefined Mode: \"" + mainObject.getString("operationType") + "\"");
                        break;
                    }
                }
            }
        } catch (JSONException e) {
            Log.e("Validate", "Could not parse server data: " + e);
        }
    }

    private String getRespondingHost(IP phoneIp){
        if(phoneIp != null) {
            SubnetUtils utils = new SubnetUtils(phoneIp.getIpAdress(), phoneIp.getSubnet());
            ArrayList<String> hosts = new ArrayList<>(Arrays.asList(utils.getInfo().getAllAddresses()));
            Log.i("Server Connection", "Total possible IP-Adresses: \"" + hosts.size() + "\"");

            String respondingHost = null;
            for (String host : hosts) {
                Log.i("Server Connection", "Testing IP-Adress:" + host);
                boolean reachable = isHostReachable(host, ReachableMode.Socket);

                if (reachable) {
                    Log.i("Server Connection", "Reachable Host:" + host);
                    respondingHost = host;
                    break;
                }
            }
            Log.i("Server Connection", "Testing IP-Adress finished");

            if(respondingHost == null){
                Log.e("Server Connection", "No reachable IP found");
            }
            return respondingHost;
        }else{
            return null;
        }
    }

    public String ipToString(int ip) {
        return String.format("%d.%d.%d.%d", (ip & 0xff), (ip >> 8 & 0xff), (ip >> 16 & 0xff), (ip >> 24 & 0xff));
    }

    private String rawIPToString(int ip) throws UnknownHostException {
        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ip = Integer.reverseBytes(ip);
        }
        byte[] IPByte = BigInteger.valueOf(ip).toByteArray();
        return InetAddress.getByAddress(IPByte).getHostAddress();
    }

    private boolean isHostReachable(String ipAdress, ReachableMode mode) {
        switch (mode) {
            case Socket: {
                Socket socket = null;
                try {
                    socket = new Socket(ipAdress, PORT);
                } catch (IOException e) {
                    // not reachable -> i do not care
                    return false;
                } finally {
                    if (socket != null) { // when there was an exception ..
                        try {
                            socket.close();
                            return true;
                        } catch (IOException e) {
                            // nope, not responding
                            return false;
                        }
                    }
                }
            }
            case InetAdress: {
                try {
                    InetAddress address = InetAddress.getByName(ipAdress);
                    if (address.isReachable(1000)) {
                        // Machine is turned on and can be pinged");
                        return true;
                    } else if (!address.getHostAddress().equals(address.getHostName())) {
                        // Machine is known in a DNS lookup");
                        return false;
                    } else {
                        // The host address and host name are equal, meaning the host name could not be resolved");
                        return false;
                    }
                } catch (IOException e) {
                    return false;
                }
            }
            default:
                return false;
        }
    }


    enum ReachableMode {
        Socket,
        InetAdress
    }

    private void scrollToPosition(final int percent) {
        this.activity.runOnUiThread(new Thread() {
            @Override
            public void run() {
                double height = NetworkManager.this.webView.getContentHeight();
                double windowHeight = NetworkManager.this.webView.getHeight();
                if(height>windowHeight) {
                    NetworkManager.this.webView.scrollTo(0, (int) ((height + windowHeight) / 100 * percent));
                }
            }
        });
    }

    private void setHTML(final String html) {
        this.activity.runOnUiThread(new Thread() {
            @Override
            public void run() {
                NetworkManager.this.webView.loadDataWithBaseURL(null, html, "text/html", "utf-8", null);
            }
        });
    }
}