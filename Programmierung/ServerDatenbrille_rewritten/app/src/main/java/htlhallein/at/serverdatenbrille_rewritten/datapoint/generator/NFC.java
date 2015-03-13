package htlhallein.at.serverdatenbrille_rewritten.datapoint.generator;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.tech.NfcF;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.util.Arrays;

import htlhallein.at.serverdatenbrille_rewritten.MainActivity;
import htlhallein.at.serverdatenbrille_rewritten.R;
import htlhallein.at.serverdatenbrille_rewritten.activityHandler.ActivityListener;

public class NFC implements ActivityListener {

    private NfcAdapter adapter;
    private PendingIntent pendingIntent;
    private IntentFilter[] intentFilter;
    private String[][] techList;

    private SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.getContext());
    private SharedPreferences.OnSharedPreferenceChangeListener mChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.compareTo(MainActivity.getContext().getString(R.string.preferences_preference_nfc_enabled)) == 0) {
                Resources resources = MainActivity.getContext().getResources();
                boolean nfcEnabled = mSharedPreferences.getBoolean(MainActivity.getContext().getString(R.string.preferences_preference_nfc_enabled), resources.getBoolean(R.bool.preferences_preference_nfc_enabled_default));

                if (nfcEnabled && (adapter == null)) {
                    NFCinitialize();
                }

                if (adapter != null) {
                    if (nfcEnabled) {
                        adapter.enableForegroundDispatch(MainActivity.getActivity(), pendingIntent, intentFilter, techList);
                    } else {
                        adapter.disableForegroundDispatch(MainActivity.getActivity());
                    }
                }
            }
        }
    };

    private void NFCinitialize() {
        Resources resources = MainActivity.getContext().getResources();
        boolean nfcEnabled = mSharedPreferences.getBoolean(MainActivity.getContext().getString(R.string.preferences_preference_nfc_enabled), resources.getBoolean(R.bool.preferences_preference_nfc_enabled_default));
        if (nfcEnabled) {
            adapter = NfcAdapter.getDefaultAdapter(MainActivity.getContext());
            if (adapter == null) {
                return; // No NFC Available
            }
            if (!this.adapter.isEnabled()) {
                Log.i(NFC.class.toString(), "NFC deaktiviert ...");
                Toast.makeText(MainActivity.getContext(), MainActivity.getContext().getString(R.string.nfc_disabled), Toast.LENGTH_LONG).show();
                // Set NFC Preference (back) to false
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putBoolean(MainActivity.getContext().getString(R.string.preferences_preference_nfc_enabled), false);
                editor.apply();

                this.adapter = null;

                return;
            }
            pendingIntent = PendingIntent.getActivity(MainActivity.getContext(), 0, new Intent(MainActivity.getContext(), MainActivity.getMClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
            IntentFilter ndefIntent = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
            try {
                ndefIntent.addDataType("*/*");
                intentFilter = new IntentFilter[]{ndefIntent};
            } catch (Exception e) {
                Log.d(NFC.class.toString(), "ndefIntent add DataType", e);
            }
            techList = new String[][]{new String[]{NfcF.class.getName()}};
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        // NFC tag read
        Resources resources = MainActivity.getContext().getResources();
        boolean nfcEnabled = mSharedPreferences.getBoolean(MainActivity.getContext().getString(R.string.preferences_preference_nfc_enabled), resources.getBoolean(R.bool.preferences_preference_nfc_enabled_default));
        if (nfcEnabled) {
            Parcelable[] data = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (data != null) {
                try {
                    for (Parcelable aData : data) {
                        NdefRecord[] recs = ((NdefMessage) aData).getRecords();
                        for (NdefRecord rec : recs) {
                            if (rec.getTnf() == NdefRecord.TNF_WELL_KNOWN &&
                                    Arrays.equals(rec.getType(), NdefRecord.RTD_TEXT)) {
                                byte[] payload = rec.getPayload();
                                String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";
                                int langCodeLen = payload[0] & 63;
                                String nfcText = new String(payload, langCodeLen + 1, payload.length - langCodeLen - 1, textEncoding);
                                Toast.makeText(MainActivity.getContext(), MainActivity.getContext().getString(R.string.nfc_tag_read), Toast.LENGTH_LONG).show();
                                Toast.makeText(MainActivity.getContext(), nfcText, Toast.LENGTH_LONG).show();
                                // TODO fire NFC Event
                                /* NFC_QRValidator validator = new NFC_QRValidator();
                                DatapointEventObject eventObject = validator.validate(this, new String(payload, langCodeLen + 1, payload.length - langCodeLen - 1, textEncoding));
                                if (eventObject != null) {
                                    DatapointEventHandler.fireDatapointEvent(eventObject);
                                } */
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e("TagDispatch", e.toString());
                }
            }
        }
    }

    @Override
    public void onCreate(Context context, Bundle savedInstanceState) {
        NFCinitialize();
        mSharedPreferences.registerOnSharedPreferenceChangeListener(mChangeListener);
    }

    @Override
    public void onResume(Context context, Activity activity) {
        if (adapter != null) {
            Resources resources = MainActivity.getContext().getResources();
            if (mSharedPreferences.getBoolean(MainActivity.getContext().getString(R.string.preferences_preference_nfc_enabled), resources.getBoolean(R.bool.preferences_preference_nfc_enabled_default))) {
                adapter.enableForegroundDispatch(activity, pendingIntent, intentFilter, techList);
            }
        }
    }

    @Override
    public void onPause(Context context, Activity activity) {
        if (adapter != null) {
            Resources resources = MainActivity.getContext().getResources();
            if (mSharedPreferences.getBoolean(MainActivity.getContext().getString(R.string.preferences_preference_nfc_enabled), resources.getBoolean(R.bool.preferences_preference_nfc_enabled_default))) {
                adapter.disableForegroundDispatch(activity);
            }
        }
    }

    @Override
    public void onDestroy(Context context) {

    }

    @Override
    public void showQRCode(Context context) {
        // Ignore this in the NFC Generator
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

    }
}