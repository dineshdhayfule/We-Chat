package com.example.wechat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.wechat.Adapter.FragmentsAdapter;
import com.example.wechat.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    FirebaseAuth mAuth;
    FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding. getRoot());

        mAuth = FirebaseAuth. getInstance();
        database = FirebaseDatabase.getInstance();

        binding.viewPager.setAdapter(new FragmentsAdapter(getSupportFragmentManager()));
        binding.tabLayout.setupWithViewPager(binding.viewPager);

        getFCMToken();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.settings :
                Intent intent = new Intent(MainActivity.this,SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.groupChat :
            case R.id.groupChat1 :
                Intent intent1 = new Intent(MainActivity.this,GroupChatActivity.class);
                startActivity(intent1);
                break;
            case R.id.logOut :
                FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            mAuth.signOut();
                            Intent intent2 = new Intent(MainActivity.this,SignlnActivity.class);
                            startActivity(intent2);
                            Toast.makeText(getApplicationContext(), "logOut", Toast.LENGTH_SHORT).show();

                        }
                    }
                });
                break;
        }
        return super.onOptionsItemSelected(item);

    }
    void getFCMToken(){
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                String token = task.getResult();
//                FirebaseUtil.currentUserDetails().update("fcmToken",token);
                Log.i("My token: ",token);
//                FirebaseUtil.
                database.getReference().child("Users")
                        .child((mAuth.getUid()))
                        .child("fcmToken").setValue(token);
            }
        });
    }
}
