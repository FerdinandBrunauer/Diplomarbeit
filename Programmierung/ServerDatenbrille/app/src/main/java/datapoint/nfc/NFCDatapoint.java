package datapoint.nfc;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.nfc.NfcAdapter;
import android.preference.PreferenceManager;
import android.widget.Toast;

import datapoint.NFC_QRValidator;
import datapoint.Validator;
import event.datapoint.DatapointEventHandler;
import event.datapoint.DatapointEventObject;
import htlhallein.at.serverdatenbrille.R;

/**
 * Copyright 2015 (C) HTL - Hallein
 * Created on:  13.01.2015
 * Author:      Ferdinand
 */
public class NFCDatapoint {
    private Context context;
    private SharedPreferences preferences;
    private SharedPreferences.OnSharedPreferenceChangeListener myPreferenceListener;
    private NfcAdapter adapter;

    public NFCDatapoint(final Context context) {
        this.context = context;

        adapter = NfcAdapter.getDefaultAdapter(context);
        if(adapter == null) {
            Toast.makeText(context, context.getString(R.string.no_nfc_support), Toast.LENGTH_LONG).show();
        } else {
            if (!adapter.isEnabled()) {
                Toast.makeText(context, context.getString(R.string.nfc_disabled), Toast.LENGTH_LONG).show();
                // TODO ask if user wants to enable nfc
                // startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
                // reinitialize it.
            } else {
                Toast.makeText(context, context.getString(R.string.nfc_enabled), Toast.LENGTH_LONG).show();
            }
        }

        this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.myPreferenceListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.compareTo(context.getString(R.string.preferences_preference_nfc_enabled)) == 0) {
                    if(adapter != null) {
                        Resources res = context.getResources();
                        boolean status = preferences.getBoolean(context.getString(R.string.preferences_preference_nfc_enabled), res.getBoolean(R.bool.preferences_preference_nfc_enabled_default));
                        if (status == true) {
                            // TODO enable it
                        } else {
                            // TODO disable it
                        }
                    }
                }
            }
        };
    }

    private void fireEvent(Object... objects) {
        Validator validator = new NFC_QRValidator();
        DatapointEventObject eventObject = validator.validate(objects);
        if (eventObject != null) {
            DatapointEventHandler.fireDatapointEvent(eventObject);
        }
    }
}
