package at.htlhallein.clientdatenbrille;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;


public class SettingsActivity extends Activity {
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor editor = sharedPreferences.edit();

        final EditText hotspotName = (EditText)findViewById(R.id.settings_hotspot_name_view);
        final EditText hotspotPassword = (EditText)findViewById(R.id.settings_hotspot_password_view);

        hotspotName.setText(sharedPreferences.getString("preference_wifi_name","Datenbrille"));
        hotspotPassword.setText(sharedPreferences.getString("preference_wifi_password", "Passwort1!"));

        hotspotName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                editor.putString("preference_wifi_name", hotspotName.getText().toString());
                editor.commit();
            }
        });
        hotspotPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                editor.putString("preference_wifi_password", hotspotPassword.getText().toString());
                editor.commit();
            }
        });
    }
}
