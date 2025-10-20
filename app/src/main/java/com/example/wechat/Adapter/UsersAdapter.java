package com.example.wechat.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wechat.ChatDetailActivity;
import com.example.wechat.Models.MessageModel;
import com.example.wechat.Models.Users;
import com.example.wechat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder>
{
    ArrayList <Users> list;
    Context context;
    List<String> blockedUsers = new ArrayList<>();
    boolean showLastMessage;

    public UsersAdapter(ArrayList<Users> list, Context context) {
        this.list = list;
        this.context = context;
        this.showLastMessage = true;
        fetchBlockedUsers();
    }

    public UsersAdapter(ArrayList<Users> list, Context context, boolean showLastMessage) {
        this.list = list;
        this.context = context;
        this.showLastMessage = showLastMessage;
        fetchBlockedUsers();
    }

    private void fetchBlockedUsers(){
        FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getUid()).child("blockedUsers")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        blockedUsers.clear();
                        for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                            blockedUsers.add(dataSnapshot.getKey());
                        }
                        notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.sample_show_user,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Users users = list.get(position);

        if(blockedUsers.contains(users.getUserId())){
            holder.itemView.setVisibility(View.GONE);
            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
            return;
        }

        // Safely check for "watcher" user to prevent crashes
        if ("watcher".equals(users.getUserName())) {
            holder.image.setImageResource(R.drawable.ic_wechat);
        } else {
            Picasso.get().load(users.getProfilePic()).placeholder(R.drawable.avatar3).into(holder.image);
        }

        // Safely set user name
        if (users.getUserName() != null) {
            holder.userName.setText(users.getUserName());
        }

        if(showLastMessage) {
            FirebaseDatabase.getInstance().getReference().child("chats")
                    .child(FirebaseAuth.getInstance().getUid() + users.getUserId())
                    .orderByChild("timeStamp")
                    .limitToLast(1)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.hasChildren()) {
                                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                    holder.lastMessage.setText(snapshot1.child("message").getValue(String.class));
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        } else {
            holder.lastMessage.setVisibility(View.GONE);
        }


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ChatDetailActivity.class);
                intent.putExtra("userId",users.getUserId());
                intent.putExtra("profilePic",users.getProfilePic());
                intent.putExtra("userName",users.getUserName());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        ImageView image;
        TextView userName, lastMessage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image =itemView.findViewById(R.id.profile_image);
            userName =itemView.findViewById(R.id.userNameList);
            lastMessage = itemView.findViewById(R.id.lastMessage);
        }
    }
}
