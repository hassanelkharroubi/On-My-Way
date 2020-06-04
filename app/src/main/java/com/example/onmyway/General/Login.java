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

import com.example.onmyway.Models.Admin_transporter_db;
import com.example.onmyway.Models.CustomFirebase;
import com.example.onmyway.Models.User;
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
import com.google.firebase.database.ValueEventListener;


public class Login extends AppCompatActivity {


    private FirebaseAuth mAuth;
    private DatabaseReference ref;
    private static final String TAG = "Login";

    private String email;
    private EditText editTextEmail;
    private EditText editTextPassword;


    private DialogMsg dialogMsg = new DialogMsg();
    private boolean gps_enabled = false;
    private boolean mLocationPermissionGranted = false;
    //local db to save the current register admin
    private Admin_transporter_db admin_transporter_db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if (!checkGooglePlayServices())
            // finish();
            Log.d(TAG, "google play services are not correct");

        getLocationPermission();

        FirebaseUser user = CustomFirebase.getCurrentUser();
        admin_transporter_db = new Admin_transporter_db(this);



        ref = CustomFirebase.getDataRefLevel1(getResources().getString(R.string.UserData));


        if (user != null) {

            if (!admin_transporter_db.getAdmin().isTransporter()) {
                Intent intent = new Intent(Login.this, Home.class);
                startActivity(intent);
                finish();
                return;
            }

            Intent intent = new Intent(Login.this, HomeUser.class);
            startActivity(intent);
            finish();
            return;

        }
        // Initialize Firebase Auth
        mAuth = CustomFirebase.getUserAuth();
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);


    }//end of create() method


    public void login(View view) {


        email = editTextEmail.getText().toString();
        String password = editTextPassword.getText().toString();
        if (!isEmail(email) || password.isEmpty()) {
            CustomToast.toast(this, "veuillez valider soit email soit le mot de passe");
            return;

        }

        dialogMsg.attendre(this, "Verification", "Veuillez attendre ");

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {


                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {


                            ref.child(mAuth.getUid()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        dialogMsg.hideDialog();
                                        Log.d(TAG, dataSnapshot.toString());

                                        User user = dataSnapshot.getValue(User.class);

                                        admin_transporter_db.deleteAdmin();
                                        admin_transporter_db.addAdmin(user);

                                        if (!user.isTransporter()) {

                                            Intent intent = new Intent(Login.this, Home.class);
                                            startActivity(intent);
                                            finish();

                                        } else {
                                            Intent intent = new Intent(Login.this, HomeUser.class);

                                            startActivity(intent);
                                            finish();
                                        }
                                    } else {
                                        dialogMsg.hideDialog();
                                        CustomToast.toast(Login.this, "cet utilisateur n'existe pas");
                                        mAuth.signOut();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                    dialogMsg.hideDialog();

                                }
                            });//end of ref
                        }//end of if statment of succusfull task of sign
                        else {
                            CustomToast.toast(Login.this, "vous ne pouvez pas de se connecter !\n veuillez verfier votre informations " +
                                    "ou bien votre connection internet");
                            dialogMsg.hideDialog();
                        }

                    }


                });//end of signIn


    }

    //fonction de verification email
    public static boolean isEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }


    //check google play services
    private boolean checkGooglePlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, Constants.GOOGLE_PLAY_SERVICES_REQUEST)
                        .show();
            } else {

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

                gps_enabled = isGPSEnabled();

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
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Constants.FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Constants.FINE_LOCATION}, Constants.REQUEST_FINE_LOCATION);
        }
    }//end of getLocationPermission

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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

        if (checkGooglePlayServices()) {
            getLocationPermission();
            if (mLocationPermissionGranted)
                gps_enabled = isGPSEnabled();
        }


    }
}

