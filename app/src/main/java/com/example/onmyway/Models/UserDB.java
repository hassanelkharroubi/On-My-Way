package com.example.onmyway.Models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class UserDB extends SQLiteOpenHelper {

    public static final String TAG = "UserDB";

    private static final String BD_NAME="GestionUSER";
    private static final String TABLE="user";
    private static final int VERSION=1;
    //les colounes de notre tableau ;

    private static final String ID="ID";
    private static final String NAME="NAME";
    private static final String ADMIN="ADMIN";
    private static final String EMAIL="EMAIL";
    private static final String PASSWORD="PASSWORD";


    public UserDB(@Nullable Context context) {

        super(context, BD_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String request="CREATE TABLE if not exists "+TABLE+" ("+ID+" varchar(20) primary key," +
                ""+NAME+" varchar(30)," +
                ""+EMAIL+" varchar(30)," +
                ""+PASSWORD+" varchar(30)," +
                ""+ADMIN+" varchar(5))";
        db.execSQL(request);




    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        String detele_table="drop table if exists "+TABLE;
        db.execSQL(detele_table);
        onCreate(db);

    }

    public void addUser(User user)
    {
        SQLiteDatabase db=getWritableDatabase();
        ContentValues data=new ContentValues();
        data.put(ID,user.getId());
        data.put(NAME,user.getfullName());
        data.put(EMAIL,user.getEmail());
        data.put(PASSWORD,user.getPassword());
        data.put(ADMIN,user.getAdmin());
        db.insert(TABLE,null,data);
    }


    public int deleteUser(String id)
    {
        SQLiteDatabase db=getWritableDatabase();
        int deleted=db.delete(TABLE,ID+" =?",new String[]{id});
        db.close();

       return deleted;

    }

    public int delletAllusers(){
        SQLiteDatabase db=getWritableDatabase();
        int deleted=db.delete(TABLE,null,null);
        db.close();
        return deleted;
    }
    public User findUserByCin(final String cin)

    {
        Log.d(TAG, cin);
        User user = null;
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT * FROM " + TABLE + " WHERE upper(" + ID + ") =?";
        Cursor cursor = db.rawQuery(query, new String[]{cin});
        if (cursor.moveToFirst()) {

            user = new User();

            user.setId(cursor.getString(0));
            user.setfullName(cursor.getString(1));
            user.setEmail( cursor.getString(2));
            user.setPassword( cursor.getString(3));
            user.setAdmin(cursor.getString(4));

        }
        db.close();
        cursor.close();

        return user;
    }

    public ArrayList<User> getAllUsers()
    {
        SQLiteDatabase db=getReadableDatabase();

        String query="select * from "+TABLE;

        ArrayList<User> users=new ArrayList<>();
        User user=new User();

        Cursor cursor= db.rawQuery(query,null);

        for (int k=0;k<cursor.getColumnCount();k++){
            Log.d(TAG,cursor.getColumnNames()[k]);
        }


        if(cursor.moveToFirst())
        {

            do {
                user.setId(cursor.getString(0));
                user.setfullName(cursor.getString(1));
                user.setEmail( cursor.getString(2));
                user.setPassword( cursor.getString(3));
                user.setAdmin(cursor.getString(4));

                users.add(user);
                //we have change reference of user or we will be add the same user to last position
                user=new User();

            }while(cursor.moveToNext());

        }
        cursor.close();
        return users;


    }

    public void addUsers(ArrayList<User> users) {

        int i=0;

        while(i<users.size())
        {
            this.addUser(users.get(i));
            Log.i("userData",users.get(i).getEmail()+"password : "+users.get(i).getPassword()+"full name :"+users.get(i).getfullName() );


            i++;
        }


    }




}
