package com.example.wechat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.wechat.Models.Users;
import com.example.wechat.databinding.ActivitySettingsBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class SettingsActivity extends BaseActivity {

    ActivitySettingsBinding binding;
    FirebaseAuth mAuth;
    FirebaseDatabase database;
    FirebaseStorage storage;
    GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        binding.backArrow.setOnClickListener(v -> finish());

        binding.themes.setVisibility(View.GONE); // Hide the theme switcher

        database.getReference().child("Users").child(mAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Users users = snapshot.getValue(Users.class);
                    Picasso.get().load(users.getProfilePic()).placeholder(R.drawable.avatar3).into(binding.profileImage);
                    binding.txtStatus.setText(users.getStatus());
                    binding.txtUserName.setText(users.getUserName());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        binding.plus.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, 25);
        });

        binding.saveButton.setOnClickListener(view -> {
            if (!binding.txtUserName.getText().toString().equals("") && !binding.txtStatus.getText().toString().equals("")) {
                String status = binding.txtStatus.getText().toString();
                String userName = binding.txtUserName.getText().toString();
                HashMap<String, Object> obj = new HashMap<>();
                obj.put("userName", userName);
                obj.put("status", status);
                database.getReference().child("Users").child(mAuth.getUid()).updateChildren(obj);
                Toast.makeText(SettingsActivity.this, "Profile Updated", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(SettingsActivity.this, "Enter UserName and status", Toast.LENGTH_SHORT).show();
            }
        });

        binding.blockedUsers.setOnClickListener(v -> startActivity(new Intent(SettingsActivity.this, BlockedUsersActivity.class)));

        binding.logOut.setOnClickListener(v -> {
            updateUserStatus(false);
            mAuth.signOut();
            mGoogleSignInClient.signOut();
            FirebaseMessaging.getInstance().deleteToken();
            Intent intent = new Intent(SettingsActivity.this, SignlnActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void updateUserStatus(boolean isOnline) {
        if (mAuth.getUid() != null) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("online", isOnline);
            if (!isOnline) {
                map.put("lastSeen", System.currentTimeMillis());
            }
            database.getReference().child("Users").child(mAuth.getUid()).updateChildren(map);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 25) {
            if (data != null && data.getData() != null) {
                Uri uri = data.getData();
                binding.profileImage.setImageURI(uri);
                final StorageReference reference = storage.getReference().child("profilePic").child(mAuth.getUid());
                reference.putFile(uri).addOnSuccessListener(taskSnapshot -> reference.getDownloadUrl().addOnSuccessListener(downloadUri -> database.getReference().child("Users").child(mAuth.getUid()).child("profilePic").setValue(downloadUri.toString())));
            }
        }
    }
}
