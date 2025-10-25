package com.example.wechat;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

import com.example.wechat.databinding.ActivityPersonalizedThemeBinding;

public class PersonalizedThemeActivity extends BaseActivity {

    ActivityPersonalizedThemeBinding binding;
    SharedPreferences themePrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPersonalizedThemeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        themePrefs = getSharedPreferences("ThemePrefs", MODE_PRIVATE);

        loadCurrentPersonalizedColors();

        binding.mainBackgroundColor.setOnClickListener(v -> showColorPicker("mainBackgroundColor"));
        binding.surfaceColor.setOnClickListener(v -> showColorPicker("surfaceColor"));
        binding.primaryAccentColor.setOnClickListener(v -> showColorPicker("primaryAccentColor"));

        binding.saveButton.setOnClickListener(v -> {
            SharedPreferences.Editor editor = themePrefs.edit();
            editor.putString("selected_theme", "personalized");
            editor.apply();
            recreate();
        });
    }

    private void loadCurrentPersonalizedColors() {
        int mainBgColor = themePrefs.getInt("mainBackgroundColor", Color.parseColor("#121212"));
        int surfaceColor = themePrefs.getInt("surfaceColor", Color.parseColor("#212121"));
        int primaryAccentColor = themePrefs.getInt("primaryAccentColor", Color.parseColor("#03DAC5"));

        binding.mainBackgroundColorPreview.setBackgroundColor(mainBgColor);
        binding.surfaceColorPreview.setBackgroundColor(surfaceColor);
        binding.primaryAccentColorPreview.setBackgroundColor(primaryAccentColor);
    }

    private void showColorPicker(String colorKey) {
        final int[] colors = {
                Color.parseColor("#121212"), Color.parseColor("#212121"), Color.parseColor("#303030"),
                Color.parseColor("#004D40"), Color.parseColor("#00695C"), Color.parseColor("#00796B"),
                Color.parseColor("#03DAC5"), Color.parseColor("#64FFDA"), Color.parseColor("#A7FFEB"),
                Color.parseColor("#1A237E"), Color.parseColor("#283593"), Color.parseColor("#303F9F"),
                Color.parseColor("#8C9EFF"), Color.parseColor("#B3E5FC"), Color.parseColor("#E1F5FE")
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick a color");

        View colorPickerView = getLayoutInflater().inflate(R.layout.color_picker_dialog, null);
        builder.setView(colorPickerView);

        AlertDialog dialog = builder.create();

        for (int i = 0; i < colors.length; i++) {
            View colorView = new View(this);
            colorView.setBackgroundColor(colors[i]);
            int colorIndex = i;
            colorView.setOnClickListener(v -> {
                SharedPreferences.Editor editor = themePrefs.edit();
                editor.putInt(colorKey, colors[colorIndex]);
                editor.apply();
                dialog.dismiss();
                loadCurrentPersonalizedColors(); // Refresh previews
            });
            ((android.widget.GridLayout) colorPickerView.findViewById(R.id.color_grid)).addView(colorView, 100, 100);
        }

        dialog.show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}
