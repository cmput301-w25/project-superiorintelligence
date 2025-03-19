package com.example.superior_intelligence;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
    private final List<String> requests;
    private final boolean isPendingRequest;

    public NotificationAdapter(List<String> requests, boolean isPendingRequest) {
        this.requests = requests;
        this.isPendingRequest = isPendingRequest;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String username = requests.get(position);

        if (isPendingRequest) {
            // Show message for pending requests
            holder.requestText.setText("You requested to follow " + username + ". Pending approval.");
            holder.acceptButton.setVisibility(View.GONE); // Hide buttons
            holder.denyButton.setVisibility(View.GONE);
        } else {
            // Show message for incoming requests
            holder.requestText.setText(username + " wants to follow you.");
            holder.acceptButton.setVisibility(View.VISIBLE);
            holder.denyButton.setVisibility(View.VISIBLE);


            // Accept request
            holder.acceptButton.setOnClickListener(v -> {
                Userbase.getInstance().acceptFollowRequest(username, User.getInstance().getUsername(), success -> {
                    if (success) {
                        // Remove request from Firestore
                        Userbase.getInstance().removeFollowRequest(username, User.getInstance().getUsername());
                        requests.remove(position);
                        notifyDataSetChanged();
                    }
                });
            });

            // Deny request
            holder.denyButton.setOnClickListener(v -> {
                Userbase.getInstance().removeFollowRequest(username, User.getInstance().getUsername());
                requests.remove(position);
                notifyDataSetChanged();
            });
        }
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView requestText;
        ImageButton acceptButton, denyButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            requestText = itemView.findViewById(R.id.notification_text);
            acceptButton = itemView.findViewById(R.id.accept_request);
            denyButton = itemView.findViewById(R.id.deny_request);
        }
    }
}
