package database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Copyright 2015 (C) HTL - Hallein
 * Created on:  13.01.2015
 * Author:      Ferdinand
 */
public class DatabaseConnection extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "datenbrille";
    private static final int DATABASE_VERSION = 1;

    private static DatabaseConnection myInstance = null;

    private DatabaseConnection(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static DatabaseConnection getInstance(Context context) {
        if (myInstance == null) {
            myInstance = new DatabaseConnection(context.getApplicationContext());
        }

        return myInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO create table if not exists
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
