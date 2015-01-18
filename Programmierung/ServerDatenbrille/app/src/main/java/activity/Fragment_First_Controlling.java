package activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import event.scroll.ScrollEventDirection;
import event.scroll.ScrollEventHandler;
import event.scroll.ScrollEventObject;
import htlhallein.at.serverdatenbrille.R;

public class Fragment_First_Controlling extends Fragment {
    private TextView scrollIndex;
    private int scrollPercentage = 0;
    private int scrollPerClick;
    private SharedPreferences.OnSharedPreferenceChangeListener mySharedPreferenceslistener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_first_controlling, container, false);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        // store this listener, otherwise it will get garbage collected and will not work anymore!
        this.mySharedPreferenceslistener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                Fragment_First_Controlling.this.scrollPerClick = Integer.parseInt(prefs.getString(getString(R.string.preferences_preference_scroll_distance_per_click), getString(R.string.preferences_preference_scroll_distance_per_click_default)));
            }
        };
        preferences.registerOnSharedPreferenceChangeListener(this.mySharedPreferenceslistener);
        this.scrollPerClick = Integer.parseInt(preferences.getString(getString(R.string.preferences_preference_scroll_distance_per_click), getString(R.string.preferences_preference_scroll_distance_per_click_default)));

        this.scrollIndex = (TextView) rootView.findViewById(R.id.tvScrollIndex);
        ImageView myArrowTop = (ImageView) rootView.findViewById(R.id.ivArrowTop);
        myArrowTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if ((scrollPercentage - Fragment_First_Controlling.this.scrollPerClick) >= 0) {
                    scrollPercentage -= Fragment_First_Controlling.this.scrollPerClick;
                } else {
                    scrollPercentage = 0;
                }

                Fragment_First_Controlling.this.scrollIndex.setText(Fragment_First_Controlling.this.scrollPercentage + "%");

                ScrollEventObject eventObject = new ScrollEventObject(this, ScrollEventDirection.DOWN, scrollPercentage);
                ScrollEventHandler.fireScrollEvent(eventObject);
            }
        });
        ImageView myArrowBottom = (ImageView) rootView.findViewById(R.id.ivArrowBottom);
        myArrowBottom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((scrollPercentage + Fragment_First_Controlling.this.scrollPerClick) <= 100) {
                    scrollPercentage += Fragment_First_Controlling.this.scrollPerClick;
                } else {
                    scrollPercentage = 100;
                }

                Fragment_First_Controlling.this.scrollIndex.setText(Fragment_First_Controlling.this.scrollPercentage + "%");

                ScrollEventObject eventObject = new ScrollEventObject(this, ScrollEventDirection.UP, scrollPercentage);
                ScrollEventHandler.fireScrollEvent(eventObject);
            }
        });

        this.scrollIndex.setText(this.scrollPercentage + "%");

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.v("Controlling", "resume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.v("Controlling", "pause");
    }
}
