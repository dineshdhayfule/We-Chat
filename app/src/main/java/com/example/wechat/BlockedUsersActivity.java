package com.example.wechat;

import androidx.annotation.NonNull;
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

public class BlockedUsersActivity extends BaseActivity {

    ActivityBlockedUsersBinding binding;
    ArrayList<Users> list = new ArrayList<>();
    FirebaseDatabase database;
    BlockedUsersAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBlockedUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        database = FirebaseDatabase.getInstance();
        adapter = new BlockedUsersAdapter(list, this);
        binding.blockedUsersRecyclerView.setAdapter(adapter);
        binding.blockedUsersRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid()).child("blockedUsers")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        list.clear();
                        for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                            database.getReference().child("Users").child(dataSnapshot.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                    if(userSnapshot.exists()){
                                        Users user = userSnapshot.getValue(Users.class);
                                        user.setUserId(userSnapshot.getKey());
                                        list.add(user);
                                        adapter.notifyDataSetChanged();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
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