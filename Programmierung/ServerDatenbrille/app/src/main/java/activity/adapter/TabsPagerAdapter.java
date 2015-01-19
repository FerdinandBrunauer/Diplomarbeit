package activity.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import activity.Fragment_First_Controlling;
import activity.Fragment_Second_QRCode;
import activity.Fragment_Third_Datapoints;

public class TabsPagerAdapter extends FragmentPagerAdapter {

    private Fragment[] fragments = {new Fragment_First_Controlling(), new Fragment_Second_QRCode(), new Fragment_Third_Datapoints()};

    public TabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int index) {
        if((index >= 0) && (index < fragments.length)) {
            return fragments[index];
        } else {
            return null;
        }
    }

    @Override
    public int getCount() {
        return fragments.length;
    }

    public void setActive(int index) {
        if((index >= 0) && (index < fragments.length)) {
            fragments[index].onResume();

            for(int i = 0; i < fragments.length; i++) {
                if(i == index) {
                    continue;
                } else {
                    setInactive(i);
                }
            }
        }
    }

    public void setInactive(int index) {
        if((index >= 0) && (index < fragments.length)) {
            fragments[index].onPause();
        }
    }

}
