package com.example.wechat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;

import com.example.wechat.Adapter.UsersAdapter;
import com.example.wechat.Models.Users;
import com.example.wechat.databinding.ActivityNewChatBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class NewChatActivity extends AppCompatActivity {

    ActivityNewChatBinding binding;
    ArrayList<Users> list = new ArrayList<>();
    FirebaseDatabase database;
    UsersAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNewChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        database = FirebaseDatabase.getInstance();

        adapter = new UsersAdapter(list, this, false);
        binding.usersRecyclerView.setAdapter(adapter);
        binding.usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        database.getReference().child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Users user = dataSnapshot.getValue(Users.class);
                    if (user != null) {
                        user.setUserId(dataSnapshot.getKey());
                        if(!user.getUserId().equals(FirebaseAuth.getInstance().getUid())){
                            list.add(user);
                        }
                    }
                }

                database.getReference().child("chats").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot chatSnapshot) {
                        ArrayList<Users> usersToRemove = new ArrayList<>();
                        for (DataSnapshot dataSnapshot : chatSnapshot.getChildren()) {
                            if (dataSnapshot.getKey() != null && dataSnapshot.getKey().contains(FirebaseAuth.getInstance().getUid())) {
                                String otherUserId = dataSnapshot.getKey().replace(FirebaseAuth.getInstance().getUid(), "");
                                for (Users user : list) {
                                    if (user.getUserId().equals(otherUserId)) {
                                        usersToRemove.add(user);
                                        break;
                                    }
                                }
                            }
                        }
                        list.removeAll(usersToRemove);
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
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