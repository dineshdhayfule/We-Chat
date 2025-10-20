package com.example.wechat.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wechat.ImageViewerActivity;
import com.example.wechat.Models.MessageModel;
import com.example.wechat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter {
    ArrayList<Object> chatItems;
    Context context;
    String recId;

    private boolean isMultiSelectMode = false;
    private ArrayList<MessageModel> selectedMessages = new ArrayList<>();

    private static final int VIEW_TYPE_SENDER = 1;
    private static final int VIEW_TYPE_RECEIVER = 2;
    private static final int VIEW_TYPE_DATE_HEADER = 3;

    public ChatAdapter(ArrayList<Object> chatItems, Context context, String recId) {
        this.chatItems = chatItems;
        this.context = context;
        this.recId = recId;
    }

    public void setMultiSelectMode(boolean isMultiSelectMode) {
        this.isMultiSelectMode = isMultiSelectMode;
        if (!isMultiSelectMode) {
            selectedMessages.clear();
        }
        notifyDataSetChanged();
    }

    public boolean isMultiSelectMode() {
        return isMultiSelectMode;
    }

    public List<MessageModel> getSelectedMessages() {
        return selectedMessages;
    }

    @Override
    public int getItemViewType(int position) {
        Object item = chatItems.get(position);
        if (item instanceof MessageModel) {
            if (((MessageModel) item).getuId().equals(FirebaseAuth.getInstance().getUid())) {
                return VIEW_TYPE_SENDER;
            } else {
                return VIEW_TYPE_RECEIVER;
            }
        } else {
            return VIEW_TYPE_DATE_HEADER;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENDER) {
            View view = LayoutInflater.from(context).inflate(R.layout.sample_sender, parent, false);
            return new SenderViewHolder(view);
        } else if (viewType == VIEW_TYPE_RECEIVER) {
            View view = LayoutInflater.from(context).inflate(R.layout.sample_reciver, parent, false);
            return new ReciverViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_date_header, parent, false);
            return new DateHeaderViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object item = chatItems.get(position);

        if (holder instanceof DateHeaderViewHolder) {
            String date = (String) item;
            ((DateHeaderViewHolder) holder).dateTextView.setText(date);
        } else {
            MessageModel message = (MessageModel) item;

            holder.itemView.setBackgroundColor(selectedMessages.contains(message) ? Color.LTGRAY : Color.TRANSPARENT);

            holder.itemView.setOnLongClickListener(v -> {
                if (!isMultiSelectMode) {
                    setMultiSelectMode(true);
                }
                toggleSelection(message, holder.itemView);
                return true;
            });

            holder.itemView.setOnClickListener(v -> {
                if (isMultiSelectMode) {
                    toggleSelection(message, holder.itemView);
                } else {
                    if (message.getImageUrl() != null) {
                        Intent intent = new Intent(context, ImageViewerActivity.class);
                        intent.putExtra("imageUrl", message.getImageUrl());
                        context.startActivity(intent);
                    }
                }
            });

            if (holder instanceof SenderViewHolder) {
                SenderViewHolder senderViewHolder = (SenderViewHolder) holder;
                if(message.getImageUrl() != null){
                    senderViewHolder.senderMsg.setVisibility(View.GONE);
                    senderViewHolder.image.setVisibility(View.VISIBLE);
                    Picasso.get().load(message.getImageUrl()).placeholder(R.drawable.placeholder).into(senderViewHolder.image);
                } else {
                    senderViewHolder.senderMsg.setText(message.getMessage());
                }
                senderViewHolder.senderTime.setText(formatTime(message.getTimeStamp()));
            } else {
                ReciverViewHolder reciverViewHolder = (ReciverViewHolder) holder;
                if(message.getImageUrl() != null){
                    reciverViewHolder.reciverMsg.setVisibility(View.GONE);
                    reciverViewHolder.image.setVisibility(View.VISIBLE);
                    Picasso.get().load(message.getImageUrl()).placeholder(R.drawable.placeholder).into(reciverViewHolder.image);
                } else {
                    reciverViewHolder.reciverMsg.setText(message.getMessage());
                }
                reciverViewHolder.reciverTime.setText(formatTime(message.getTimeStamp()));
                reciverViewHolder.senderName.setVisibility(View.GONE);
            }
        }
    }

    private void toggleSelection(MessageModel message, View view) {
        if (selectedMessages.contains(message)) {
            selectedMessages.remove(message);
            view.setBackgroundColor(Color.TRANSPARENT);
        } else {
            selectedMessages.add(message);
            view.setBackgroundColor(Color.LTGRAY);
        }
    }

    @Override
    public int getItemCount() {
        return chatItems.size();
    }

    private String formatTime(long timeInMillis) {
        SimpleDateFormat formatter = new SimpleDateFormat("h:mm a", Locale.getDefault());
        return formatter.format(new Date(timeInMillis));
    }

    public static class DateHeaderViewHolder extends RecyclerView.ViewHolder {
        TextView dateTextView;
        public DateHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
        }
    }

    public class ReciverViewHolder extends RecyclerView.ViewHolder {
        TextView reciverMsg, reciverTime, senderName;
        ImageView image;

        public ReciverViewHolder(@NonNull View itemView) {
            super(itemView);
            reciverMsg = itemView.findViewById(R.id.reciverText);
            reciverTime = itemView.findViewById(R.id.reciverTime);
            senderName = itemView.findViewById(R.id.senderName);
            image = itemView.findViewById(R.id.image);
        }
    }

    public class SenderViewHolder extends RecyclerView.ViewHolder {
        TextView senderMsg, senderTime;
        ImageView image;

        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            senderMsg = itemView.findViewById(R.id.senderText);
            senderTime = itemView.findViewById(R.id.senderTime);
            image = itemView.findViewById(R.id.image);
        }
    }
}
