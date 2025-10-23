package com.example.wechat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.wechat.Adapter.GroupsAdapter;
import com.example.wechat.Models.Group;
import com.example.wechat.databinding.ActivityGroupsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class GroupsActivity extends BaseActivity {

    ActivityGroupsBinding binding;
    FirebaseDatabase database;
    ArrayList<Object> list = new ArrayList<>();
    GroupsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        database = FirebaseDatabase.getInstance();
        
        adapter = new GroupsAdapter(list, this);
        binding.groupsRecyclerView.setAdapter(adapter);
        binding.groupsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        binding.progressBar.setVisibility(View.VISIBLE);

        database.getReference().child("groups").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                binding.progressBar.setVisibility(View.GONE);
                list.clear();

                ArrayList<Group> myGroups = new ArrayList<>();
                ArrayList<Group> invitedGroups = new ArrayList<>();

                Group mainGroup = new Group();
                mainGroup.setGroupId("general");
                mainGroup.setGroupName("Group Chat");
                myGroups.add(mainGroup);

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Group group = dataSnapshot.getValue(Group.class);
                    if (group != null) {
                        group.setGroupId(dataSnapshot.getKey());
                        if (group.getMembers() != null && group.getMembers().containsKey(FirebaseAuth.getInstance().getUid())) {
                            myGroups.add(group);
                        } else if (group.getInvitedMembers() != null && group.getInvitedMembers().containsKey(FirebaseAuth.getInstance().getUid())) {
                            invitedGroups.add(group);
                        }
                    }
                }

                if (!invitedGroups.isEmpty()) {
                    list.add("Invitations");
                    list.addAll(invitedGroups);
                }

                if (!myGroups.isEmpty()) {
                    list.add("My Groups");
                    list.addAll(myGroups);
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.progressBar.setVisibility(View.GONE);
            }
        });

        binding.fabCreateGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(GroupsActivity.this, CreateGroupActivity.class));
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}
