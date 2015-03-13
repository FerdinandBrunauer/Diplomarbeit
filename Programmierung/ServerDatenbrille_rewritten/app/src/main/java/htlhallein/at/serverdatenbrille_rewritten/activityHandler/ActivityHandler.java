package htlhallein.at.serverdatenbrille_rewritten.activityHandler;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import java.util.ArrayList;

public class ActivityHandler {
    // All methods in this class use the same monitor, so it is fully THREADSAFE

    private static ArrayList<ActivityListener> listeners = new ArrayList<>();

    public static synchronized void addListener(ActivityListener listener) {
        listeners.add(listener);
    }

    public static synchronized void onCreate(Context context, Bundle savedInstanceState) {
        for (ActivityListener listener : listeners) {
            listener.onCreate(context, savedInstanceState);
        }
    }

    public static synchronized void onResume(Context context, Activity activity) {
        for (ActivityListener listener : listeners) {
            listener.onResume(context, activity);
        }
    }

    public static synchronized void onPause(Context context, Activity activity) {
        for (ActivityListener listener : listeners) {
            listener.onPause(context, activity);
        }
    }

    public static synchronized void onDestroy(Context context) {
        for (ActivityListener listener : listeners) {
            listener.onDestroy(context);
        }
    }

    public static synchronized void showQRCode(Context context) {
        for (ActivityListener listener : listeners) {
            listener.showQRCode(context);
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
