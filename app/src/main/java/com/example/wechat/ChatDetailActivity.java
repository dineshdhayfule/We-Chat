package com.example.wechat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

import com.example.wechat.Adapter.ChatAdapter;
import com.example.wechat.Models.MessageModel;
import com.example.wechat.Models.Users;
import com.example.wechat.databinding.ActivityChatDetailBinding;
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
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ChatDetailActivity extends BaseActivity {

    ActivityChatDetailBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;
    ChatAdapter chatAdapter;
    MessageModel repliedToMessage = null;

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int GALLERY_REQUEST_CODE = 25;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setWallpaper();

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        final String senderId = auth.getUid();
        String recieveId = getIntent().getStringExtra("userId");
        String userName = getIntent().getStringExtra("userName");
        String profilePic = getIntent().getStringExtra("profilePic");

        binding.userName.setText(userName);
        try {
            Picasso.get().load(profilePic).into(binding.profileImage);
        } catch (Exception e) {
            e.printStackTrace();
        }

        binding.backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (chatAdapter.isMultiSelectMode()) {
                    chatAdapter.setMultiSelectMode(false);
                    binding.delete.setVisibility(View.GONE);
                } else {
                    Intent intent = new Intent(ChatDetailActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            }
        });

        binding.profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChatDetailActivity.this, UserProfileActivity.class);
                intent.putExtra("userId", recieveId);
                startActivity(intent);
            }
        });

        final ArrayList<Object> chatItems = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatItems, this, recieveId, binding.chatRecyclerView);

        binding.chatRecyclerView.setAdapter(chatAdapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.chatRecyclerView.setLayoutManager(layoutManager);

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
                    binding.repliedToSender.setText(repliedToMessage.getuId().equals(senderId) ? "You" : userName);
                    if(repliedToMessage.getImageUrl() != null){
                        binding.repliedToMessage.setText("Photo");
                    } else {
                        binding.repliedToMessage.setText(repliedToMessage.getMessage());
                    }
                }
            }
        }).attachToRecyclerView(binding.chatRecyclerView);
        
        binding.cancelReply.setOnClickListener(v -> {
            repliedToMessage = null;
            binding.replyLayout.setVisibility(View.GONE);
        });

        final String senderRoom = senderId + recieveId;
        final String receiverRoom = recieveId + senderId;

        database.getReference().child("Users").child(recieveId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Users user = snapshot.getValue(Users.class);
                    if (user.isOnline()) {
                        binding.lastSeen.setText("Online");
                        binding.lastSeen.setVisibility(View.VISIBLE);
                        binding.typingIndicator.setVisibility(View.GONE);
                    } else {
                        binding.lastSeen.setText("Last seen at " + formatLastSeen(user.getLastSeen()));
                        binding.lastSeen.setVisibility(View.VISIBLE);
                        binding.typingIndicator.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        database.getReference().child("typingStatus").child(senderRoom).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String typingStatus = snapshot.getValue(String.class);
                    if(typingStatus.equals(recieveId)){
                        binding.lastSeen.setVisibility(View.GONE);
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
                database.getReference().child("typingStatus").child(receiverRoom).setValue(senderId);
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(() -> database.getReference().child("typingStatus").child(receiverRoom).setValue("false"), 2000);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        database.getReference().child("chats")
                .child(senderRoom)
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

                                if (!model.getuId().equals(senderId)) {
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
                if(ContextCompat.checkSelfPermission(ChatDetailActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(ChatDetailActivity.this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
                } else {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, CAMERA_REQUEST_CODE);
                }
            }
        });

        binding.menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popup = new PopupMenu(ChatDetailActivity.this, view);
                popup.getMenuInflater().inflate(R.menu.personal_chat_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        int itemId = item.getItemId();
                        if (itemId == R.id.action_clear_chat) {
                            database.getReference().child("chats").child(senderRoom).removeValue();
                            return true;
                        } else if (itemId == R.id.action_delete) {
                            chatAdapter.setMultiSelectMode(true);
                            binding.delete.setVisibility(View.VISIBLE);
                            return true;
                        } else if (itemId == R.id.action_settings) {
                            startActivity(new Intent(ChatDetailActivity.this, SettingsActivity.class));
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
                database.getReference().child("chats").child(senderRoom).child(message.getMessageId()).removeValue();
            }
            chatAdapter.setMultiSelectMode(false);
            binding.delete.setVisibility(View.GONE);
        });

        binding.send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = binding.enterMessage.getText().toString();
                if (message.isEmpty()) {
                    Toast.makeText(ChatDetailActivity.this, "Enter a message", Toast.LENGTH_SHORT).show();
                } else {
                    final MessageModel model = new MessageModel(senderId, message);
                    model.setTimeStamp(new Date().getTime());
                    if(repliedToMessage != null){
                        model.setRepliedToMessage(repliedToMessage.getMessage());
                        model.setRepliedToSender(repliedToMessage.getuId());
                        model.setRepliedToMessageId(repliedToMessage.getMessageId());
                        binding.replyLayout.setVisibility(View.GONE);
                        repliedToMessage = null;
                    }
                    binding.enterMessage.setText("");
                    database.getReference().child("chats")
                            .child(senderRoom)
                            .push()
                            .setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    database.getReference().child("chats")
                                            .child(receiverRoom)
                                            .push()
                                            .setValue(model);
                                }
                            });
                }
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
                    reference.putFile(selectedImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String imageUrl = uri.toString();
                                    final MessageModel model = new MessageModel(auth.getUid(), "Photo");
                                    model.setTimeStamp(new Date().getTime());
                                    model.setImageUrl(imageUrl);

                                    final String senderRoom = auth.getUid() + getIntent().getStringExtra("userId");
                                    final String receiverRoom = getIntent().getStringExtra("userId") + auth.getUid();

                                    database.getReference().child("chats").child(senderRoom).push().setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            database.getReference().child("chats").child(receiverRoom).push().setValue(model);
                                        }
                                    });
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
                                final MessageModel model = new MessageModel(auth.getUid(), "Photo");
                                model.setTimeStamp(new Date().getTime());
                                model.setImageUrl(imageUrl);

                                final String senderRoom = auth.getUid() + getIntent().getStringExtra("userId");
                                final String receiverRoom = getIntent().getStringExtra("userId") + auth.getUid();

                                database.getReference().child("chats").child(senderRoom).push().setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        database.getReference().child("chats").child(receiverRoom).push().setValue(model);
                                    }
                                });
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

    @Override
    public void onBackPressed() {
        if (chatAdapter.isMultiSelectMode()) {
            chatAdapter.setMultiSelectMode(false);
            binding.delete.setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }
    }

    private void setWallpaper(){
        SharedPreferences sharedPreferences = getSharedPreferences("wallpaper", MODE_PRIVATE);
        int wallpaperId = sharedPreferences.getInt("wallpaperId", 0);
        String wallpaperUri = sharedPreferences.getString("wallpaperUri", null);

        if(wallpaperId != 0){
            binding.RelativeLayout.setBackgroundResource(wallpaperId);
        } else if (wallpaperUri != null){
            try {
                binding.RelativeLayout.setBackground(new android.graphics.drawable.BitmapDrawable(getResources(), MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(wallpaperUri))));
            } catch (IOException e) {
                e.printStackTrace();
            }
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

    private String formatLastSeen(long timestamp) {
        Calendar now = Calendar.getInstance();
        Calendar time = Calendar.getInstance();
        time.setTimeInMillis(timestamp);

        if (now.get(Calendar.YEAR) == time.get(Calendar.YEAR) && now.get(Calendar.DAY_OF_YEAR) == time.get(Calendar.DAY_OF_YEAR)) {
            return "today at " + new SimpleDateFormat("h:mm a", Locale.getDefault()).format(time.getTime());
        } else if (now.get(Calendar.YEAR) == time.get(Calendar.YEAR) && now.get(Calendar.DAY_OF_YEAR) - 1 == time.get(Calendar.DAY_OF_YEAR)) {
            return "yesterday at " + new SimpleDateFormat("h:mm a", Locale.getDefault()).format(time.getTime());
        } else {
            return new SimpleDateFormat("MMMM d, yyyy 'at' h:mm a", Locale.getDefault()).format(time.getTime());
        }
    }
}
