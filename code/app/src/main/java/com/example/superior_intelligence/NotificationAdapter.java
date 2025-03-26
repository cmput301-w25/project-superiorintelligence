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

    /**
     * Constructs a NotificationAdapter.
     * @param requests          the list of usernames associated with follow requests
     * @param isPendingRequest  true if the adapter is showing pending requests sent by the user,
     *                          false if it's showing incoming requests received by the user
     */
    public NotificationAdapter(List<String> requests, boolean isPendingRequest) {
        this.requests = requests;
        this.isPendingRequest = isPendingRequest;
    }

    /**
     * Inflates the notification item layout and creates a ViewHolder.
     * @param parent   the parent ViewGroup
     * @param viewType the view type of the new view
     * @return a new ViewHolder instance for the notification item
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_item, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Binds the data for a single notification to the ViewHolder.
     * Handles UI logic depending on whether the request is incoming or pending.
     * @param holder   the ViewHolder to update
     * @param position the position of the item within the dataset
     */
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
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition == RecyclerView.NO_POSITION) return;
                String userToAccept = requests.get(adapterPosition);
                Userbase.getInstance().acceptFollowRequest(userToAccept, User.getInstance().getUsername(), success -> {
                    if (success) {
                        // Remove request from Firestore
                        Userbase.getInstance().removeFollowRequest(username, User.getInstance().getUsername());
                        requests.remove(adapterPosition);
                        notifyItemRemoved(adapterPosition);
                    }
                });
            });

            // Deny request
            holder.denyButton.setOnClickListener(v -> {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition == RecyclerView.NO_POSITION) return;
                String userToDecline = requests.get(adapterPosition);

                Userbase.getInstance().removeFollowRequest(userToDecline, User.getInstance().getUsername());
                requests.remove(adapterPosition);
                notifyItemRemoved(adapterPosition);
            });
        }
    }

    /**
     * Returns the number of items (notifications) in the dataset.
     * @return the size of the requests list
     */
    @Override
    public int getItemCount() {
        return requests.size();
    }

    /**
     * ViewHolder class for holding the views in a notification item.
     * Contains the request message and optional accept/deny buttons.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView requestText;
        ImageButton acceptButton, denyButton;

        /**
         * Makes a ViewHolder and binds the views from the layout.
         * @param itemView the notification item view
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            requestText = itemView.findViewById(R.id.notification_text);
            acceptButton = itemView.findViewById(R.id.accept_request);
            denyButton = itemView.findViewById(R.id.deny_request);
        }
    }
}
