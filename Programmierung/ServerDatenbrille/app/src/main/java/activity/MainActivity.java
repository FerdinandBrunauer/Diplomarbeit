package activity;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ProgressBar;

import java.io.File;
import java.util.ArrayList;

import activity.adapter.TabsPagerAdapter;
import database.DatabaseConnection;
import database.KmzReader;
import database.Placemark;
import database.XmlParser;
import database.openDataUtilities.OpenDataPackage;
import database.openDataUtilities.OpenDataResource;
import database.openDataUtilities.OpenDataUtilities;
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
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.qr_code_action: {
                Intent intent = new Intent(this, QRCodeActivity.class);
                startActivity(intent);
                return true;
            }
            case R.id.sync_action: {
                //TODO: get packageIds from preferences
                new PackageCrawler().execute("a5841caf-afe2-4f98-bb68-bd4899e8c9cb");
            }
            default:
                return false;
        }
    }

    class PackageCrawler extends AsyncTask<String,Integer,String> {
        private ArrayList<OpenDataPackage> openDataPackages = new ArrayList<>();

        @Override
        protected String doInBackground(String... params) {
            DatabaseConnection db = new DatabaseConnection(getApplicationContext());
            for (int i=0; i<params.length;i++) {
                //TODO: check for update
                //TODO: progressBar
                OpenDataPackage p = OpenDataUtilities.getPackageById(params[i]);
                if(p!=null) {
                    db.insertPackage(p);
                    //if(packageId != -1) {
                        for (OpenDataResource res : p.getResources()) {
                            if (res.getFormat().toUpperCase().compareTo("KMZ") == 0) {
                                OpenDataUtilities.downloadFromUrl(res.getUrl(), res.getId() + ".kmz");
                                try {
                                    File kmlFile = KmzReader.getKmlFile(res.getId() + ".kmz");
                                    ArrayList<Placemark> placemarks = XmlParser.getPlacemarksFromKmlFile(kmlFile);
                                    for (int n=0; n<placemarks.size(); n++) {
                                        String result = OpenDataUtilities.getRequestResult(placemarks.get(n).getLink());
                                        String parsedHtml = OpenDataUtilities.parseHTML(result);
                                        Bitmap image = OpenDataUtilities.getPlacemarkImage(result);

                                        db.addDatapoint(
                                                parsedHtml,
                                                image,
                                                placemarks.get(n).getName(),
                                                "" + p.getId(),
                                                "" + placemarks.get(n).getLocation().getLatitude(),
                                                "" + placemarks.get(n).getLocation().getLongitude(),
                                                placemarks.get(n).getLink()
                                        );
                                    }
                                } catch (Exception e) {
                                    System.err.println(e);
                                }
                            }
                        }
                    //}
                }
            }
            return null;
        }

        private ProgressDialog dialog = new ProgressDialog(MainActivity.this);
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
