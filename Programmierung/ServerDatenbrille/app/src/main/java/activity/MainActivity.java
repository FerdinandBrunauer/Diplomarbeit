package activity;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import activity.adapter.TabsPagerAdapter;
import htlhallein.at.serverdatenbrille.R;
import server.Server;

@SuppressWarnings("deprecation")
public class MainActivity extends FragmentActivity implements ActionBar.TabListener {

    private ViewPager myViewPager;
    //    private TabsPagerAdapter myTabsPagerAdapter; // temporarily kept as a variable in the constructor
    private ActionBar myActionBar;
    private Server server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        final TabsPagerAdapter myTabsPagerAdapter;

        this.myViewPager = (ViewPager) findViewById(R.id.pager);
        this.myActionBar = getActionBar();
        myTabsPagerAdapter = new TabsPagerAdapter(getSupportFragmentManager());

        this.myViewPager.setAdapter(myTabsPagerAdapter);
        this.myActionBar.setHomeButtonEnabled(false);
        this.myActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        this.myActionBar.addTab(this.myActionBar.newTab().setText(R.string.first_tab_name).setTabListener(this));
        this.myActionBar.addTab(this.myActionBar.newTab().setText(R.string.second_tab_name).setTabListener(this));
        this.myActionBar.addTab(this.myActionBar.newTab().setText(R.string.third_tab_name).setTabListener(this));

        this.myViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                Log.v("ViewSelected", "" + position);

                MainActivity.this.myActionBar.setSelectedNavigationItem(position);
                Fragment selectedFragment = myTabsPagerAdapter.getItem(position);

                int indexPause1 = position - 1;
                int indexPause2 = position + 1;
                int indexResume = position;

                if(indexPause1 == -1)
                    indexPause1 = 2;
                if(indexPause2 == 3)
                    indexPause2 = 0;

            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        this.server = new Server();
        new Thread(this.server).start();
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        this.myViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean temp = super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_activity, menu);
        return temp;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.settings: {
                Intent intent = new Intent(this, SettingsActivity.class);
                this.startActivity(intent);
                return true;
            }
            default: {
                return false;
            }
        }
    }
}
