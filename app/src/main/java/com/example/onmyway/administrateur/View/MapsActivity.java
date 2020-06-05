package com.example.onmyway.administrateur.View;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.onmyway.Models.CustomFirebase;
import com.example.onmyway.Models.GeoPoint;
import com.example.onmyway.Models.UserDB;
import com.example.onmyway.R;
import com.example.onmyway.Utils.Constants;
import com.example.onmyway.Utils.CustomToast;
import com.example.onmyway.Utils.DialogMsg;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MapActivity";

    private GoogleMap mMap;

    private MarkerOptions markerOptions;
    private SupportMapFragment mapFragment;


    //for dialog msg
    private DialogMsg dialogMsg;
    private String userAuthKey=null;
    private GeoPoint geoPoint;
    private String fullName;
    private UserDB userDB;

//****************************************************here start methods*********************************************************


    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("position de chauffeur");

        dialogMsg=new DialogMsg();
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        initMap();


        //***********for dataBase fire base and dataBase authentification***************

        geoPoint=new GeoPoint();
        userDB = new UserDB(this);




        //test for intent is gaming from Chercher(show user om map)

        Intent intent=getIntent();
        fullName = userDB.findUserByCin(intent.getStringExtra("cin").toUpperCase()).getfullName();


            userAuthKey=intent.getStringExtra("id");

            Log.d(TAG,userAuthKey);
            showUserOnMap();




    }//end of onCreate()

    private void showUserOnMap() {

        Log.d(TAG,"inside showUserOnMap+"+userAuthKey);
        Log.d(TAG, "full name= " + fullName + "");


        dialogMsg.attendre(this, "Recherche", "En train de chercher la position de chauffeur");

        CustomFirebase.getDataRefLevel1(getString(R.string.OnlineUserLocation))
                .child(userAuthKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                dialogMsg.hideDialog();

                if (!dataSnapshot.exists()) {

                    CustomToast.toast(MapsActivity.this, "ce chauffeur n'pas commencer le travail");
                    return;
                }


                Log.d(TAG,"children ="+dataSnapshot.toString());
                geoPoint=dataSnapshot.getValue(GeoPoint.class);
                moveCamera(new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude()), Constants.DEFAULT_ZOOM);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                CustomToast.toast(MapsActivity.this, "veuillez verfier votre connection ");
                dialogMsg.hideDialog();

            }
        });//end of  mDatabase.child(userAuthKey)



    }//end of showOmMap()

    //init googme map
    private void initMap() {

        markerOptions = new MarkerOptions();
        mapFragment.getMapAsync(this);

    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);

    }

    //move camera to right place
    private void moveCamera(LatLng latLng, float zoom) {
        Log.d(TAG, "inside moveCamera");
        markerOptions=new MarkerOptions();
        markerOptions.position(latLng);
        Marker marker= mMap.addMarker(markerOptions);
        marker.setSnippet("speed: " + geoPoint.getSpeed());
        if (fullName != null)
            marker.setTitle(fullName);


        // mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "OnResume");
        if(dialogMsg!=null)
         dialogMsg.hideDialog();

    }




    //pour afficher le menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar, menu);
        menu.removeItem(R.id.enligne);
        return super.onCreateOptionsMenu(menu);
    }
    //pour selection un element de toolbar

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == android.R.id.home)
            onBackPressed();

        if (item.getItemId() == R.id.ajouter)
            startActivity(new Intent(this, RegisterActivity.class));


        if (item.getItemId() == R.id.suprimer)
            startActivity(new Intent(this, Chercher.class));

        if (item.getItemId() == R.id.chercher)
            startActivity(new Intent(this, Chercher.class));

        return super.onOptionsItemSelected(item);
    }




}
