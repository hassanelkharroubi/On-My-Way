package com.example.onmyway.administrateur.View;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.onmyway.General.Login;
import com.example.onmyway.General.UserRecyclerAdapter;
import com.example.onmyway.Models.User;
import com.example.onmyway.Models.UserDB;
import com.example.onmyway.R;
import com.example.onmyway.User.View.HomeUser;
import com.example.onmyway.Utils.CustomToast;
import com.example.onmyway.Utils.DialogMsg;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.JsonArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ListAllUser extends AppCompatActivity {

    private ProgressDialog progressDialog;

    private RecyclerView recyclerView;

    private final String TAG="ListAllUser";
    UserRecyclerAdapter adapter;

    //for sqlite database
    private UserDB userDB;
    private ArrayList<User> usersFireBase,users;
    private  User user;
    private DialogMsg dialogMsg = new DialogMsg();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_list_all_user);

        //get toolbar_layout
        Toolbar toolbar = findViewById(R.id.toolbar);


        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView=findViewById(R.id.recycler);

        //store user catched from firebase
        user=new User();
        //add all user from firebase to usersFireBase
        usersFireBase=new ArrayList<>();
        //for local data base(UserDB)
        users=new ArrayList<>();
        userDB=new UserDB(this);
        //readFromDataBase();
    }

    public void readFromDataBase()
    {
        users.clear();
        users=userDB.getAllUsers();
        if(users.size()==0)
        {

            progressDialog=new ProgressDialog(this);
            //show progress dialog
            dialogMsg.attendre(this, "Recherche", "Veuillez attendre .....");

            RequestQueue queue = Volley.newRequestQueue(this);
// Request a string response from the provided URL.
            String url = "https://goapppfe.000webhostapp.com/selection.php";
            Log.d(TAG,url);

            StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d(TAG,response.toString());
                    dialogMsg.hideDialog();
                    try {
                        JSONArray array= new JSONArray(response);
                        for(int i=0;i<array.length();i++) {
                            JSONObject jsonobject = array.getJSONObject(i);
                            String cin=jsonobject.getString("CIN");
                            String fullname=jsonobject.getString("fullname");
                            String email=jsonobject.getString("Email");
                            String password=jsonobject.getString("Password");
                            String admin=jsonobject.getString("admin");
                            User user=new User(fullname,email,password,cin,admin);
                            usersFireBase.add(user);

                        }
                        userDB.addUsers(usersFireBase);
                        setAdapter(usersFireBase);


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("error",error.toString());
                    dialogMsg.hideDialog();
                    Log.d(TAG,"erreur de Volley "+error.getLocalizedMessage());
                }
            });
            request.setRetryPolicy(new DefaultRetryPolicy(
                    60000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(request);


        }//if users array is not 0
        else {
            setAdapter(users);
        }


    }

    private void setAdapter(ArrayList<User> list)
    {
         adapter = new UserRecyclerAdapter(list, ListAllUser.this);

        recyclerView.setAdapter(adapter);

        recyclerView.setLayoutManager(new LinearLayoutManager(ListAllUser.this));

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.toolbar,menu);
        menu.removeItem(R.id.enligne);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId()==R.id.ajouter)
            startActivity(new Intent(this, RegisterActivity.class));

        if(item.getItemId()==android.R.id.home)
        {
            onBackPressed();

        }

        if(item.getItemId()==R.id.suprimer)
            startActivity(new Intent(this, Chercher.class));
        if(item.getItemId()==R.id.chercher)
            startActivity(new Intent(this, Chercher.class));
        if (item.getItemId()==R.id.actualiser){
            int nbrow=userDB.delletAllusers();

            Log.d(TAG,"numbe rof rows deleted "+nbrow+" ");

            readFromDataBase();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        users=new ArrayList<>();
        readFromDataBase();

    }
}
