package htlhallein.at.serverdatenbrille.activityHandler;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;

public class ActivityHandler {
    private static ArrayList<ActivityListener> listeners = new ArrayList<>();

    public static synchronized void addListener(ActivityListener listener) {
        Log.d(ActivityHandler.class.toString(), "addListener " + listener.getClass().toString());
        listeners.add(listener);
    }

    public static synchronized void onCreate(Bundle savedInstanceState) {
        Log.d(ActivityHandler.class.toString(), "onCreate");
        ActivityListener[] listeners = ActivityHandler.listeners.toArray(new ActivityListener[ActivityHandler.listeners.size()]);
        for (ActivityListener listener : listeners) {
            listener.onCreate(savedInstanceState);
        }
    }

    public static synchronized void onStart() {
        Log.d(ActivityHandler.class.toString(), "onStart");
        ActivityListener[] listeners = ActivityHandler.listeners.toArray(new ActivityListener[ActivityHandler.listeners.size()]);
        for (ActivityListener listener : listeners) {
            listener.onStart();
        }
    }

    public static synchronized void onResume() {
        Log.d(ActivityHandler.class.toString(), "onResume");
        ActivityListener[] listeners = ActivityHandler.listeners.toArray(new ActivityListener[ActivityHandler.listeners.size()]);
        for (ActivityListener listener : listeners) {
            listener.onResume();
        }
    }

    public static synchronized void onPause() {
        Log.d(ActivityHandler.class.toString(), "onPause");
        ActivityListener[] listeners = ActivityHandler.listeners.toArray(new ActivityListener[ActivityHandler.listeners.size()]);
        for (ActivityListener listener : listeners) {
            listener.onPause();
        }
    }

    public static synchronized void onStop() {
        Log.d(ActivityHandler.class.toString(), "onStop");
        ActivityListener[] listeners = ActivityHandler.listeners.toArray(new ActivityListener[ActivityHandler.listeners.size()]);
        for (ActivityListener listener : listeners) {
            listener.onStop();
        }
    }

    public static synchronized void onDestroy() {
        Log.d(ActivityHandler.class.toString(), "onDestroy");
        ActivityListener[] listeners = ActivityHandler.listeners.toArray(new ActivityListener[ActivityHandler.listeners.size()]);
        for (ActivityListener listener : listeners) {
            listener.onDestroy();
        }
    }

    public static synchronized void showQRCode() {
        Log.d(ActivityHandler.class.toString(), "qrCode");
        ActivityListener[] listeners = ActivityHandler.listeners.toArray(new ActivityListener[ActivityHandler.listeners.size()]);
        for (ActivityListener listener : listeners) {
            listener.showQRCode();
        }
    }

    public static synchronized void onNewIntent(Intent intent) {
        Log.d(ActivityHandler.class.toString(), "onNewIntent");
        ActivityListener[] listeners = ActivityHandler.listeners.toArray(new ActivityListener[ActivityHandler.listeners.size()]);
        for (ActivityListener listener : listeners) {
            listener.onNewIntent(intent);
        }
    }

    public static synchronized void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(ActivityHandler.class.toString(), "onActivityResult");
        ActivityListener[] listeners = ActivityHandler.listeners.toArray(new ActivityListener[ActivityHandler.listeners.size()]);
        for (ActivityListener listener : listeners) {
            listener.onActivityResult(requestCode, resultCode, data);
        }
    }

    public static synchronized void clearListener() {
        Log.d(ActivityHandler.class.toString(), "clearListener");
        listeners.clear();
    }
}
