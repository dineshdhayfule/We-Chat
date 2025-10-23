package com.example.wechat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.wechat.Models.Users;
import com.example.wechat.databinding.ActivityUserProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class UserProfileActivity extends BaseActivity {

    ActivityUserProfileBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        String userId = getIntent().getStringExtra("userId");

        database.getReference().child("Users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Users user = snapshot.getValue(Users.class);
                    Picasso.get().load(user.getProfilePic()).into(binding.profileImage);
                    binding.userName.setText(user.getUserName());
                    binding.txtStatus.setText(user.getStatus());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        database.getReference().child("Users").child(auth.getUid()).child("blockedUsers").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    binding.blockUser.setText("Unblock User");
                } else {
                    binding.blockUser.setText("Block User");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.backArrow.setOnClickListener(v -> {
            finish();
        });

        binding.blockUser.setOnClickListener(v -> {
            if(binding.blockUser.getText().toString().equals("Block User")){
                database.getReference().child("Users").child(auth.getUid()).child("blockedUsers").child(userId).setValue(true);
                Toast.makeText(UserProfileActivity.this, "User Blocked", Toast.LENGTH_SHORT).show();
            } else {
                database.getReference().child("Users").child(auth.getUid()).child("blockedUsers").child(userId).removeValue();
                Toast.makeText(UserProfileActivity.this, "User Unblocked", Toast.LENGTH_SHORT).show();
            }
        });

        binding.muteSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    database.getReference().child("Users").child(auth.getUid()).child("mutedUsers").child(userId).setValue(true);
                    Toast.makeText(UserProfileActivity.this, "Notifications Muted", Toast.LENGTH_SHORT).show();
                } else {
                    database.getReference().child("Users").child(auth.getUid()).child("mutedUsers").child(userId).removeValue();
                    Toast.makeText(UserProfileActivity.this, "Notifications Unmuted", Toast.LENGTH_SHORT).show();
                }
            }
        });

        database.getReference().child("Users").child(auth.getUid()).child("mutedUsers").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    binding.muteSwitch.setChecked(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}