package activity.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import activity.Fragment_First_Controlling;
import activity.Fragment_Second_Datapoints;
import activity.Fragment_Third_Settings;

public class TabsPagerAdapter extends FragmentPagerAdapter {

    private Fragment[] fragments = {new Fragment_First_Controlling(), new Fragment_Second_Datapoints(), new Fragment_Third_Settings()}; // TODO

    public TabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int index) {
        if ((index >= 0) && (index < fragments.length)) {
            return fragments[index];
        } else {
            return null;
        }
    }

    @Override
    public int getCount() {
        return fragments.length;
    }

}
