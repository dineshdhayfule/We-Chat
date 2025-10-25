package com.example.wechat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.example.wechat.Adapter.FragmentsAdapter;
import com.example.wechat.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;

public class MainActivity extends BaseActivity {

    ActivityMainBinding binding;
    FirebaseAuth mAuth;
    FirebaseDatabase database;

    private final ActivityResultLauncher<String> requestPermissionLauncher = 
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
        } else {
            // Inform user that your app will not show notifications.
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        binding.viewPager.setAdapter(new FragmentsAdapter(getSupportFragmentManager()));

        askNotificationPermission();
        getFCMToken();

        binding.fabNewChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, NewChatActivity.class));
            }
        });

//        binding.searchIcon.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(MainActivity.this, "Search not implemented", Toast.LENGTH_SHORT).show();
//            }
//        });

        binding.groupIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, GroupsActivity.class));
            }
        });

        binding.settingsIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUserStatus(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        updateUserStatus(false);
    }

    private void updateUserStatus(boolean isOnline) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("online", isOnline);
        if(!isOnline){
            map.put("lastSeen", System.currentTimeMillis());
        }
        database.getReference().child("Users").child(mAuth.getUid()).updateChildren(map);
    }

    private void askNotificationPermission() {
        // This is only necessary for API level 33 and higher.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK", directly request the permission.
                //       If the user selects "No thanks", allow the user to continue without notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    void getFCMToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String token = task.getResult();
                Log.i("My token: ", token);
                database.getReference().child("Users")
                        .child((mAuth.getUid()))
                        .child("fcmToken").setValue(token);
            }
        });
    }
}
