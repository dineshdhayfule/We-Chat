package com.example.wechat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class GroupChatActivity extends BaseActivity {

    ActivityGroupChatBinding binding;
    GroupChatAdapter chatAdapter;
    FirebaseDatabase database;
    FirebaseStorage storage;
    MessageModel repliedToMessage = null;
    String groupId;
    String groupName;

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int GALLERY_REQUEST_CODE = 25;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setWallpaper();

        groupId = getIntent().getStringExtra("groupId");
        groupName = getIntent().getStringExtra("groupName");

        binding.userName.setText(groupName);

        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        binding.backArrow.setOnClickListener(view -> finish());

        binding.profileImage.setOnClickListener(v -> openGroupInfo());
        binding.userName.setOnClickListener(v -> openGroupInfo());

        final ArrayList<Object> chatItems = new ArrayList<>();

        chatAdapter = new GroupChatAdapter(chatItems, this, binding.chatRecyclerView);
        binding.chatRecyclerView.setAdapter(chatAdapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.chatRecyclerView.setLayoutManager(layoutManager);

        final String chatNode = "general".equals(groupId) ? "Group Chat" : "group_messages/" + groupId;
        final String currentUserId = FirebaseAuth.getInstance().getUid();

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                if (chatItems.get(position) instanceof MessageModel) {
                    repliedToMessage = (MessageModel) chatItems.get(position);
                    binding.replyLayout.setVisibility(View.VISIBLE);
                    database.getReference().child("Users").child(repliedToMessage.getuId()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                Users user = snapshot.getValue(Users.class);
                                binding.repliedToSender.setText(user.getUserName());
                                if(repliedToMessage.getImageUrl() != null){
                                    binding.repliedToMessage.setText("Photo");
                                } else {
                                    binding.repliedToMessage.setText(repliedToMessage.getMessage());
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }
            }
        }).attachToRecyclerView(binding.chatRecyclerView);

        binding.cancelReply.setOnClickListener(v -> {
            repliedToMessage = null;
            binding.replyLayout.setVisibility(View.GONE);
        });

        database.getReference().child(chatNode)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        chatItems.clear();
                        long lastTimestamp = 0;
                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                            MessageModel model = snapshot1.getValue(MessageModel.class);
                            if (model != null) {
                                model.setMessageId(snapshot1.getKey());

                                if (!model.getuId().equals(currentUserId)) {
                                    HashMap<String, Object> readUpdate = new HashMap<>();
                                    readUpdate.put("read", true);
                                    snapshot1.getRef().updateChildren(readUpdate);
                                }

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

        database.getReference().child("typingStatus").child(groupId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            String typingStatus = snapshot.getValue(String.class);
                            if(!typingStatus.equals(FirebaseAuth.getInstance().getUid()) && !typingStatus.equals("false")){
                                database.getReference().child("Users").child(typingStatus).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                        if(userSnapshot.exists()){
                                            Users user = userSnapshot.getValue(Users.class);
                                            binding.typingIndicator.setText(user.getUserName() + " is typing...");
                                            binding.typingIndicator.setVisibility(View.VISIBLE);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

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
                database.getReference().child("typingStatus").child(groupId).setValue(FirebaseAuth.getInstance().getUid());
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(() -> database.getReference().child("typingStatus").child(groupId).setValue("false"), 2000);
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
                startActivityForResult(intent, GALLERY_REQUEST_CODE);
            }
        });

        binding.camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(GroupChatActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(GroupChatActivity.this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
                } else {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, CAMERA_REQUEST_CODE);
                }
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
                            database.getReference().child(chatNode).removeValue();
                            return true;
                        } else if (itemId == R.id.action_delete) {
                            chatAdapter.setMultiSelectMode(true);
                            binding.deleteLayout.setVisibility(View.VISIBLE);
                            return true;
                        } else if (itemId == R.id.action_group_info) {
                            openGroupInfo();
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
                database.getReference().child(chatNode).child(message.getMessageId()).removeValue();
            }
            chatAdapter.setMultiSelectMode(false);
            binding.deleteLayout.setVisibility(View.GONE);
        });

        binding.send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String message = binding.enterMessage.getText().toString();
                if (message.isEmpty()) {
                } else {
                    final MessageModel model = new MessageModel(FirebaseAuth.getInstance().getUid(), message);
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
            }
        });
    }

    private void openGroupInfo(){
        Intent intent = new Intent(GroupChatActivity.this, GroupInfoActivity.class);
        intent.putExtra("groupId", groupId);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final String chatNode = "general".equals(groupId) ? "Group Chat" : "group_messages/" + groupId;
        if(resultCode == RESULT_OK){
            if(requestCode == GALLERY_REQUEST_CODE){
                if(data.getData() != null){
                    Uri selectedImage = data.getData();
                    final StorageReference reference = storage.getReference().child("chats").child(new Date().getTime() + "");
                    reference.putFile(selectedImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String imageUrl = uri.toString();
                                    final MessageModel model = new MessageModel(FirebaseAuth.getInstance().getUid(), "Photo");
                                    model.setTimeStamp(new Date().getTime());
                                    model.setImageUrl(imageUrl);

                                    database.getReference().child(chatNode).push().setValue(model);
                                }
                            });
                        }
                    });
                }
            } else if (requestCode == CAMERA_REQUEST_CODE) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                photo.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                byte[] bb = bytes.toByteArray();
                final StorageReference reference = storage.getReference().child("chats").child(new Date().getTime() + "");
                reference.putBytes(bb).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String imageUrl = uri.toString();
                                final MessageModel model = new MessageModel(FirebaseAuth.getInstance().getUid(), "Photo");
                                model.setTimeStamp(new Date().getTime());
                                model.setImageUrl(imageUrl);

                                database.getReference().child(chatNode).push().setValue(model);
                            }
                        });
                    }
                });
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

    private void setWallpaper(){
        SharedPreferences sharedPreferences = getSharedPreferences("wallpaper", MODE_PRIVATE);
        int wallpaperId = sharedPreferences.getInt("wallpaperId", 0);
        String wallpaperUri = sharedPreferences.getString("wallpaperUri", null);

        if(wallpaperId != 0){
            binding.relativeLayout.setBackgroundResource(wallpaperId);
        } else if (wallpaperUri != null){
            try {
                binding.relativeLayout.setBackground(new android.graphics.drawable.BitmapDrawable(getResources(), MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(wallpaperUri))));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } 
    }

    @Override
    public void onBackPressed() {
        if (chatAdapter.isMultiSelectMode()) {
            chatAdapter.setMultiSelectMode(false);
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
        SimpleDateFormat formatter = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
        return formatter.format(cal.getTime());
    }
}