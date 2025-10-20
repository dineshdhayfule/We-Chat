package com.example.wechat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;

import com.example.wechat.Adapter.BlockedUsersAdapter;
import com.example.wechat.Models.Users;
import com.example.wechat.databinding.ActivityBlockedUsersBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class BlockedUsersActivity extends AppCompatActivity {

    ActivityBlockedUsersBinding binding;
    FirebaseDatabase database;
    FirebaseAuth auth;
    BlockedUsersAdapter adapter;
    List<Users> blockedUsersList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBlockedUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        adapter = new BlockedUsersAdapter(blockedUsersList, this);
        binding.blockedUsersRecyclerView.setAdapter(adapter);
        binding.blockedUsersRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        fetchBlockedUsers();
    }

    private void fetchBlockedUsers() {
        database.getReference().child("Users").child(auth.getUid()).child("blockedUsers").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                blockedUsersList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String userId = dataSnapshot.getKey();
                    if (userId != null) {
                        database.getReference().child("Users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                if (userSnapshot.exists()) {
                                    Users user = userSnapshot.getValue(Users.class);
                                    user.setUserId(userSnapshot.getKey());
                                    blockedUsersList.add(user);
                                    adapter.notifyDataSetChanged();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}