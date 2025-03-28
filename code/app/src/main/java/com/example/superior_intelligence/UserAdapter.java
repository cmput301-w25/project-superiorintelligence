/**
 * Adapter class for displaying a list of users in a RecyclerView.
 * This class binds data from a list of {@link HelperClass} objects to the UI.
 */

package com.example.superior_intelligence;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<HelperClass> userList;
    private OnUserClickListener listener;
    /**
     * Constructs a new {@code UserAdapter} with the specified list of users.
     *
     * @param userList the list of {@link HelperClass} objects to be displayed.
     */
    public UserAdapter(List<HelperClass> userList, OnUserClickListener listener) {
        this.userList = userList;
        this.listener = listener;
    }

    /**
     * Called when the RecyclerView needs a new {@link UserViewHolder} to represent an item.
     *
     * @param parent   the parent ViewGroup.
     * @param viewType the view type of the new View.
     * @return a new {@code UserViewHolder} that holds a View of the given view type.
     */
    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_item, parent, false);
        return new UserViewHolder(view);
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     *
     * @param holder   the {@code UserViewHolder} to bind the data to.
     * @param position the position of the data item within the dataset.
     */
    @Override
    public void onBindViewHolder(UserViewHolder holder, int position) {
        HelperClass user = userList.get(position);
        holder.nameTextView.setText(user.getName());
        holder.usernameTextView.setText(user.getUsername());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUserClick(user);
            }
        });
    }

    /**
     * Returns the total number of items in the dataset.
     *
     * @return the size of the dataset.
     */
    @Override
    public int getItemCount() {
        return userList.size();
    }

    /**
     * ViewHolder class for holding user item views.
     */
    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, usernameTextView;

        /**
         * Constructs a new {@code UserViewHolder} with the specified view.
         *
         * @param itemView the view representing a single item in the list.
         */
        public UserViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.user_name);
            usernameTextView = itemView.findViewById(R.id.user_username);
        }
    }

    public interface OnUserClickListener {
        void onUserClick(HelperClass user);
    }
}