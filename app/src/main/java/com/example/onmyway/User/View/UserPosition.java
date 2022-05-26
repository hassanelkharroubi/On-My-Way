package com.example.onmyway.User.View;

import android.Manifest;
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

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.onmyway.GoogleDirection.FetchURL;
import com.example.onmyway.GoogleDirection.ShowDirection;
import com.example.onmyway.GoogleDirection.TaskLoadedCallback;
import com.example.onmyway.Models.DestinationDB;
import com.example.onmyway.Models.GeoPoint;
import com.example.onmyway.Models.SaveUser;
import com.example.onmyway.R;
import com.example.onmyway.Service.MyBackgroundLocationService;
import com.example.onmyway.Utils.Constants;
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

import org.json.JSONException;
import org.json.JSONObject;


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
    private Boolean mLocationPermissionGranted = false;
    private boolean gps_enabled = false;
    private LocationManager locationManager;
    //for stop backgound services
    private boolean stop = false;
    private String mCin;

    private int counterForUpdateTimeDestination = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_position);
        mCin = new SaveUser(this).getUser().getId();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        markerOptions = new MarkerOptions();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {

                super.onLocationResult(locationResult);

                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        counterForUpdateTimeDestination++;

                        GeoPoint geoPoint = new GeoPoint();
                        geoPoint.setLongitude(location.getLongitude());
                        geoPoint.setLatitude(location.getLatitude());
                        geoPoint.setTime(location.getTime());
                        geoPoint.setSpeed(location.getSpeed());

                        origin = new LatLng(location.getLatitude(), location.getLongitude());
                        if (counterForUpdateTimeDestination == 600) {
                            String url = ShowDirection.getUrl(origin, new DestinationDB(UserPosition.this).getDestination(), "driving", getString(R.string.google_map_api));
                            new FetchURL(UserPosition.this).execute(url, "driving");
                            counterForUpdateTimeDestination = 0;
                        }
                        if (marker != null)
                            marker.remove();
                        moveCamera(origin, mMap.getCameraPosition().zoom);

                        // todo : insert to database locations

                        RequestQueue queue = Volley.newRequestQueue(UserPosition.this);
// Request a string response from the provided URL.
                        String url = "https://goapppfe.000webhostapp.com/insert_coordinate.php?" +
                                "cin=" + mCin +
                                "&latitude=" + location.getLatitude() +
                                "&longitude=" + location.getLongitude() +
                                "&speed=" + location.getSpeed() +
                                "&time=" + location.getTime();
                        Log.d(TAG, url);

                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url,
                                null, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {

                                if (response.has("success")) {
                                    //les donnes sont correct
                                    //todo : you can add here what you want after updatign location on map
                                    try {
                                        Log.d(TAG, response.getString("success"));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    //todo: coordiantes don't insert
                                    Log.d(TAG, "erreur de mise a jour les donnes");
                                }


                            }
                        }, new Response.ErrorListener() {

                            @Override
                            public void onErrorResponse(VolleyError error) {
                                // TODO: Handle error
                                Log.d(TAG, "erreur de Volley " + error.getLocalizedMessage());

                            }
                        });

                        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                                60000,
                                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                        queue.add(jsonObjectRequest);


                    }

                }
            }
        };//end location callback

        locationRequest();
        getLocationPermission();
        if (mLocationPermissionGranted) {
            isGPSEnabled();
            if (gps_enabled) {
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


        markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        marker = mMap.addMarker(markerOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }//end moveCamera()

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);


    }


    private boolean isGPSEnabled() {

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager != null) {

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
            case Constants.GPS_REQUEST_CODE: {

                if (gps_enabled = isGPSEnabled()) {
                    initMap();
                }
                break;
            }
        }
    }//end of onActivityResult()

    public void showDirection(View view) {
        // destination=destinationDB.getDestination();
        Intent intent = new Intent(this, ChooseDestinationLocation.class);
        intent.putExtra(TAG, TAG);
        intent.putExtra("origin", origin);
        Log.d(TAG,"choose Destiniation "+origin);

        startActivity(intent);

    }//end of showDirection


    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, Constants.REQUEST_FINE_LOCATION);
        }
    }//end of getLocationPermission

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case Constants.REQUEST_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                } else
                    getLocationPermission();
            }
        }

    }//end of onRequestPermissionsResult(...);

    //location request class
    private void locationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(Constants.UPDATE_INTERVAL); //use a value fo about 10 to 15s for a real app
        locationRequest.setFastestInterval(Constants.FASTEST_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }//end of locationRequest();


    private void startLocationUpdates() {

        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
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
        //stopService();
        stop=true;
        stopLocationUpdates();
        DestinationDB destinationDB = new DestinationDB(this);
        destinationDB.deleteDestination();
        String url="https://goapppfe.000webhostapp.com/delete_coordinate.php?cin="+mCin;
        RequestQueue queue = Volley.newRequestQueue(UserPosition.this);
        StringRequest stringRequest=new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if ("yes".equals(response)){
                    //todo :
                    Log.d(TAG,"Coordinate has been deleted for "+mCin);
                }
                else{
                    Log.d(TAG,"coordinate of the has not been deleted");
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG,"error has been occurs "+error.getLocalizedMessage());

            }
        });

        queue.add(stringRequest);

        startActivity(new Intent(this, HomeUser.class));
        finish();
    }//end of stop()//work is done



    @Override
    protected void onResume() {
        super.onResume();

        //stopService();
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
            //startService();
        }

    }

    @Override
    public void onTaskDone(String distance, String duration, Object... values) {
//        String userkey = CustomFirebase.getCurrentUser().getUid();
//
//        CustomFirebase.getDataRefLevel1(getString(R.string.DurationToDestination)).child(userkey).child("duration").setValue(duration);
//        CustomFirebase.getDataRefLevel1(getString(R.string.DurationToDestination)).child(userkey).child("distance").setValue(distance);


    }
}
