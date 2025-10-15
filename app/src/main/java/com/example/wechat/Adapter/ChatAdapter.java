package com.example.wechat.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wechat.Models.MessageModel;
import com.example.wechat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter {
    ArrayList<Object> chatItems;
    Context context;
    String recId;

    private static final int VIEW_TYPE_SENDER = 1;
    private static final int VIEW_TYPE_RECEIVER = 2;
    private static final int VIEW_TYPE_DATE_HEADER = 3;

    public ChatAdapter(ArrayList<Object> chatItems, Context context, String recId) {
        this.chatItems = chatItems;
        this.context = context;
        this.recId = recId;
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
            if (holder instanceof SenderViewHolder) {
                SenderViewHolder senderViewHolder = (SenderViewHolder) holder;
                senderViewHolder.senderMsg.setText(message.getMessage());
                senderViewHolder.senderTime.setText(formatTime(message.getTimeStamp()));
            } else {
                ReciverViewHolder reciverViewHolder = (ReciverViewHolder) holder;
                reciverViewHolder.reciverMsg.setText(message.getMessage());
                reciverViewHolder.reciverTime.setText(formatTime(message.getTimeStamp()));
                reciverViewHolder.senderName.setVisibility(View.GONE);
            }

            holder.itemView.setOnClickListener(v -> {
                new AlertDialog.Builder(context)
                        .setTitle("Delete Message")
                        .setMessage("Are you sure you want to delete this message?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            String senderRoom = FirebaseAuth.getInstance().getUid() + recId;
                            FirebaseDatabase.getInstance().getReference().child("chats")
                                    .child(senderRoom)
                                    .child(message.getMessageId()).removeValue();
                        })
                        .setNegativeButton("No", null)
                        .show();
            });
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

        public ReciverViewHolder(@NonNull View itemView) {
            super(itemView);
            reciverMsg = itemView.findViewById(R.id.reciverText);
            reciverTime = itemView.findViewById(R.id.reciverTime);
            senderName = itemView.findViewById(R.id.senderName);
        }
    }

    public class SenderViewHolder extends RecyclerView.ViewHolder {
        TextView senderMsg, senderTime;

        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            senderMsg = itemView.findViewById(R.id.senderText);
            senderTime = itemView.findViewById(R.id.senderTime);
        }
    }
}
