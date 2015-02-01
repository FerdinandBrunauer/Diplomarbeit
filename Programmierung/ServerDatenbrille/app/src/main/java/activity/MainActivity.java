package activity;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;

import activity.adapter.TabsPagerAdapter;
import database.DatabaseConnection;
import database.KmzReader;
import database.Placemark;
import database.XmlParser;
import database.openDataUtilities.OpenDataPackage;
import database.openDataUtilities.OpenDataResource;
import database.openDataUtilities.OpenDataUtilities;
import datapoint.NFC_QRValidator;
import datapoint.Validator;
import event.datapoint.DatapointEventHandler;
import event.datapoint.DatapointEventObject;
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

        // Instantiate Database
        DatabaseConnection.getInstance(this);

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Log.v("QR-Code", "cancelled");
            } else {
                Log.v("QR-Code", "Scanned: " + result.getContents());

                Validator validator = new NFC_QRValidator();
                DatapointEventObject eventObject = validator.validate(result.getContents());
                if (eventObject != null) {
                    DatapointEventHandler.fireDatapointEvent(eventObject);
                }
            }
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.qr_code_action: {
                IntentIntegrator integrator = new IntentIntegrator(this);
                integrator.setWide();
                integrator.autoWide();
                integrator.initiateScan();
                return true;
            }
            case R.id.sync_action: {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                String preferencePackage = preferences.getString(getString(R.string.preferences_preference_packages), "");
                Gson gson = new Gson();
                Type type = new TypeToken<ArrayList<Package>>() {
                }.getType();
                ArrayList<Package> storedPackages = gson.fromJson(preferencePackage, type);
                String[] keys = new String[storedPackages.size()];
                for (int i = 0; i < keys.length; i++)
                    keys[i] = storedPackages.get(i).getKey();
                new PackageCrawler().execute(keys);

                return true;
            }
            default:
                return false;
        }
    }

    public class PackageCrawler extends AsyncTask<String, String, String> {
        private ProgressDialog dialog = new ProgressDialog(MainActivity.this);

        private DatabaseConnection db;

        private void writeToDatabase(OpenDataPackage openDataPackage, int packageNr, int packagesCount) {
            if (openDataPackage != null) {
                setDialogTitle(getString(R.string.crawler_add_packageinfo) + " (" + (packageNr + 1) + "/" + packagesCount + ") ");
                db.insertPackage(openDataPackage);
                dialog.setMax(dialog.getMax() + (openDataPackage.getResources().size() * 10));

                for (OpenDataResource res : openDataPackage.getResources()) {
                    if (res.getFormat().toUpperCase().compareTo("KMZ") == 0) {
                        OpenDataUtilities.downloadFromUrl(res.getUrl(), res.getId() + ".kmz");
                        int needSteps = 0;
                        int stepsMade = 0;
                        setDialogTitle(getString(R.string.crawler_unzip) + " (" + (packageNr + 1) + "/" + packagesCount + ") ");
                        try {
                            File kmlFile = KmzReader.getKmlFile(res.getId() + ".kmz");
                            ArrayList<Placemark> placemarks = XmlParser.getPlacemarksFromKmlFile(kmlFile);
                            needSteps = placemarks.size();
                            dialog.setMax(dialog.getMax() + needSteps);
                            for (int n = 0; n < placemarks.size(); n++) {
                                setDialogTitle(getString(R.string.crawler_add_datapoint) + " (" + (packageNr + 1) + "/" + packagesCount + ")\n" + getString(R.string.craalwer_datapoint) + ": " + (n + 1) + "/" + placemarks.size());
                                String result = OpenDataUtilities.getRequestResult(placemarks.get(n).getLink());
                                String parsedHtml = OpenDataUtilities.parseHTML(result);
                                Bitmap image = OpenDataUtilities.getPlacemarkImage(result);

                                db.addDatapoint(
                                        parsedHtml,
                                        image,
                                        placemarks.get(n).getName(),
                                        "" + openDataPackage.getId(),
                                        "" + placemarks.get(n).getLocation().getLatitude(),
                                        "" + placemarks.get(n).getLocation().getLongitude(),
                                        placemarks.get(n).getLink()
                                );
                                dialog.setProgress(dialog.getProgress() + 1);
                                stepsMade += 1;
                            }
                        } catch (Exception e) {
                            Log.wtf("Error", "unpack KMZ-File", e);

                            int removeSteps = needSteps - stepsMade;
                            dialog.setMax(dialog.getMax() - removeSteps);
                        }
                    }
                    dialog.setProgress(dialog.getProgress() + 10);
                }
            }
            dialog.setProgress(dialog.getProgress() + 30);
        }

        public void deletePackage(String packageId) {
            onPreExecute();
            db.deletePackageInclusiveDatapoints(packageId);
            onPostExecute(null);
        }

        @Override
        protected String doInBackground(String... params) {
            dialog.setProgress(0);
            dialog.setProgressNumberFormat(null);
            dialog.setProgressPercentFormat(null);
            dialog.setTitle(R.string.crawler_open_database);
            dialog.setMax(params.length * 30);

            for (int i = 0; i < params.length; i++) {
                dialog.setTitle(getString(R.string.crawler_load_packageinfo) + " (" + (i + 1) + "/" + params.length + ") ");

                OpenDataPackage openDataPackage = OpenDataUtilities.getPackageById(params[i]);
                if (!db.isPackageInDatabase(openDataPackage)) {
                    writeToDatabase(openDataPackage, i, params.length);
                } else {
                    if (db.checkForPackageUpdate(openDataPackage)) {
                        db.deletePackageInclusiveDatapoints(openDataPackage);
                        writeToDatabase(openDataPackage, i, params.length);
                    }
                }
                dialog.setProgress(dialog.getProgress() + 30);
            }

            dialog.setProgress(dialog.getMax());
            return null;
        }

        private void setDialogTitle(final String title) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dialog.setTitle(title);
                }
            });
        }

        @Override
        protected void onPreExecute() {
            db = new DatabaseConnection(getApplicationContext());
            this.dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            this.dialog.setTitle("Please Wait");
            this.dialog.show();
        }

        @Override
        protected void onPostExecute(String result) {
            db.close();
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    }

    public class RemovePackage extends AsyncTask<String, Integer, String> {
        private ProgressDialog dialog = new ProgressDialog(MainActivity.this);

        private DatabaseConnection db;

        @Override
        protected String doInBackground(String... params) {
            db.deletePackageInclusiveDatapoints(params[0]);
            return null;
        }

        private void setDialogTitle(final String title) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dialog.setTitle(title);
                }
            });
        }

        @Override
        protected void onPreExecute() {
            db = new DatabaseConnection(getApplicationContext());
            this.dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            this.dialog.setTitle("Please Wait");
            this.dialog.show();
        }

        @Override
        protected void onPostExecute(String result) {
            db.close();
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    }
}
