package activity;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import activity.adapter.TabsPagerAdapter;
import htlhallein.at.serverdatenbrille.R;

@SuppressWarnings("deprecation")
public class MainActivity extends FragmentActivity implements ActionBar.TabListener {

    private ViewPager myViewPager;
//    private TabsPagerAdapter myTabsPagerAdapter;
    private ActionBar myActionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        TabsPagerAdapter myTabsPagerAdapter;

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
                MainActivity.this.myActionBar.setSelectedNavigationItem(position);
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
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
