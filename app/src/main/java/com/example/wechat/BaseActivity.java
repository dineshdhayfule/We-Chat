package com.example.wechat;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // The theme is now handled automatically by the system
        // using the values/styles.xml and values-night/styles.xml files.
        // No manual theme setting is needed here.
        super.onCreate(savedInstanceState);
    }
}
