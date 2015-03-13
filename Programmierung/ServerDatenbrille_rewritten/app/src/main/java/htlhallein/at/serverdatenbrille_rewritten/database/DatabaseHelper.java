package htlhallein.at.serverdatenbrille_rewritten.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import java.util.ArrayList;
import java.util.List;

import htlhallein.at.serverdatenbrille_rewritten.MainActivity;
import htlhallein.at.serverdatenbrille_rewritten.memoryObjects.DataPackage;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "datenbrille";
    private static final int DATABASE_VERSION = 1; // never change
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
        SQLiteStatement statement = getInstance().getWritableDatabase().compileStatement("INSERT INTO `Datapoint`(`idPackage`,`latitude`,`longitude`,`name`,`content`) VALUES (?,?,?,?,?);");
        statement.bindLong(1, idPackage);
        statement.bindDouble(2, latitude);
        statement.bindDouble(3, longitude);
        statement.bindString(4, name);
        statement.bindString(5, content);
        return statement.executeInsert();
    }

    public static long addPackage(final String openDataID, final String linkOpenData, final String name) {
        SQLiteStatement statement = getInstance().getWritableDatabase().compileStatement("INSERT INTO `Package`(`idOpenData`,`linkOpenData`,`name`,`updated`) VALUES (?,?,?,?);");
        statement.bindString(1, openDataID);
        statement.bindString(2, linkOpenData);
        statement.bindString(3, name);
        statement.bindString(4, System.currentTimeMillis() + "");
        return statement.executeInsert();
    }

    public static List<DataPackage> getDataPackages() {
        List<DataPackage> packages = new ArrayList<DataPackage>();

        Cursor cursor = getInstance().getReadableDatabase().rawQuery("SELECT `idPackage`, `name`, `idOpenData` FROM `Package`;", null);
        if (cursor.moveToFirst()) {
            do {
                packages.add(new DataPackage(cursor.getLong(0), cursor.getString(1), cursor.getString(2)));
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

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE `Datapoint` (`idDatapoint` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, `idPackage` INTEGER NOT NULL, `latitude` REAL, `longitude` REAL, `name` TEXT, `content` TEXT);");
        db.execSQL("CREATE TABLE `Package` (`idPackage` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, `idOpenData` TEXT UNIQUE, `linkOpenData` TEXT, `name` TEXT, `updated` INTEGER);");
        db.execSQL("CREATE TRIGGER datapointDeleteTrigger AFTER DELETE ON `Package` BEGIN DELETE FROM `Datapoint` WHERE `idPackage`=OLD.`idPackage`; END;");

        db.execSQL("INSERT INTO `Package`(`idOpenData`, `linkOpenData`, `name`, `updated`) VALUES ('a5841caf-afe2-4f98-bb68-bd4899e8c9cb', '', 'Museen', 0);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Nothing to do
    }

}