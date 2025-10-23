package com.example.wechat.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.wechat.Adapter.UsersAdapter;
import com.example.wechat.Models.Users;
import com.example.wechat.databinding.FragmentChatsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;


public class ChatsFragment extends Fragment {


    FragmentChatsBinding binding;
    ArrayList <Users> list = new ArrayList<>();
    FirebaseDatabase database;
    UsersAdapter adapter;

    public ChatsFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        binding=FragmentChatsBinding.inflate(inflater,container,false);
        database = FirebaseDatabase.getInstance();
        adapter = new UsersAdapter(list, getContext(), false);

        binding.chatRecyclerView.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.chatRecyclerView.setLayoutManager(layoutManager);

        // Forcefully remove any and all item decorations
        if (binding.chatRecyclerView.getItemDecorationCount() > 0) {
            binding.chatRecyclerView.removeItemDecorationAt(0);
        }

        database.getReference().child("chats").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                final Set<String> userIds = new HashSet<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (dataSnapshot.getKey() != null && dataSnapshot.getKey().contains(FirebaseAuth.getInstance().getUid())) {
                        userIds.add(dataSnapshot.getKey().replace(FirebaseAuth.getInstance().getUid(), ""));
                    }
                }

                if (userIds.isEmpty()) {
                    list.clear();
                    adapter.notifyDataSetChanged();
                    return;
                }

                database.getReference().child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot usersSnapshot) {
                        list.clear();
                        for (DataSnapshot dataSnapshot : usersSnapshot.getChildren()) {
                            if (userIds.contains(dataSnapshot.getKey())) {
                                Users user = dataSnapshot.getValue(Users.class);
                                if (user != null) {
                                    user.setUserId(dataSnapshot.getKey());
                                    list.add(user);
                                }
                            }
                        }
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

        return binding.getRoot();
    }

    public UsersAdapter getAdapter() {
        return adapter;
    }
}