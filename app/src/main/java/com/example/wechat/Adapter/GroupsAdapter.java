package com.example.wechat.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wechat.GroupChatActivity;
import com.example.wechat.Models.Group;
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

public class GroupsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    ArrayList<Object> list;
    Context context;

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_INVITE = 1;
    private static final int VIEW_TYPE_GROUP = 2;

    public GroupsAdapter(ArrayList<Object> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @Override
    public int getItemViewType(int position) {
        Object item = list.get(position);
        if (item instanceof String) {
            return VIEW_TYPE_HEADER;
        } else {
            Group group = (Group) item;
            if (group.getInvitedMembers() != null && group.getInvitedMembers().containsKey(FirebaseAuth.getInstance().getUid())) {
                return VIEW_TYPE_INVITE;
            } else {
                return VIEW_TYPE_GROUP;
            }
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_HEADER) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_group_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.sample_show_group, parent, false);
            return new GroupViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object item = list.get(position);
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).headerTitle.setText((String) item);
        } else {
            GroupViewHolder groupViewHolder = (GroupViewHolder) holder;
            Group group = (Group) item;

            if (group.getGroupImage() != null && !group.getGroupImage().isEmpty()) {
                Picasso.get().load(group.getGroupImage()).placeholder(R.drawable.ic_group).into(groupViewHolder.image);
            } else {
                groupViewHolder.image.setImageResource(R.drawable.ic_group);
            }

            groupViewHolder.groupName.setText(group.getGroupName());

            // Set last message
            final String chatNode = "general".equals(group.getGroupId()) ? "Group Chat" : "group_messages/" + group.getGroupId();
            FirebaseDatabase.getInstance().getReference().child(chatNode)
                    .orderByChild("timeStamp")
                    .limitToLast(1)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.hasChildren()) {
                                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                    String lastMsg = snapshot1.child("message").getValue(String.class);
                                    if(lastMsg != null){
                                        if(lastMsg.length() > 30){
                                            groupViewHolder.lastMessage.setText(lastMsg.substring(0, 30) + "...");
                                        } else {
                                            groupViewHolder.lastMessage.setText(lastMsg);
                                        }
                                    }
                                    Long time = snapshot1.child("timeStamp").getValue(Long.class);
                                    if (time != null) {
                                        SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm a");
                                        groupViewHolder.timestamp.setText(dateFormat.format(new Date(time)));
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

            if (getItemViewType(position) == VIEW_TYPE_INVITE) {
                groupViewHolder.joinButton.setVisibility(View.VISIBLE);
                groupViewHolder.itemView.setClickable(false);
            } else {
                groupViewHolder.joinButton.setVisibility(View.GONE);
                groupViewHolder.itemView.setClickable(true);
            }

            groupViewHolder.joinButton.setOnClickListener(v -> {
                String currentUserId = FirebaseAuth.getInstance().getUid();
                FirebaseDatabase.getInstance().getReference().child("groups").child(group.getGroupId()).child("invitedMembers").child(currentUserId).removeValue();
                FirebaseDatabase.getInstance().getReference().child("groups").child(group.getGroupId()).child("members").child(currentUserId).setValue("member");
            });

            groupViewHolder.itemView.setOnClickListener(v -> {
                if (getItemViewType(position) != VIEW_TYPE_INVITE) {
                    Intent intent = new Intent(context, GroupChatActivity.class);
                    intent.putExtra("groupId", group.getGroupId());
                    intent.putExtra("groupName", group.getGroupName());
                    context.startActivity(intent);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView headerTitle;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            headerTitle = itemView.findViewById(R.id.header_title);
        }
    }

    public static class GroupViewHolder extends RecyclerView.ViewHolder {
        ImageView image, joinButton;
        TextView groupName, lastMessage, timestamp;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.profile_image);
            groupName = itemView.findViewById(R.id.groupNameList);
            lastMessage = itemView.findViewById(R.id.lastMessage);
            timestamp = itemView.findViewById(R.id.timestamp);
            joinButton = itemView.findViewById(R.id.join_button);
        }
    }
}
