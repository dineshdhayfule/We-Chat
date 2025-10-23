package com.example.wechat;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.example.wechat.Models.Users;
import com.example.wechat.databinding.ActivitySettingsBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
        setContentView(binding. getRoot());

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        binding.backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        binding.themes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showThemeDialog();
            }
        });

        binding.changeWallpaper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingsActivity.this, WallpaperActivity.class));
            }
        });

        database.getReference().child("Users")
                .child(FirebaseAuth.getInstance().getUid())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Users users = snapshot.getValue(Users.class);
                                Picasso.get().load(users.getProfilePic()).into(binding.profileImage);

                                binding.txtStatus.setText(users.getStatus());
                                binding.txtUserName.setText(users.getUserName());
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
        binding.plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(getApplicationContext(), "Opening Gallery for profile pic", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,25);
            }
        });
        binding.saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              if (!binding.txtUserName.getText().toString().equals("") && !binding.txtStatus.getText().toString().equals("") )
              {
                  String status= binding.txtStatus.getText().toString();
                  String userName= binding.txtUserName.getText().toString();

                  HashMap <String,Object> obj = new HashMap<>();
                  obj.put("userName",userName);
                  obj.put("status",status);

                  database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid()).updateChildren(obj);
                  Toast.makeText(SettingsActivity.this, "Profile Updated", Toast.LENGTH_LONG).show();

              }
              
              else {
                  Toast.makeText(SettingsActivity.this, "Enter UserName and status", Toast.LENGTH_SHORT).show();
              }
            }
        });

        binding.blockedUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingsActivity.this, BlockedUsersActivity.class));
            }
        });

        binding.logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserStatus(false);
                mAuth.signOut();
                mGoogleSignInClient.signOut();
                FirebaseMessaging.getInstance().deleteToken();

                Intent intent = new Intent(SettingsActivity.this, SignlnActivity.class);
                startActivity(intent);
                finishAffinity();
            }
        });
    }
    
    private void updateUserStatus(boolean isOnline) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("online", isOnline);
        if(!isOnline){
            map.put("lastSeen", System.currentTimeMillis());
        }
        database.getReference().child("Users").child(mAuth.getUid()).updateChildren(map);
    }

    private void showThemeDialog() {
        final String[] themes = {"Modern & Focused Blue", "Sleek & Vibrant Teal"};
        SharedPreferences prefs = getSharedPreferences("ThemePrefs", MODE_PRIVATE);
        String currentTheme = prefs.getString("selected_theme", "modern_blue");
        int checkedItem = "sleek_teal".equals(currentTheme) ? 1 : 0;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Theme");
        builder.setSingleChoiceItems(themes, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selectedTheme = "modern_blue";
                if (which == 1) {
                    selectedTheme = "sleek_teal";
                }

                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("selected_theme", selectedTheme);
                editor.apply();

                dialog.dismiss();
                recreate();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
        if (data.getData()!=null);
        {
            Uri uri  = data.getData();
            binding.profileImage.setImageURI(uri);

            final StorageReference reference = storage.getReference().child("profilePic")
                    .child(FirebaseAuth.getInstance().getUid());

            reference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
               reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                   @Override
                   public void onSuccess(Uri uri) {
                       database.getReference().child("Users")
                               .child(mAuth.getUid())
                               .child("profilePic").setValue(uri.toString());
                   }
               });
                }
            });
        }
    }
}