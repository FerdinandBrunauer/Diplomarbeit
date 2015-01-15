package activity;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import htlhallein.at.serverdatenbrille.R;

@SuppressWarnings("deprecation")
public class SettingsActivity extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference);
    }
}
