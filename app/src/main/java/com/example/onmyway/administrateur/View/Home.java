package com.example.onmyway.administrateur.View;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.onmyway.General.Login;
import com.example.onmyway.Models.CustomFirebase;
import com.example.onmyway.Models.UserDB;
import com.example.onmyway.R;


public class Home extends AppCompatActivity {

    private static final String TAG = "Home";
    private UserDB userDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        TextView textView1 = findViewById(R.id.wlcm);
        //get toolbar_layout
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Accueil");
        userDB = new UserDB(this);
        //we have to do this manually
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);

        String admin = sharedPref.getString(getString(R.string.Admin), null);
        Log.d(TAG, admin + " ");
        textView1.setText(admin + "");

    }


    public void ajouter(View view) {
        startActivity(new Intent(this, RegisterActivity.class));
    }

    public void supprimer(View view) {
        startActivity(new Intent(this, Chercher.class));

    }
    public void chercher(View view) {

        startActivity(new Intent(this,Chercher.class));

    }

    public void signOut(View view) {
        CustomFirebase.getUserAuth().signOut();
        startActivity(new Intent(this, Login.class));
        finish();
    }


    public void transporter(View view) {
        startActivity(new Intent(Home.this, ListAllUser.class));
    }
}
