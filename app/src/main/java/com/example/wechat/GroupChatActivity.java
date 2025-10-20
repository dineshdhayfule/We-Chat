package com.example.wechat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;

import com.example.wechat.Adapter.GroupChatAdapter;
import com.example.wechat.Models.MessageModel;
import com.example.wechat.databinding.ActivityGroupChatBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GroupChatActivity extends AppCompatActivity {

    ActivityGroupChatBinding binding;
    GroupChatAdapter chatAdapter;
    FirebaseDatabase database;
    FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        binding.backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (chatAdapter.isMultiSelectMode()) {
                    chatAdapter.setMultiSelectMode(false);
                    binding.delete.setVisibility(View.GONE);
                } else {
                    Intent intent = new Intent(GroupChatActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            }
        });

        final ArrayList<Object> chatItems = new ArrayList<>();

        chatAdapter = new GroupChatAdapter(chatItems, this);
        binding.chatRecyclerView.setAdapter(chatAdapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.chatRecyclerView.setLayoutManager(layoutManager);

        database.getReference().child("Group Chat")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        chatItems.clear();
                        long lastTimestamp = 0;
                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                            MessageModel model = snapshot1.getValue(MessageModel.class);
                            if (model != null) {
                                model.setMessageId(snapshot1.getKey());

                                if (!isSameDay(lastTimestamp, model.getTimeStamp())) {
                                    chatItems.add(getFormattedDate(model.getTimeStamp()));
                                }
                                chatItems.add(model);
                                lastTimestamp = model.getTimeStamp();
                            }
                        }
                        chatAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        database.getReference().child("typingStatus").child("group")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            String typingStatus = snapshot.getValue(String.class);
                            if(!typingStatus.equals(FirebaseAuth.getInstance().getUid())){
                                binding.typingIndicator.setText(typingStatus + " is typing...");
                                binding.typingIndicator.setVisibility(View.VISIBLE);
                            } else {
                                binding.typingIndicator.setVisibility(View.GONE);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        final Handler handler = new Handler();
        binding.enterMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                database.getReference().child("typingStatus").child("group").setValue(FirebaseAuth.getInstance().getUid());
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(() -> database.getReference().child("typingStatus").child("group").setValue("false"), 2000);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.attachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 25);
            }
        });

        binding.menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popup = new PopupMenu(GroupChatActivity.this, view);
                popup.getMenuInflater().inflate(R.menu.chat_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        int itemId = item.getItemId();
                        if (itemId == R.id.action_clear_chat) {
                            database.getReference().child("Group Chat").removeValue();
                            return true;
                        } else if (itemId == R.id.action_delete) {
                            chatAdapter.setMultiSelectMode(true);
                            binding.delete.setVisibility(View.VISIBLE);
                            return true;
                        }
                        return false;
                    }
                });
                popup.show();
            }
        });

        binding.delete.setOnClickListener(v -> {
            List<MessageModel> selectedMessages = chatAdapter.getSelectedMessages();
            for (MessageModel message : selectedMessages) {
                database.getReference().child("Group Chat").child(message.getMessageId()).removeValue();
            }
            chatAdapter.setMultiSelectMode(false);
            binding.delete.setVisibility(View.GONE);
        });

        binding.send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String message = binding.enterMessage.getText().toString();
                if (message.isEmpty()) {
                } else {
                    final MessageModel model = new MessageModel(FirebaseAuth.getInstance().getUid(), message);
                    model.setTimeStamp(new Date().getTime());
                    binding.enterMessage.setText("");
                    database.getReference().child("Group Chat")
                            .push()
                            .setValue(model);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == 25){
                if(data.getData() != null){
                    Uri selectedImage = data.getData();
                    final StorageReference reference = storage.getReference().child("chats").child(new Date().getTime() + "");
                    reference.putFile(selectedImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String messageText = uri.toString();
                                    final MessageModel model = new MessageModel(FirebaseAuth.getInstance().getUid(), messageText);
                                    model.setTimeStamp(new Date().getTime());
                                    model.setImageUrl(messageText);

                                    database.getReference().child("Group Chat").push().setValue(model);
                                }
                            });
                        }
                    });
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (chatAdapter.isMultiSelectMode()) {
            chatAdapter.setMultiSelectMode(false);
            binding.delete.setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }
    }

    private boolean isSameDay(long timestamp1, long timestamp2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTimeInMillis(timestamp1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTimeInMillis(timestamp2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    private String getFormattedDate(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        SimpleDateFormat formatter = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
        return formatter.format(cal.getTime());
    }
}