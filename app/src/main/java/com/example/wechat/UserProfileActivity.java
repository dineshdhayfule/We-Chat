package com.example.wechat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.example.wechat.Models.Users;
import com.example.wechat.databinding.ActivityUserProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class UserProfileActivity extends AppCompatActivity {

    ActivityUserProfileBinding binding;
    FirebaseDatabase database;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        String userId = getIntent().getStringExtra("userId");

        binding.backArrow.setOnClickListener(v -> {
            finish();
        });

        database.getReference().child("Users").child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Users user = snapshot.getValue(Users.class);
                            Picasso.get().load(user.getProfilePic()).placeholder(R.drawable.avatar3).into(binding.profileImage);
                            binding.userName.setText(user.getUserName());
                            binding.txtStatus.setText(user.getStatus());
                            binding.muteSwitch.setChecked(user.isMuted());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        binding.muteSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                database.getReference().child("Users").child(auth.getUid()).child("mutedUsers").child(userId).setValue(isChecked);
                if(isChecked){
                    Toast.makeText(UserProfileActivity.this, "Notifications Muted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(UserProfileActivity.this, "Notifications Unmuted", Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.blockUser.setOnClickListener(v -> {
            database.getReference().child("Users").child(auth.getUid()).child("blockedUsers").child(userId).setValue(true);
            Toast.makeText(UserProfileActivity.this, "User Blocked", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}