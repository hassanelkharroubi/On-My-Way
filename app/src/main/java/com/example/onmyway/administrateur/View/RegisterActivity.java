package com.example.onmyway.administrateur.View;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.onmyway.Models.User;
import com.example.onmyway.Models.UserDB;
import com.example.onmyway.R;
import com.example.onmyway.Utils.CustomToast;

import org.json.JSONObject;


public class RegisterActivity extends AppCompatActivity {

    private Toolbar toolbar;


    private static final String TAG="register";
    private String cin;
    private EditText editTextCin;

    private EditText editTextFullName;
    private String fullName;

    private String email;
    private EditText editTextEmail;
    private Switch mSwitch;

    private String password;
    private EditText editTextPassword;

    private EditText editTextConfirmPassword;

    private User user;

    //for sqlite data base
    private UserDB userDB;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register);


        userDB=new UserDB(this);
        //start new Thread to check network state and internet acess


        //get toolbar_layout
        toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(getResources().getString(R.string.driver));


        editTextEmail=findViewById(R.id.email);
        editTextPassword=findViewById(R.id.password);
        editTextConfirmPassword=findViewById(R.id.confirmpassword);
        editTextFullName=findViewById(R.id.fullname);
        editTextCin=findViewById(R.id.cin);
        mSwitch=findViewById(R.id.switch_admin);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.toolbar,menu);
        menu.removeItem(R.id.ajouter);
        menu.removeItem(R.id.actualiser);
        return super.onCreateOptionsMenu(menu);
}

    public void register(View view) {

        if(allInputValid())
        {

            //todo : on suppose que tous clients ne sont pas amdin
            RequestQueue queue = Volley.newRequestQueue(this);
// Request a string response from the provided URL.
            String url = "https://goapppfe.000webhostapp.com/Ajout.php?" +
                    "cin=" +user.getId()+
                    "&email=" +user.getEmail()+
                    "&fullname=" +user.getfullName()+
                    "&admin="+user.getAdmin() +
                    "&passowrd="+user.getPassword();
            Log.d(TAG,url);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url,
                    null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {

                    if (response.has("success")){
                        //todo : informer admin que l'utilisateur a ete bien ajoute
                        userDB.addUser(user);
                        Log.d(TAG,"user has been add");
                        Toast.makeText(RegisterActivity.this, "success", Toast.LENGTH_SHORT).show();
                        //les donnes sont correct

                    }
                    // todo : else erreur de qlq part

                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    // TODO: Handle error
                    Log.d(TAG,"erreur de Volley "+error.getLocalizedMessage());

                }
            });

            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                    60000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(jsonObjectRequest);


        }
        else
        {
            CustomToast.toast(this, "Veuilez verifier les donnees que vouz avez saisi ....!");
        }

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId()==android.R.id.home)
            startActivity(new Intent(this, Home.class));
        if(item.getItemId()==R.id.enligne)
            startActivity(new Intent(this, ListAllUser.class));
        if(item.getItemId()==R.id.chercher)
            startActivity(new Intent(this, Chercher.class));


        return super.onOptionsItemSelected(item);
    }





    //fonction de verification email
    public static boolean isEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }
    //this method will valid input in the RegisterActivity.java
    private boolean allInputValid()
    {

        cin=editTextCin.getText().toString().trim();
        fullName=editTextFullName.getText().toString().trim();
        email=editTextEmail.getText().toString().trim();
        password=editTextPassword.getText().toString();
        String confirmPassword = editTextConfirmPassword.getText().toString();
       if(!cin.isEmpty() && !fullName.isEmpty() && isEmail(email)
               && !password.isEmpty() && password.equals(confirmPassword))
       {
          boolean s= mSwitch.isChecked();
          if (s){
              user = new User(fullName, email.toLowerCase(), password, cin.toUpperCase(),"yes");
          }
          else{
              user = new User(fullName, email.toLowerCase(), password, cin.toUpperCase(),"no");
          }
           return true;
       }
       return  false;


    }


}
