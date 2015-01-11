package htlhallein.at.clientdatenbrille;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.webkit.WebView;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.List;

public class NetworkManager extends Thread {

    private WebView webView;
    private String html = "";

    private Context context;
    private Activity activity;
    private WifiManager wifiManager;
    private IP deviceIP;
    private int wifiNetId = -1;
    private String WifiName = "Datenbrille";
    private String WifiPassword = "Passwort1!";
    private int timeout = 100;

    private boolean shouldClose = false;
    private boolean preferencesChanged = false;

    //----------------------------------------------------------------------------------------------
    //  Constructor
    //----------------------------------------------------------------------------------------------
    public NetworkManager(Context context, Activity activity, WebView webView) {
        this.context = context;
        this.activity = activity;
        this.webView = webView;
        this.wifiManager = (WifiManager) this.context.getSystemService(Context.WIFI_SERVICE);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this.context);
        try {
            this.WifiName = preferences.getString("preference_wifi_name", this.WifiName);
        } catch (Exception e) {
        }
        try {
            this.WifiPassword = preferences.getString("preference_wifi_password", this.WifiPassword);
        } catch (Exception e) {
        }
    }

    //----------------------------------------------------------------------------------------------
    // RUN
    //----------------------------------------------------------------------------------------------
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

            boolean connectedToRightNetwork = false;
            if (!this.preferencesChanged && !this.shouldClose && this.wifiManager.isWifiEnabled()) {
                WifiInfo info = this.wifiManager.getConnectionInfo();
                makeLog("Connection INFO SSID: " + info.getSSID() + " and Network ID: \"" + info.getNetworkId() + "\"");
                if (info.getSSID().compareTo("\"" + this.WifiName + "\"") == 0) {
                    makeLog("Already connected to the right Network");
                    connectedToRightNetwork = true;
                } else {
                    // I am guessing, that it is not the right network, but i don't really know
                    this.wifiManager.disableNetwork(info.getNetworkId());
                    makeLog("Disconnected from wrong Network");
                    connectedToRightNetwork = false;
                }
            }

            boolean success = false;
            if (!this.preferencesChanged && !this.shouldClose && this.wifiManager.isWifiEnabled() && !connectedToRightNetwork) {
                success = this.wifiManager.enableNetwork(this.wifiNetId, true);
                if (success) {
                    makeLog("Connected to Network! Great!");
                } else {
                    makeLog("Not Connected to Network! Something is wrong! :(");
                }
            }
            if (connectedToRightNetwork) {
                success = true;
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

            if (success && !this.preferencesChanged && !this.shouldClose && this.wifiManager.isWifiEnabled()) {
                makeLog("Starting search for Host");

                int a, b, c, d;
                a = (this.deviceIP.getSubnetRaw() & 0xff);
                b = (this.deviceIP.getSubnetRaw() >> 8 & 0xff);
                c = (this.deviceIP.getSubnetRaw() >> 16 & 0xff);
                d = (this.deviceIP.getSubnetRaw() >> 24 & 0xff);
                int possibleA, possibleB, possibleC, possibleD;
                possibleA = (a == 255 ? 0 : (255 - a - 1));
                possibleB = (b == 255 ? 0 : (255 - b - 1));
                possibleC = (c == 255 ? 0 : (255 - c - 1));
                possibleD = (d == 255 ? 0 : (255 - d - 1));

                int possibilitys = 0;
                if (possibleA == 0) {
                    possibilitys = 1;
                }
                if (possibleB != 0) {
                    possibilitys *= possibleB;
                }
                if (possibleC != 0) {
                    possibilitys *= possibleC;
                }
                if (possibleD != 0) {
                    possibilitys *= possibleD;
                }
                makeLog("Total Possible IP's in Network: \"" + possibilitys + "\"");

                // TODO get all devices and find the server
            }

            try {
                sleep(200);
            } catch (InterruptedException e) {
                // Just to make sure, that the Loop don't waste too much resources
            }

            break; // TODO REMOVE
        }
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

    public String ipToString(int ip) {
        return String.format("%d.%d.%d.%d", (ip & 0xff), (ip >> 8 & 0xff), (ip >> 16 & 0xff), (ip >> 24 & 0xff));
    }
}