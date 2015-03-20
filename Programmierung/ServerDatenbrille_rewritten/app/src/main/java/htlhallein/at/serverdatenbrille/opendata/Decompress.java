package htlhallein.at.serverdatenbrille.opendata;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Decompress {
    private String zipFile;
    private String location;

    public Decompress(String zipFile, String location) {
        this.zipFile = zipFile;
        this.location = location;

        dirChecker("");
    }

    public void unzip() {
        try {
            FileInputStream fileInputStream = new FileInputStream(zipFile);
            ZipInputStream zipInputStream = new ZipInputStream(fileInputStream);
            ZipEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                Log.d(this.getClass().toString(), "Unzipping " + zipEntry.getName());

                if (zipEntry.isDirectory()) {
                    dirChecker(zipEntry.getName());
                } else {
                    FileOutputStream fileOutputStream = new FileOutputStream(location + zipEntry.getName());
                    for (int c = zipInputStream.read(); c != -1; c = zipInputStream.read()) {
                        fileOutputStream.write(c);
                    }

                    zipInputStream.closeEntry();
                    fileOutputStream.close();
                }

            }
            zipInputStream.close();
        } catch (Exception e) {
            Log.e(this.getClass().toString(), "unzip", e);
        }

    }

    private void dirChecker(String dir) {
        File file = new File(location + dir);

        if (!file.isDirectory()) {
            file.mkdirs();
        }
    }
}
