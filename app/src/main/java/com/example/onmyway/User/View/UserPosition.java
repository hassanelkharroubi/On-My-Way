package com.example.onmyway.User.View;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.onmyway.GoogleDirection.FetchURL;
import com.example.onmyway.GoogleDirection.ShowDirection;
import com.example.onmyway.GoogleDirection.TaskLoadedCallback;
import com.example.onmyway.Models.CustomFirebase;
import com.example.onmyway.Models.DestinationDB;
import com.example.onmyway.Models.GeoPoint;
import com.example.onmyway.R;
import com.example.onmyway.Service.MyBackgroundLocationService;
import com.example.onmyway.Utils.Constants;
import com.example.onmyway.Utils.CustomToast;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseReference;


public class UserPosition extends FragmentActivity implements OnMapReadyCallback, TaskLoadedCallback {


    public static final String TAG = "fromUserPosition";
    //for google map
    private GoogleMap mMap;
    private LatLng origin;
    private SupportMapFragment mapFragment;
    private MarkerOptions markerOptions;
    private Marker marker;
//loaction permission
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Boolean mLocationPermissionGranted= false;
    private boolean gps_enabled=false;
    private DatabaseReference ref;
    //for stop backgound services
    private boolean stop=false;
    //we will use this var to update de time to the destination every 10min
    private int counterForUpdateTimeDestination = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_position);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        ref=CustomFirebase.getDataRefLevel1(getString(R.string.OnlineUserLocation));
        markerOptions = new MarkerOptions();

        locationCallback = new LocationCallback()
        {
            @Override
            public void onLocationResult(LocationResult locationResult)
            {


                if (isGPSEnabled())

                    Log.d(TAG, "yes sis enabled ");

                else
                    Log.d(TAG, "no is not enabled ");


                super.onLocationResult(locationResult);


                for (Location location : locationResult.getLocations())
                {
                    if (location != null )

                    {
                        counterForUpdateTimeDestination += 2;

                        GeoPoint geoPoint=new GeoPoint();
                        geoPoint.setLongitude(location.getLongitude());
                        geoPoint.setLatitude(location.getLatitude());
                        geoPoint.setTime(location.getTime());
                        geoPoint.setSpeed(Math.floor(location.getSpeed()));

                        origin = new LatLng(location.getLatitude(), location.getLongitude());
                        if (counterForUpdateTimeDestination == 600) {
                            String url = ShowDirection.getUrl(origin, new DestinationDB(UserPosition.this).getDestination(), "driving", getString(R.string.google_map_api));
                            new FetchURL(UserPosition.this).execute(url, "driving");
                            counterForUpdateTimeDestination = 0;
                        }
                        moveCamera(origin, Constants.DEFAULT_ZOOM);
                        ref.child(CustomFirebase.getCurrentUser().getUid()).setValue(geoPoint);

                    }

                }
            }
        };//end location callback

        locationRequest();
        getLocationPermission();
        if(mLocationPermissionGranted)
        {
            isGPSEnabled();
            if(gps_enabled)
            {
                initMap();
            }
        }



    }//end of create()


    //init googme map
    private void initMap() {


        mapFragment.getMapAsync(this);


    }//end of initMap()

    //move camera to right place

    private void moveCamera(LatLng latLng, float zoom) {

        if (marker != null)
            marker.remove();
        markerOptions.position(latLng);
        marker = mMap.addMarker(markerOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        // mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom - 2));
    }//end moveCamera()

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);


    }


    private boolean isGPSEnabled() {

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager != null)
        {

            if (gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                return gps_enabled;
            new AlertDialog.Builder(this)
                    .setMessage("Activer GPS !.")
                    .setPositiveButton("Activer",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                    startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), Constants.GPS_REQUEST_CODE);
                                }
                            })
                    .setCancelable(false)
                    .show();
        }

        return false;

    }//end of GPSEnabled()

    //handle the result of startActivityForResult
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Constants.GPS_REQUEST_CODE:
            {

                if(gps_enabled=isGPSEnabled())
                {
                    initMap();
                }
                break;
            }
        }
    }//end of onActivityResult()

    public void showDirection(View view)
        {
            // destination=destinationDB.getDestination();
            Intent intent=new Intent(this,ChooseDestinationLocation.class);
            intent.putExtra(TAG,TAG);
            Log.d(TAG, "origin= " + origin);
            Log.d(TAG, "destination= " + new DestinationDB(this).getDestination());
            intent.putExtra("origin",origin);
            startActivity(intent);
            //finish();

       }//end of showDirection



    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, Constants.REQUEST_FINE_LOCATION);
        }
    }//end of getLocationPermission

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        mLocationPermissionGranted = false;
        switch (requestCode)
        {
            case Constants.REQUEST_FINE_LOCATION:
            {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    mLocationPermissionGranted = true;
                }
                else
                    getLocationPermission();
            }
        }

    }//end of onRequestPermissionsResult(...);
    //location request class
    private void locationRequest()
    {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(Constants.UPDATE_INTERVAL); //use a value fo about 10 to 15s for a real app
        locationRequest.setFastestInterval(Constants.FASTEST_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }//end of locationRequest();


    private void startLocationUpdates() {

        try {
            mFusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
        catch (NullPointerException e)
        {
            e.printStackTrace();
        }
    }//end of startLocationUpdates()
    public void stopLocationUpdates() {
        mFusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }//end of stopLocationUpdates()

    //for background services
    public void startService() {
        Intent serviceIntent = new Intent(this, MyBackgroundLocationService.class);

        ContextCompat.startForegroundService(this, serviceIntent);
    }//end of startService()

    public void stopService() {
        Intent serviceIntent = new Intent(this, MyBackgroundLocationService.class);
        stopService(serviceIntent);
    }//end of stopService()


    public void stop(View view)
    {
        stopService();
        stop=true;
        stopLocationUpdates();
        DestinationDB destinationDB = new DestinationDB(this);
        destinationDB.deleteDestination();

        CustomFirebase.getDataRefLevel1(getString(R.string.DurationToDestination))
                .child(CustomFirebase.getCurrentUser().getUid()).removeValue();

        startActivity(new Intent(this, HomeUser.class));
        finish();
    }//end of stop()//work is done



    @Override
    protected void onResume() {
        super.onResume();

        Log.d(TAG, "i'm in resume method ");
        stopService();
        if(gps_enabled=isGPSEnabled())
        {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!stop) {
            stopLocationUpdates();
            startService();
        }

    }

    @Override
    public void onTaskDone(String distance, String duration, Object... values) {
        if (distance == null)
            return;
        if (distance == null) {
            CustomToast.toast(this, "la direction n'existe pas dont la destination que vous avez choisi !");
            startActivity(new Intent(this, UserPosition.class));
            finish();
            return;
        }

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
        CustomToast.toast(this, "Il reste " + duration);


    }
}
