package com.thesis.dermocura.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.thesis.dermocura.R;
import com.thesis.dermocura.models.ModelChat;

import java.util.List;


public class AdapterChat extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ModelChat> messageList;
    private static final int VIEW_TYPE_OUTGOING = 1;
    private static final int VIEW_TYPE_INCOMING = 2;

    public AdapterChat(List<ModelChat> messageList) {
        this.messageList = messageList;
    }

    @Override
    public int getItemViewType(int position) {
        return messageList.get(position).isOutgoing() ? VIEW_TYPE_OUTGOING : VIEW_TYPE_INCOMING;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_OUTGOING) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_outgoing_message, parent, false);
            return new OutgoingMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_incoming_message, parent, false);
            return new IncomingMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ModelChat message = messageList.get(position);
        if (holder instanceof OutgoingMessageViewHolder) {
            ((OutgoingMessageViewHolder) holder).bind(message);
        } else {
            ((IncomingMessageViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class OutgoingMessageViewHolder extends RecyclerView.ViewHolder {
        TextView textViewMessage, textViewTime;

        public OutgoingMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewMessage = itemView.findViewById(R.id.textViewMessage);
            textViewTime = itemView.findViewById(R.id.textViewTime);
        }

        void bind(ModelChat message) {
            textViewMessage.setText(message.getText());
            textViewTime.setText(message.getTime());
        }
    }

    static class IncomingMessageViewHolder extends RecyclerView.ViewHolder {
        TextView textViewMessage, textViewTime;

        public IncomingMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewMessage = itemView.findViewById(R.id.textViewMessage);
            textViewTime = itemView.findViewById(R.id.textViewTime);
        }

        void bind(ModelChat message) {
            textViewMessage.setText(message.getText());
            textViewTime.setText(message.getTime());
        }
    }
}
