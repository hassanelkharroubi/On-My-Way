package com.example.onmyway.User.View;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.onmyway.General.Login;
import com.example.onmyway.Models.Admin_transporter_db;
import com.example.onmyway.Models.CustomFirebase;
import com.example.onmyway.Models.DestinationDB;
import com.example.onmyway.R;
import com.example.onmyway.Utils.CheckLogin;
import com.example.onmyway.Utils.Constants;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;


public class HomeUser extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "HomeUser";
    SupportMapFragment mapFragment;
    private Boolean mLocationPermissionGranted= false;
    boolean gps_enabled = false;
    private Button startB;
    LatLng myLocation;
    //loaction permission;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    LocationManager locationManager;
    private TextView addressV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_user);



        DestinationDB destinationDB = new DestinationDB(this);
        //check if we word started or not
        LatLng stored = destinationDB.getDestination();
        //when choose destination oncreate called in this classe so we have to verfiy is there is
        //an inetnt is comming from ChooseDestinationLocation
        //an inetnt is comming from ChooseDestinationLocation

        if (stored != null && !getIntent().hasExtra("latlng")) {
            Log.d(TAG, "inside on create method in stored location ");
            startActivity(new Intent(this, UserPosition.class));
            finish();
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Accueil");



        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        initMap();
        getLocationPermission();
        if(mLocationPermissionGranted)
        {
            gps_enabled= isGPSEnabled();
            if(gps_enabled)
            {
                getLastLocation();
            }

        }

        TextView fullnameV = findViewById(R.id.fullname);
        fullnameV.setText("Bienvenue Mr. " + new Admin_transporter_db(this).getAdmin().getfullName().toUpperCase());

        addressV=findViewById(R.id.address);
        addressV.setVisibility(View.GONE);
        startB=findViewById(R.id.start);
        startB.setVisibility(View.GONE);



    }//end of create();

    private void initMap()
    {
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setRotateGesturesEnabled(true);


        Intent intent = getIntent();
        if (intent.hasExtra("latlng")) {
            LatLng latLng = intent.getParcelableExtra("latlng");
            googleMap.addMarker(new MarkerOptions().position(latLng));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, Constants.DEFAULT_ZOOM - 5));
        } else {
            if (myLocation != null) {
                googleMap.addMarker(new MarkerOptions().position(myLocation));
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, Constants.DEFAULT_ZOOM));

            } else {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(33.9693414, -6.9273031), Constants.DEFAULT_ZOOM - 10));

            }
        }
    }
//end of onMapReady()


    public void start(View view) {

        startActivity(new Intent(this, UserPosition.class));
        finish();


    }


    //section for location services
    public boolean isGPSEnabled() {

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

    public void getLocationPermission() {

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, Constants.REQUEST_FINE_LOCATION);
        }
    }//end of getLocationPermission

    public void getLastLocation() {
        mFusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location location = task.getResult();

                if (location != null) {
                    myLocation = new LatLng(location.getLongitude(), location.getLatitude());


                }
            }

        });

    }//end of getLastLocation()


    public void choseLocation(View view) {
        startActivity(new Intent(this, ChooseDestinationLocation.class));

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (CheckLogin.toLogin(this)) finish();


        Intent intent=getIntent();
        boolean hasIntent = false;
        if(intent.hasExtra("address") || intent.hasExtra("latlang"))
        {

            mapFragment.getMapAsync(this);
            startB.setVisibility(View.VISIBLE);
            addressV.setVisibility(View.VISIBLE);
            hasIntent = true;

            LatLng latLng=intent.getParcelableExtra("latlng");
            //if we wanna back the prev activity to show direction
            if(!intent.getStringExtra("address").isEmpty())
                addressV.setText(intent.getStringExtra("address"));
            else
                addressV.setText("latitude "+latLng.latitude+",longitude "+latLng.longitude);

        }
        //we can go back from userPosition by press onback key
        //so we have to check if tranporter is working or not
        //check data base DestinationDB
        DestinationDB destinationDB = new DestinationDB(this);
        Log.d(TAG, "we are here ");
        LatLng latLng = destinationDB.getDestination();
        if (latLng != null && !hasIntent) {
            finish();
        }


    }//end of onResume

    @Override
    public void onBackPressed() {

        super.onBackPressed();
        Log.d(TAG, "you are in onBackPressed");
        DestinationDB destinationDB = new DestinationDB(this);
        destinationDB.deleteDestination();
    }

    //handle the result of startActivityForResult
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Constants.GPS_REQUEST_CODE:
            {

                gps_enabled=isGPSEnabled();
                if (gps_enabled) {
                    getLastLocation();

                }
                break;
            }
        }
    }//end of onActivityResult()


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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.user_sign_out, menu);

        return super.onCreateOptionsMenu(menu);

    }//end of onCreateOptionsMenu

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.signOut) {

            CustomFirebase.getUserAuth().signOut();
            startActivity(new Intent(this, Login.class));
            new Admin_transporter_db(this).deleteAdmin();
            finish();

        }

        return super.onOptionsItemSelected(item);

    }
}
