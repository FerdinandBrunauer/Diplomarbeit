package activity;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import datapoint.gps.GPSDatapoint;
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
    // NFC
    private SharedPreferences preferences;
    private SharedPreferences.OnSharedPreferenceChangeListener myPreferenceListener;
    private NfcAdapter adapter;
    private PendingIntent pendingIntent;
    private IntentFilter[] intentFilter;
    private String[][] techList;
    // GPS
    private GPSDatapoint gpsDatapoint;

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
        DatabaseConnection.setContext(this);

        this.gpsDatapoint = new GPSDatapoint(this);

        this.server = new Server(this);
        new Thread(this.server).start(); // TODO FOR DEBUGGING DEACTIVATED

        this.preferences = PreferenceManager.getDefaultSharedPreferences(this);
        this.nfcInitialize();
        this.myPreferenceListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.compareTo(MainActivity.this.getString(R.string.preferences_preference_nfc_enabled)) == 0) {
                    boolean nfcEnabled = nfcPreferenceEnabled();

                    if (nfcEnabled && (adapter == null))
                        nfcInitialize();

                    if (adapter != null) {
                        if (nfcEnabled) {
                            adapter.enableForegroundDispatch(MainActivity.this, pendingIntent, intentFilter, techList);
                        } else {
                            adapter.disableForegroundDispatch(MainActivity.this);
                        }
                    }
                }
            }
        };
        this.preferences.registerOnSharedPreferenceChangeListener(this.myPreferenceListener);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (adapter != null)
            if (nfcPreferenceEnabled())
                adapter.enableForegroundDispatch(this, pendingIntent, intentFilter, techList);

        server.enableWifiAP();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (adapter != null)
            if (nfcPreferenceEnabled())
                adapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        server.disableWifiAP(); // XXX Not the best way ...
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
                DatapointEventObject eventObject = validator.validate(this, result.getContents());
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
                List<String> formats = new ArrayList<String>();
                formats.add("QR_CODE");
                integrator.setDesiredBarcodeFormats(formats);
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

    @Override
    protected void onNewIntent(Intent intent) {
        if (nfcPreferenceEnabled()) {
            String action = intent.getAction();
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            // parse through all NDEF messages and their records and pick text type only
            Parcelable[] data = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (data != null) {
                try {
                    for (int i = 0; i < data.length; i++) {
                        NdefRecord[] recs = ((NdefMessage) data[i]).getRecords();
                        for (int j = 0; j < recs.length; j++) {
                            if (recs[j].getTnf() == NdefRecord.TNF_WELL_KNOWN &&
                                    Arrays.equals(recs[j].getType(), NdefRecord.RTD_TEXT)) {
                                byte[] payload = recs[j].getPayload();
                                String textEncoding = ((payload[0] & 0200) == 0) ? "UTF-8" : "UTF-16";
                                int langCodeLen = payload[0] & 0077;

                                Validator validator = new NFC_QRValidator();
                                DatapointEventObject eventObject = validator.validate(this, new String(payload, langCodeLen + 1, payload.length - langCodeLen - 1, textEncoding));
                                if (eventObject != null) {
                                    DatapointEventHandler.fireDatapointEvent(eventObject);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e("TagDispatch", e.toString());
                }
            }
        }
    }

    private void nfcInitialize() {
        if (nfcPreferenceEnabled()) {
            this.adapter = NfcAdapter.getDefaultAdapter(this);
            if (this.adapter == null) {
                return;
            }
            if (!this.adapter.isEnabled()) {
                Log.v("NFC", this.getString(R.string.nfc_disabled));

                Toast.makeText(this, this.getString(R.string.nfc_disabled), Toast.LENGTH_LONG).show();

                SharedPreferences.Editor editor = this.preferences.edit();
                editor.putBoolean(this.getString(R.string.preferences_preference_nfc_enabled), false);
                editor.commit();

                this.adapter = null;

                return;
            }
            pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, this.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
            IntentFilter ndefIntent = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
            try {
                ndefIntent.addDataType("*/*");
                intentFilter = new IntentFilter[]{ndefIntent};
            } catch (Exception e) {
                Log.e("NfcTagDispatch", e.toString());
            }
            techList = new String[][]{new String[]{NfcF.class.getName()}};
        }
    }

    private boolean nfcPreferenceEnabled() {
        return preferences.getBoolean(this.getString(R.string.preferences_preference_nfc_enabled), this.getResources().getBoolean(R.bool.preferences_preference_nfc_enabled_default));
    }

    public class PackageCrawler extends AsyncTask<String, String, String> {
        private ProgressDialog dialog = new ProgressDialog(MainActivity.this);

        private void writeToDatabase(OpenDataPackage openDataPackage, int packageNr, int packagesCount) {
            if (openDataPackage != null) {
                setDialogTitle(getString(R.string.crawler_add_packageinfo) + " (" + (packageNr + 1) + "/" + packagesCount + ") ");
                DatabaseConnection.insertPackage(openDataPackage);
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
                            dialog.setProgress(dialog.getProgress() + 30);
                            dialog.setMax(dialog.getMax() + needSteps);
                            for (int n = 0; n < placemarks.size(); n++) {
                                setDialogTitle(getString(R.string.crawler_add_datapoint) + " (" + (packageNr + 1) + "/" + packagesCount + ")\n" + getString(R.string.craalwer_datapoint) + ": " + (n + 1) + "/" + placemarks.size());
                                String result = OpenDataUtilities.getRequestResult(placemarks.get(n).getLink());
                                String parsedHtml = OpenDataUtilities.parseHTML(result);
                                Bitmap image = OpenDataUtilities.getPlacemarkImage(result);

                                DatabaseConnection.addDatapoint(
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
        }

        public void deletePackage(String packageId) {
            onPreExecute();
            DatabaseConnection.deletePackageInclusiveDatapoints(packageId);
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
                if (!DatabaseConnection.isPackageInDatabase(openDataPackage)) {
                    writeToDatabase(openDataPackage, i, params.length);
                } else {
                    if (DatabaseConnection.checkForPackageUpdate(openDataPackage)) {
                        DatabaseConnection.deletePackageInclusiveDatapoints(openDataPackage);
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
            this.dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            this.dialog.setTitle("Please Wait");
            this.dialog.show();
        }

        @Override
        protected void onPostExecute(String result) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    }

    public class RemovePackage extends AsyncTask<String, Integer, String> {
        private ProgressDialog dialog = new ProgressDialog(MainActivity.this);

        @Override
        protected String doInBackground(String... params) {
            DatabaseConnection.deletePackageInclusiveDatapoints(params[0]);
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
            this.dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            this.dialog.setTitle("Please Wait");
            this.dialog.show();
        }

        @Override
        protected void onPostExecute(String result) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    }
}
