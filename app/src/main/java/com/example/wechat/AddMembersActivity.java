package com.example.wechat;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.wechat.Adapter.UsersAdapter;
import com.example.wechat.Models.Group;
import com.example.wechat.Models.Users;
import com.example.wechat.databinding.ActivityAddMembersBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class AddMembersActivity extends BaseActivity {

    ActivityAddMembersBinding binding;
    FirebaseDatabase database;
    UsersAdapter adapter;
    ArrayList<Users> usersList = new ArrayList<>();
    String groupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddMembersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        database = FirebaseDatabase.getInstance();
        groupId = getIntent().getStringExtra("groupId");

        adapter = new UsersAdapter(usersList, this, true);
        binding.usersRecyclerView.setAdapter(adapter);
        binding.usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        binding.progressBar.setVisibility(View.VISIBLE);

        loadUsers();

        binding.fabDone.setOnClickListener(v -> {
            ArrayList<Users> selectedUsers = adapter.getSelectedUsers();
            if (selectedUsers.isEmpty()) {
                Toast.makeText(this, "Please select at least one member to add", Toast.LENGTH_SHORT).show();
                return;
            }
            addMembersToGroup(selectedUsers);
        });
    }

    private void loadUsers() {
        database.getReference().child("groups").child(groupId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot groupSnapshot) {
                if (groupSnapshot.exists()) {
                    Group group = groupSnapshot.getValue(Group.class);
                    final HashMap<String, Object> existingMembers = group.getMembers() != null ? group.getMembers() : new HashMap<>();
                    final HashMap<String, Object> invitedMembers = group.getInvitedMembers() != null ? group.getInvitedMembers() : new HashMap<>();

                    database.getReference().child("Users").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot usersSnapshot) {
                            binding.progressBar.setVisibility(View.GONE);
                            usersList.clear();
                            for (DataSnapshot dataSnapshot : usersSnapshot.getChildren()) {
                                Users user = dataSnapshot.getValue(Users.class);
                                String userId = dataSnapshot.getKey();
                                if (user != null && userId != null) {
                                    user.setUserId(userId);
                                    // Check if user is NOT current user, NOT a member, and NOT invited
                                    if (!userId.equals(FirebaseAuth.getInstance().getUid()) &&
                                            !existingMembers.containsKey(userId) &&
                                            !invitedMembers.containsKey(userId)) {
                                        usersList.add(user);
                                    }
                                }
                            }
                            adapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                             binding.progressBar.setVisibility(View.GONE);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                 binding.progressBar.setVisibility(View.GONE);
            }
        });
    }


    private void addMembersToGroup(ArrayList<Users> selectedUsers) {
        for (Users user : selectedUsers) {
            database.getReference().child("groups").child(groupId).child("invitedMembers").child(user.getUserId()).setValue(true);
        }
        Toast.makeText(this, "Invitations sent", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}
