package htlhallein.at.serverdatenbrille.database;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import htlhallein.at.serverdatenbrille.MainActivity;
import htlhallein.at.serverdatenbrille.activityHandler.ActivityHandler;
import htlhallein.at.serverdatenbrille.activityHandler.ActivityListener;
import htlhallein.at.serverdatenbrille.datapoint.gps.GPSDatapointObject;
import htlhallein.at.serverdatenbrille.memoryObjects.DataPackage;

public class DatabaseHelper extends SQLiteOpenHelper implements ActivityListener {

    private static final String DATABASE_NAME = "datenbrille";
    private static final int DATABASE_VERSION = 1; // never change
    private static final double LOCATION_TOLERANCE = 0.0000000005d;
    private static DatabaseHelper myInstance;

    private DatabaseHelper() {
        super(MainActivity.getContext(), DATABASE_NAME, null, DATABASE_VERSION);
        ActivityHandler.addListener(this);
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

    public static long addPackage(final String openDataID, final String linkOpenData, final String name, final long updated,final int color) {
        try {
            SQLiteStatement statement = getInstance().getWritableDatabase().compileStatement("INSERT INTO `Package`(`idOpenData`,`linkOpenData`,`name`,`updated`, `datapointsInstalled`,`color`, `displayed`) VALUES (?,?,?,?,?,?,?);");
            statement.bindString(1, openDataID);
            statement.bindString(2, linkOpenData);
            statement.bindString(3, name);
            statement.bindLong(4, updated);
            statement.bindString(5, "false");
            statement.bindLong(6,color);
            statement.bindLong(7,1);
            return statement.executeInsert();
        }catch (Exception e){
            Log.e("DatabaseHelper", "Error: " + e);
            return -1;
        }
    }

    public static long installPackage(String openDataId, long updateTimestamp){
        SQLiteStatement statement = getInstance().getWritableDatabase().compileStatement("UPDATE `Package` SET `updated` = ?, `datapointsInstalled` = ? WHERE `idOpenData` = ?;");
        statement.bindLong(1, updateTimestamp);
        statement.bindString(2, "true");
        statement.bindString(3, openDataId);
        return statement.executeInsert();
    }

    public static List<DataPackage> getDataPackages() {
        List<DataPackage> packages = new ArrayList<>();


        Cursor cursor = getInstance().getReadableDatabase().rawQuery("SELECT `idPackage`, `name`, `idOpenData`, `datapointsInstalled`, `updated`, `color` , `displayed` FROM `Package`;", null);
        if (cursor.moveToFirst()) {
            do {
                packages.add(new DataPackage(cursor.getLong(0), cursor.getString(1), cursor.getString(2), Boolean.parseBoolean(cursor.getString(3)), cursor.getLong(4), cursor.getInt(5), cursor.getInt(6)));
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
        if (cursor.moveToFirst()) {
            return cursor.getString(0);
        } else {
            return null;
        }
    }

    public static boolean checkForUpdate(String openDataId, long timestamp){
        Cursor cursor = getInstance().getReadableDatabase().rawQuery(
                "SELECT `updated` FROM `Package` WHERE `idOpenData` = '" + openDataId + "';", null);

        return (!cursor.moveToFirst() || (timestamp > cursor.getLong(0)));
    }

    public static List<GPSDatapointObject> getAllDatapoints() {
        try {
            List<GPSDatapointObject> list = new LinkedList<>();

            Cursor cursor = getInstance().getReadableDatabase().query(
                    "Datapoint", new String[]{"idPackage","idDatapoint", "latitude", "longitude", "name"}, null, null, null, null, null);

            if (cursor.moveToFirst()) {
                do {
                    GPSDatapointObject object = new GPSDatapointObject();
                    object.setODId(cursor.getLong(0));
                    object.setId(cursor.getInt(1));
                    object.setLatitude(cursor.getDouble(2));
                    object.setLongitude(cursor.getDouble(3));
                    object.setTitle(cursor.getString(4));
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
        db.execSQL("CREATE TABLE `Package` (`idPackage` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, `idOpenData` TEXT UNIQUE, `linkOpenData` TEXT, `name` TEXT, `updated` INTEGER, `datapointsInstalled` TEXT, `color` INTEGER, `displayed` INTEGER);");
        db.execSQL("CREATE TRIGGER datapointDeleteTrigger AFTER DELETE ON `Package` BEGIN DELETE FROM `Datapoint` WHERE `idPackage`=OLD.`idPackage`; END;");


        db.execSQL("INSERT INTO `Package`(`idOpenData`, `linkOpenData`, `name`, `updated`, `datapointsInstalled`, `color`,  `displayed`) VALUES ('abcd-1234', '', 'Fachhochschule Salzburg',0, 'true', -6289107, 1);");
        db.execSQL("INSERT INTO `Datapoint`(`idPackage`, `latitude`, `longitude`, `name`,`content`) VALUES (1,47.72337822992805,13.087143525481224, 'Fachhochschule Salzburg', '" + fhtext + "');");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Nothing to do
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

    }

    @Override
    public void onStart() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void onDestroy() {
        Log.d(DatabaseHelper.class.toString(), "Stop Databaseconnection ...");
        this.close();
        myInstance = null;
    }

    @Override
    public void showQRCode() {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

    }

    @Override
    public void onNewIntent(Intent intent) {

    }

    public static long editPackageColor(long id, int color) {
        SQLiteStatement statement = getInstance().getWritableDatabase().compileStatement("UPDATE `Package` SET `color` = ? WHERE `idPackage` = ?;");
        statement.bindLong(2, id);
        statement.bindLong(1, color);
        return statement.executeInsert();
    }

    public static long editPackageDispalyed(long id, int displayed) {
        SQLiteStatement statement = getInstance().getWritableDatabase().compileStatement("UPDATE `Package` SET `displayed` = ? WHERE `idPackage` = ?;");
        statement.bindLong(2, id);
        statement.bindLong(1, displayed);
        return statement.executeInsert();
    }

    private static String fhtext = "<div class=\"content_box\"><div id=\"c1652\" class=\"csc-default\"><header class=\"csc-header csc-header-n1\"><h2 class=\"title_pattern uppercase\"><span>Forschung am Studiengang Informationstechnik &amp; System-Management</span></h2></header><div class=\"csc-textpic-text\"><p class=\"bodytext\"><b>Digital und lebensnah</b><br>Informationstechnologien werden immer intelligenter<i><br></i>\n" +
            "</p>\n" +
            "<p class=\"bodytext\"><i>Im Informationszeitalter wird die Qualität eines Produkts durch seine Software bestimmt. So wie sich Lebewesen ihrer Umgebung anpassen, richten sich intelligente Systeme nach dem Verhalten ihrer Nutzerinnen und Nutzer. Sechs praxisnahe Wissenslinien für IT-Profis der Zukunft ...</i>\n" +
            "</p>\n" +
            "<p class=\"bodytext\">Software von morgen besteht aus verschiedenen Komponenten, die den Bedürfnissen ihrer Verwenderinnen und Verwender optimal entsprechen. Die Konzeption und Umsetzung solch hochflexibler Systeme steht im Zentrum der Wissenslinie Informatik und Softwaretechnik.\n" +
            "</p>\n" +
            "<p class=\"bodytext\">Im modernen Wirtschaftsleben wird mobile Datenübertragung immer wichtiger. Dieser steigende Informationsfluss stellt jedoch wachsende Anforderungen an die Sicherheit von Netzwerken. Gemeinsam mit dem Advanced Networking Center der Salzburg Research befasst sich der Schwerpunkt Netzwerktechnologien und -Security mit der wissenschaftlichen Analyse und Optimierung hochvolumiger Datenströme.\n" +
            "</p>\n" +
            "<p class=\"bodytext\">Erfolgreiche Unternehmen brauchen Innovationen und optimierte Prozesse. Die Basis dafür liefern die Informationstechnologien: sei es im Bereich von Simulation, Entwicklung und automatisiertem Testen oder bei Produktion und Qualitätssteigerung. Überall wo ein intelligentes Produkt entsteht, sind komplexe IT-Prozesse im Spiel. Die Wissenslinie Industrielle Systeme und Signalverarbeitung liefert dafür ein praxisnahes Fundament.\n" +
            "</p>\n" +
            "<p class=\"bodytext\">Wer aus empirischen Daten die richtigen Schlüsse ziehen kann, nutzt das eigentliche Potential der modernen vernetzen IT. Neben den vielfältigen Techniken zur Gewinnung von Information aus Rohdaten mittels Statistik und Data-Mining untersucht der Studienschwerpunkt Datenanalyse und e-Health besonders auch deren Anwendung im Bereich der Medizin. Das Forschungsspektrum reicht von 3D-Simulation von Implantaten über Qualitätssicherung und kostensparenden Systemem für den Umgang mit sensiblen Patientendaten bis hin zu Proteinklassifikation und zur Analyse von Börseninstrumenten.\n" +
            "</p>\n" +
            "<p class=\"bodytext\">Globale Zukunftsarbeit funktioniert virtuell. Immer öfter wird länderübergreifend an Forschungs- und Ausbildungsprojekten gearbeitet. Nur wenn die internationalen Arbeitsgruppen optimal miteinander vernetzt sind, kann das Ergebnis der Kooperation überzeugen. Die Wissenslinie IT-Management und Wirtschaft liefert das Know-how für ertragreiche internationale Zusammenarbeit.\n" +
            "</p>\n" +
            "<p class=\"bodytext\">Die Zusammenarbeit in einem globalisierten Umfeld erfordert fundierte Kenntnisse in der internationalen \"Arbeitssprache\" Englisch. Teamarbeit in multinationalen Projekten setzt voraus, dass alle Miglieder gegenüber kulturellen Unterschieden sensibilisiert sind und damit konstruktiv umgehen können. Der Schwerpunkt Englisch und interkullturelle Kommunikation fördert den sprachlichen Komptenzerwerb und liefert das nötige Rüstzeug für international erfolgreiche Kooperation.</p></div></div></div>";
}