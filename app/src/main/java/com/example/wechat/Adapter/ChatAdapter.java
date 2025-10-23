package com.example.wechat.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
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
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {
    ArrayList<Object> chatItems;
    ArrayList<Object> chatItemsFull;
    Context context;
    String recId;
    RecyclerView recyclerView;

    private boolean isMultiSelectMode = false;
    private ArrayList<MessageModel> selectedMessages = new ArrayList<>();
    private int selectedPosition = -1;

    private static final int VIEW_TYPE_SENDER = 1;
    private static final int VIEW_TYPE_RECEIVER = 2;
    private static final int VIEW_TYPE_DATE_HEADER = 3;

    public ChatAdapter(ArrayList<Object> chatItems, Context context, String recId, RecyclerView recyclerView) {
        this.chatItems = chatItems;
        this.chatItemsFull = new ArrayList<>(chatItems);
        this.context = context;
        this.recId = recId;
        this.recyclerView = recyclerView;
    }

    public MessageModel getSelectedUser(){
        if(selectedPosition != -1 && selectedPosition < chatItems.size()){
            if(chatItems.get(selectedPosition) instanceof MessageModel){
                return (MessageModel) chatItems.get(selectedPosition);
            }
        }
        return null;
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
                if(message.getRepliedToMessage() != null){
                    senderViewHolder.replyLayout.setVisibility(View.VISIBLE);
                    FirebaseDatabase.getInstance().getReference().child("Users").child(message.getRepliedToSender()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                Users user = snapshot.getValue(Users.class);
                                senderViewHolder.repliedToSender.setText(user.getUserName());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    if(message.getRepliedToMessage().equals("Photo")){
                        senderViewHolder.repliedToMessage.setText("Photo");
                    } else {
                        senderViewHolder.repliedToMessage.setText(message.getRepliedToMessage());
                    }

                    senderViewHolder.replyLayout.setOnClickListener(v -> scrollToMessage(message.getRepliedToMessageId()));

                } else {
                    senderViewHolder.replyLayout.setVisibility(View.GONE);
                }

                if(message.getImageUrl() != null){
                    senderViewHolder.senderMsg.setVisibility(View.GONE);
                    senderViewHolder.image.setVisibility(View.VISIBLE);
                    Picasso.get().load(message.getImageUrl()).placeholder(R.drawable.placeholder).into(senderViewHolder.image);
                } else {
                    senderViewHolder.senderMsg.setText(message.getMessage());
                }
                senderViewHolder.senderTime.setText(formatTime(message.getTimeStamp()));

                if (message.isRead()) {
                    senderViewHolder.readReceipt.setImageResource(R.drawable.ic_double_tick);
                    TypedValue typedValue = new TypedValue();
                    context.getTheme().resolveAttribute(R.attr.primaryAccentColor, typedValue, true);
                    senderViewHolder.readReceipt.setColorFilter(typedValue.data, android.graphics.PorterDuff.Mode.SRC_IN);
                } else {
                    senderViewHolder.readReceipt.setImageResource(R.drawable.ic_single_tick);
                    TypedValue typedValue = new TypedValue();
                    context.getTheme().resolveAttribute(R.attr.textColorHighEmphasis, typedValue, true);
                    senderViewHolder.readReceipt.setColorFilter(typedValue.data, android.graphics.PorterDuff.Mode.SRC_IN);
                }

            } else {
                ReciverViewHolder reciverViewHolder = (ReciverViewHolder) holder;
                if(message.getRepliedToMessage() != null){
                    reciverViewHolder.replyLayout.setVisibility(View.VISIBLE);
                    FirebaseDatabase.getInstance().getReference().child("Users").child(message.getRepliedToSender()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                Users user = snapshot.getValue(Users.class);
                                reciverViewHolder.repliedToSender.setText(user.getUserName());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                     if(message.getRepliedToMessage().equals("Photo")){
                        reciverViewHolder.repliedToMessage.setText("Photo");
                    } else {
                        reciverViewHolder.repliedToMessage.setText(message.getRepliedToMessage());
                    }
                    reciverViewHolder.replyLayout.setOnClickListener(v -> scrollToMessage(message.getRepliedToMessageId()));
                } else {
                    reciverViewHolder.replyLayout.setVisibility(View.GONE);
                }

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

    private void scrollToMessage(String messageId) {
        for (int i = 0; i < chatItems.size(); i++) {
            Object item = chatItems.get(i);
            if (item instanceof MessageModel) {
                if (((MessageModel) item).getMessageId().equals(messageId)) {
                    final int position = i;
                    ((LinearLayoutManager) recyclerView.getLayoutManager()).scrollToPositionWithOffset(position, 0);
                    new Handler().postDelayed(() -> {
                        RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(position);
                        if (viewHolder != null) {
                            viewHolder.itemView.setBackgroundColor(Color.parseColor("#803F51B5"));
                            new Handler().postDelayed(() -> viewHolder.itemView.setBackgroundColor(Color.TRANSPARENT), 1000);
                        }
                    }, 300);
                    break;
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

    @Override
    public Filter getFilter() {
        return chatFilter;
    }

    private Filter chatFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Object> filteredList = new ArrayList<>();
            if(constraint == null || constraint.length() == 0){
                filteredList.addAll(chatItemsFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for(Object item : chatItemsFull){
                    if(item instanceof MessageModel){
                        if(((MessageModel) item).getMessage().toLowerCase().contains(filterPattern)){
                            filteredList.add(item);
                        }
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            chatItems.clear();
            chatItems.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

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
        TextView reciverMsg, reciverTime, senderName, repliedToSender, repliedToMessage;
        ImageView image;
        View replyLayout;

        public ReciverViewHolder(@NonNull View itemView) {
            super(itemView);
            reciverMsg = itemView.findViewById(R.id.reciverText);
            reciverTime = itemView.findViewById(R.id.reciverTime);
            senderName = itemView.findViewById(R.id.senderName);
            image = itemView.findViewById(R.id.image);
            replyLayout = itemView.findViewById(R.id.reply_layout);
            repliedToSender = itemView.findViewById(R.id.replied_to_sender);
            repliedToMessage = itemView.findViewById(R.id.replied_to_message);
        }
    }

    public class SenderViewHolder extends RecyclerView.ViewHolder {
        TextView senderMsg, senderTime, repliedToSender, repliedToMessage;
        ImageView image, readReceipt;
        View replyLayout;

        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            senderMsg = itemView.findViewById(R.id.senderText);
            senderTime = itemView.findViewById(R.id.senderTime);
            image = itemView.findViewById(R.id.image);
            replyLayout = itemView.findViewById(R.id.reply_layout);
            repliedToSender = itemView.findViewById(R.id.replied_to_sender);
            repliedToMessage = itemView.findViewById(R.id.replied_to_message);
            readReceipt = itemView.findViewById(R.id.read_receipt);
        }
    }
}
