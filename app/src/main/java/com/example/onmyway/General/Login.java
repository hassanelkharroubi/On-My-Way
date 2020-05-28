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

import com.example.onmyway.Models.Administrateur;
import com.example.onmyway.Models.CustomFirebase;
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

import java.util.ArrayList;




public class Login extends AppCompatActivity {


    private FirebaseAuth mAuth;
    private DatabaseReference ref;
    private static final String TAG="Login";

    private String email;
    private EditText editTextEmail;
    private EditText editTextPassword;


    private DialogMsg dialogMsg=new DialogMsg();
    private boolean gps_enabled=false;
    private boolean mLocationPermissionGranted=false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if (!checkGooglePlayServices())
            // finish();
            Log.d(TAG, "google play services are not correct");

        getLocationPermission();

        FirebaseUser user= CustomFirebase.getCurrentUser();

        ref= CustomFirebase.getDataRefLevel1(getResources().getString(R.string.UserData));


        if (user != null) {
            if (Administrateur.email.equals(user.getEmail()))
            {
                Intent intent=new Intent(Login.this, Home.class);
                startActivity(intent);
                finish();
                return;
            }
            UserDB userDB=new UserDB(Login.this);
            ArrayList<User> users=userDB.getAllUsers();
            Intent intent=new Intent(Login.this, HomeUser.class);
            intent.putExtra("email",user.getEmail());
            intent.putExtra("fullName",users.get(0).getfullName());
            intent.putExtra("cin",users.get(0).getId());
            startActivity(intent);
            finish();

        }
        // Initialize Firebase Auth
        mAuth = CustomFirebase.getUserAuth();
        editTextEmail=findViewById(R.id.email);
        editTextPassword=findViewById(R.id.password);


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

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {


                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {
                            if(email.equals(Administrateur.email))
                            {
                                dialogMsg.hideDialog();
                                Intent intent=new Intent(Login.this,Home.class);
                                startActivity(intent);
                                finish();

                            }
                            else
                            {
                                Query query= ref.orderByKey().equalTo(mAuth.getUid());
                                query.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        dialogMsg.hideDialog();
                                        if(dataSnapshot.hasChildren())
                                        {

                                            for (DataSnapshot userData:dataSnapshot.getChildren())
                                            {
                                                User user=userData.getValue(User.class);
                                                if(user!=null)
                                                {
                                                    UserDB userDB=new UserDB(Login.this);
                                                    userDB.addUser(user);
                                                    Intent intent=new Intent(Login.this, HomeUser.class);
                                                    intent.putExtra("email",user.getEmail());
                                                    intent.putExtra("fullName",user.getfullName());
                                                    intent.putExtra("cin",user.getId());
                                                    startActivity(intent);
                                                    finish();

                                                }
                                            }
                                        }



                                        else
                                        {

                                            CustomToast.toast(Login.this, "cet utilisateur n'existe pas");
                                            mAuth.signOut();
                                        }

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        CustomToast.toast(Login.this, "Veuillez verfier votre connection ");

                                    }
                                });

                            }

                        }//end of if statment of succusfull task of sigin
                        else
                            {
                                CustomToast.toast(Login.this, "le mot de passe ou email est incorrect ");
                                dialogMsg.hideDialog();


                          }

                    }
                });


    }

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
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Constants.FINE_LOCATION}, Constants.REQUEST_FINE_LOCATION);
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

