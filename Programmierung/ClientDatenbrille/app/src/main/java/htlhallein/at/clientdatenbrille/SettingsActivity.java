package htlhallein.at.clientdatenbrille;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by Ferdi on 04.01.2015.
 */
public class SettingsActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
