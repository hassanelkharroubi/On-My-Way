package com.example.onmyway.Models;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class SaveUser {

    private User mUser;
    private SharedPreferences pref;
    private Context mContext;
    private SharedPreferences.Editor editor;

    public SaveUser(Context mContext) {
        mUser=new User();
        pref = mContext.getApplicationContext().getSharedPreferences("UserLogin", 0); // 0 - for private mode
        this.mContext = mContext;
        editor = pref.edit();
    }
    public void disconnect(){

        editor.clear();
        editor.commit(); // commit changes
    }
    public User getUser() {

        // getting String
        mUser.setfullName(pref.getString("fullname", null));
       mUser.setId( pref.getString("cin", null)); // getting Float
        mUser.setEmail(pref.getString("email", null)); // getting Long
        mUser.setPassword(pref.getString("password", null)); // getting boolean
      mUser.setAdmin(pref.getString("admin", null));
        Log.d("SaveUser","calling for user "+mUser.getEmail());



        return mUser;
    }

    public void setUser(User user) {
        editor.putString("admin", user.getAdmin()); // Storing string
        editor.putString("fullname", user.getfullName()); // Storing string
        editor.putString("cin", user.getId()); // Storing string
        editor.putString("email", user.getEmail()); // Storing string
        editor.putString("password", user.getPassword()); // Storing string
        Log.d("SaveUser","all data are saved "+user.getfullName());
        editor.commit(); // commit changes

    }
}
