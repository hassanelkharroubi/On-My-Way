package com.example.onmyway.Utils;

import android.Manifest;

import com.google.android.gms.maps.model.LatLng;

public final class Constants {

    public static final int GOOGLE_PLAY_SERVICES_REQUEST=1;
    public final static long UPDATE_INTERVAL = 4 * 1000;  /* 4 secs */
    public final static long FASTEST_INTERVAL = 2000; /* 2 sec */

    public static final int GPS_REQUEST_CODE = 100;

    public static final int REQUEST_FINE_LOCATION= 200;


    public static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;

    public static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;

    public static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;

    public static final float DEFAULT_ZOOM = 15f;
    public  static  final LatLng fes=new LatLng(34.0240853,-5.0717807);
    public final static float speed = 55;
}
