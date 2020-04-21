package com.example.onmyway.administrateur;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.onmyway.CustomToast;
import com.example.onmyway.DB.CustomFirebase;
import com.example.onmyway.DB.UserDB;
import com.example.onmyway.DialogMsg;
import com.example.onmyway.ListAllUser;
import com.example.onmyway.R;
import com.example.onmyway.UserInfo.GeoPoint;
import com.example.onmyway.UserInfo.User;
import com.example.onmyway.connection.Internet;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
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


//on va utiliser cet objet pour les requetes comme sql
        refUserData= FirebaseDatabase.getInstance().getReference().child(getString(R.string.UserData));
        locationRef= FirebaseDatabase.getInstance().getReference().child(getResources().getString(R.string.OnlineUserLocation));

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
    public void toast(String msg) {
        LayoutInflater layoutInflater= getLayoutInflater();
        View toastLayout=layoutInflater.inflate(R.layout.toast, (ViewGroup) findViewById(R.id.showtoast));
        TextView textView= toastLayout.findViewById(R.id.toastMsg);
        textView.setText(msg+" ");
        Toast toast=new Toast(this);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(toastLayout);
        toast.setGravity(Gravity.BOTTOM|Gravity.CENTER, 0, 0);
        toast.show();
    }
    public void chercherUser(View view)
    {
        keyWord=chercherV.getText().toString();

        if(!keyWord.isEmpty())
        {
            //query by keyword in userDB
            keyWord=keyWord.toLowerCase();
            users=userDB.getAllUsers();

            if(users.size()==0)
            {
                toast("vous n'avez aucun cheuffaur");

                return;
            }


                user=chercher(keyWord);

                if(user!=null)
                {
                    fullnameV.setText(user.getfullName().toUpperCase());
                    cinV.setText(user.getId());
                    emailV.setText(user.getEmail());
                    //SHOW ALL VIEWYS
                    fullnameV.setVisibility(View.VISIBLE);
                    cinV.setVisibility(View.VISIBLE);
                    emailV.setVisibility(View.VISIBLE);
                    operationV.setVisibility(View.VISIBLE);

                }


            return;


        }


        CustomToast.toast("veuillez tapez le CIN ",Chercher.this);


    }//end of chercher user
    private User chercher(String str)
    {
        int i=0;

        while(users.size()>i)
        {
            String cin=users.get(i).getId();
            if(cin!=null)
            {

                if(cin.toLowerCase().equals(str))
                    return users.get(i);

            }
            i++;
        }

        return null;
    }
    public void supprimerUser(View view) {
        dialogMsg.attendre(this,"Supprimer","Chercher user");
        userDB.deleteUser(keyWord.toLowerCase());
        findUserInFireBaseByCin(true);

        fullnameV.setVisibility(View.GONE);
        cinV.setVisibility(View.GONE);
        emailV.setVisibility(View.GONE);
        operationV.setVisibility(View.GONE);
    }
    private String getCinFromIntent()
    {

        if(getIntent().hasExtra("cin"))
            return getIntent().getStringExtra("cin");
        return null;

    }
    public void afficherSurMap(View view) {

        findUserInFireBaseByCin(false);
    }


    //this boolean is used for to detect if we are going to show on map(false) or going to delete user(true)
    private void findUserInFireBaseByCin(final boolean delete)
    {
        Query query=null;
            query=refUserData.orderByChild("id").equalTo(keyWord);

        dialogMsg.attendre(this,"Rechercher....","veuillez attende....");

            query.addValueEventListener(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                        if (snapshot.exists()) {

                            idUserInFireBase = snapshot.getKey();
                            if (delete) {
                                User deleteUser = snapshot.getValue(User.class);
                                CustomFirebase.getUserAuth().signOut();
                                CustomFirebase.DeleteUser(deleteUser.getEmail(), deleteUser.getPassword());
                                refUserData.child(idUserInFireBase).removeValue();
                                CustomFirebase.getUserAuth().signInWithEmailAndPassword(Administrateur.email, Administrateur.password)
                                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                startActivity(new Intent(Chercher.this, Home.class));
                                                finish();

                                            }
                                        });

                                dialogMsg.hideDialog();
                                return;
                            }
                            dialogMsg.hideDialog();

                            Intent intent = new Intent(Chercher.this, MapsActivity.class);
                            intent.putExtra("id", idUserInFireBase);
                            startActivity(intent);



                        }//end of test if snapshot.exist()

                        dialogMsg.hideDialog();

                    }//end of for loop


                }//end of DataChange()

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(Chercher.this, "Veuillez verfier votre connection", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();

                }
            });

    }
    @Override
    protected void onResume() {
        super.onResume();


        String cin=getCinFromIntent();

        if(cin!=null)
        {
            User searchUser=chercher(cin.toLowerCase());
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

