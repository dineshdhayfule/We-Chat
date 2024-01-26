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

public class ChatAdapter extends RecyclerView.Adapter
{
    ArrayList<MessageModel> messageModel;
    Context context;
    String recId;

    int SENDER_VIEW_TYPE =1;
    int RECEIVER_VIEW_TYPE =2;

    public ChatAdapter(ArrayList<MessageModel> messageModel, Context context)
    {
        this.messageModel = messageModel;
        this.context = context;
    }

    public ChatAdapter(ArrayList<MessageModel> messageModel, Context context, String recId) {
        this.messageModel = messageModel;
        this.context = context;
        this.recId = recId;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        if (viewType==SENDER_VIEW_TYPE)
        {
            View view = LayoutInflater.from(context).inflate(R.layout.sample_sender,parent,false);
            return new SenderViewHolder(view);
        }
        else
        {
            View view = LayoutInflater.from(context).inflate(R.layout.sample_reciver,parent,false);
            return new ReciverViewHolder(view);
        }

    }

    @Override
    public int getItemViewType(int position)
    {
        if (messageModel.get(position).getuId().equals(FirebaseAuth.getInstance().getUid())){
            return SENDER_VIEW_TYPE;
        }
        else {
            return RECEIVER_VIEW_TYPE;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position)
    {

        MessageModel messageModel1 = messageModel.get(position);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new AlertDialog.Builder(context)
                        .setMessage("Delete Message?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                String senderRoom = FirebaseAuth.getInstance().getUid() + recId ;
                                database.getReference().child("chats")
                                .child(senderRoom)
                                        .child(messageModel1.getMessageId())
                                        .setValue(null);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).show();

            }
        });

        if (holder.getClass()== SenderViewHolder.class)
        {
            ((SenderViewHolder) holder).senderMsg.setText(messageModel1.getMessage());

            Date date = new Date(messageModel1.getTimeStamp());
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("h:mm a");
            String strDate = simpleDateFormat.format(date);
            ((SenderViewHolder) holder).senderTime.setText(strDate.toString());

        }
        else
        {
            ((ReciverViewHolder) holder).reciverMsg.setText(messageModel1.getMessage());

            Date date = new Date(messageModel1.getTimeStamp());
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("h:mm a");
            String strDate = simpleDateFormat.format(date);
            ((ReciverViewHolder) holder).reciverTime.setText(strDate.toString());
        }
    }

    @Override
    public int getItemCount()
    {
        return messageModel.size();
    }

    public class ReciverViewHolder extends RecyclerView.ViewHolder
    {
    TextView reciverMsg ,reciverTime;

        public ReciverViewHolder(@NonNull View itemView)
        {
            super(itemView);
            reciverMsg = itemView.findViewById(R.id.reciverText);
            reciverTime = itemView.findViewById(R.id.reciverTime);
        }
    }

    public class SenderViewHolder extends RecyclerView.ViewHolder
    {
        TextView senderMsg ,senderTime;

        public SenderViewHolder(@NonNull View itemView)
        {
            super(itemView);
            senderMsg = itemView.findViewById(R.id.senderText);
            senderTime = itemView.findViewById(R.id.senderTime);
        }
    }
}
