package com.example.onmyway.General;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onmyway.Models.User;
import com.example.onmyway.R;
import com.example.onmyway.administrateur.View.Chercher;

import java.util.ArrayList;
public class UserRecyclerAdapter extends RecyclerView.Adapter<UserRecyclerAdapter.ViewHolder>{

    private ArrayList<User> mUsers;
    private Context context;
    public UserRecyclerAdapter(ArrayList<User> users,Context context)
    {
        mUsers = new ArrayList<>();
        this.mUsers = users;
        this.context=context;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_user_list_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        holder.username.setText(mUsers.get(position).getfullName());
        holder.email.setText(mUsers.get(position).getEmail());
        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(context, Chercher.class);
                intent.putExtra("cin",mUsers.get(position).getId());
                context.startActivity(intent);
            }
        });



    }

    @Override
    public int getItemCount()
    {
        return mUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        TextView username, email;
        LinearLayout container;

        private ViewHolder(View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.username);
            email = itemView.findViewById(R.id.email);
            container=itemView.findViewById(R.id.user_layout);
        }

    }

}
















