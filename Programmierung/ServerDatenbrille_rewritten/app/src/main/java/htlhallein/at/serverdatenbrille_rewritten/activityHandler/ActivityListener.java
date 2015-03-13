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
    public abstract void showQRCode(Context context);

    public abstract void onActivityResult(int requestCode, int resultCode, Intent data);

    // NFC specific
    public abstract void onNewIntent(Intent intent);

}
