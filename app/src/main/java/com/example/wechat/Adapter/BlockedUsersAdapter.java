package com.example.wechat.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wechat.Models.Users;
import com.example.wechat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.List;

public class BlockedUsersAdapter extends RecyclerView.Adapter<BlockedUsersAdapter.ViewHolder> {

    List<Users> blockedUsersList;
    Context context;

    public BlockedUsersAdapter(List<Users> blockedUsersList, Context context) {
        this.blockedUsersList = blockedUsersList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_blocked_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Users user = blockedUsersList.get(position);

        holder.userName.setText(user.getUserName());
        Picasso.get().load(user.getProfilePic()).placeholder(R.drawable.avatar3).into(holder.profileImage);

        holder.unblockButton.setOnClickListener(v -> {
            FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getUid())
                    .child("blockedUsers").child(user.getUserId()).removeValue();
        });
    }

    @Override
    public int getItemCount() {
        return blockedUsersList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImage;
        TextView userName;
        Button unblockButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profile_image);
            userName = itemView.findViewById(R.id.userName);
            unblockButton = itemView.findViewById(R.id.unblock_button);
        }
    }
}