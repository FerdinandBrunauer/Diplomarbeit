package htlhallein.at.serverdatenbrille.opendata;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import htlhallein.at.serverdatenbrille.DatapointFragment;
import htlhallein.at.serverdatenbrille.MainActivity;
import htlhallein.at.serverdatenbrille.R;
import htlhallein.at.serverdatenbrille.database.DatabaseHelper;
import htlhallein.at.serverdatenbrille.memoryObjects.DataPackage;
import htlhallein.at.serverdatenbrille.memoryObjects.OpenDataPackage;
import htlhallein.at.serverdatenbrille.memoryObjects.OpenDataResource;
import htlhallein.at.serverdatenbrille.memoryObjects.Placemark;
import htlhallein.at.serverdatenbrille.opendata.kmzUtil.KmzReader;
import htlhallein.at.serverdatenbrille.opendata.kmzUtil.StringUtils;
import htlhallein.at.serverdatenbrille.opendata.kmzUtil.XmlParser;

public class PackageCrawler extends AsyncTask<String, String, String> {
    private ProgressDialog dialog = new ProgressDialog(MainActivity.getContext());
    private int packageCounter;
    private int packagesCount;
    public static boolean isRunning = true;

    @Override
    protected void onPreExecute() {
        final int[] clickcounter = {0};
        super.onPreExecute();
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setIndeterminate(false);
        dialog.setTitle("Please Wait");
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.setButton(DialogInterface.BUTTON_NEUTRAL, MainActivity.getContext().getString(R.string.cancel), (DialogInterface.OnClickListener) null);
        dialog.show();

        final Button dialogButton = dialog.getButton( DialogInterface.BUTTON_NEUTRAL );
        dialogButton.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick ( View view ) {
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialog.setTitle(MainActivity.getContext().getString(R.string.crawler_cancel));
                dialog.setIndeterminate(true);
                isRunning = false;
                clickcounter[0]++;
                if(clickcounter[0] >= 5){
                    dialog.setTitle(MainActivity.getContext().getString(R.string.crawler_clicks_op));
                    clickcounter[0] = 0;
                }
            }

        });
    }


    @Override
    protected String doInBackground(String... params) {
        dialog.setProgress(0);
        dialog.setProgressNumberFormat(null);
        dialog.setProgressPercentFormat(null);
        dialog.setTitle(MainActivity.getContext().getString(R.string.crawler_open_database));
        dialog.setMax(1000);

        List<DataPackage> packageList = DatabaseHelper.getDataPackages();
        packageCounter = 1;

        if(!isRunning){
            return "";
        }

        if(!hasActiveInternetConnection()){
            return "";
        }
        if(params.length == 0){
            dialog.setProgress(packagesCount*100);
            packagesCount = packageList.size();
            dialog.setMax(packagesCount*1000);
            for (DataPackage dataPackage : packageList) {
                if(!isRunning){
                    return "";
                }
                setDialogTitle(MainActivity.getContext().getString(R.string.crawler_load_packageinfo) + " (" + (packageCounter) + "/" + packageList.size() + ") ");
                if (dataPackage.isDatapointsInstalled()) {
                    try {
                        if(!isRunning){
                            return "";
                        }
                        if (checkForUpdate(dataPackage)) {
                            if(!isRunning){
                                return "";
                            }
                            updatePackage(dataPackage);
                        }
                    } catch (Exception e) {
                        Log.e(PackageCrawler.class.toString(), "doInBackground Unknown error: " + e);
                    }
                } else {
                    try {
                        if(!isRunning){
                            return "";
                        }
                        installPackage(dataPackage);
                    } catch (Exception e) {
                        Log.e(PackageCrawler.class.toString(), "doInBackground Unknown error: " + e);
                    }
                }
                packageCounter++;
            }
        }else{
            packagesCount = 1;
            dialog.setProgress(100);
            for (DataPackage dataPackage : packageList) {
                if(!isRunning){
                    DatabaseHelper.deletePackage(dataPackage.getId());
                    return "";
                }
                if(dataPackage.getIdOpenData().compareTo(params[0]) == 0){
                    if(!isRunning){
                        DatabaseHelper.deletePackage(dataPackage.getId());
                        return "";
                    }
                    setDialogTitle(MainActivity.getContext().getString(R.string.crawler_load_packageinfo) + " (" + (packageCounter) + "/" + packagesCount + ") ");
                    dialog.setProgress(dialog.getProgress() + 100);
                    if (dataPackage.isDatapointsInstalled()) {
                        try {
                            if(!isRunning){
                                DatabaseHelper.deletePackage(dataPackage.getId());
                                return "";
                            }
                            if (checkForUpdate(dataPackage)) {
                                if(!isRunning){
                                    DatabaseHelper.deletePackage(dataPackage.getId());
                                    return "";
                                }
                                updatePackage(dataPackage);
                            }
                        } catch (Exception e) {
                            Log.e(PackageCrawler.class.toString(), "doInBackground Unknown error: " + e);
                        }
                    } else {
                        try {
                            if(!isRunning){
                                DatabaseHelper.deletePackage(dataPackage.getId());
                                return "";
                            }
                            installPackage(dataPackage);
                        } catch (Exception e) {
                            Log.e(PackageCrawler.class.toString(), "doInBackground Unknown error: " + e);
                        }
                    }
                    packageCounter++;

                }
            }
        }

        dialog.setProgress(dialog.getMax());
        return null;
    }


    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
        DatapointFragment myFragment = (DatapointFragment)MainActivity.getActivity().getFragmentManager().findFragmentByTag("DatapointFragment");
        if (myFragment != null && myFragment.isVisible()) {
            MainActivity.getActivity().getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.flContent, new DatapointFragment(), "DatapointFragment")
                    .commit();
        }
        isRunning = true;
    }

    private boolean checkForUpdate(DataPackage dataPackage) {
        OpenDataResource kmzFile = getSupportedFile(dataPackage);
        return DatabaseHelper.checkForUpdate(dataPackage.getIdOpenData(), kmzFile.getCreationTimestamp());
    }

    private void updatePackage(DataPackage dataPackage) {
        DatabaseHelper.deletePackage(dataPackage.getId());
        installPackage(dataPackage);
    }

    private void installPackage(DataPackage dataPackage) {
        dialog.setProgress(dialog.getProgress()+100);
        OpenDataResource supportedResource = getSupportedFile(dataPackage);
        if(isRunning) {
            if (supportedResource != null) {
                setDialogTitle(MainActivity.getContext().getString(R.string.crawler_download) + " (" + (packageCounter) + "/" + packagesCount + ") ");
                OpenDataUtil.downloadFromUrl(supportedResource.getUrl(), supportedResource.getId() + "." + supportedResource.getFormat().toLowerCase());
                String filepath = Environment.getExternalStorageDirectory() + "/datenbrille/download/" + supportedResource.getId() + "." + supportedResource.getFormat().toLowerCase();
                dialog.setProgress(dialog.getProgress()+100);

                switch (supportedResource.getFormat().toUpperCase()) {
                    case "KMZ": {
                        if(isRunning)installKmz(supportedResource, dataPackage);else DatabaseHelper.deletePackage(dataPackage.getId());
                    }
                    case "KML": {
                        if(isRunning)installKml(supportedResource, filepath, dataPackage);else DatabaseHelper.deletePackage(dataPackage.getId());
                    }
                }
            }
        }else DatabaseHelper.deletePackage(dataPackage.getId());
    }

    private void installKmz(OpenDataResource kmzResource, DataPackage dataPackage){
        setDialogTitle(MainActivity.getContext().getString(R.string.crawler_unzip) + " (" + (packageCounter) + "/" + packagesCount + ") ");
        File kmlFile = KmzReader.getKmlFile(kmzResource.getId() + ".kmz");
        dialog.setProgress(dialog.getProgress()+100);
        if(isRunning) {
            if (kmlFile != null) {
                ArrayList<Placemark> placemarks = XmlParser.getPlacemarksFromKmlFile(kmlFile);
                dialog.setProgress(dialog.getProgress() + 100);
                int datapointCounter = 1;
                int datapointsCount = placemarks.size();

                for (Placemark placemark : placemarks) {
                    if(isRunning) {
                        try {
                            setDialogTitle(
                                    MainActivity.getContext().getString(R.string.crawler_add_datapoint) +
                                            " (" + (packageCounter) + "/" + packagesCount + ")\n"
                                            + MainActivity.getContext().getString(R.string.crawler_datapoint)
                                            + ": " + (datapointCounter) + "/" + placemarks.size());
                            DatabaseHelper.addDatapoint(
                                    dataPackage.getId(),
                                    placemark.getLocation().getLatitude(),
                                    placemark.getLocation().getLongitude(),
                                    placemark.getName(),
                                    OpenDataParser.parseWebsite(OpenDataUtil.getRequestResult(placemark.getLink())));
                            Log.d(this.getClass().toString(), "Added Datapoint: " + placemark.getName());
                            dialog.setProgress(dialog.getProgress() + 500/datapointsCount);
                        } catch (Exception e) {
                            Log.d(PackageCrawler.class.toString(), "Error while adding Datapoint! Error: \"" + e.getMessage() + "\"");
                        }
                        datapointCounter++;
                    }else{
                        DatabaseHelper.deletePackage(dataPackage.getId());
                        return;
                    }
                }
                DatabaseHelper.installPackage(dataPackage.getIdOpenData(), kmzResource.getCreationTimestamp());
            }
        }else{
            DatabaseHelper.deletePackage(dataPackage.getId());
        }
    }

    private void installKml(OpenDataResource kmlResource, String path, DataPackage dataPackage){

        File kmlFile = new File(path);
        ArrayList<Placemark> placemarks = XmlParser.getPlacemarksFromKmlFile(kmlFile);
        int datapointCounter = 1;
        int datapointsCount = placemarks.size();
        dialog.setProgress(dialog.getProgress() + 100);
        dialog.setMax(dialog.getMax() + datapointsCount);

        for (Placemark placemark : placemarks) {
            if(isRunning) {
                try {
                    setDialogTitle(
                            MainActivity.getContext().getString(R.string.crawler_add_datapoint) +
                                    " (" + (packageCounter) + "/" + packagesCount + ")\n"
                                    + MainActivity.getContext().getString(R.string.crawler_datapoint)
                                    + ": " + (datapointCounter) + "/" + placemarks.size());
                    DatabaseHelper.addDatapoint(
                            dataPackage.getId(),
                            placemark.getLocation().getLatitude(),
                            placemark.getLocation().getLongitude(),
                            placemark.getName(),
                            StringUtils.unescapeHtml3(placemark.getDescription()));
                    Log.d(this.getClass().toString(), "Added Datapoint: " + placemark.getName());
                    dialog.setProgress(dialog.getProgress() + 600/datapointsCount);
                } catch (Exception e) {
                    Log.d(PackageCrawler.class.toString(), "Error while adding Datapoint! Error: \"" + e.getMessage() + "\"");
                }
                datapointCounter++;
            }else{
                DatabaseHelper.deletePackage(dataPackage.getId());
                return;
            }
        }
        DatabaseHelper.installPackage(dataPackage.getIdOpenData(), kmlResource.getCreationTimestamp());
    }



    private OpenDataResource getSupportedFile(DataPackage dataPackage) {
        List<OpenDataResource> openDataResources = OpenDataUtil.getPackageById(dataPackage.getIdOpenData()).getResources();
        for (OpenDataResource openDataResource : openDataResources) {
            for(String supportedFile : OpenDataUtil.supportedFiles) {
                if (openDataResource.getFormat().toUpperCase().equals(supportedFile)) {
                    return openDataResource;
                }
            }
        }
        return null;
    }

    private void setDialogTitle(final String title) {
        MainActivity.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog.setTitle(title);
            }
        });
    }

    private boolean hasActiveInternetConnection() {
        if (isNetworkAvailable()) {
            try {
                HttpURLConnection urlc = (HttpURLConnection) (new URL("http://www.google.com").openConnection());
                urlc.setRequestProperty("User-Agent", "Test");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(1500);
                urlc.connect();
                return (urlc.getResponseCode() == 200);
            } catch (IOException e) {
                Log.e(PackageCrawler.class.toString(), "Error checking internet connection", e);
                Toast.makeText(MainActivity.getContext(), MainActivity.getContext().getString(R.string.crawler_no_connection), Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.d(PackageCrawler.class.toString(), "No network available!");
            Toast.makeText(MainActivity.getContext(), MainActivity.getContext().getString(R.string.crawler_no_connection), Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) MainActivity.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }
}
