package com.example.wechat.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wechat.Models.Users;
import com.example.wechat.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class GroupMembersAdapter extends RecyclerView.Adapter<GroupMembersAdapter.ViewHolder> {

    ArrayList<Users> list;
    Context context;
    HashMap<String, Object> memberRoles;
    private OnItemLongClickListener longClickListener;

    public interface OnItemLongClickListener {
        void onItemLongClick(Users user);
    }

    public GroupMembersAdapter(ArrayList<Users> list, Context context, OnItemLongClickListener longClickListener) {
        this.list = list;
        this.context = context;
        this.longClickListener = longClickListener;
    }

    public void setMemberRoles(HashMap<String, Object> memberRoles) {
        this.memberRoles = memberRoles;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.sample_show_group_member, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Users user = list.get(position);
        Picasso.get().load(user.getProfilePic()).placeholder(R.drawable.avatar3).into(holder.image);
        holder.userName.setText(user.getUserName());

        if (memberRoles != null) {
            Object roleObj = memberRoles.get(user.getUserId());
            if (roleObj instanceof String) {
                String role = (String) roleObj;
                if ("creator".equals(role)) {
                    holder.adminTag.setText("Creator");
                    holder.adminTag.setVisibility(View.VISIBLE);
                } else if ("admin".equals(role)) {
                    holder.adminTag.setText("Admin");
                    holder.adminTag.setVisibility(View.VISIBLE);
                } else {
                    holder.adminTag.setVisibility(View.GONE);
                }
            } else {
                holder.adminTag.setVisibility(View.GONE);
            }
        }

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onItemLongClick(user);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView userName, adminTag;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.profile_image);
            userName = itemView.findViewById(R.id.userNameList);
            adminTag = itemView.findViewById(R.id.admin_tag);
        }
    }
}
