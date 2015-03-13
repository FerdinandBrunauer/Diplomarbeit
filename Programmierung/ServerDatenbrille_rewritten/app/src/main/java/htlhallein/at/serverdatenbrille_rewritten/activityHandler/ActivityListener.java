package htlhallein.at.serverdatenbrille_rewritten.activityHandler;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public interface ActivityListener {

    public abstract void onCreate(Context context, Bundle savedInstanceState);

    public abstract void onResume(Context context, Activity activity);

    public abstract void onPause(Context context, Activity activity);

    public abstract void onDestroy(Context context);

    // QR specific
    public void showQRCode(Context context);

    // NFC specific
    public void onNewIntent(Intent intent);
}
