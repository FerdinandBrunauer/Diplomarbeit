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
import htlhallein.at.serverdatenbrille_rewritten.datapoint.Validator;
import htlhallein.at.serverdatenbrille_rewritten.event.datapoint.DatapointEventHandler;
import htlhallein.at.serverdatenbrille_rewritten.event.datapoint.DatapointEventObject;

public class QRCode implements ActivityListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {

    }

    @Override
    public void onStart() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public void showQRCode() {
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
                DatapointEventObject eventObject = Validator.validate(result.getContents());
                if (eventObject != null) {
                    DatapointEventHandler.fireDatapointEvent(eventObject);
                }
            }
        }
    }

    @Override
    public void onNewIntent(Intent intent) {

    }
}
