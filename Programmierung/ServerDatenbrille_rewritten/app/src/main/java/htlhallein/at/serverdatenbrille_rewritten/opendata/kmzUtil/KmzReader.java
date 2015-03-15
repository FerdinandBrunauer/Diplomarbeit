package htlhallein.at.serverdatenbrille_rewritten.opendata.kmzUtil;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import htlhallein.at.serverdatenbrille_rewritten.opendata.Decompress;

public class KmzReader {

    public static File getKmlFile(String kmzFileName){

        try {
            String tempFolderPath = Environment.getExternalStorageDirectory() + "/datenbrille/temp/TEMP_" + System.currentTimeMillis() + "/";
            String zipFile = Environment.getExternalStorageDirectory() + "/datenbrille/download/" + kmzFileName;

            Decompress d = new Decompress(zipFile, tempFolderPath);
            d.unzip();

            final File tempFolder = new File(tempFolderPath);


            File[] tempFolderFiles = tempFolder.listFiles();

            String kmlFilePath = null;

            for (int i = 0; i < tempFolderFiles.length; i++) {
                String extension = tempFolderFiles[i].getAbsolutePath().substring(tempFolderFiles[i].getAbsolutePath().lastIndexOf('.') + 1).toLowerCase();
                if (extension.compareTo("kml") == 0) {
                    kmlFilePath = tempFolderFiles[i].getAbsolutePath();
                    break;
                } else {
                    tempFolderFiles[i].delete();
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
