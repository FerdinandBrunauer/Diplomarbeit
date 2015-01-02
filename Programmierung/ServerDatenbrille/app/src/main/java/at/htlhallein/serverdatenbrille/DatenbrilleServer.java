package at.htlhallein.serverdatenbrille;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.ArrayList;

import at.htlhallein.datenpunkte.DatenpunkteEventObject;
import at.htlhallein.datenpunkte.DatenpunkteListener;
import at.htlhallein.wifiHotspot.ClientScanResult;
import at.htlhallein.wifiHotspot.FinishScanListener;
import at.htlhallein.wifiHotspot.WIFI_AP_STATE;

/**
 * Created by Toshiba on 02.01.2015.
 */
public class DatenbrilleServer extends Thread implements DatenpunkteListener {
    private final WifiManager mWifiManager;
    private Context context;
    private WifiConfiguration configuration;

    public DatenbrilleServer(Context context) {
        this.context = context;
        this.mWifiManager = (WifiManager) this.context.getSystemService(Context.WIFI_SERVICE);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this.context);
        this.configuration = new WifiConfiguration();
        configuration.hiddenSSID = true;
        configuration.SSID = preferences.getString("WLANname", "Datenbrille");
        configuration.preSharedKey = preferences.getString("WLANpasswort", "Passwort1!");
        configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
    }

    @Override
    public void run() {
        setWifiApEnabled(this.configuration, true);
    }

    public void close() {
        setWifiApEnabled(this.configuration, false);
    }

    @Override
    public void newDatapoint(DatenpunkteEventObject obj) {

    }

    public boolean setWifiApEnabled(WifiConfiguration wifiConfig, boolean enabled) {
        try {
            if (enabled) { // disable WiFi in any case
                mWifiManager.setWifiEnabled(false);
            }

            Method method = mWifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            return (Boolean) method.invoke(mWifiManager, wifiConfig, enabled);
        } catch (Exception e) {
            Log.e(this.getClass().toString(), "", e);
            return false;
        }
    }

    public WIFI_AP_STATE getWifiApState() {
        try {
            Method method = mWifiManager.getClass().getMethod("getWifiApState");

            int tmp = ((Integer) method.invoke(mWifiManager));

            // Fix for Android 4
            if (tmp >= 10) {
                tmp = tmp - 10;
            }

            return WIFI_AP_STATE.class.getEnumConstants()[tmp];
        } catch (Exception e) {
            Log.e(this.getClass().toString(), "", e);
            return WIFI_AP_STATE.WIFI_AP_STATE_FAILED;
        }
    }

    public boolean isWifiApEnabled() {
        return getWifiApState() == WIFI_AP_STATE.WIFI_AP_STATE_ENABLED;
    }

    public WifiConfiguration getWifiApConfiguration() {
        try {
            Method method = mWifiManager.getClass().getMethod("getWifiApConfiguration");
            return (WifiConfiguration) method.invoke(mWifiManager);
        } catch (Exception e) {
            Log.e(this.getClass().toString(), "", e);
            return null;
        }
    }

    public boolean setWifiApConfiguration(WifiConfiguration wifiConfig) {
        try {
            Method method = mWifiManager.getClass().getMethod("setWifiApConfiguration", WifiConfiguration.class);
            return (Boolean) method.invoke(mWifiManager, wifiConfig);
        } catch (Exception e) {
            Log.e(this.getClass().toString(), "", e);
            return false;
        }
    }

    public void getClientList(boolean onlyReachables, FinishScanListener finishListener) {
        getClientList(onlyReachables, 300, finishListener);
    }

    public void getClientList(final boolean onlyReachables, final int reachableTimeout, final FinishScanListener finishListener) {


        Runnable runnable = new Runnable() {
            public void run() {

                BufferedReader br = null;
                final ArrayList<ClientScanResult> result = new ArrayList<ClientScanResult>();

                try {
                    br = new BufferedReader(new FileReader("/proc/net/arp"));
                    String line;
                    while ((line = br.readLine()) != null) {
                        String[] splitted = line.split(" +");

                        if ((splitted != null) && (splitted.length >= 4)) {
                            // Basic sanity check
                            String mac = splitted[3];

                            if (mac.matches("..:..:..:..:..:..")) {
                                boolean isReachable = InetAddress.getByName(splitted[0]).isReachable(reachableTimeout);

                                if (!onlyReachables || isReachable) {
                                    result.add(new ClientScanResult(splitted[0], splitted[3], splitted[5], isReachable));
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e(this.getClass().toString(), e.toString());
                } finally {
                    try {
                        br.close();
                    } catch (IOException e) {
                        Log.e(this.getClass().toString(), e.getMessage());
                    }
                }

                // Get a handler that can be used to post to the main thread
                Handler mainHandler = new Handler(context.getMainLooper());
                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {
                        finishListener.onFinishScan(result);
                    }
                };
                mainHandler.post(myRunnable);
            }
        };

        Thread mythread = new Thread(runnable);
        mythread.start();
    }
}
