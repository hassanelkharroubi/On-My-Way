package com.example.onmyway.General;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.onmyway.R;

public class Splash extends AppCompatActivity {

    public static final String TAG = "Splash";


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
