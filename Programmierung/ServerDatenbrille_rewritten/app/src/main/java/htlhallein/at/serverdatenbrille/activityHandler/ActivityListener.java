package htlhallein.at.serverdatenbrille.activityHandler;

import android.content.Intent;
import android.os.Bundle;

public interface ActivityListener {

    public abstract void onCreate(Bundle savedInstanceState);

    public abstract void onStart();

    public abstract void onResume();

    public abstract void onPause();

    public abstract void onStop();

    public abstract void onDestroy();

    // QR specific
    public abstract void showQRCode();

    public abstract void onActivityResult(int requestCode, int resultCode, Intent data);

    // NFC specific
    public abstract void onNewIntent(Intent intent);

}
