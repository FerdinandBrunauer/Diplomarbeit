package htlhallein.at.serverdatenbrille_rewritten.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.location.Location;
import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import htlhallein.at.serverdatenbrille_rewritten.MainActivity;
import htlhallein.at.serverdatenbrille_rewritten.datapoint.gps.GPSDatapointObject;
import htlhallein.at.serverdatenbrille_rewritten.memoryObjects.DataPackage;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "datenbrille";
    private static final int DATABASE_VERSION = 1; // never change
    private static final double LOCATION_TOLERANCE = 0.0000000005d;
    private static DatabaseHelper myInstance;

    private DatabaseHelper() {
        super(MainActivity.getContext(), DATABASE_NAME, null, DATABASE_VERSION);
    }

    private synchronized static DatabaseHelper getInstance() {
        if (myInstance == null) {
            myInstance = new DatabaseHelper();
        }
        return myInstance;
    }

    public static long addDatapoint(final long idPackage, final double latitude, final double longitude, final String name, final String content) {
        SQLiteStatement statement = getInstance().getWritableDatabase().compileStatement("INSERT INTO `Datapoint`(`idPackage`,`latitude`,`longitude`,`name`,`content`) VALUES (?,?,?,?,?,?);");
        statement.bindLong(1, idPackage);
        statement.bindDouble(2, latitude);
        statement.bindDouble(3, longitude);
        statement.bindString(4, name);
        statement.bindString(5, content);
        return statement.executeInsert();
    }

    public static long addPackage(final String openDataID, final String linkOpenData, final String name) {
        SQLiteStatement statement = getInstance().getWritableDatabase().compileStatement("INSERT INTO `Package`(`idOpenData`,`linkOpenData`,`name`,`updated`, `datapointsInstalled`) VALUES (?,?,?,?);");
        statement.bindString(1, openDataID);
        statement.bindString(2, linkOpenData);
        statement.bindString(3, name);
        statement.bindString(4, System.currentTimeMillis() + "");
        statement.bindString(5, "false");
        return statement.executeInsert();
    }

    public static List<DataPackage> getDataPackages() {
        List<DataPackage> packages = new ArrayList<DataPackage>();

        Cursor cursor = getInstance().getReadableDatabase().rawQuery("SELECT `idPackage`, `name`, `idOpenData`, `datapointsInstalled`, `updated` FROM `Package`;", null);
        if (cursor.moveToFirst()) {
            do {
                packages.add(new DataPackage(cursor.getLong(0), cursor.getString(1), cursor.getString(2), Boolean.parseBoolean(cursor.getString(3)), cursor.getLong(4)));
            } while (cursor.moveToNext());
        }

        return packages;
    }

    // Returns the number of rows affected by this SQL statement execution
    public static int deletePackage(final long id) {
        SQLiteStatement statement = getInstance().getWritableDatabase().compileStatement("DELETE FROM `Package` WHERE `idPackage`=?;");
        statement.bindLong(1, id);
        return statement.executeUpdateDelete();
    }

    public static String getDatapointcontentFromLocation(final double latitude, final double longitude) {
        Cursor cursor = getInstance().getReadableDatabase().rawQuery(
                "SELECT `content` FROM `Datapoint` WHERE (`latitude` BETWEEN " +
                        (latitude - LOCATION_TOLERANCE) + " AND " +
                        (latitude + LOCATION_TOLERANCE) + ") AND (`longitude` BETWEEN " +
                        (longitude - LOCATION_TOLERANCE) + " AND " +
                        (longitude + LOCATION_TOLERANCE) + ");", null);

        // TODO TEST if that works
        if (cursor.moveToFirst()) {
            return cursor.getString(0);
        } else {
            return null;
        }
    }

    public static List<GPSDatapointObject> getAllDatapoints() {
        try {
            List<GPSDatapointObject> list = new LinkedList<>();

            Cursor cursor = getInstance().getReadableDatabase().query(
                    "Datapoint", new String[]{"idDatapoint", "latitude", "longitude"}, null, null, null, null, null);

            if (cursor.moveToFirst()) {
                do {
                    GPSDatapointObject object = new GPSDatapointObject();
                    object.setId(cursor.getInt(0));
                    object.setLatitude(cursor.getDouble(1));
                    object.setLongitude(cursor.getDouble(2));
                    list.add(object);
                } while (cursor.moveToNext());
                return list;
            }

            return list;
        } catch (Exception e) {
            Log.v("DatabaseConnection", "Error while getting all Datapoints", e);
            return null;
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE `Datapoint` (`idDatapoint` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, `idPackage` INTEGER NOT NULL, `latitude` REAL, `longitude` REAL, `name` TEXT, `content` TEXT);");
        db.execSQL("CREATE TABLE `Package` (`idPackage` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, `idOpenData` TEXT UNIQUE, `linkOpenData` TEXT, `name` TEXT, `updated` INTEGER, `datapointsInstalled` TEXT);");
        db.execSQL("CREATE TRIGGER datapointDeleteTrigger AFTER DELETE ON `Package` BEGIN DELETE FROM `Datapoint` WHERE `idPackage`=OLD.`idPackage`; END;");

        db.execSQL("INSERT INTO `Package`(`idOpenData`, `linkOpenData`, `name`, `updated`) VALUES ('a5841caf-afe2-4f98-bb68-bd4899e8c9cb', '', 'Museen', 0);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Nothing to do
    }

}