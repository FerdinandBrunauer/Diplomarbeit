package htlhallein.at.serverdatenbrille_rewritten.activityHandler;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;

public class ActivityHandler {
    // All methods in this class use the same monitor, so it is fully THREADSAFE

    private static ArrayList<ActivityListener> listeners = new ArrayList<>();

    public static synchronized void addListener(ActivityListener listener) {
        listeners.add(listener);
    }

    public static synchronized void onCreate(Bundle savedInstanceState) {
        Log.d(ActivityHandler.class.toString(), "onCreate");
        for (ActivityListener listener : listeners) {
            listener.onCreate(savedInstanceState);
        }
    }

    public static synchronized void onStart() {
        Log.d(ActivityHandler.class.toString(), "onStart");
        for (ActivityListener listener : listeners) {
            listener.onStart();
        }
    }

    public static synchronized void onResume() {
        Log.d(ActivityHandler.class.toString(), "onResume");
        for (ActivityListener listener : listeners) {
            listener.onResume();
        }
    }

    public static synchronized void onPause() {
        Log.d(ActivityHandler.class.toString(), "onPause");
        for (ActivityListener listener : listeners) {
            listener.onPause();
        }
    }

    public static synchronized void onStop() {
        Log.d(ActivityHandler.class.toString(), "onStop");
        for (ActivityListener listener : listeners) {
            listener.onStop();
        }
    }

    public static synchronized void onDestroy() {
        Log.d(ActivityHandler.class.toString(), "onDestroy");
        for (ActivityListener listener : listeners) {
            listener.onDestroy();
        }
    }

    public static synchronized void showQRCode() {
        for (ActivityListener listener : listeners) {
            listener.showQRCode();
        }
    }

    public static synchronized void onNewIntent(Intent intent) {
        for (ActivityListener listener : listeners) {
            listener.onNewIntent(intent);
        }
    }

    public static synchronized void onActivityResult(int requestCode, int resultCode, Intent data) {
        for (ActivityListener listener : listeners) {
            listener.onActivityResult(requestCode, resultCode, data);
        }
    }

}
