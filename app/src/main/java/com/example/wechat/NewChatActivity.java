package com.example.wechat;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;

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

public class NewChatActivity extends BaseActivity {

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

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                if(adapter.getSelectedUser() != null){
                    binding.fabConfirm.setVisibility(View.VISIBLE);
                } else {
                    binding.fabConfirm.setVisibility(View.GONE);
                }
            }
        });

        binding.fabConfirm.setOnClickListener(v -> {
            Users selectedUser = adapter.getSelectedUser();
            if(selectedUser != null){
                String senderId = FirebaseAuth.getInstance().getUid();
                String receiverId = selectedUser.getUserId();

                String senderRoom = senderId + receiverId;
                String receiverRoom = receiverId + senderId;

                // By creating a placeholder, we ensure that the chat room exists,
                // so it will appear in the main chat list.
                database.getReference().child("chats").child(senderRoom).child("placeholder").setValue(true);
                database.getReference().child("chats").child(receiverRoom).child("placeholder").setValue(true);

                Intent intent = new Intent(NewChatActivity.this, ChatDetailActivity.class);
                intent.putExtra("userId",selectedUser.getUserId());
                intent.putExtra("profilePic",selectedUser.getProfilePic());
                intent.putExtra("userName",selectedUser.getUserName());
                startActivity(intent);
                finish();
            }
        });
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.new_chat_menu, menu);
//        MenuItem searchItem = menu.findItem(R.id.search);
//        SearchView searchView = (SearchView) searchItem.getActionView();
//
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                adapter.getFilter().filter(newText);
//                return false;
//            }
//        });
//
//        return super.onCreateOptionsMenu(menu);
//    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}