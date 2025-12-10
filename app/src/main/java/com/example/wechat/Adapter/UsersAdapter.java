package com.example.wechat.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wechat.ChatDetailActivity;
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

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> implements Filterable {

    ArrayList<Users> list;
    ArrayList<Users> listFull;
    Context context;
    boolean isMultiSelectMode;
    private final ArrayList<Users> selectedUsers = new ArrayList<>(); // For multi-selection

    public UsersAdapter(ArrayList<Users> list, Context context, boolean isMultiSelectMode) {
        this.list = list;
        this.listFull = new ArrayList<>(list);
        this.context = context;
        this.isMultiSelectMode = isMultiSelectMode;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.sample_show_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Users user = list.get(position);
        Picasso.get().load(user.getProfilePic()).placeholder(R.drawable.avatar3).into(holder.image);
        holder.userName.setText(user.getUserName());

        if (isMultiSelectMode) {
            // --- MULTI-SELECT MODE (NEW GROUP) ---
            holder.lastMessage.setVisibility(View.GONE);
            holder.timestamp.setVisibility(View.GONE);
            holder.checkbox.setVisibility(View.VISIBLE);
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
            holder.checkbox.setChecked(selectedUsers.contains(user));
        } else if (context instanceof com.example.wechat.NewChatActivity) {
            // --- NEW CHAT MODE ---
            holder.lastMessage.setVisibility(View.GONE);
            holder.timestamp.setVisibility(View.GONE);
            holder.checkbox.setVisibility(View.GONE);
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        } else {
            // --- NORMAL CHAT LIST MODE ---
            holder.lastMessage.setVisibility(View.VISIBLE);
            holder.timestamp.setVisibility(View.VISIBLE);
            holder.checkbox.setVisibility(View.GONE);
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);

            // Set last message and timestamp
            FirebaseDatabase.getInstance().getReference().child("chats")
                    .child(FirebaseAuth.getInstance().getUid() + user.getUserId())
                    .orderByChild("timeStamp")
                    .limitToLast(1)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            if (snapshot.hasChildren()) {
                                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                    String lastMsg = snapshot1.child("message").getValue(String.class);
                                    holder.lastMessage.setText(lastMsg != null && lastMsg.length() > 30 ? lastMsg.substring(0, 30) + "..." : lastMsg);
                                    Long time = snapshot1.child("timeStamp").getValue(Long.class);
                                    if (time != null) {
                                        holder.timestamp.setText(new SimpleDateFormat("h:mm a", Locale.US).format(new Date(time)));
                                    } else {
                                        holder.timestamp.setText("");
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {}
                    });
        }

        holder.itemView.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition == RecyclerView.NO_POSITION) {
                return;
            }
            Users clickedUser = list.get(adapterPosition);
            if (isMultiSelectMode) {
                if (selectedUsers.contains(clickedUser)) {
                    selectedUsers.remove(clickedUser);
                } else {
                    selectedUsers.add(clickedUser);
                }
                notifyItemChanged(adapterPosition);
            } else {
                // Open chat detail
                Intent intent = new Intent(context, ChatDetailActivity.class);
                intent.putExtra("userId", clickedUser.getUserId());
                intent.putExtra("profilePic", clickedUser.getProfilePic());
                intent.putExtra("userName", clickedUser.getUserName());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public Filter getFilter() {
        return userFilter;
    }

    private final Filter userFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Users> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(listFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (Users item : listFull) {
                    if (item.getUserName() != null && item.getUserName().toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            list.clear();
            list.addAll((List<Users>) results.values);
            notifyDataSetChanged();
        }
    };

    public ArrayList<Users> getSelectedUsers() {
        return selectedUsers;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView userName, lastMessage, timestamp;
        CheckBox checkbox;

        public ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.profile_image);
            userName = itemView.findViewById(R.id.userNameList);
            lastMessage = itemView.findViewById(R.id.lastMessage);
            timestamp = itemView.findViewById(R.id.timestamp);
            checkbox = itemView.findViewById(R.id.checkbox);
        }
    }
}