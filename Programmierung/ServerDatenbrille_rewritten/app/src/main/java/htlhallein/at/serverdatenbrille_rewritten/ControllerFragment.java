package htlhallein.at.serverdatenbrille_rewritten;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import htlhallein.at.serverdatenbrille_rewritten.event.scroll.ScrollEventDirection;
import htlhallein.at.serverdatenbrille_rewritten.event.scroll.ScrollEventHandler;
import htlhallein.at.serverdatenbrille_rewritten.event.scroll.ScrollEventObject;

public class ControllerFragment extends Fragment {

    // needed for OnTouchListener
    public static final int MESSAGE_CHECK_BTN_STILL_PRESSED = 1;
    protected static Handler myLongPressHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_CHECK_BTN_STILL_PRESSED:
                    ImageView btn = (ImageView) MainActivity.getActivity().findViewById(msg.arg1);
                    if (btn.getTag() != null) { // button is still pressed
                        btn.performClick(); // perform Click or different long press action
                        Message msg1 = new Message(); // schedule next btn pressed check
                        msg1.copyFrom(msg);
                        myLongPressHandler.sendMessageDelayed(msg1, msg1.arg2);
                    }
                    break;
            }
        }
    };
    protected SharedPreferences.OnSharedPreferenceChangeListener mySharedPreferenceslistener;
    private TextView scrollIndex;
    private static int scrollPercentage = 0;
    private static int scrollPerClick;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_controlling, container, false);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        // store this listener, otherwise it will get garbage collected and will not work anymore!
        this.mySharedPreferenceslistener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                try {
                    ControllerFragment.scrollPerClick = Integer.parseInt(prefs.getString(getString(R.string.preferences_preference_scroll_distance_per_click), getString(R.string.preferences_preference_scroll_distance_per_click_default)));
                } catch (IllegalStateException e) {
                    // Can be ignored
                }
            }
        };
        preferences.registerOnSharedPreferenceChangeListener(this.mySharedPreferenceslistener);
        scrollPerClick = Integer.parseInt(preferences.getString(getString(R.string.preferences_preference_scroll_distance_per_click), getString(R.string.preferences_preference_scroll_distance_per_click_default)));


        this.scrollIndex = (TextView) rootView.findViewById(R.id.tvScrollIndex);
        this.scrollIndex.setText(scrollPercentage + "%");

        ImageView myArrowTop = (ImageView) rootView.findViewById(R.id.ivArrowTop);
        myArrowTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scrollPercentage -= scrollPerClick;
                if (scrollPercentage < 0)
                    scrollPercentage = 0;
                scrollIndex.setText(scrollPercentage + "%");

                ScrollEventObject eventObject = new ScrollEventObject(this, ScrollEventDirection.UP, scrollPercentage);
                ScrollEventHandler.fireScrollEvent(eventObject);
            }
        });
        myArrowTop.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        v.performClick();
                        Message msg = new Message();
                        msg.what = MESSAGE_CHECK_BTN_STILL_PRESSED;
                        msg.arg1 = R.id.ivArrowTop;
                        msg.arg2 = 200; // repeat time in ms
                        v.setTag(v);
                        myLongPressHandler.sendMessageDelayed(msg, msg.arg2);
                        break;
                    }
                    case MotionEvent.ACTION_MOVE:
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        v.setTag(null); // mark btn as not pressed
                        break;
                }
                return true;
            }
        });

        ImageView myArrowBottom = (ImageView) rootView.findViewById(R.id.ivArrowBottom);
        myArrowBottom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scrollPercentage += scrollPerClick;
                if (scrollPercentage > 100)
                    scrollPercentage = 100;
                scrollIndex.setText(scrollPercentage + "%");

                ScrollEventObject eventObject = new ScrollEventObject(this, ScrollEventDirection.DOWN, scrollPercentage);
                ScrollEventHandler.fireScrollEvent(eventObject);
            }
        });
        myArrowBottom.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        v.performClick();
                        Message msg = new Message();
                        msg.what = MESSAGE_CHECK_BTN_STILL_PRESSED;
                        msg.arg1 = R.id.ivArrowBottom;
                        msg.arg2 = 200;
                        v.setTag(v);
                        myLongPressHandler.sendMessageDelayed(msg, msg.arg2);
                        break;
                    }
                    case MotionEvent.ACTION_MOVE:
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        v.setTag(null);
                        break;
                }
                return true;
            }
        });

        return rootView;
    }
}
