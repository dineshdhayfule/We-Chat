package com.example.wechat;

import android.os.Bundle;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class ImageViewerActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        ImageView imageView = findViewById(R.id.image_viewer);
        String imageUrl = getIntent().getStringExtra("imageUrl");

        if (imageUrl != null) {
            Picasso.get().load(imageUrl).into(imageView);
        }
    }
}