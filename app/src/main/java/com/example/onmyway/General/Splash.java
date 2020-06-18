package com.example.onmyway.General;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.onmyway.Models.DestinationDB;
import com.example.onmyway.R;
import com.example.onmyway.User.View.UserPosition;
import com.google.android.gms.maps.model.LatLng;

public class Splash extends AppCompatActivity {

    public static final String TAG = "Splash";


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        DestinationDB destinationDB = new DestinationDB(this);


        //check if we word started or not
        LatLng stored = destinationDB.getDestination();
        //when choose destination oncreate called in this classe so we have to verfiy is there is
        //an inetnt is comming from ChooseDestinationLocation
        //an inetnt is comming from ChooseDestinationLocation

        if (stored != null) {
            Log.d(TAG, "inside on create method in stored location ");
            startActivity(new Intent(this, UserPosition.class));
            finish();
            return;
        }


        ImageView img;
        img = findViewById(R.id.img);
        img.animate().alpha(4000).setDuration(1);
        Handler handler = new Handler();
        Log.d(TAG, "here we go");

         handler.postDelayed(new Runnable() {
             @Override
             public void run()
             {
                 Log.d(TAG, "inside run methode of handler");
                 startActivity(new Intent(Splash.this, Login.class));
                 finish();
             }
         }, 1000);
        Log.d(TAG, "end of  of handler");

    }
}
