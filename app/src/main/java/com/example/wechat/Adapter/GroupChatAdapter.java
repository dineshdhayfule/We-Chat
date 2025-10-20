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
import com.example.wechat.Models.Users;
import com.example.wechat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class GroupChatAdapter extends RecyclerView.Adapter {
    ArrayList<Object> chatItems;
    Context context;
    HashMap<String, Integer> userColorMap = new HashMap<>();

    private boolean isMultiSelectMode = false;
    private ArrayList<MessageModel> selectedMessages = new ArrayList<>();

    private static final int VIEW_TYPE_SENDER = 1;
    private static final int VIEW_TYPE_RECEIVER = 2;
    private static final int VIEW_TYPE_DATE_HEADER = 3;

    public GroupChatAdapter(ArrayList<Object> chatItems, Context context) {
        this.chatItems = chatItems;
        this.context = context;
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
            if (((MessageModel) item).getuId() != null && ((MessageModel) item).getuId().equals(FirebaseAuth.getInstance().getUid())) {
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

                if (position > 0 && chatItems.get(position - 1) instanceof MessageModel) {
                    MessageModel previousMessage = (MessageModel) chatItems.get(position - 1);
                    if (previousMessage.getuId().equals(message.getuId())) {
                        reciverViewHolder.senderName.setVisibility(View.GONE);
                    } else {
                        reciverViewHolder.senderName.setVisibility(View.VISIBLE);
                    }
                } else {
                    reciverViewHolder.senderName.setVisibility(View.VISIBLE);
                }

                if (message.getuId() != null) {
                    FirebaseDatabase.getInstance().getReference().child("Users").child(message.getuId())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        Users user = snapshot.getValue(Users.class);
                                        if (user != null) {
                                            reciverViewHolder.senderName.setText(user.getUserName());
                                            if (user.getUserId() != null) {
                                                reciverViewHolder.senderName.setTextColor(getUserColor(user.getUserId()));
                                            } else {
                                                reciverViewHolder.senderName.setTextColor(Color.GRAY);
                                            }
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                }
                            });
                }
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

    private int getUserColor(String userId) {
        if(userId == null) return Color.GRAY;
        if (userColorMap.containsKey(userId)) {
            return userColorMap.get(userId);
        }
        Random random = new Random(userId.hashCode());
        int color = Color.rgb(random.nextInt(200), random.nextInt(200), random.nextInt(200));
        userColorMap.put(userId, color);
        return color;
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
