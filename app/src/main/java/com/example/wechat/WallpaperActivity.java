package com.example.wechat;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import com.example.wechat.Adapter.WallpaperAdapter;
import com.example.wechat.databinding.ActivityWallpaperBinding;

import java.util.ArrayList;

public class WallpaperActivity extends BaseActivity {

    ActivityWallpaperBinding binding;
    WallpaperAdapter adapter;
    ArrayList<Integer> wallpaperList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWallpaperBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        for (int i = 1; i <= 17; i++) {
            int resourceId = getResources().getIdentifier("w" + i, "drawable", getPackageName());
            if (resourceId != 0) {
                wallpaperList.add(resourceId);
            }
        }

        adapter = new WallpaperAdapter(wallpaperList, this);
        binding.wallpaperRecyclerView.setAdapter(adapter);
        binding.wallpaperRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        binding.fabChooseFromLocal.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(intent, 45);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == 45){
                if(data.getData() != null){
                    Uri selectedImage = data.getData();
                    // Persist the permission to read the URI
                    getContentResolver().takePersistableUriPermission(selectedImage, Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    SharedPreferences sharedPreferences = getSharedPreferences("wallpaper", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("wallpaperUri", selectedImage.toString());
                    editor.remove("wallpaperId"); // Clear the old setting
                    editor.apply();
                    finish();
                }
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}