package htlhallein.at.serverdatenbrille_rewritten.opendata;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import htlhallein.at.serverdatenbrille_rewritten.MainActivity;
import htlhallein.at.serverdatenbrille_rewritten.database.DatabaseHelper;
import htlhallein.at.serverdatenbrille_rewritten.memoryObjects.DataPackage;
import htlhallein.at.serverdatenbrille_rewritten.opendata.kmzUtil.KmzReader;
import htlhallein.at.serverdatenbrille_rewritten.opendata.kmzUtil.Placemark;
import htlhallein.at.serverdatenbrille_rewritten.opendata.kmzUtil.XmlParser;

/**
 * Created by Alexander on 14.03.2015.
 */
public class PacakageCrawler extends AsyncTask<String, String, String> {
    private ProgressDialog dialog = new ProgressDialog(MainActivity.getContext());

    //TODO: add dialog
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {
        List<DataPackage> packageList = DatabaseHelper.getDataPackages();

        for(DataPackage dataPackage:packageList){
            if(dataPackage.isDatapointsInstalled()){
                if(checkForUpdate(dataPackage)){
                    updatePackage(dataPackage);
                }
            }else{
                installPackage(dataPackage);
            }
        }

        return null;
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
    }

    private boolean checkForUpdate(DataPackage dataPackage){
        OpenDataResource kmzFile = getKmzFile(dataPackage);
        if(kmzFile.checkForUpdate()){
            return true;
        }
        return false;
    }

    private void updatePackage(DataPackage dataPackage){
        DatabaseHelper.deletePackage(dataPackage.getId());
        installPackage(dataPackage);
    }

    private void installPackage(DataPackage dataPackage){
        OpenDataResource kmzResource = getKmzFile(dataPackage);
        if(kmzResource != null){
            OpenDataUtil.downloadFromUrl(kmzResource.getUrl(), kmzResource.getId() + ".kmz");
            File kmlFile = KmzReader.getKmlFile(kmzResource.getId() + ".kmz");
            if(kmlFile != null){
                ArrayList<Placemark> placemarks = XmlParser.getPlacemarksFromKmlFile(kmlFile);
                if(placemarks != null){
                    for(Placemark placemark:placemarks){
                        DatabaseHelper.addDatapoint(
                                dataPackage.getId(),
                                placemark.getLocation().getLatitude(),
                                placemark.getLocation().getLongitude(),
                                placemark.getName(),
                                placemark.getLink());
                    }
                }
            }
        }
    }

    private OpenDataResource getKmzFile(DataPackage dataPackage){
        List<OpenDataResource> openDataResources = OpenDataUtil.getPackageById(dataPackage.getIdOpenData()).getResources();
        for(OpenDataResource openDataResource:openDataResources){
            if(openDataResource.getFormat().toUpperCase().equals("KMZ")){
                return openDataResource;
            }
        }
        return null;
    }
}
