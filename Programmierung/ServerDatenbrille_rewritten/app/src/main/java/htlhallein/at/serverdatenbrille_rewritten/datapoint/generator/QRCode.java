package htlhallein.at.serverdatenbrille_rewritten.datapoint.generator;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.List;

import htlhallein.at.serverdatenbrille_rewritten.MainActivity;
import htlhallein.at.serverdatenbrille_rewritten.R;
import htlhallein.at.serverdatenbrille_rewritten.activityHandler.ActivityListener;

public class QRCode implements ActivityListener {

    @Override
    public void showQRCode(Context context) {
        // Very Important
        IntentIntegrator intentIntegrator = new IntentIntegrator(MainActivity.getActivity());
        List<String> formats = new ArrayList<>();
        formats.add("QR_CODE");
        intentIntegrator.setDesiredBarcodeFormats(formats);
        intentIntegrator.initiateScan();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Log.d(QRCode.class.toString(), "cancelled");
                // Toast.makeText(MainActivity.getContext(), MainActivity.getContext().getString(R.string.qr_code_canceled), Toast.LENGTH_LONG).show();
            } else {
                Log.d(QRCode.class.toString(), "Scanned: " + result.getContents());
                Toast.makeText(MainActivity.getContext(), MainActivity.getContext().getString(R.string.qr_code_scanned), Toast.LENGTH_LONG).show();
                // TODO
                /* NFC_QRValidator validator = new NFC_QRValidator();
                DatapointEventObject eventObject = validator.validate(this, result.getContents());
                if (eventObject != null) {
                    DatapointEventHandler.fireDatapointEvent(eventObject);
                } */
            }
        }
    }

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
    public void onNewIntent(Intent intent) {

    }
}
