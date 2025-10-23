package com.example.wechat;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyTheme();
    }

    protected void applyTheme() {
        SharedPreferences prefs = getSharedPreferences("ThemePrefs", MODE_PRIVATE);
        String theme = prefs.getString("selected_theme", "modern_blue");

        if ("sleek_teal".equals(theme)) {
            setTheme(R.style.AppTheme_SleekTeal);
        } else {
            setTheme(R.style.AppTheme_ModernBlue);
        }
    }
}