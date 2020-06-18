package com.example.onmyway.Service;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.util.Log;

import com.example.onmyway.Models.Destination;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;

public class GeoCoding extends AsyncTask<Object, Void, Object> {

    public static final String TAG = "GeoCoding";
    private GeoCodingDoneListener geoCodingDoneListener;
    private Context context;

    public GeoCoding(Context context) {
        this.context = context;
        this.geoCodingDoneListener = (GeoCodingDoneListener) context;
    }

    @Override
    protected Object doInBackground(Object... objects) {
        Geocoder geocoder = new Geocoder(context);
        Destination destination = new Destination();

        List<Address> addressList;
        if (objects[0] instanceof String) {
            try {
                Log.d(TAG, "yes is string " + objects[0].toString());
                addressList = geocoder.getFromLocationName((String) objects[0], 1);
                Log.d("doin", objects[0].toString());
                if (addressList.size() > 0) {
                    Address address = addressList.get(0);

                    Log.d(TAG, "yes the place is known " + address.getLocality());
                    destination.setLatLng(new LatLng(address.getLatitude(), address.getLongitude()));
                    destination.setDestination(objects[0].toString());
                    return destination;
                }
                return null;

            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

        }
        if (objects[0] instanceof LatLng) {
            Log.d("doing", objects[0].toString());
            LatLng latLng = (LatLng) objects[0];
            destination.setLatLng(latLng);
            try {

                addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                if (addressList.size() > 0) {
                    String address = addressList.get(0).getAddressLine(0);

                    destination.setDestination(address);
                    return destination;
                }
                return destination;

            } catch (IOException e) {
                e.printStackTrace();
                return destination;

            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        geoCodingDoneListener.geoCodingDone(o);


    }
}
