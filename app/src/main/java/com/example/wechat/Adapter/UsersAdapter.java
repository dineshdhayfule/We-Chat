package com.example.wechat.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
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

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> implements Filterable {

    ArrayList<Users> list;
    ArrayList<Users> listFull;
    Context context;
    boolean isMultiSelectMode;
    private int selectedPosition = -1;
    private ArrayList<Users> selectedUsers = new ArrayList<>();

    public UsersAdapter(ArrayList<Users> list, Context context, boolean isMultiSelectMode) {
        this.list = list;
        this.listFull = new ArrayList<>(list);
        this.context = context;
        this.isMultiSelectMode = isMultiSelectMode;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.sample_show_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Users user = list.get(position);
        Picasso.get().load(user.getProfilePic()).placeholder(R.drawable.avatar3).into(holder.image);
        holder.userName.setText(user.getUserName());

        // Set last message
        FirebaseDatabase.getInstance().getReference().child("chats")
                .child(FirebaseAuth.getInstance().getUid() + user.getUserId())
                .orderByChild("timeStamp")
                .limitToLast(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.hasChildren()) {
                            for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                String lastMsg = snapshot1.child("message").getValue(String.class);
                                if (lastMsg != null) {
                                    if (lastMsg.length() > 30) {
                                        holder.lastMessage.setText(lastMsg.substring(0, 30) + "...");
                                    } else {
                                        holder.lastMessage.setText(lastMsg);
                                    }
                                }

                                Long time = snapshot1.child("timeStamp").getValue(Long.class);
                                if (time != null) {
                                    SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm a");
                                    holder.timestamp.setText(dateFormat.format(new Date(time)));
                                } else {
                                    holder.timestamp.setText("");
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


        if (isMultiSelectMode) {
            holder.checkbox.setVisibility(View.VISIBLE);
            holder.checkbox.setChecked(selectedUsers.contains(user));
        } else {
            holder.checkbox.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (isMultiSelectMode) {
                if (selectedUsers.contains(user)) {
                    selectedUsers.remove(user);
                } else {
                    selectedUsers.add(user);
                }
                notifyItemChanged(position);
            } else {
                Intent intent = new Intent(context, ChatDetailActivity.class);
                intent.putExtra("userId", user.getUserId());
                intent.putExtra("profilePic", user.getProfilePic());
                intent.putExtra("userName", user.getUserName());
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

    private Filter userFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Users> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(listFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (Users item : listFull) {
                    if (item.getUserName().toLowerCase().contains(filterPattern)) {
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
            list.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

    public Users getSelectedUser() {
        if (selectedPosition != -1) {
            return list.get(selectedPosition);
        }
        return null;
    }

    public ArrayList<Users> getSelectedUsers() {
        return selectedUsers;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView userName, lastMessage, timestamp;
        CheckBox checkbox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.profile_image);
            userName = itemView.findViewById(R.id.userNameList);
            lastMessage = itemView.findViewById(R.id.lastMessage);
            timestamp = itemView.findViewById(R.id.timestamp);
            checkbox = itemView.findViewById(R.id.checkbox);
        }
    }
}
