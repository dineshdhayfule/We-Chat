package com.example.wechat.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wechat.R;

import java.util.ArrayList;

public class WallpaperAdapter extends RecyclerView.Adapter<WallpaperAdapter.ViewHolder> {

    ArrayList<Integer> wallpaperList;
    Context context;

    public WallpaperAdapter(ArrayList<Integer> wallpaperList, Context context) {
        this.wallpaperList = wallpaperList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_wallpaper, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.wallpaper.setImageResource(wallpaperList.get(position));

        holder.itemView.setOnClickListener(v -> {
            SharedPreferences sharedPreferences = context.getSharedPreferences("wallpaper", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("wallpaperId", wallpaperList.get(position));
            editor.remove("wallpaperUri"); // Clear the old setting
            editor.apply();
            ((Activity) context).finish();
        });
    }

    @Override
    public int getItemCount() {
        return wallpaperList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView wallpaper;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            wallpaper = itemView.findViewById(R.id.wallpaper);
        }
    }
}