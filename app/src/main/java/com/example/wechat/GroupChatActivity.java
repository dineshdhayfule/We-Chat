package com.example.wechat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextThemeWrapper;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.wechat.Adapter.GroupChatAdapter;
import com.example.wechat.Models.MessageModel;
import com.example.wechat.Models.Users;
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
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class GroupChatActivity extends BaseActivity {

    ActivityGroupChatBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;
    GroupChatAdapter groupChatAdapter;
    MessageModel repliedToMessage = null;
    String groupId, groupName;

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int GALLERY_REQUEST_CODE = 25;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        groupId = getIntent().getStringExtra("groupId");
        groupName = getIntent().getStringExtra("groupName");

        binding.userName.setText(groupName);

        if("general".equals(groupId)){
            binding.profileImage.setImageResource(R.drawable.ic_group);
        } else {
            database.getReference().child("groups").child(groupId).child("groupImage").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        String groupImage = snapshot.getValue(String.class);
                        if(groupImage != null && !groupImage.isEmpty()){
                            Picasso.get().load(groupImage).placeholder(R.drawable.ic_group).into(binding.profileImage);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        binding.backArrow.setOnClickListener(view -> {
            if (groupChatAdapter.isMultiSelectMode()) {
                groupChatAdapter.setMultiSelectMode(false);
                binding.deleteLayout.setVisibility(View.GONE);
            } else {
                finish();
            }
        });

        binding.profileImage.setOnClickListener(v -> {
            Intent intent = new Intent(GroupChatActivity.this, GroupInfoActivity.class);
            intent.putExtra("groupId", groupId);
            startActivity(intent);
        });

        final ArrayList<Object> chatItems = new ArrayList<>();
        groupChatAdapter = new GroupChatAdapter(chatItems, this, binding.chatRecyclerView);

        binding.chatRecyclerView.setAdapter(groupChatAdapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.chatRecyclerView.setLayoutManager(layoutManager);

        final String chatNode = "general".equals(groupId) ? "Group Chat" : "group_messages/" + groupId;

        final Map<String, String> typingUsers = new HashMap<>();
        database.getReference().child("typingStatus").child(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    typingUsers.clear();
                    for(DataSnapshot userSnapshot : snapshot.getChildren()){
                        if(!userSnapshot.getKey().equals(auth.getUid())){
                            typingUsers.put(userSnapshot.getKey(), userSnapshot.getValue(String.class));
                        }
                    }

                    List<String> userNames = new ArrayList<>(typingUsers.values());
                    if(userNames.isEmpty()){
                        binding.typingIndicator.setVisibility(View.GONE);
                    } else {
                        String typingText = String.join(", ", userNames) + (userNames.size() > 1 ? " are" : " is") + " typing...";
                        binding.typingIndicator.setText(typingText);
                        binding.typingIndicator.setVisibility(View.VISIBLE);
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
                database.getReference().child("Users").child(auth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            Users user = snapshot.getValue(Users.class);
                            database.getReference().child("typingStatus").child(groupId).child(auth.getUid()).setValue(user.getUserName());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(() -> database.getReference().child("typingStatus").child(groupId).child(auth.getUid()).removeValue(), 2000);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        database.getReference().child(chatNode)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        chatItems.clear();
                        long lastTimestamp = 0;
                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                            if("placeholder".equals(snapshot1.getKey())){
                                continue;
                            }
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
                        groupChatAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        binding.attachment.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, GALLERY_REQUEST_CODE);
        });

        binding.camera.setOnClickListener(v -> {
            if(ContextCompat.checkSelfPermission(GroupChatActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(GroupChatActivity.this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
            } else {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, CAMERA_REQUEST_CODE);
            }
        });

        binding.menu.setOnClickListener(view -> {
            Context wrapper = new ContextThemeWrapper(GroupChatActivity.this, R.style.App_PopupMenu_Dark);
            PopupMenu popup = new PopupMenu(wrapper, view);
            popup.getMenuInflater().inflate(R.menu.chat_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.action_group_info) {
                    Intent intent = new Intent(GroupChatActivity.this, GroupInfoActivity.class);
                    intent.putExtra("groupId", groupId);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.action_clear_chat) {
                    database.getReference().child(chatNode).removeValue();
                    return true;
                } else if (itemId == R.id.action_delete) {
                    groupChatAdapter.setMultiSelectMode(true);
                    binding.deleteLayout.setVisibility(View.VISIBLE);
                    return true;
                } else if (itemId == R.id.action_settings) {
                    startActivity(new Intent(GroupChatActivity.this, SettingsActivity.class));
                    return true;
                }
                return false;
            });
            popup.show();
        });

        binding.delete.setOnClickListener(v -> {
            List<MessageModel> selectedMessages = groupChatAdapter.getSelectedMessages();
            for (MessageModel message : selectedMessages) {
                database.getReference().child(chatNode).child(message.getMessageId()).removeValue();
            }
            groupChatAdapter.setMultiSelectMode(false);
            binding.deleteLayout.setVisibility(View.GONE);
        });

        binding.send.setOnClickListener(view -> {
            String message = binding.enterMessage.getText().toString();
            if (message.isEmpty()) {
                Toast.makeText(GroupChatActivity.this, "Enter a message", Toast.LENGTH_SHORT).show();
            } else {
                final MessageModel model = new MessageModel(auth.getUid(), message);
                model.setTimeStamp(new Date().getTime());
                if(repliedToMessage != null){
                    model.setRepliedToMessage(repliedToMessage.getMessage());
                    model.setRepliedToSender(repliedToMessage.getuId());
                    model.setRepliedToMessageId(repliedToMessage.getMessageId());
                    binding.replyLayout.setVisibility(View.GONE);
                    repliedToMessage = null;
                }
                binding.enterMessage.setText("");
                database.getReference().child(chatNode)
                        .push()
                        .setValue(model);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == GALLERY_REQUEST_CODE){
                if(data.getData() != null){
                    Uri selectedImage = data.getData();
                    final StorageReference reference = storage.getReference().child("chats").child(new Date().getTime() + "");
                    reference.putFile(selectedImage).addOnSuccessListener(taskSnapshot -> reference.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        final MessageModel model = new MessageModel(auth.getUid(), "Photo");
                        model.setTimeStamp(new Date().getTime());
                        model.setImageUrl(imageUrl);
                        final String chatNode = "general".equals(groupId) ? "Group Chat" : "group_messages/" + groupId;
                        database.getReference().child(chatNode).push().setValue(model);
                    }));
                }
            } else if (requestCode == CAMERA_REQUEST_CODE) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                photo.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                byte[] bb = bytes.toByteArray();
                final StorageReference reference = storage.getReference().child("chats").child(new Date().getTime() + "");
                reference.putBytes(bb).addOnSuccessListener(taskSnapshot -> reference.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    final MessageModel model = new MessageModel(auth.getUid(), "Photo");
                    model.setTimeStamp(new Date().getTime());
                    model.setImageUrl(imageUrl);
                    final String chatNode = "general".equals(groupId) ? "Group Chat" : "group_messages/" + groupId;
                    database.getReference().child(chatNode).push().setValue(model);
                }));
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == CAMERA_REQUEST_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, CAMERA_REQUEST_CODE);
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (groupChatAdapter.isMultiSelectMode()) {
            groupChatAdapter.setMultiSelectMode(false);
            binding.deleteLayout.setVisibility(View.GONE);
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
        SimpleDateFormat formatter = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
        return formatter.format(cal.getTime());
    }
}
