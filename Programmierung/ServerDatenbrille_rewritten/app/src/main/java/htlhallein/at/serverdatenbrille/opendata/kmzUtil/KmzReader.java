package htlhallein.at.serverdatenbrille.opendata.kmzUtil;

import android.os.Environment;
import android.util.Log;

import java.io.File;

import htlhallein.at.serverdatenbrille.opendata.Decompress;
import htlhallein.at.serverdatenbrille.opendata.PackageCrawler;

public class KmzReader {

    public static File getKmlFile(String kmzFileName) {

        try {
            String tempFolderPath = Environment.getExternalStorageDirectory() + "/datenbrille/temp/TEMP_" + System.currentTimeMillis() + "/";
            String zipFile = Environment.getExternalStorageDirectory() + "/datenbrille/download/" + kmzFileName;

            Decompress d = new Decompress(zipFile, tempFolderPath);
            d.unzip();

            final File tempFolder = new File(tempFolderPath);


            File[] tempFolderFiles = tempFolder.listFiles();

            String kmlFilePath = null;

            for (File tempFolderFile : tempFolderFiles) {
                if (!PackageCrawler.isRunning) break;
                String extension = tempFolderFile.getAbsolutePath().substring(tempFolderFile.getAbsolutePath().lastIndexOf('.') + 1).toLowerCase();
                if (extension.compareTo("kml") == 0) {
                    kmlFilePath = tempFolderFile.getAbsolutePath();
                    break;
                } else {
                    tempFolderFile.delete();
                }
            }

            if (kmlFilePath != null) {
                File file = new File(kmlFilePath);
                tempFolder.deleteOnExit();
                file.deleteOnExit();
                return file;
            } else {
                Log.e("KmzReader.getKmlFile", "No kml-file found!");
                return null;
            }
        } catch (Exception e) {
            Log.e("KmzReader.getKmlFile", "Could not read File");
            return null;
        }
    }

}
