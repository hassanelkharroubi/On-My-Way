package com.example.onmyway.General;


import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.onmyway.Models.Administrateur;
import com.example.onmyway.Models.CustomFirebase;
import com.example.onmyway.Models.SaveUser;
import com.example.onmyway.Models.User;
import com.example.onmyway.Models.UserDB;
import com.example.onmyway.R;
import com.example.onmyway.User.View.HomeUser;
import com.example.onmyway.Utils.Constants;
import com.example.onmyway.Utils.CustomToast;
import com.example.onmyway.Utils.DialogMsg;
import com.example.onmyway.administrateur.View.Home;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Login extends AppCompatActivity {
    private static final String TAG="Login";

    private String email;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private DialogMsg dialogMsg=new DialogMsg();
    private boolean gps_enabled=false;
    private boolean mLocationPermissionGranted=false;
    private SaveUser mSaveUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mSaveUser=new SaveUser(this);
        User user=mSaveUser.getUser();



        Log.d(TAG,"inside Login");
        if (!checkGooglePlayServices())
            // finish();
            Log.d(TAG, "google play services are not correct");

        //getLocationPermission();

        if (user.getId()!=null){

            if (user.getAdmin().equals("yes")){
                Intent intent=new Intent(Login.this, Home.class);
                startActivity(intent);
                finish();

            }
            else{
                Intent intent=new Intent(Login.this, HomeUser.class);
                startActivity(intent);
                finish();

            }


        }

        else{
            Log.d(TAG,"user is  null");
            // Initialize Firebase Auth
            editTextEmail=findViewById(R.id.email);
            editTextPassword=findViewById(R.id.password);
        }

    }//end of create() method

    public void login(View view) {

        email=editTextEmail.getText().toString();
        String password = editTextPassword.getText().toString();
        if(!isEmail(email) || password.isEmpty())
        {
            CustomToast.toast(this, "veuillez valider soit email soit le mot de passe");
            return;
        }

        dialogMsg.attendre(this,"Verification","Veuillez attendre ");
        Log.d(TAG,"all data are ok ");


        RequestQueue queue = Volley.newRequestQueue(this);
// Request a string response from the provided URL.
        String url = "https://goapppfe.000webhostapp.com/login.php?email="+email.toLowerCase()+"&password="+password;
        Log.d(TAG,url);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url,
                null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (response.has("CIN")){
                            //les donnes sont correct
                            try {
                                dialogMsg.hideDialog();
                                String cin=response.getString("CIN");
                                String fullname=response.getString("fullname");
                                String email=response.getString("Email");
                                String password=response.getString("Password");
                                String admin=response.getString("admin");
                                User user=new User(fullname,email,password,cin,admin);

                                if (admin.equals("yes")){

                                    mSaveUser.setUser(user);
                                    Intent intent=new Intent(Login.this, Home.class);
                                    startActivity(intent);
                                    finish();

                                }
                                else{
                                    mSaveUser.setUser(user);
                                    Intent intent=new Intent(Login.this, HomeUser.class);
                                    startActivity(intent);
                                    finish();

                                }

                            } catch (JSONException e) {

                                Log.e(TAG,"erreur de parse json object");
                                e.printStackTrace();
                            }
                        }
                        else{
                            if (response.has("userNotFound")){
                                dialogMsg.hideDialog();
                                CustomToast.toast(Login.this, "cet utilisateur n'existe pas");
                                //user n'existe pas
                            }
                            else{
                                //erreur de cnx aqu base de donnees ou qlq chose comme ca (internet)
                                dialogMsg.hideDialog();
                                CustomToast.toast(Login.this, "Veuillez verfier votre connection ");
                            }
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        dialogMsg.hideDialog();
                        Log.d(TAG,"erreur de Volley "+error.getLocalizedMessage());

                    }
                });

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                60000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(jsonObjectRequest);
    }//end login

    //fonction de verification email
    public static boolean isEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }


    //check google play services
    private boolean checkGooglePlayServices()
    {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS)
        {
            if (apiAvailability.isUserResolvableError(resultCode))
            {
                apiAvailability.getErrorDialog(this, resultCode, Constants.GOOGLE_PLAY_SERVICES_REQUEST)
                        .show();
            } else
                {

                    CustomToast.toast(getApplicationContext(), "votre telephone n'est pas mise a jour ");
                finish();
            }
            return false;
        }
        return true;
    }

    //ask for permissions
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

                gps_enabled=isGPSEnabled();

                break;
            }
        }
    }//end of onActivityResult()


    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Constants.FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            mLocationPermissionGranted = true;
            Log.d(TAG,"all permission are granetd");
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Constants.FINE_LOCATION}, Constants.REQUEST_FINE_LOCATION);
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

    @Override
    protected void onResume() {
        super.onResume();

        if(checkGooglePlayServices())
        {
            getLocationPermission();
            if(mLocationPermissionGranted)
                gps_enabled=isGPSEnabled();
        }


    }
}

