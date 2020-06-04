package com.example.onmyway.administrateur.View;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.onmyway.Models.Admin_transporter_db;
import com.example.onmyway.Models.CustomFirebase;
import com.example.onmyway.Models.User;
import com.example.onmyway.Models.UserDB;
import com.example.onmyway.R;
import com.example.onmyway.Utils.CustomToast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class RegisterActivity extends AppCompatActivity {

    private Toolbar toolbar;


    private static final String TAG="register";
    private String cin;
    private EditText editTextCin;

    private EditText editTextFullName;
    private String fullName;

    private String email;
    private EditText editTextEmail;

    private String password;
    private EditText editTextPassword;

    private EditText editTextConfirmPassword;

    private User user;
    private FirebaseAuth mAuth;
    private DatabaseReference myRef;

    //for sqlite data base
    private UserDB userDB;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference(getResources().getString(R.string.UserData));
        userDB=new UserDB(this);
        //start new Thread to check network state and internet acess


        //get toolbar_layout
        toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(getResources().getString(R.string.driver));

        // Initialize Firebase Auth

        mAuth= CustomFirebase.getUserAuth();

        editTextEmail=findViewById(R.id.email);
        editTextPassword=findViewById(R.id.password);
        editTextConfirmPassword=findViewById(R.id.confirmpassword);
        editTextFullName=findViewById(R.id.fullname);
        editTextCin=findViewById(R.id.cin);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.toolbar,menu);
        menu.removeItem(R.id.ajouter);

        return super.onCreateOptionsMenu(menu);
}

    public void register(View view) {


        if(allInputValid())
        {

            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
            {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful())
                            {
                                if (user.isTransporter()) {
                                    //check if the cin is already registered
                                    if (userDB.addUser(user) == -1) {
                                        CustomToast.toast(RegisterActivity.this, "cet utilisateur deja existe ");
                                        CustomFirebase.getCurrentUser().delete();
                                    } else {
                                        //we have to add listener for that event
                                        myRef.child(mAuth.getUid()).setValue(user);
                                        //SIGNOUT FROM THE CURRENT registered user
                                        mAuth.signOut();
                                        //we neeed to verfiy also if the previous Admin in signed in successful
                                        mAuth.signInWithEmailAndPassword(new Admin_transporter_db(RegisterActivity.this).getAdmin().getEmail()
                                                , new Admin_transporter_db(RegisterActivity.this).getAdmin().getPassword());
                                        CustomToast.toast(RegisterActivity.this, "les informations sont bien ajouté.");

                                    }
                                } else {
                                    //we have to add listener for that event
                                    myRef.child(mAuth.getUid()).setValue(user);
                                    //SIGNOUT FROM THE CURRENT registered user
                                    mAuth.signOut();
                                    //we neeed to verfiy also if the previous Admin in signed in successful
                                    mAuth.signInWithEmailAndPassword(new Admin_transporter_db(RegisterActivity.this).getAdmin().getEmail()
                                            , new Admin_transporter_db(RegisterActivity.this).getAdmin().getPassword());
                                    CustomToast.toast(RegisterActivity.this, "les informations sont bien ajouté.");

                                }


                                startActivity(new Intent(RegisterActivity.this,RegisterActivity.class));
                            }
                            else
                            {

                                CustomToast.toast(RegisterActivity.this, "on ne peut pas ajouter neveau utilidateur !Verfier votre connection.");


                            }
                        }
                    });

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
        if (item.getItemId() == R.id.enligne)
            startActivity(new Intent(this, ListAllUser.class));


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
           Switch swich = findViewById(R.id.check);

           user = new User(fullName, email, password, cin.toUpperCase(), swich.isChecked());
           return true;
       }
       return  false;


    }




}
