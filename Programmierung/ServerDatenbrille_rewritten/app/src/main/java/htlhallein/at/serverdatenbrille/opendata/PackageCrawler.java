package htlhallein.at.serverdatenbrille.opendata;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import htlhallein.at.serverdatenbrille.MainActivity;
import htlhallein.at.serverdatenbrille.R;
import htlhallein.at.serverdatenbrille.database.DatabaseHelper;
import htlhallein.at.serverdatenbrille.memoryObjects.DataPackage;
import htlhallein.at.serverdatenbrille.memoryObjects.OpenDataResource;
import htlhallein.at.serverdatenbrille.memoryObjects.Placemark;
import htlhallein.at.serverdatenbrille.opendata.kmzUtil.KmzReader;
import htlhallein.at.serverdatenbrille.opendata.kmzUtil.XmlParser;

/**
 * Created by Alexander on 14.03.2015.
 */
public class PackageCrawler extends AsyncTask<String, String, String> {
    private ProgressDialog dialog = new ProgressDialog(MainActivity.getContext());
    private int packageCounter;
    private int packagesCount;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setTitle("Please Wait");
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
    }

    @Override
    protected String doInBackground(String... params) {
        dialog.setProgress(0);
        dialog.setProgressNumberFormat(null);
        dialog.setProgressPercentFormat(null);
        dialog.setTitle(MainActivity.getContext().getString(R.string.crawler_open_database));
        dialog.setMax(params.length * 30);

        List<DataPackage> packageList = DatabaseHelper.getDataPackages();

        packageCounter = 1;
        packagesCount = packageList.size();
        for (DataPackage dataPackage : packageList) {
            setDialogTitle(MainActivity.getContext().getString(R.string.crawler_load_packageinfo) + " (" + (packageCounter) + "/" + packageList.size() + ") ");
            if (dataPackage.isDatapointsInstalled()) {
                try {
                    if (checkForUpdate(dataPackage)) {
                        updatePackage(dataPackage);
                    }
                } catch (Exception e) {
                    Log.e("PackageCrawler.doInBackground", "Unknown error: " + e);
                }
            } else {
                try {
                    installPackage(dataPackage);
                } catch (Exception e) {
                    Log.e("PackageCrawler.doInBackground", "Unknown error: " + e);
                }
            }
            dialog.setProgress(dialog.getProgress() + 30);
            packageCounter++;
        }
        dialog.setProgress(dialog.getMax());
        return null;
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if(dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    private boolean checkForUpdate(DataPackage dataPackage) {
        OpenDataResource kmzFile = getKmzFile(dataPackage);
        if (DatabaseHelper.checkForUpdate(dataPackage.getIdOpenData(),kmzFile.getCreationTimestamp())) {
            return true;
        }
        return false;
    }

    private void updatePackage(DataPackage dataPackage) {
        DatabaseHelper.deletePackage(dataPackage.getId());
        installPackage(dataPackage);
    }

    private void installPackage(DataPackage dataPackage) {
        OpenDataResource kmzResource = getKmzFile(dataPackage);
        if (kmzResource != null) {
            OpenDataUtil.downloadFromUrl(kmzResource.getUrl(), kmzResource.getId() + ".kmz");
            setDialogTitle(MainActivity.getContext().getString(R.string.crawler_unzip) + " (" + (packageCounter) + "/" + packagesCount + ") ");
            File kmlFile = KmzReader.getKmlFile(kmzResource.getId() + ".kmz");
            if (kmlFile != null) {
                ArrayList<Placemark> placemarks = XmlParser.getPlacemarksFromKmlFile(kmlFile);
                int datapointCounter = 1;
                int datapointsCount = placemarks.size();
                dialog.setProgress(dialog.getProgress() + 30);
                dialog.setMax(dialog.getMax() + datapointsCount);

                if (placemarks != null) {
                    for (Placemark placemark : placemarks) {
                        setDialogTitle(
                                MainActivity.getContext().getString(R.string.crawler_add_datapoint) +
                                        " (" + (packageCounter) + "/" + packagesCount + ")\n"
                                        + MainActivity.getContext().getString(R.string.crawler_datapoint)
                                        + ": " + (datapointCounter) + "/" + placemarks.size());
                        DatabaseHelper.addDatapoint(
                                dataPackage.getId(),
                                placemark.getLocation().getLatitude(),
                                placemark.getLocation().getLongitude(),
                                placemark.getName(), // TODO parser
                                OpenDataUtil.getRequestResult(placemark.getLink()));
                        Log.d(this.getClass().toString(), "Added Datapoint: " + placemark.getName());
                        dialog.setProgress(dialog.getProgress() + 1);
                        datapointCounter++;
                    }
                    DatabaseHelper.installPackage(dataPackage.getIdOpenData(),kmzResource.getCreationTimestamp());
                }
            }
        }
    }

    private OpenDataResource getKmzFile(DataPackage dataPackage) {
        List<OpenDataResource> openDataResources = OpenDataUtil.getPackageById(dataPackage.getIdOpenData()).getResources();
        for (OpenDataResource openDataResource : openDataResources) {
            if (openDataResource.getFormat().toUpperCase().equals("KMZ")) {
                return openDataResource;
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
}
