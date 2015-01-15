package activity.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import activity.Fragment_First_Controlling;
import activity.Fragment_Second_QRCode;
import activity.Fragment_Third_Datapoints;

public class TabsPagerAdapter extends FragmentPagerAdapter {

    public TabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int index) {

        switch (index) {
            case 0:
                return new Fragment_First_Controlling();
            case 1:
                return new Fragment_Second_QRCode();
            case 2:
                return new Fragment_Third_Datapoints();
        }

        return null;
    }

    @Override
    public int getCount() {
        return 3;
    }

}
