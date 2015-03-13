package htlhallein.at.serverdatenbrille_rewritten.datapoint.generator;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import htlhallein.at.serverdatenbrille_rewritten.activityHandler.ActivityListener;

public class GPS implements ActivityListener {

    @Override
    public void onCreate(Context context, Bundle savedInstanceState) {

    }

    @Override
    public void onResume(Context context, Activity activity) {

    }

    @Override
    public void onPause(Context context, Activity activity) {

    }

    @Override
    public void onDestroy(Context context) {

    }

    @Override
    public void showQRCode(Context context) {
        // Ignore this in the GPS Generator
    }

    @Override
    public void onNewIntent(Intent intent) {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

    }
}
