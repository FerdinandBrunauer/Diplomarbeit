package htlhallein.at.clientdatenbrille;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Toast;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class NetworkManager extends Thread {

    private WebView webView;

    private Context context;
    private WifiConfiguration wifiConfiguration;
    private WifiManager wifiManager;
    private String WifiName = "Datenbrille";
    private String WifiPassword = "Passwort1!";

    private boolean shouldClose = false;
    private boolean preferencesChanged = false;

    //----------------------------------------------------------------------------------------------
    //  Constructor
    //----------------------------------------------------------------------------------------------
    public NetworkManager(Context context, WebView webView) {
        this.context = context;
        this.webView = webView;
        this.wifiConfiguration = new WifiConfiguration();
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

        this.wifiConfiguration.SSID = String.format("\"%s\"", this.WifiName);
        this.wifiConfiguration.preSharedKey = String.format("\"%s\"", this.WifiPassword);
        this.wifiManager.addNetwork(this.wifiConfiguration);
    }

    private static String intToIP(int ipAddress) {
        String ret = String.format("%d.%d.%d.%d", (ipAddress & 0xff),
                (ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff),
                (ipAddress >> 24 & 0xff));

        return ret;
    }

    //----------------------------------------------------------------------------------------------
    // RUN
    //----------------------------------------------------------------------------------------------
    @Override
    public void run() {
        while (!this.shouldClose) {
            // ist den wlan überhaupt aktiviert??
            if (!this.wifiManager.isWifiEnabled()) {
                // wenn nein, aktivieren
                this.wifiManager.setWifiEnabled(true);
                makeToast("WIFI activated");
            } else {
                makeToast("WIFI already enabled");
            }

            // wenn jemand in den einstellungen etwas geändert hat, schließen wir die Verbindung zu
            // dem jeweiligen Hotspot, sofern eine Verbindung noch existiert
            if (this.preferencesChanged && !this.shouldClose) {
                preferencesChanged = false;
                // Neues bekanntes Netzwerk anlegen
                this.wifiConfiguration.SSID = String.format("\"%s\"", this.WifiName);
                this.wifiConfiguration.preSharedKey = String.format("\"%s\"", this.WifiPassword);
                this.wifiManager.addNetwork(this.wifiConfiguration);
                // Von derzeitigem Netzwerk trennen
                this.wifiManager.disconnect();
                makeToast("ADDED Configuration");
            }

            // Wir versuchen in Erfahrung zu bringen, ob wir überhaupt mit einem Netzwerk verbunden
            // sind, wenn ja, ob es das richtige ist
            String connectedNetworkSSID = this.wifiManager.getConnectionInfo().getSSID();
            boolean connectedToTheRightNetwork = (connectedNetworkSSID.compareTo(this.WifiName) == 0);
            if (!connectedToTheRightNetwork && !this.shouldClose) {
                // disconnect
                this.wifiManager.disconnect();

                // and connect
                List<WifiConfiguration> savedWifiConfigurations = this.wifiManager.getConfiguredNetworks();
                for (WifiConfiguration aktualWifiConfiguration : savedWifiConfigurations) {
                    if (aktualWifiConfiguration.SSID != null) {
                        if (aktualWifiConfiguration.SSID.compareTo(String.format("\"%s\"", this.WifiName)) == 0) {
                            // es ist das richtige

                            // verfügbar machen
                            this.wifiManager.enableNetwork(aktualWifiConfiguration.networkId, true);
                            // verbinden
                            this.wifiManager.reconnect();

                            makeToast("Verbunden!");

                            // wir brauchen die anderen netzwerke nicht mehr zu überprüfen
                            break;
                        }
                    }
                }
            }
            if (this.shouldClose)
                continue;

            // Wenn wir nun mit einem Netzwerk verbunden sind, versuchen wir, den Server ausfindig
            // zu machen
            // Problem: Das Subnet ist unbekannt!
            String mask = intToIP(this.wifiManager.getDhcpInfo().netmask);
            makeToast("SUBNET: " + mask);
            break;
//            ArrayList<String> hostsInMyNetwork = scanSubNet("192.168.1.");

            // Wenn wir den Server gefunden haben, versuchen wir, uns mit diesem zu Verbinden

            // Wenn die Verbindung erfolgreich war, hören wir unseren Socket ab
            // Immer wieder schauen, ob der Server beendet werden sollte oder sich die Einstellungen
            // geändert haben.
        }
    }

    private void setHTML(String html) {
        this.webView.loadDataWithBaseURL(null, html, "text/html", "utf-8", null);
    }

    public void setWifiName(String name) {
        this.WifiName = name;
        this.preferencesChanged = true;
    }

    public void setWifiPassword(String password) {
        this.WifiPassword = password;
        this.preferencesChanged = true;
    }

    private void makeToast(String text) {
        Toast.makeText(this.context, text, Toast.LENGTH_LONG);
        // TODO
        // needs to run in UI Thread
    }

    private ArrayList<String> scanSubNet(String subnet) {
        ArrayList<String> hosts = new ArrayList<String>();

        InetAddress inetAddress = null;
        for (int i = 1; i < 10; i++) {
            Log.d("Scan-Sub-Net", "Trying: " + subnet + String.valueOf(i));
            try {
                inetAddress = InetAddress.getByName(subnet + String.valueOf(i));
                if (inetAddress.isReachable(1000)) {
                    hosts.add(inetAddress.getHostName());
                    Log.d("Scan-Sub-Net Hostname:", inetAddress.getHostName());
                }
            } catch (Exception e) {
                // shit happens
            }
        }

        return hosts;
    }
}