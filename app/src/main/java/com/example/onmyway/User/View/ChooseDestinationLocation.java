package com.example.onmyway.User.View;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.onmyway.GoogleDirection.FetchURL;
import com.example.onmyway.GoogleDirection.ShowDirection;
import com.example.onmyway.GoogleDirection.TaskLoadedCallback;
import com.example.onmyway.Models.CustomFirebase;
import com.example.onmyway.Models.Destination;
import com.example.onmyway.Models.DestinationDB;
import com.example.onmyway.R;
import com.example.onmyway.Service.GeoCoding;
import com.example.onmyway.Service.GeoCodingDoneListener;
import com.example.onmyway.Utils.CheckLogin;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class ChooseDestinationLocation extends AppCompatActivity implements OnMapReadyCallback, TaskLoadedCallback, GeoCodingDoneListener {

    private static final String TAG = "chooseLocation";
    private GoogleMap mMap;
    private MarkerOptions locationMarker;
    private Marker marker;
    Polyline currentPolyline;

    //for database
    private DestinationDB destinationDB;
    private LatLng origin;
    //we willl use this var to detect on click listenr for google map
    private boolean isShowingDirection = false;
    //dialogbox for show dorection
    //dialog box for show direction
    private DialogMsg dialogMsg;

    //view
    private TextView searchText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_destination_location);
        if (CheckLogin.toLogin(this)) finish();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        locationMarker = new MarkerOptions();
        mapFragment.getMapAsync(this);
        destinationDB = new DestinationDB(this);
        searchText = findViewById(R.id.input_search);
        getSearchQuery();

    }//end of onCreate()

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(true);
        //only for show maroccain map at the first time
        LatLng maroc = new LatLng(31.7218851, -11.6443955);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(maroc, Constants.DEFAULT_ZOOM - 10));
        if (!isShowingDirection) {
            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {

                    destinationDB.deleteDestination();

                    destinationDB.addDestination(latLng);

                    if (marker != null)
                        marker.remove();
                    locationMarker.position(latLng);
                    marker = mMap.addMarker(locationMarker);
                    new GeoCoding(ChooseDestinationLocation.this).execute(latLng);

                }
            });

        }


    }//end of onMapReady()


    private void confirm(Destination destination) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String msg = destination.getDestination();
        LatLng latLng = destination.getLatLng();

        builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Log.d(TAG, "send intent");
                sendIntent(destination);
                finish();

            }
        });
        builder.setNegativeButton("Non", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //zoom in for more places
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, Constants.DEFAULT_ZOOM - 6));
                new DestinationDB(ChooseDestinationLocation.this).deleteDestination();

            }
        });

        if (msg == null) {
            builder.setMessage("Veuillez confimer le choix de destination ").create().show();

        } else
            builder.setMessage("Veuillez confimer le choix de destination " + msg).create().show();


    }//end of confirm()

    private void sendIntent(Destination destination) {
        Log.d(TAG, "destination city = " + destination.getDestination() + " destination Latlng= " + destination.getLatLng());

        Intent intent = new Intent(this, HomeUser.class);
        intent.putExtra("latlng", destination.getLatLng());
        intent.putExtra("address", destination.getDestination());
        new DestinationDB(this).addDestination(destination.getLatLng());

        startActivity(intent);
    }

    @Override
    public void onTaskDone(String distance, String duration, Object... values) {

        dialogMsg.hideDialog();


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

        if (currentPolyline != null) {

            currentPolyline.remove();
        }
        currentPolyline = mMap.addPolyline((PolylineOptions) values[0]);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(origin, Constants.DEFAULT_ZOOM + 10));

    }//end of onTaskDone();

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = getIntent();
        if (intent.hasExtra(UserPosition.TAG)) {

            isShowingDirection = true;
            dialogMsg = new DialogMsg();
            dialogMsg.attendre(this, "La direction ", "Veuillez attendre ....");
            origin = intent.getParcelableExtra("origin");
            if (origin != null) {
                DestinationDB destinationDB = new DestinationDB(this);
                Log.d(TAG, destinationDB.getDestination().toString() + "");
                String url = ShowDirection.getUrl(origin, destinationDB.getDestination(), "driving", getString(R.string.google_map_api));
                Log.d("url", url);
                findViewById(R.id.relLayout1).setVisibility(View.GONE);
                new FetchURL(this).execute(url, "driving");
            } else
                CustomToast.toast(this, "pas de direction ");
        }

    }//end of onResume()


    @Override
    public void geoCodingDone(Object result) {
        if (result != null)
            confirm((Destination) result);

        else
            CustomToast.toast(this, "erreur de connection ");


    }

    private void getSearchQuery() {
        Log.d(TAG, "init: initializing");

        searchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {

                Log.d(TAG, "event key code is =" + actionId);
                if (actionId == EditorInfo.IME_ACTION_SEARCH)
                    geoLocate();

                return false;
            }
        });
    }

    private void geoLocate() {
        Log.d(TAG, "geoLocate: geolocating");

        String searchString = searchText.getText().toString();

        Log.d(TAG, searchString);
        if (searchString.isEmpty()) {
            CustomToast.toast(ChooseDestinationLocation.this, "Veuillez entrer la destination ou bien la choisir a partir de google map");
            return;

        }
        Log.d(TAG, searchString);

        searchString = searchString.trim();

        new GeoCoding(this).execute(searchString);

    }

}
