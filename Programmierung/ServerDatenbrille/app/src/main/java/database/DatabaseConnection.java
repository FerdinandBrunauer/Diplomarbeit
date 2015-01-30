package database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.os.Environment;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import database.openDataUtilities.Datapoint;
import database.openDataUtilities.OpenDataPackage;
import database.openDataUtilities.OpenDataResource;

public class DatabaseConnection extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = Environment.getExternalStorageDirectory() + "/datenbrille/database/datenbrille";
    private static final int DATABASE_VERSION = 3;

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

    public DatabaseConnection(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static DatabaseConnection getInstance(Context context) {
        if (myInstance == null) {
            myInstance = new DatabaseConnection(context.getApplicationContext());
        }

        return myInstance;
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

    public void insertPackage(OpenDataPackage odPackage) {
        List<OpenDataResource> resources = odPackage.getResources();

        try {
            SQLiteDatabase db = this.getWritableDatabase();

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
            System.err.print(e);
        }
    }

    public void addDatapoint(String html, Bitmap image, String title, String packageId, String latitude, String longitude, String weblink) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = null;
        if (image != null) {
            image.compress(Bitmap.CompressFormat.PNG, 100, out);
            buffer = out.toByteArray();
        }

        SQLiteDatabase db = this.getWritableDatabase();
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

    public List<Datapoint> getDatapointsEnd(Context context, int startID, int count) {
        List<Datapoint> datapoints = new ArrayList<>();

        String sql = "SELECT " + ID_DATAPOINT + ", " + TITLE_DATAPOINT + ", " + IMAGE_DATAPOINT + " FROM " + DATAPOINT_TABLE + " WHERE " + ID_DATAPOINT + ">?" + " LIMIT ?";
        SQLiteDatabase db = getInstance(context).getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, new String[]{"" + startID, "" + count});
        if (cursor != null) {
            cursor.moveToFirst();
            do {
                datapoints.add(new Datapoint(cursor.getInt(0), null, null, null, null, cursor.getString(1), null, cursor.getBlob(2)));
            } while (cursor.moveToNext());
        }

        return datapoints;
    }

    public List<Datapoint> getDatapointsBeginning(Context context, int startID, int count) {
        List<Datapoint> datapoints = new ArrayList<>();

        // TODO

        return datapoints;
    }

    //TODO: GET DATAPOINT BY LOCATION
    //TODO: GET DATAPOINT
    //TODO: UPDATE FUNCTION
    //TODO: ISPACKAGEINDATABASE
    //TODO: CHECK FOR UPDATE FUNCTION
}
