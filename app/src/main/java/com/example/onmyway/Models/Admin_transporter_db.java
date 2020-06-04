package com.example.onmyway.Models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;


public class Admin_transporter_db extends SQLiteOpenHelper {

    public Admin_transporter_db(@Nullable Context context) {
        super(context, Utils.DATABASE_NAME, null, Utils.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //creer tableau
        String CREATE_ADMIN_TABLE = "CREATE TABLE IF NOT EXISTS " + Utils.TABLE_NAME + " (" +
                Utils.CIN + " VARCHAR(10) PRIMARY KEY," +
                Utils.NAME + " VARCHAR(30)," +
                Utils.EMAIL + " VARCHAR(30)," +
                Utils.PASSWORD + " VARCHAR(30)," +
                Utils.TRANSPORTER + " INT)";

        db.execSQL(CREATE_ADMIN_TABLE);
        db.close();

    }

    public User getAdmin() {
        SQLiteDatabase database = this.getReadableDatabase();

        String admin = "SELECT * FROM " + Utils.TABLE_NAME;
        User user = null;
        boolean isTransporter;


        Cursor cursor = database.rawQuery(admin, null);

        if (cursor.moveToFirst()) {
            do {
                //if 0 that is mean is admin
                isTransporter = cursor.getInt(cursor.getColumnIndex(Utils.TRANSPORTER)) == 1;

                user = new User(cursor.getString(cursor.getColumnIndex(Utils.NAME)),
                        cursor.getString(cursor.getColumnIndex(Utils.EMAIL)),
                        cursor.getString(cursor.getColumnIndex(Utils.PASSWORD)),
                        cursor.getString(cursor.getColumnIndex(Utils.CIN)), isTransporter);

            } while (cursor.moveToNext());
        }
        database.close();
        cursor.close();
        return user;
    }//end of getAdmin()

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + Utils.TABLE_NAME);
        onCreate(db);
    }

    public void addAdmin(User user) {

        SQLiteDatabase database = this.getWritableDatabase();
        //we will use that in Login to verfiy if the usrr is admin or non
        int isTransporter;
        if (user.isTransporter())
            isTransporter = 1;
        else
            isTransporter = 0;


        ContentValues contentValues = new ContentValues();

        contentValues.put(Utils.CIN, user.getId());
        contentValues.put(Utils.NAME, user.getfullName());
        contentValues.put(Utils.EMAIL, user.getEmail());
        contentValues.put(Utils.PASSWORD, user.getPassword());
        contentValues.put(Utils.TRANSPORTER, isTransporter);

        database.insert(Utils.TABLE_NAME, null, contentValues);
        database.close();
    }//end of add admin

    public void deleteAdmin() {

        SQLiteDatabase database = this.getWritableDatabase();

        database.delete(Utils.TABLE_NAME, null, null);


        database.close();
    }//end of deleteAdmin();

    private class Utils {
        private static final int DATABASE_VERSION = 1;
        private static final String DATABASE_NAME = "AdminDB";
        private static final String TABLE_NAME = "Admin_transporter";


        private static final String NAME = "fullname";
        private static final String EMAIL = "email";
        private static final String PASSWORD = "password";
        private static final String CIN = "cin";
        private static final String TRANSPORTER = "transporter";

    }


}



