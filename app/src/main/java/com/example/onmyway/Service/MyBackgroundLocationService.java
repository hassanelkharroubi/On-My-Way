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

import com.example.onmyway.GoogleDirection.FetchURL;
import com.example.onmyway.GoogleDirection.ShowDirection;
import com.example.onmyway.GoogleDirection.TaskLoadedCallback;
import com.example.onmyway.Models.CustomFirebase;
import com.example.onmyway.Models.DestinationDB;
import com.example.onmyway.Models.GeoPoint;
import com.example.onmyway.R;
import com.example.onmyway.User.View.UserPosition;
import com.example.onmyway.Utils.Constants;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;


public class MyBackgroundLocationService extends Service implements TaskLoadedCallback {
    private static final String TAG= "Background";
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    //  private String CHANNEL_ID = "my_channel_01";

    //data base
    private DatabaseReference mDatabase;

    private int counterForUpdateTimeDestination = 0;

    public MyBackgroundLocationService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
       // mDatabase= FirebaseDatabase.getInstance().getReference();
        mDatabase= CustomFirebase.getDataRefLevel1(getResources().getString(R.string.OnlineUserLocation));


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        Log.d(TAG,"start location update ");

        mLocationCallback=new LocationCallback()
        {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                for (Location location : locationResult.getLocations())
                {

                    {
                        counterForUpdateTimeDestination += 2;
                        Log.d(TAG, "le nobre de sec = " + counterForUpdateTimeDestination);

                        GeoPoint geoPoint=new GeoPoint();
                        geoPoint.setLongitude(location.getLongitude());
                        geoPoint.setLatitude(location.getLatitude());
                        geoPoint.setTime(location.getTime());
                        geoPoint.setSpeed(location.getSpeed());

                        LatLng origin = new LatLng(location.getLatitude(), location.getLongitude());
                        if (counterForUpdateTimeDestination == 600) {
                            String url = ShowDirection.getUrl(origin, new DestinationDB(MyBackgroundLocationService.this).getDestination(), "driving", getString(R.string.google_map_api));
                            new FetchURL(MyBackgroundLocationService.this).execute(url, "driving");
                            counterForUpdateTimeDestination = 0;
                        }

                        mDatabase.child(CustomFirebase.getCurrentUser().getUid()).setValue(geoPoint);

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


    @Override
    public void onTaskDone(String distance, String duration, Object... values) {
        if (distance == null)
            return;

        Log.d(TAG, "number of km before regex = " + distance);
        String numberOfKM = distance.replaceAll("km", "");

        //will assume depending on https://www.energy.gov/eere/vehicles/fact-671-april-18-2011-average-truck-speeds that average speed is 55km/h
        //t=d/v
        Log.d(TAG, "number of km= " + numberOfKM);
        double time = Double.parseDouble(numberOfKM) / Constants.speed;
        Log.d(TAG, "time to destination = " + time);
        if (time < 1) {
            time = Math.floor(time * 60);
            duration = time + " mins";
        } else {
            double hour = Math.floor(time);
            double min = Math.floor((time - hour) * 60);
            duration = hour + " hours " + min + " mins";

        }

        Log.d(TAG, "extract number from =" + duration);
        String userkey = CustomFirebase.getCurrentUser().getUid();

        CustomFirebase.getDataRefLevel1(getString(R.string.DurationToDestination)).child(userkey).child("duration").setValue(duration);
        CustomFirebase.getDataRefLevel1(getString(R.string.DurationToDestination)).child(userkey).child("distance").setValue(distance);

    }
}
