package com.example.onmyway.administrateur.View;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onmyway.General.UserRecyclerAdapter;
import com.example.onmyway.Models.User;
import com.example.onmyway.Models.UserDB;
import com.example.onmyway.R;
import com.example.onmyway.Utils.CustomToast;
import com.example.onmyway.Utils.DialogMsg;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ListAllUser extends AppCompatActivity {

    private ProgressDialog progressDialog;

    private RecyclerView recyclerView;

    private DatabaseReference ref;
    private final String TAG="ListAllUser";

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

        ref=FirebaseDatabase.getInstance().getReference().child(getResources().getString(R.string.UserData));
        userDB=new UserDB(this);
        readFromDataBase();
    }

    public void readFromDataBase()
    {
        users=userDB.getAllUsers();
        if(users.size()==0)
        {
            //show progress dialog
            dialogMsg.attendre(this, "Recherche", "Veuillez attendre .....");

            ref.addValueEventListener(new ValueEventListener(){
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    dialogMsg.hideDialog();

                    if (!dataSnapshot.exists())
                    {


                        CustomToast.toast(ListAllUser.this, "pas de cheffaures! veuillez ajouter neveau");
                       startActivity(new Intent(ListAllUser.this, Home.class));

                        return;
                    }


                    for (DataSnapshot userSnapshot: dataSnapshot.getChildren())
                    {
                        user = userSnapshot.getValue(User.class);
                        //filter between admin and transporter
                        //add tranporter only to recyclerView
                        if (user.isTransporter())
                            usersFireBase.add(user);
                    }
                    userDB.addUsers(usersFireBase);
                    setAdapter(usersFireBase);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                    CustomToast.toast(ListAllUser.this, "Veuillez verfier votre connexion ....");
                }

            });
        }//if users array is not 0
        else setAdapter(users);





    }

    private void setAdapter(ArrayList<User> list)
    {
        UserRecyclerAdapter adapter = new UserRecyclerAdapter(list, ListAllUser.this);

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
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        users=new ArrayList<>();
        readFromDataBase();

    }
}
