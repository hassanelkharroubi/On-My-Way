package com.example.onmyway.Service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.onmyway.Models.CustomFirebase;
import com.example.onmyway.Models.GeoPoint;
import com.example.onmyway.R;
import com.example.onmyway.User.View.UserPosition;
import com.example.onmyway.Utils.Constants;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;


public class MyBackgroundLocationService extends Service {
    private static final String TAG= "Background";
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    //  private String CHANNEL_ID = "my_channel_01";

    //data base
    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;

    public MyBackgroundLocationService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
       // mDatabase= FirebaseDatabase.getInstance().getReference();
        mDatabase= CustomFirebase.getDataRefLevel1(getResources().getString(R.string.OnlineUserLocation));
        currentUser = CustomFirebase.getCurrentUser();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        Log.d(TAG,"start location update ");

        mLocationCallback=new LocationCallback()
        {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                for (Location location : locationResult.getLocations())
                {

                    if (location != null ) {
                        GeoPoint geoPoint=new GeoPoint();
                        geoPoint.setLongitude(location.getLongitude());
                        geoPoint.setLatitude(location.getLatitude());
                        geoPoint.setTime(location.getTime());
                        geoPoint.setSpeed(location.getSpeed());
                        mDatabase.child(currentUser.getUid()).setValue(geoPoint);
                    }

                }
            }
        };
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG,"onSatartCommand:Called");

        Intent notificationIntent = new Intent(this, UserPosition.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, App.CHANNEL_ID)
                .setContentTitle("GPS SERVICES")
                .setContentText("Location service is running in the background")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);
        startLocationUpdate();

        return START_STICKY;
    }


    private void startLocationUpdate() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(Constants.UPDATE_INTERVAL);
        locationRequest.setFastestInterval(Constants.FASTEST_INTERVAL);
        Log.d(TAG, "getLocation: getting location information.");
        mFusedLocationClient.requestLocationUpdates(locationRequest, mLocationCallback, Looper.myLooper());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"onDestroy:Called");
        stopForeground(true);
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }


}
