package com.example.onmyway.User.View;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.onmyway.GoogleDirection.FetchURL;
import com.example.onmyway.GoogleDirection.ShowDirection;
import com.example.onmyway.GoogleDirection.TaskLoadedCallback;
import com.example.onmyway.Models.DestinationDB;
import com.example.onmyway.R;
import com.example.onmyway.Service.GeoCoding;
import com.example.onmyway.Service.GeoCodingDoneListener;
import com.example.onmyway.Utils.Constants;
import com.example.onmyway.Utils.CustomToast;
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
    private LatLng latLngDestination;
    private LatLng origin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_destination_location);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        locationMarker=new MarkerOptions();
        mapFragment.getMapAsync(this);
        destinationDB=new DestinationDB(this);

    }//end of onCreate()

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap=googleMap;
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(true);
        //only for show maroccain map at the first time
        LatLng maroc=new LatLng(31.7218851,-11.6443955);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(maroc, Constants.DEFAULT_ZOOM - 10));

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

                latLngDestination = latLng;

                destinationDB.deleteDestination();

                destinationDB.addDestination(latLng);

                if(marker!=null)
                    marker.remove();
                locationMarker.position(latLng);
                marker=  mMap.addMarker(locationMarker);
                new GeoCoding(ChooseDestinationLocation.this).execute(latLng);

            }
        });
    }//end of onMapReady()




    private void confirm(String msg,final LatLng latLng,final String address)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Log.d(TAG, "send intent");
                sendIntent(latLng,address);
                finish();

            }
        });
        builder.setNegativeButton("no", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //zoom in for more places
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, Constants.DEFAULT_ZOOM - 5));

            }
        });

        if (msg == null) {
            builder.setMessage("Veuillez confimer le choix de destination ").create().show();

        } else
            builder.setMessage("Veuillez confimer le choix de destination " + msg).create().show();





    }//end of confirm()

    private void sendIntent(LatLng latLng, String address)
    {
        Log.d(TAG, "send intent inside senIntent()" + latLng.toString() + "adresse =" + address);
        Intent intent=new Intent(this,HomeUser.class);
        intent.putExtra("latlng",latLng);
        intent.putExtra("address",address);
        startActivity(intent);
    }

    @Override
    public void onTaskDone(String distance,String duration,Object... values) {

        CustomToast.toast(this, "Il reste " + duration);

        if(currentPolyline!=null) {

            currentPolyline.remove();
        }
       currentPolyline= mMap.addPolyline((PolylineOptions) values[0]);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(origin, Constants.DEFAULT_ZOOM + 10));

    }//end of onTaskDone();
    @Override
    protected void onResume() {
        super.onResume();

        Intent intent=getIntent();
        if(intent.hasExtra(UserPosition.TAG))
        {
            origin = intent.getParcelableExtra("origin");
            if(origin!=null)
            {
                String url = ShowDirection.getUrl(origin, new DestinationDB(this).getDestination(), "driving", getString(R.string.google_map_api));
                Log.d("url", url);
                new FetchURL(this).execute(url,"driving");
            }
            else
                CustomToast.toast(this, "pas de direction ");
        }

    }//end of onResume()


    @Override
    public void geoCodingDone(Object result) {
        if (result != null) {
            if (result instanceof String) {
                confirm((String) result, latLngDestination, (String) result);

            } else {
                confirm(null, latLngDestination, "commencer le travail ou choisir une destination");
                // sendIntent(,"click commencer ou choisir une destination");

            }
        } else {
            CustomToast.toast(this, "Veuillez connecter au internet");
        }

    }
}
