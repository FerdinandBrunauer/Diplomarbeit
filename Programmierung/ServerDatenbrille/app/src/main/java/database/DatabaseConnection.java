package database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.util.LinkedList;
import java.util.List;

import database.openDataUtilities.OpenDataPackage;
import database.openDataUtilities.OpenDataResource;
import datapoint.gps.GPSDatapointObject;

public class DatabaseConnection extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "datenbrille";
    private static final int DATABASE_VERSION = 2;

    // OPEN DATA PACKAGE TABLE
    private static final String OPEN_DATA_PACKAGES_TABLE = "openDataPackages";
    private static final String ID_OPEN_DATA_PACKAGE = "id";
    private static final String NAME_OPEN_DATA_PACKAGE = "name";
    private static final String TITLE_OPEN_DATA_PACKAGE = "title";
    private static final String NOTES_OPEN_DATA_PACKAGE = "notes";
    private static final String UPDATE_TIMESTAMP_OPEN_DATA_PACKAGE = "updateTimestamp";

    // DATAPOINT TABLE
    private static final String DATAPOINT_TABLE = "datapoints";
    private static final String ID_DATAPOINT = "idDatapoint";
    private static final String ID_OPEN_DATA_PACKAGE_IN_DATAPOINT = "idOpenDataPackage";
    private static final String LATITUDE_DATAPOINT = "latitude";
    private static final String LONGITUDE_DATAPOINT = "longitude";
    private static final String WEBLINK_DATAPOINT = "weblink";
    private static final String DESCRIPTION_DATAPOINT = "description";
    private static final String IMAGE_DATAPOINT = "image";
    private static final String TITLE_DATAPOINT = "title";
    private static final String NAME_DATAPOINT = "name";

    private static DatabaseConnection myInstance = null;
    private static Context myContext = null;

    private DatabaseConnection(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        Log.v("DatabaseConnection", "Databasename: \"" + DATABASE_NAME + "\"");
    }

    public synchronized static DatabaseConnection getInstance(Context context) {
        if (myInstance == null) {
            if (myContext == null) {
                myInstance = new DatabaseConnection(context);
            } else {
                myInstance = new DatabaseConnection(myContext);
            }
        }

        return myInstance;
    }

    public static void setContext(Context context) {
        myContext = context;
    }

    public static void insertPackage(OpenDataPackage odPackage) {
        List<OpenDataResource> resources = odPackage.getResources();

        try {
            SQLiteDatabase db = getInstance(null).getWritableDatabase();

            ContentValues cv = new ContentValues();
            cv.put(ID_OPEN_DATA_PACKAGE, odPackage.getId());
            cv.put(NAME_OPEN_DATA_PACKAGE, odPackage.getName());
            cv.put(TITLE_OPEN_DATA_PACKAGE, odPackage.getTitle());
            cv.put(NOTES_OPEN_DATA_PACKAGE, odPackage.getNotes());

            for (OpenDataResource res : resources) {
                if (res.getFormat().toUpperCase().compareTo("KMZ") == 0) {
                    cv.put(UPDATE_TIMESTAMP_OPEN_DATA_PACKAGE, res.getCreationTimestamp());
                }
            }
            db.insert(OPEN_DATA_PACKAGES_TABLE, null, cv);
        } catch (Exception e) {
            Log.v("DatabaseConnection", "Error while inserting Package", e);
        }
    }

    public static void addDatapoint(String html, Bitmap image, String title, String packageId, String latitude, String longitude, String weblink) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = null;
        if (image != null) {
            image.compress(Bitmap.CompressFormat.PNG, 100, out);
            buffer = out.toByteArray();
        }

        SQLiteDatabase db = getInstance(null).getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DESCRIPTION_DATAPOINT, html);
        cv.put(IMAGE_DATAPOINT, buffer);
        cv.put(TITLE_DATAPOINT, title);
        cv.put(ID_OPEN_DATA_PACKAGE_IN_DATAPOINT, packageId);
        cv.put(LATITUDE_DATAPOINT, latitude);
        cv.put(LONGITUDE_DATAPOINT, longitude);
        cv.put(WEBLINK_DATAPOINT, weblink);

        db.insert(DATAPOINT_TABLE, null, cv);
    }

    public static String[] getDatapointByLocation(Location location) {
        String[] result = null;
        try {

            SQLiteDatabase db = getInstance(null).getReadableDatabase();
            Cursor cursor = db.query(DATAPOINT_TABLE, null,
                    LONGITUDE_DATAPOINT + " = ? AND " + LATITUDE_DATAPOINT + " = ?",
                    new String[]{"" + location.getLongitude(), "" + location.getLatitude()}, null, null, null);
            cursor.moveToFirst();
            result = new String[]{
                    cursor.getString(cursor.getColumnIndex(ID_DATAPOINT)),
                    cursor.getString(cursor.getColumnIndex(DESCRIPTION_DATAPOINT)),
                    cursor.getString(cursor.getColumnIndex(TITLE_DATAPOINT)),
                    cursor.getString(cursor.getColumnIndex(ID_OPEN_DATA_PACKAGE_IN_DATAPOINT)),
            };
            cursor.close();
            return result;
        } catch (Exception e) {
            Log.v("DatabaseConnection", "Error while getting Datapoint by Location", e);
            return result;
        }
    }

    public static boolean isPackageInDatabase(OpenDataPackage openDataPackage) {
        if (openDataPackage != null)
            Log.v("DatabaseConnection", "Datapackage ID: \"" + openDataPackage.getId() + "\"");
        else
            return false;

        try {
            getInstance(null).close();
            myContext.deleteDatabase(DATABASE_NAME);
            myInstance = new DatabaseConnection(myContext);

            SQLiteDatabase db = getInstance(null).getReadableDatabase();
            Cursor cursor = db.query(
                    OPEN_DATA_PACKAGES_TABLE,
                    new String[]{ID_OPEN_DATA_PACKAGE},
                    ID_OPEN_DATA_PACKAGE + " = ?",
                    new String[]{openDataPackage.getId()},
                    null,
                    null,
                    null
            );

            if (cursor.getCount() == 0) {
                cursor.close();
                return false;
            } else {
                cursor.close();
                return true;
            }
        } catch (Exception e) {
            Log.v("DatabaseConnection", "Error reading if Package is in Database", e);
            return false;
        }
    }

    public static void deletePackageInclusiveDatapoints(OpenDataPackage openDataPackage) {
        try {
            SQLiteDatabase db = getInstance(null).getReadableDatabase();
            db.delete(DATAPOINT_TABLE, ID_OPEN_DATA_PACKAGE_IN_DATAPOINT + "=" + openDataPackage.getId(), null);
            db.delete(OPEN_DATA_PACKAGES_TABLE, ID_OPEN_DATA_PACKAGE + "=" + openDataPackage.getId(), null);
        } catch (Exception e) {
            Log.v("DatabaseConnection", "Error while deleting Packages from Database", e);
        }
    }

    public static void deletePackageInclusiveDatapoints(String packageId) {
        try {
            SQLiteDatabase db = getInstance(null).getReadableDatabase();
            db.delete(DATAPOINT_TABLE, ID_OPEN_DATA_PACKAGE_IN_DATAPOINT + "= '" + packageId + "'", null);
            db.delete(OPEN_DATA_PACKAGES_TABLE, ID_OPEN_DATA_PACKAGE + "= '" + packageId + "'", null);
        } catch (Exception e) {
            Log.v("DatabaseConnection", "Error while deleting Packages from Database", e);
        }
    }

    public static boolean checkForPackageUpdate(OpenDataPackage openDataPackage) {
        try {
            String updateTimestamp = "";
            for (OpenDataResource res : openDataPackage.getResources()) {
                if (res.getFormat().toUpperCase().compareTo("KMZ") == 0) {
                    updateTimestamp = "" + res.getCreationTimestamp();
                }
            }

            SQLiteDatabase db = getInstance(null).getReadableDatabase();
            Cursor cursor = db.query(
                    OPEN_DATA_PACKAGES_TABLE,
                    null,
                    ID_OPEN_DATA_PACKAGE + " = '" + openDataPackage.getId() + "'",
                    null,
                    null,
                    null,
                    null
            );
            String timestampInDatabase = "";
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    timestampInDatabase = cursor.getString(cursor.getColumnIndex(UPDATE_TIMESTAMP_OPEN_DATA_PACKAGE));
                }
                cursor.close();
            }

            return (timestampInDatabase.compareTo(updateTimestamp) > 0);

        } catch (Exception e) {
            Log.v("DatabaseConnection", "Error while check for Update", e);
            return false;
        }
    }

    public static List<GPSDatapointObject> getAllDatapoints() {
        try {
            List<GPSDatapointObject> list = new LinkedList<GPSDatapointObject>(); // much faster than ArrayList

            SQLiteDatabase db = getInstance(null).getReadableDatabase();
            Cursor cursor = db.query(DATAPOINT_TABLE, new String[]{ID_DATAPOINT, LATITUDE_DATAPOINT, LONGITUDE_DATAPOINT}, null, null, null, null, null);
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

            return null;
        } catch (Exception e) {
            Log.v("DatabaseConnection", "Error while gettint all Datapoints", e);
            return null;
        }
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + OPEN_DATA_PACKAGES_TABLE + " ("
                + ID_OPEN_DATA_PACKAGE + " TEXT PRIMARY KEY , "
                + NAME_OPEN_DATA_PACKAGE + " TEXT , "
                + NOTES_OPEN_DATA_PACKAGE + " TEXT , "
                + UPDATE_TIMESTAMP_OPEN_DATA_PACKAGE + " TEXT , "
                + TITLE_OPEN_DATA_PACKAGE + " TEXT);");

        db.execSQL("CREATE TABLE " + DATAPOINT_TABLE + " ("
                + ID_DATAPOINT + " INTEGER PRIMARY KEY AUTOINCREMENT , "
                + ID_OPEN_DATA_PACKAGE_IN_DATAPOINT + " TEXT , "
                + LATITUDE_DATAPOINT + " TEXT , "
                + LONGITUDE_DATAPOINT + " TEXT , "
                + WEBLINK_DATAPOINT + " TEXT , "
                + DESCRIPTION_DATAPOINT + " TEXT , "
                + TITLE_DATAPOINT + " TEXT , "
                + NAME_DATAPOINT + " TEXT , "
                + IMAGE_DATAPOINT + " BLOB , "
                + "FOREIGN KEY (" + ID_OPEN_DATA_PACKAGE_IN_DATAPOINT + ") REFERENCES "
                + OPEN_DATA_PACKAGES_TABLE + " (" + ID_OPEN_DATA_PACKAGE + "));");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + OPEN_DATA_PACKAGES_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + DATAPOINT_TABLE);

        onCreate(db);
    }
}
