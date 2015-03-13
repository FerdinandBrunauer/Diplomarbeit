package htlhallein.at.serverdatenbrille_rewritten;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;

import htlhallein.at.serverdatenbrille_rewritten.database.DatabaseConnection;
import htlhallein.at.serverdatenbrille_rewritten.database.KmzReader;
import htlhallein.at.serverdatenbrille_rewritten.database.Placemark;
import htlhallein.at.serverdatenbrille_rewritten.database.XmlParser;
import htlhallein.at.serverdatenbrille_rewritten.database.openDataUtilities.OpenDataPackage;
import htlhallein.at.serverdatenbrille_rewritten.database.openDataUtilities.OpenDataResource;
import htlhallein.at.serverdatenbrille_rewritten.database.openDataUtilities.OpenDataUtilities;

class PackageCrawler extends AsyncTask<String, String, String> {
    private ProgressDialog dialog = new ProgressDialog(MainActivity.getContext());

    private void writeToDatabase(OpenDataPackage openDataPackage, int packageNr, int packagesCount) {
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        if (openDataPackage != null) {
            setDialogTitle(MainActivity.getContext().getString(R.string.crawler_add_packageinfo) + " (" + (packageNr + 1) + "/" + packagesCount + ") ");
            DatabaseConnection.insertPackage(openDataPackage);
            dialog.setMax(dialog.getMax() + (openDataPackage.getResources().size() * 10));

            for (OpenDataResource res : openDataPackage.getResources()) {
                if (res.getFormat().toUpperCase().compareTo("KMZ") == 0) {
                    OpenDataUtilities.downloadFromUrl(res.getUrl(), res.getId() + ".kmz");
                    int needSteps = 0;
                    int stepsMade = 0;
                    setDialogTitle(MainActivity.getContext().getString(R.string.crawler_unzip) + " (" + (packageNr + 1) + "/" + packagesCount + ") ");
                    try {
                        File kmlFile = KmzReader.getKmlFile(res.getId() + ".kmz");
                        ArrayList<Placemark> placemarks = XmlParser.getPlacemarksFromKmlFile(kmlFile);
                        needSteps = placemarks.size();
                        dialog.setProgress(dialog.getProgress() + 30);
                        dialog.setMax(dialog.getMax() + needSteps);
                        for (int n = 0; n < placemarks.size(); n++) {
                            setDialogTitle(MainActivity.getContext().getString(R.string.crawler_add_datapoint) + " (" + (packageNr + 1) + "/" + packagesCount + ")\n" + MainActivity.getContext().getString(R.string.craalwer_datapoint) + ": " + (n + 1) + "/" + placemarks.size());
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

    @Override
    protected String doInBackground(String... params) {
        dialog.setProgress(0);
        dialog.setProgressNumberFormat(null);
        dialog.setProgressPercentFormat(null);
        dialog.setTitle(R.string.crawler_open_database);
        dialog.setMax(params.length * 30);

        for (int i = 0; i < params.length; i++) {
            dialog.setTitle(MainActivity.getContext().getString(R.string.crawler_load_packageinfo) + " (" + (i + 1) + "/" + params.length + ") ");

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
        MainActivity.getActivity().runOnUiThread(new Runnable() {
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