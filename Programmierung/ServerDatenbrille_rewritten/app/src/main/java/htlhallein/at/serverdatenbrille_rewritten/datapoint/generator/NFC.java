package htlhallein.at.serverdatenbrille_rewritten.datapoint.generator;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.preference.PreferenceManager;

import htlhallein.at.serverdatenbrille_rewritten.R;
import htlhallein.at.serverdatenbrille_rewritten.activityHandler.ActivityListener;

public class NFC implements ActivityListener {

    private NfcAdapter mNfcAdapter;
    private PendingIntent mPendingIntent;
    private IntentFilter[] mIntentFilter;
    private String[][] mTechList;

    private SharedPreferences mPreferences;
    private String mPreferenceKey = "";
    private SharedPreferences.OnSharedPreferenceChangeListener mPreferencesChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.compareTo(mPreferenceKey) == 0) {
                // TODO
            }
        }
    };
    private boolean mPreferenceDefault = false;

    // TODO

    private boolean nfcPreferenceEnabled() {
        return mPreferences.getBoolean(mPreferenceKey, mPreferenceDefault);
    }

    private void nfcInitialize() {
        /* if (nfcPreferenceEnabled()) {
            this.mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
            if (this.mNfcAdapter == null) {
                return;
            }
            if (!this.mNfcAdapter.isEnabled()) {
                // TODO
                // Toast.makeText(this, this.getString(R.string.nfc_disabled), Toast.LENGTH_LONG).show();

                SharedPreferences.Editor editor = this.preferences.edit();
                editor.putBoolean(this.getString(R.string.preferences_preference_nfc_enabled), false);
                editor.apply();

                this.mNfcAdapter = null;

                return;
            }
            pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, this.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
            IntentFilter ndefIntent = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
            try { */
        //ndefIntent.addDataType("*/*");
                /* intentFilter = new IntentFilter[]{ndefIntent};
            } catch (Exception e) {
                Log.e("NfcTagDispatch", e.toString());
            }
            techList = new String[][]{new String[]{NfcF.class.getName()}};
        } */
    }

    @Override
    public void onCreate(Context context, Bundle savedInstanceState) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Resources resources = context.getResources();
        mPreferenceKey = resources.getString(R.string.preferences_preference_nfc_enabled);
        mPreferenceDefault = resources.getBoolean(R.bool.preferences_preference_nfc_enabled_default);
        mPreferences.registerOnSharedPreferenceChangeListener(mPreferencesChangeListener);
    }

    @Override
    public void onResume(Context context, Activity activity) {
        if (mNfcAdapter != null)
            if (nfcPreferenceEnabled())
                mNfcAdapter.enableForegroundDispatch(activity, mPendingIntent, mIntentFilter, mTechList);
    }

    @Override
    public void onPause(Context context, Activity activity) {
        if (mNfcAdapter != null)
            if (nfcPreferenceEnabled())
                mNfcAdapter.disableForegroundDispatch(activity);
    }

    @Override
    public void onDestroy(Context context) {

    }

    @Override
    public void showQRCode(Context context) {
        // Ignore this in the NFC Generator
    }
}
