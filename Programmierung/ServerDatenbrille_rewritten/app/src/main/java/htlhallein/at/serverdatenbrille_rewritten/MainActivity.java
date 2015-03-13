package htlhallein.at.serverdatenbrille_rewritten;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
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
import htlhallein.at.serverdatenbrille_rewritten.drawer.NsMenuAdapter;
import htlhallein.at.serverdatenbrille_rewritten.drawer.NsMenuItemModel;

public class MainActivity extends Activity {

    // Drawer
    private static final int nsMenuItem_ControllerID = 0, nsMenuItem_Packages = 1, nsMenuItem_Datapoints = 2, nsMenuItem_qrcode = 3, nsMenuItem_settings = 4;
    // Context
    private static Context mContext;
    // Activity
    private static Activity mActivity;
    // Class
    private static Class mClass;
    private ListView mDrawerList;
    private DrawerLayout mDrawer;
    private CustomActionBarDrawerToggle mDrawerToggle;

    public static Context getContext() {
        return mContext;
    }

    public static Activity getActivity() {
        return mActivity;
    }

    public static Class getMClass() {
        return mClass;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_drawer);

        // Set Controller fragment
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.flContent, new ControllerFragment())
                .commit();

        // make context available global
        mContext = this;
        // make Activity available global
        mActivity = this;
        // make Class available global
        mClass = getClass();

        // enable ActionBar app icon to behave as action to toggle nav drawer
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setHomeButtonEnabled(true);
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

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        ActivityHandler.onNewIntent(intent);
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
        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, @NonNull MenuItem item) {
        switch (item.getItemId()) {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        ActivityHandler.onActivityResult(requestCode, resultCode, data);
    }

    private void _initMenu() {
        NsMenuAdapter mAdapter = new NsMenuAdapter(this);
        mAdapter.addHeader(R.string.drawer_title_activitys);
        NsMenuItemModel nsItemController = new NsMenuItemModel(R.string.drawer_action_controlling, R.drawable.ic_controller, nsMenuItem_ControllerID);
        NsMenuItemModel nsItemPackages = new NsMenuItemModel(R.string.drawer_action_packages, R.drawable.ic_packages, nsMenuItem_Packages);
        NsMenuItemModel nsItemDatapoints = new NsMenuItemModel(R.string.drawer_action_datapoints, R.drawable.ic_datapoints, nsMenuItem_Datapoints);
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
            super(mActivity, mDrawerLayout, R.drawable.ic_drawer, R.string.app_name, R.string.app_name);
        }

        @Override
        public void onDrawerClosed(View view) {
            invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
        }

        @Override
        public void onDrawerOpened(View drawerView) {
            invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
        }
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mDrawer.closeDrawer(mDrawerList);

            NsMenuItemModel clickedItem = (NsMenuItemModel) mDrawerList.getItemAtPosition(position);
            switch (clickedItem.id) {
                case nsMenuItem_ControllerID: {
                    getFragmentManager()
                            .beginTransaction()
                            .replace(R.id.flContent, new ControllerFragment())
                            .commit();
                    break;
                }
                case nsMenuItem_Packages: {
                    getFragmentManager()
                            .beginTransaction()
                            .replace(R.id.flContent, new DatapointFragment())
                            .commit();
                    break;
                }
                case nsMenuItem_Datapoints: {
                    // TODO
                    break;
                }
                case nsMenuItem_qrcode: {
                    ActivityHandler.showQRCode(MainActivity.this);
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
        }
    }

}