package activity;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
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
import android.view.View;
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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

@SuppressWarnings("deprecation")
public class MainActivity extends FragmentActivity implements ActionBar.TabListener, ConnectionCallbacks,
        OnConnectionFailedListener, LocationListener{

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

    // LogCat tag
    private static final String TAG = MainActivity.class.getSimpleName();
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private Location mLastLocation;

    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;

    private LocationRequest mLocationRequest;

    // Location updates intervals in sec
    private static int UPDATE_INTERVAL = 2000; // 10 sec
    private static int FATEST_INTERVAL = 2000; // 5 sec
    private static int DISPLACEMENT = 1; // 10 meters;
    private SensorManager sensorManager;
    private float currentDegree = 0.0f;

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

        //Server
        this.server = new Server(this);
        //new Thread(this.server).start(); // TODO FOR DEBUGGING DEACTIVATED

        //NFC
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

        //GPS
        // First we need to check availability of play services
        if (checkPlayServices()) {

            // Building the GoogleApi client
            buildGoogleApiClient();
            createLocationRequest();
        }

    }

    private boolean gpsPreferenceEnabled() {
        return preferences.getBoolean(this.getString(R.string.preferences_preference_gps_enabled), this.getResources().getBoolean(R.bool.preferences_preference_gps_enabled_default));
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (adapter != null)
            if (nfcPreferenceEnabled())
                adapter.enableForegroundDispatch(this, pendingIntent, intentFilter, techList);

        checkPlayServices();
        // Resuming the periodic location updates

        if (mGoogleApiClient.isConnected() && gpsPreferenceEnabled()) {
            startLocationUpdates();Log.d(TAG, "Periodic location updates stopped!");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (adapter != null)
            if (nfcPreferenceEnabled())
                adapter.disableForegroundDispatch(this);
        stopLocationUpdates();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        this.server.disableWifiAP();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
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

    private void displayLocation() {

        mLastLocation = LocationServices.FusedLocationApi
                .getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();

            double altitude = mLastLocation.getAltitude();
            String provider = mLastLocation.getProvider();
            double test = mLastLocation.getAccuracy();
            double speed = mLastLocation.getSpeed();
            double bearing = mLastLocation.getBearing();
            Toast toast = Toast.makeText(this, longitude + ":" + latitude, Toast.LENGTH_SHORT);
            toast.show();
            //TODO: go ferdi
            //fireEvent(context, latitude, longitude, currentDegree);
        }
    }
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    /**
     * Creating location request object
     * */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    /**
     * Method to verify google play services on the device
     * */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
                finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Starting the location updates
     * */
    protected void startLocationUpdates() {
        this.sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        this.sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_NORMAL);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

    }

    /**
     * Stopping location updates
     */
    protected void stopLocationUpdates() {
        this.sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        this.sensorManager.unregisterListener(sensorEventListener);
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    /**
     * Google api callback methods
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }

    @Override
    public void onConnected(Bundle arg0) {
        if (gpsPreferenceEnabled()) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        // Assign the new location
        mLastLocation = location;

        // Displaying the new location on UI
        displayLocation();
    }

    private SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            float degree = Math.round(event.values[0]);
            currentDegree = -degree;
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };
}
