package com.example.wechat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.example.wechat.Models.Users;
import com.example.wechat.databinding.ActivitySettingsBinding;
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

import java.util.HashMap;


public class SettingsActivity extends AppCompatActivity {

    ActivitySettingsBinding binding;
    FirebaseAuth mAuth;
    FirebaseDatabase database;
    FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding. getRoot());
        getSupportActionBar().hide();

        mAuth = FirebaseAuth. getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();


        binding.backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingsActivity.this,MainActivity.class);
                startActivity(intent);
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