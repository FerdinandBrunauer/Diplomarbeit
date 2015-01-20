package activity;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import activity.adapter.TabsPagerAdapter;
import htlhallein.at.serverdatenbrille.R;
import server.Server;

@SuppressWarnings("deprecation")
public class MainActivity extends FragmentActivity implements ActionBar.TabListener {

    private ViewPager myViewPager;
    private TabsPagerAdapter myTabsPagerAdapter;
    private ActionBar myActionBar;
    private Server server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        this.myViewPager = (ViewPager) findViewById(R.id.pager);
        this.myActionBar = getActionBar();
        this.myTabsPagerAdapter = new TabsPagerAdapter(getSupportFragmentManager());

        this.myViewPager.setAdapter(this.myTabsPagerAdapter);
        this.myActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        this.myActionBar.setDisplayShowHomeEnabled(false);
        this.myActionBar.setDisplayHomeAsUpEnabled(false);
        this.myActionBar.setIcon(R.drawable.ic_launcher);

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

        this.server = new Server(this);
//        new Thread(this.server).start(); TODO FOR DEBUGGING DEACTIVATED
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
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.qr_code_action: {
                Intent intent = new Intent(this, QRCodeActivity.class);
                startActivity(intent);
                return true;
            }
            default:
                return false;
        }
    }
}
