package com.example.wechat;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.wechat.Adapter.UsersAdapter;
import com.example.wechat.Models.Group;
import com.example.wechat.Models.Users;
import com.example.wechat.databinding.ActivityCreateGroupBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class CreateGroupActivity extends BaseActivity {

    ActivityCreateGroupBinding binding;
    FirebaseDatabase database;
    UsersAdapter adapter;
    ArrayList<Users> usersList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateGroupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        database = FirebaseDatabase.getInstance();
        adapter = new UsersAdapter(usersList, this, true); // true for multi-select mode
        binding.usersRecyclerView.setAdapter(adapter);
        binding.usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        binding.progressBar.setVisibility(View.VISIBLE);

        // Fetch all users to display for selection
        database.getReference().child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                binding.progressBar.setVisibility(View.GONE);
                usersList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Users user = dataSnapshot.getValue(Users.class);
                    String userId = dataSnapshot.getKey();
                    if (user != null && userId != null) {
                        user.setUserId(userId);
                        if (!userId.equals(FirebaseAuth.getInstance().getUid())) {
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

        binding.fabDone.setOnClickListener(v -> {
            ArrayList<Users> selectedUsers = adapter.getSelectedUsers();
            if (selectedUsers.isEmpty()) {
                Toast.makeText(CreateGroupActivity.this, "Please select at least one member", Toast.LENGTH_SHORT).show();
                return;
            }
            showGroupNameDialog(selectedUsers);
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void showGroupNameDialog(ArrayList<Users> selectedUsers) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Group Name");

        final EditText input = new EditText(this);
        input.setHint("Group Name");
        builder.setView(input);

        builder.setPositiveButton("Create", (dialog, which) -> {
            String groupName = input.getText().toString().trim();
            if (groupName.isEmpty()) {
                Toast.makeText(CreateGroupActivity.this, "Group name cannot be empty", Toast.LENGTH_SHORT).show();
            } else {
                createGroup(groupName, selectedUsers);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void createGroup(String groupName, ArrayList<Users> selectedUsers) {
        String currentUserId = FirebaseAuth.getInstance().getUid();
        String groupId = database.getReference().child("groups").push().getKey();

        Group group = new Group();
        group.setGroupId(groupId);
        group.setGroupName(groupName);
        group.setCreatedBy(currentUserId); // Keep track of the original creator

        HashMap<String, Object> members = new HashMap<>();
        members.put(currentUserId, "creator"); // Assign "creator" role to the current user
        for (Users user : selectedUsers) {
            members.put(user.getUserId(), "member"); // Assign "member" role to others
        }
        group.setMembers(members);

        if (groupId != null) {
            database.getReference().child("groups").child(groupId).setValue(group).addOnSuccessListener(aVoid -> {
                Toast.makeText(CreateGroupActivity.this, groupName + " created", Toast.LENGTH_SHORT).show();
                finish(); // Go back to the groups list
            });
        }
    }
}
