package activity;

import android.os.Bundle;

import activity.preference.PreferenceFragment;
import htlhallein.at.serverdatenbrille.R;

public class Fragment_Third_Settings extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference);
    }
}
