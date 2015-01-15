package activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
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
    private ImageView myArrowTop;
    private ImageView myArrowBottom;
    private TextView scrollIndex;
    private int scrollPercentage = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_first_controlling, container, false);

        this.scrollIndex = (TextView) rootView.findViewById(R.id.tvScrollIndex);
        this.myArrowTop = (ImageView) rootView.findViewById(R.id.ivArrowTop);
        this.myArrowTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if((scrollPercentage >= 2) && (scrollPercentage <= 100)) {
                    scrollPercentage -= 2;
                }

                Fragment_First_Controlling.this.scrollIndex.setText(Fragment_First_Controlling.this.scrollPercentage + "%");

                ScrollEventObject eventObject = new ScrollEventObject(this, ScrollEventDirection.DOWN, scrollPercentage);
                ScrollEventHandler.fireScrollEvent(eventObject);
            }
        });
        this.myArrowBottom = (ImageView) rootView.findViewById(R.id.ivArrowBottom);
        this.myArrowBottom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if((scrollPercentage <= 98) && (scrollPercentage >= 0)) {
                    scrollPercentage += 2;
                }

                Fragment_First_Controlling.this.scrollIndex.setText(Fragment_First_Controlling.this.scrollPercentage + "%");

                ScrollEventObject eventObject = new ScrollEventObject(this, ScrollEventDirection.UP, scrollPercentage);
                ScrollEventHandler.fireScrollEvent(eventObject);
            }
        });

        this.scrollIndex.setText(this.scrollPercentage + "%");

        return rootView;
    }
}
