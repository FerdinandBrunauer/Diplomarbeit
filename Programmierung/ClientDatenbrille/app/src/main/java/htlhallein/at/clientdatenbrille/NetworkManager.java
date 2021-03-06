package htlhallein.at.clientdatenbrille;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.webkit.WebView;

import org.apache.commons.net.util.SubnetUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.net.SocketFactory;

public class NetworkManager extends Thread {

    private static final int TIMEOUT = 1000;
    private static final int PORT = 6484;

    private WebView webView;
    private String html = "";
    private Context context;
    private Activity activity;
    private WifiManager wifiManager;
    private IP deviceIP;
    private int wifiNetId = -1;
    private String WifiName = "Datenbrille";
    private String WifiPassword = "Passwort1!";
    private boolean shouldClose = false;
    private boolean preferencesChanged = false;
    Socket serverConnection = null;

    public NetworkManager(Context context, Activity activity, WebView webView) {
        this.context = context;
        this.activity = activity;
        this.webView = webView;
        this.wifiManager = (WifiManager) this.context.getSystemService(Context.WIFI_SERVICE);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this.context);
        try {
            this.WifiName = preferences.getString("preference_wifi_name", this.WifiName);
        } catch (Exception e) {
            // Maybe the Preference does not exist any more?
        }
        try {
            this.WifiPassword = preferences.getString("preference_wifi_password", this.WifiPassword);
        } catch (Exception e) {
            // Maybe the Preference does not exist any more?
        }
    }

    @Override
    public void run() {
        while (!this.shouldClose) {
            if (this.wifiManager.isWifiEnabled()) {
                makeLog("WIFI already enabled");
            } else {
                this.wifiManager.setWifiEnabled(true);
                makeLog("WIFI enabled");
            }

            if ((this.preferencesChanged || (this.wifiNetId == -1)) && this.wifiManager.isWifiEnabled() && !this.shouldClose) {
                WifiConfiguration wifiConfiguration = new WifiConfiguration();
                wifiConfiguration.SSID = "\"" + this.WifiName + "\"";
                wifiConfiguration.preSharedKey = "\"" + this.WifiPassword + "\"";
                wifiConfiguration.status = WifiConfiguration.Status.ENABLED;

                this.wifiNetId = -1;

                List<WifiConfiguration> wifiEntrys = this.wifiManager.getConfiguredNetworks();
                for (WifiConfiguration actualEntry : wifiEntrys) {
                    if (actualEntry.SSID.compareTo(wifiConfiguration.SSID) == 0) {
                        // -------------------------------------------------------------------------
                        // WARNING
                        // WE CAN NOT CHANGE THE PASSWORD AND NOT READ OUT THE SAVED PASSWORD!!!!!
                        // -------------------------------------------------------------------------
                        this.wifiNetId = actualEntry.networkId;
                        makeLog("WIFI Entry already exists with ID \"" + this.wifiNetId + "\"");
                        break;
                    }
                }

                if (this.wifiNetId == -1) {
                    this.wifiNetId = this.wifiManager.addNetwork(wifiConfiguration);
                    makeLog("Added WIFI Entry with ID \"" + this.wifiNetId + "\"");
                }
            }

            boolean success = false;
            if (!this.preferencesChanged && !this.shouldClose && this.wifiManager.isWifiEnabled()) {
                success = this.wifiManager.enableNetwork(this.wifiNetId, true);
                if (success) {
                    makeLog("Connected to Network! Great!");
                } else {
                    makeLog("Not Connected to Network! Something is wrong! :(");
                }
            }

            // 15 seconds time for getting a IP-Adress
            long startTime = System.currentTimeMillis();
            while (success && !this.preferencesChanged && !this.shouldClose && this.wifiManager.isWifiEnabled()) {
                if (this.wifiManager.getConnectionInfo().getNetworkId() != -1) {
                    int subnetRaw = this.wifiManager.getDhcpInfo().netmask;
                    String subnet = ipToString(subnetRaw);
                    // WIFI - Connection established but do we have already an IP ?
                    if (subnet.compareTo("0.0.0.0") == 0) {
                        makeLog("Could not obtain Subnetmask");
                        success = false;
                        break;
                    } else {
                        makeLog("Obtained subnetmask: " + subnet);
                        success = true;
                        break;
                    }

                }
                if ((System.currentTimeMillis() - startTime) > 15000) {
                    success = false;
                }

                try {
                    sleep(200);
                } catch (Exception e) {
                    // shit happens ...
                }
            }

            if (success && !this.preferencesChanged && !this.shouldClose && this.wifiManager.isWifiEnabled()) {
                int phoneIPRaw = this.wifiManager.getConnectionInfo().getIpAddress();
                String phoneIP = "";
                try {
                    phoneIP = rawIPToString(phoneIPRaw);
                } catch (UnknownHostException e) {
                    // Holy shit happened here???
                    success = false;
                }

                int subnetRaw = this.wifiManager.getDhcpInfo().netmask;
                String subnet = ipToString(subnetRaw);
                makeLog("Phone IP-Adress: \"" + phoneIP + "\" and Subnetmask: \"" + subnet + "\"");

                this.deviceIP = new IP(phoneIPRaw, phoneIP, subnetRaw, subnet);
            }

            final ArrayList<String> respondingHosts = new ArrayList<>();
            if (success && !this.preferencesChanged && !this.shouldClose && this.wifiManager.isWifiEnabled()) {
                makeLog("Starting search for Host");
                SubnetUtils utils = new SubnetUtils(this.deviceIP.getIpAdress(), this.deviceIP.getSubnet());
                ArrayList<String> hosts = new ArrayList<>(Arrays.asList(utils.getInfo().getAllAddresses()));
                makeLog("Total possible IP-Adresses: \"" + hosts.size() + "\"");

                for (String host : hosts) {
                    Log.v("Testing IP", host);
                    boolean reachable = isHostReachable(host, ReachableMode.Socket);

                    if (reachable) {
                        makeLog("Reachable Host:" + host);
                        respondingHosts.add(host);
                        break;
                    }
                }
                Log.v("Testing IP", "finished");
            }


            boolean connectionEtablished = false;
            if (success && !this.preferencesChanged && !this.shouldClose && this.wifiManager.isWifiEnabled()) {
                makeLog("Total count of Reachable Hosts: \"" + respondingHosts.size() + "\"");
                if (respondingHosts.size() > 0) {
                    for (String host : respondingHosts) {
                        try {
                            serverConnection = SocketFactory.getDefault().createSocket(host, PORT);
                            if (serverConnection.isConnected()) {
                                makeLog("Connected to Server");
                                OutputStream outputStream = serverConnection.getOutputStream();
                                outputStream.write("Datenbrille-Handshake".getBytes());
                                outputStream.flush();
                                outputStream.write('\r');
                                outputStream.flush();
                            } else {
                                makeLog("Can not connect to Server");
                            }
                            connectionEtablished = true;
                            break;
                        } catch (IOException e) {
                            connectionEtablished = false;
                        }
                    }
                } else {
                    makeLog("No Host found.");
                }
            }

            String data = "";
            byte[] buffer;// = new byte[1024];
            while ((serverConnection != null) && success && connectionEtablished && !this.preferencesChanged && !this.shouldClose && this.wifiManager.isWifiEnabled()) {
                try {
                    data = "";
                    while (serverConnection.getInputStream().available() > 0) {
                        buffer = new byte[serverConnection.getInputStream().available()];
                        serverConnection.getInputStream().read(buffer);
                        data += new String(buffer);
                    }
                    if (data.compareTo("") == 0)
                        continue;

                    JSONObject mainObject = new JSONObject(data);
                    if (mainObject.has("operationType")) {
                        switch (mainObject.getString("operationType")) {
                            case "HTML": {
                                String htmlRaw = mainObject.getString("HTML");
                                String html = new String(Base64.decode(htmlRaw.getBytes(), Base64.NO_WRAP));
                                setHTML(html);
                                break;
                            }
                            case "SCROLL": {
                                scrollToPosition(mainObject.getInt("percent"));
                                break;
                            }
                            default: {
                                Log.v("NetworkManager", "Undefined Mode: \"" + mainObject.getString("operationType") + "\"");
                                break;
                            }
                        }
                    }
                } catch (IOException e) {
                    Log.v("NetworkManager", "IOError while reading Data from the Server!");
                    break;
                } catch (Exception e) {
                    Log.v("NetworkManager", "Undefined Error while reading Data from the Server!");
                }
            }
            try {
                serverConnection.getInputStream().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                serverConnection.getOutputStream().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                serverConnection.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            makeLog("Connection closed -> Reconnect starting soon");

            try {
                sleep(5000);
            } catch (InterruptedException e) {
                // Just to make sure, that the Loop don't waste too much resources
            }
        }
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

    public void setWifiName(String name) {
        this.WifiName = name;
        this.preferencesChanged = true;
    }

    public void setWifiPassword(String password) {
        this.WifiPassword = password;
        this.preferencesChanged = true;
    }

    private void makeLog(String text) {
        this.html += "<p>" + text + "</p>";
        this.setHTML("<html><body>" + this.html + "</body></html>");
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

    public String ipToString(int ip) {
        return String.format("%d.%d.%d.%d", (ip & 0xff), (ip >> 8 & 0xff), (ip >> 16 & 0xff), (ip >> 24 & 0xff));
    }

    public void closeConnection(){
        try {
            serverConnection.getInputStream().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            serverConnection.getOutputStream().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            serverConnection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    enum ReachableMode {
        Socket,
        InetAdress
    }
}