package com.example.wechat;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.wechat.Adapter.GroupMembersAdapter;
import com.example.wechat.Models.Group;
import com.example.wechat.Models.Users;
import com.example.wechat.databinding.ActivityGroupInfoBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class GroupInfoActivity extends BaseActivity implements GroupMembersAdapter.OnItemLongClickListener {

    ActivityGroupInfoBinding binding;
    FirebaseDatabase database;
    FirebaseStorage storage;
    String groupId;
    GroupMembersAdapter membersAdapter;
    ArrayList<Users> membersList = new ArrayList<>();
    Group currentGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        groupId = getIntent().getStringExtra("groupId");

        membersAdapter = new GroupMembersAdapter(membersList, this, this);
        binding.membersRecyclerView.setAdapter(membersAdapter);
        binding.membersRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadGroupInfo();

        binding.leaveGroupButton.setOnClickListener(v -> leaveGroup());

        binding.addMembersButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddMembersActivity.class);
            intent.putExtra("groupId", groupId);
            startActivity(intent);
        });

        binding.editGroupName.setOnClickListener(v -> showEditGroupNameDialog());

        binding.editGroupImage.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, 33);
        });

        binding.deleteGroupButton.setOnClickListener(v -> deleteGroup());
    }

    private void loadGroupInfo() {
        if ("general".equals(groupId)) {
            binding.groupName.setText("Group Chat");
            binding.groupImage.setImageResource(R.drawable.ic_group);
            binding.addMembersButton.setVisibility(View.GONE);
            binding.leaveGroupButton.setVisibility(View.GONE);
            binding.editGroupName.setVisibility(View.GONE);
            binding.editGroupImage.setVisibility(View.GONE);
            binding.deleteGroupButton.setVisibility(View.GONE);
            loadAllUsersAsMembers();
            return;
        }

        database.getReference().child("groups").child(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentGroup = snapshot.getValue(Group.class);
                    if (currentGroup != null) {
                        binding.groupName.setText(currentGroup.getGroupName());
                        if (currentGroup.getGroupImage() != null && !currentGroup.getGroupImage().isEmpty()) {
                            Picasso.get().load(currentGroup.getGroupImage()).placeholder(R.drawable.ic_group).into(binding.groupImage);
                        }
                        membersAdapter.setMemberRoles(currentGroup.getMembers());
                        loadGroupMembers(currentGroup);

                        Object roleObj = currentGroup.getMembers().get(FirebaseAuth.getInstance().getUid());
                        String currentUserRole = roleObj instanceof String ? (String) roleObj : "";

                        if ("creator".equals(currentUserRole) || "admin".equals(currentUserRole)) {
                            binding.addMembersButton.setVisibility(View.VISIBLE);
                            binding.editGroupName.setVisibility(View.VISIBLE);
                            binding.editGroupImage.setVisibility(View.VISIBLE);
                        } else {
                            binding.addMembersButton.setVisibility(View.GONE);
                            binding.editGroupName.setVisibility(View.GONE);
                            binding.editGroupImage.setVisibility(View.GONE);
                        }

                        if("creator".equals(currentUserRole)){
                            binding.deleteGroupButton.setVisibility(View.VISIBLE);
                        } else {
                            binding.deleteGroupButton.setVisibility(View.GONE);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void loadAllUsersAsMembers() {
        membersList.clear();
        database.getReference().child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                membersList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Users user = dataSnapshot.getValue(Users.class);
                    if (user != null) {
                        user.setUserId(dataSnapshot.getKey());
                        membersList.add(user);
                    }
                }
                membersAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void loadGroupMembers(Group group) {
        membersList.clear();
        if (group.getMembers() == null) return;
        for (String memberId : group.getMembers().keySet()) {
            database.getReference().child("Users").child(memberId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Users user = snapshot.getValue(Users.class);
                        if (user != null) {
                            user.setUserId(snapshot.getKey());
                            membersList.add(user);
                            membersAdapter.notifyDataSetChanged();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }
    }

    private void leaveGroup() {
        new AlertDialog.Builder(this)
                .setTitle("Leave Group")
                .setMessage("Are you sure you want to leave this group?")
                .setPositiveButton("Leave", (dialog, which) -> {
                    String currentUserId = FirebaseAuth.getInstance().getUid();
                    database.getReference().child("groups").child(groupId).child("members").child(currentUserId).removeValue()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(GroupInfoActivity.this, "You have left the group", Toast.LENGTH_SHORT).show();
                                finish();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteGroup() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Group")
                .setMessage("Are you sure you want to delete this group? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    database.getReference().child("groups").child(groupId).removeValue().addOnSuccessListener(aVoid -> {
                        database.getReference().child("group_messages").child(groupId).removeValue();
                        Toast.makeText(GroupInfoActivity.this, "Group deleted", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditGroupNameDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter New Group Name");

        final EditText input = new EditText(this);
        input.setHint("Group Name");
        input.setText(currentGroup.getGroupName());
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newGroupName = input.getText().toString().trim();
            if (newGroupName.isEmpty()) {
                Toast.makeText(this, "Group name cannot be empty", Toast.LENGTH_SHORT).show();
            } else {
                database.getReference().child("groups").child(groupId).child("groupName").setValue(newGroupName);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 33) {
            if (data != null && data.getData() != null) {
                Uri sFile = data.getData();
                final StorageReference reference = storage.getReference().child("group_pictures").child(groupId);

                reference.putFile(sFile).addOnSuccessListener(taskSnapshot -> reference.getDownloadUrl().addOnSuccessListener(uri -> {
                    database.getReference().child("groups").child(groupId).child("groupImage").setValue(uri.toString());
                }));
            }
        }
    }

    @Override
    public void onItemLongClick(Users user) {
        String currentUserId = FirebaseAuth.getInstance().getUid();
        Object currentUserRoleObj = currentGroup.getMembers().get(currentUserId);
        String currentUserRole = currentUserRoleObj instanceof String ? (String) currentUserRoleObj : "";

        Object clickedUserRoleObj = currentGroup.getMembers().get(user.getUserId());
        String clickedUserRole = clickedUserRoleObj instanceof String ? (String) clickedUserRoleObj : "";

        if ("creator".equals(currentUserRole)) {
            final CharSequence[] options;
            if (user.getUserId().equals(currentUserId)) return; // Can't act on self
            if ("admin".equals(clickedUserRole)) {
                options = new CharSequence[]{"Remove Admin", "Remove Member"};
            } else {
                options = new CharSequence[]{"Make Admin", "Remove Member"};
            }

            new AlertDialog.Builder(this)
                    .setTitle("Manage Member")
                    .setItems(options, (dialog, which) -> {
                        if (options[which].equals("Make Admin")) {
                            database.getReference().child("groups").child(groupId).child("members").child(user.getUserId()).setValue("admin");
                        } else if (options[which].equals("Remove Admin")) {
                            database.getReference().child("groups").child(groupId).child("members").child(user.getUserId()).setValue("member");
                        } else if (options[which].equals("Remove Member")) {
                            database.getReference().child("groups").child(groupId).child("members").child(user.getUserId()).removeValue();
                        }
                    }).show();
        } else if ("admin".equals(currentUserRole)){
            if ("creator".equals(clickedUserRole) || "admin".equals(clickedUserRole) || user.getUserId().equals(currentUserId)) return; // Admins can't act on other admins, the creator or self

            new AlertDialog.Builder(this)
                    .setTitle("Manage Member")
                    .setItems(new CharSequence[]{"Remove Member"}, (dialog, which) -> {
                        database.getReference().child("groups").child(groupId).child("members").child(user.getUserId()).removeValue();
                    }).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}
