package com.example.onmyway.administrateur.View;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.onmyway.General.Login;
import com.example.onmyway.Models.Administrateur;
import com.example.onmyway.Models.CustomFirebase;
import com.example.onmyway.Models.User;
import com.example.onmyway.Models.UserDB;
import com.example.onmyway.R;
import com.example.onmyway.User.View.HomeUser;
import com.example.onmyway.Utils.CustomToast;
import com.example.onmyway.Utils.DialogMsg;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
public class Chercher extends AppCompatActivity {

    private static final String TAG = "Chercher";
    private TextView chercherV;
    private TextView fullnameV;
    private TextView emailV;
    private TextView cinV;

    //input search of user(cin)
    private String keyWord;
    private String idUserInFireBase;

    private LinearLayout operationV;
    private DatabaseReference ref;
    private DatabaseReference refUserData;
    private DatabaseReference locationRef;
    ArrayList<User> users;
    private User user;
    UserDB userDB;
//on a besoin de ca  lorsqu'on va faire des requete au firebase
    private ProgressDialog progressDialog;
    private DialogMsg dialogMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chercher);

        dialogMsg=new DialogMsg();


        chercherV=findViewById(R.id.search);
        fullnameV=findViewById(R.id.fullname);
        cinV=findViewById(R.id.cin);
        emailV=findViewById(R.id.email);
        operationV=findViewById(R.id.operation);

        userDB=new UserDB(this);
        user=new User();
        users=new ArrayList<>();
        users=userDB.getAllUsers();
        idUserInFireBase=null;

        if(users.size()==0)
        {



            ref= FirebaseDatabase.getInstance().getReference().child(getResources().getString(R.string.UserData));

            ref.addValueEventListener(new ValueEventListener(){
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    for (DataSnapshot userSnapshot: dataSnapshot.getChildren())
                    {
                        if(userSnapshot.exists())
                        {
                            user = userSnapshot.getValue(User.class);
                            //save users in UserDB
                            userDB.addUser(user);
                            //add to array users
                            users.add(user);

                        }
                        else
                        {
                            Toast.makeText(Chercher.this, "user data n'existe pas", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }


                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }

    }

    public void chercherUser(View view)
    {
        keyWord=chercherV.getText().toString();

        if(!keyWord.isEmpty())
        {
            //query by keyword in userDB
            keyWord = keyWord.toUpperCase();
            user = userDB.findUserByCin(keyWord);

            if (user == null)
            {

                CustomToast.toast(this, "vous n'avez pas ce cheuffaur");

                return;
            }

                    fullnameV.setText(user.getfullName().toUpperCase());
                    cinV.setText(user.getId());
                    emailV.setText(user.getEmail());
                    //SHOW ALL VIEWYS
                    fullnameV.setVisibility(View.VISIBLE);
                    cinV.setVisibility(View.VISIBLE);
                    emailV.setVisibility(View.VISIBLE);
                    operationV.setVisibility(View.VISIBLE);


        } else
            CustomToast.toast(Chercher.this, "veuillez tapez le CIN ");


    }//end of chercher user

    public void supprimerUser(View view) {

        findUserInFireBaseByCin(keyWord,true);

    }
    private String getCinFromIntent()
    {

        if(getIntent().hasExtra("cin"))
            return getIntent().getStringExtra("cin");
        return null;

    }
    public void afficherSurMap(View view) {
        findUserInFireBaseByCin(keyWord,false);
    }


    //this boolean is used for to detect if we are going to show on map(false) or going to delete user(true)
    private void findUserInFireBaseByCin(String cin,final boolean delete)
    {

        dialogMsg.attendre(this,"Rechercher....","veuillez attende....");
        String url;
        User searchUser = userDB.findUserByCin(cin.toUpperCase());

        if (delete){

            if("yes".equals(searchUser.getAdmin())){
                Toast.makeText(this, "Vous n'avez pas le droit de supprimer "+searchUser.getfullName(), Toast.LENGTH_SHORT).show();
                dialogMsg.hideDialog();
                return;
            }

           url= "https://goapppfe.000webhostapp.com/Suppression.php?cin="+cin.toUpperCase();
            Log.d(TAG,url);
            RequestQueue queue = Volley.newRequestQueue(this);
            StringRequest stringRequest=new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    //todo : handle erreur of return of delete user
                    Log.d(TAG,response.toString());
                    dialogMsg.hideDialog();
                    if ("yes".equals(response)){
                        userDB.deleteUser(keyWord.toLowerCase());
                        fullnameV.setVisibility(View.GONE);
                        cinV.setVisibility(View.GONE);
                        emailV.setVisibility(View.GONE);
                        operationV.setVisibility(View.GONE);
                        chercherV.setText("");
                    }
                    else {
                        Log.d(TAG,"on ne peut pas supprimer le client");
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    dialogMsg.hideDialog();
                    Log.d(TAG,"erreur "+error.getLocalizedMessage());
                }
            });


            stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                    60000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(stringRequest);


        }
        else{
            dialogMsg.hideDialog();
            Intent intent = new Intent(Chercher.this, MapsActivity.class);
            intent.putExtra("cin", keyWord);
            startActivity(intent);
        }

    }
    @Override
    protected void onResume() {
        super.onResume();

        String cin=getCinFromIntent();

        if(cin!=null)
        {
            User searchUser = userDB.findUserByCin(cin.toUpperCase());
            //search keyword
            keyWord=cin;
            if(searchUser!=null)
            {
                chercherV.setText(cin);
                fullnameV.setText(searchUser.getfullName().toUpperCase());
                cinV.setText(searchUser.getId());
                emailV.setText(searchUser.getEmail());
                //SHOW ALL VIEWYS
                fullnameV.setVisibility(View.VISIBLE);
                cinV.setVisibility(View.VISIBLE);
                emailV.setVisibility(View.VISIBLE);
                operationV.setVisibility(View.VISIBLE);

            }


        }
    }




}

