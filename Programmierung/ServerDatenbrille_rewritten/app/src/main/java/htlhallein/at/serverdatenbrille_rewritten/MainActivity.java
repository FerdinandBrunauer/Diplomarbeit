package htlhallein.at.serverdatenbrille_rewritten;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import htlhallein.at.serverdatenbrille_rewritten.activityHandler.ActivityHandler;
import htlhallein.at.serverdatenbrille_rewritten.datapoint.generator.GPS;
import htlhallein.at.serverdatenbrille_rewritten.datapoint.generator.NFC;
import htlhallein.at.serverdatenbrille_rewritten.datapoint.generator.QRCode;
import htlhallein.at.serverdatenbrille_rewritten.drawer_menu.NsMenuAdapter;
import htlhallein.at.serverdatenbrille_rewritten.drawer_menu.NsMenuItemModel;

public class MainActivity extends Activity {

    // Drawer
    private static final int nsMenuItem_ControllerID = 0, nsMenuItem_Packages = 1, nsMenuItem_Datapoints = 2, nsMenuItem_qrcode = 3, nsMenuItem_settings = 4;
    private ListView mDrawerList;
    private DrawerLayout mDrawer;
    private CustomActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_drawer);

        // enable ActionBar app icon to behave as action to toggle nav drawer
        try {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setHomeButtonEnabled(true);
        } catch (NullPointerException e) {
            // There is no Action bar ...
            Log.e(MainActivity.class.toString(), "Keine ActionBar vorhanden!");
        }

        // set Icon when Drawer is opened
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawer.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        _initMenu();
        // override drawertoggleadapter to make it possible, to react to open and close drawer
        mDrawerToggle = new CustomActionBarDrawerToggle(this, mDrawer);
        mDrawer.setDrawerListener(mDrawerToggle);

        // Event
        ActivityHandler.addListener(new GPS());
        ActivityHandler.addListener(new NFC());
        ActivityHandler.addListener(new QRCode());

        ActivityHandler.onCreate(this, savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();

        ActivityHandler.onResume(this, this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        ActivityHandler.onPause(this, this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        ActivityHandler.onDestroy(this);
    }

    private void _initMenu() {
        NsMenuAdapter mAdapter = new NsMenuAdapter(this);
        mAdapter.addHeader(R.string.drawer_title_activitys);
        NsMenuItemModel nsItemController = new NsMenuItemModel(R.string.drawer_action_controlling, R.drawable.ic_controller, nsMenuItem_ControllerID);
        NsMenuItemModel nsItemPackages = new NsMenuItemModel(R.string.drawer_action_packages, R.drawable.ic_packages, nsMenuItem_Packages);
        // TODO load count
        nsItemPackages.counter = 1;
        NsMenuItemModel nsItemDatapoints = new NsMenuItemModel(R.string.drawer_action_datapoints, R.drawable.ic_datapoints, nsMenuItem_Datapoints);
        // TODO load count
        nsItemDatapoints.counter = 1;
        mAdapter.addItem(nsItemController);
        mAdapter.addItem(nsItemPackages);
        mAdapter.addItem(nsItemDatapoints);

        mAdapter.addHeader(R.string.drawer_title_actions);
        NsMenuItemModel nsItemQr = new NsMenuItemModel(R.string.drawer_action_qr_code, R.drawable.ic_qr, nsMenuItem_qrcode);
        mAdapter.addItem(nsItemQr);

        mAdapter.addHeader(R.string.drawer_title_settings);
        NsMenuItemModel nsItemSettings = new NsMenuItemModel(R.string.drawer_action_settings, R.drawable.ic_action_settings, nsMenuItem_settings);
        mAdapter.addItem(nsItemSettings);

        mDrawerList = (ListView) findViewById(R.id.drawer);
        if (mDrawerList != null)
            mDrawerList.setAdapter(mAdapter);

        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        // Handle your other action bar items...
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings: {
                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.flContent, new PrefsFragment())
                        .commit();

                if (mDrawer.isDrawerOpen(mDrawerList))
                    mDrawer.closeDrawer(mDrawerList);

                return true;
            }
            case R.id.qr_code_action: {
                ActivityHandler.showQRCode(this);
                return true;
            }
            case R.id.sync_action: {
                // TODO
                return true;
            }
            default:
                return super.onMenuItemSelected(featureId, item);
        }
    }

    public static class PrefsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preference);
        }
    }

    @SuppressWarnings("deprecation")
    private class CustomActionBarDrawerToggle extends ActionBarDrawerToggle {

        public CustomActionBarDrawerToggle(Activity mActivity, DrawerLayout mDrawerLayout) {
            super(
                    mActivity,
                    mDrawerLayout,
                    R.drawable.ic_drawer,
                    R.string.app_name,
                    R.string.app_name);
        }

        @Override
        public void onDrawerClosed(View view) {
            //getActionBar().setTitle(getString(R.string.ns_menu_close));
            invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
        }

        @Override
        public void onDrawerOpened(View drawerView) {
            //getActionBar().setTitle(getString(R.string.ns_menu_open));
            invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
        }
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mDrawerList.setItemChecked(position, true);
            NsMenuItemModel clickedItem = (NsMenuItemModel) mDrawerList.getItemAtPosition(position);
            switch (((NsMenuItemModel) mDrawerList.getItemAtPosition(position)).id) {
                case nsMenuItem_ControllerID: {
                    // TODO
                    break;
                }
                case nsMenuItem_Packages: {
                    // TODO
                    break;
                }
                case nsMenuItem_Datapoints: {
                    // TODO
                    break;
                }
                case nsMenuItem_qrcode: {
                    // TODO
                    break;
                }
                case nsMenuItem_settings: {
                    getFragmentManager()
                            .beginTransaction()
                            .replace(R.id.flContent, new PrefsFragment())
                            .commit();
                    break;
                }
            }
            mDrawer.closeDrawer(mDrawerList);
        }
    }

}