package com.example.superior_intelligence;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter class for displaying a list of comments in a RecyclerView.
 * Handles the creation and binding of ViewHolder instances to display comment data.
 */
public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentViewHolder> {

    private final List<Comment> comments;

    /**
     * Constructs a specified list of comments.
     * If the provided list is null, it initializes with an empty list.
     * @param comments the list of comments to display
     */
    public CommentsAdapter(List<Comment> comments) {
        this.comments = (comments != null) ? comments : new ArrayList<>();
    }

    /**
     * Creates the layout for a comment item and returns a new ViewHolder instance.
     * @param parent   the parent ViewGroup
     * @param viewType the view type of the new view
     * @return a new CommentViewHolder that holds the comment item view
     */
    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_item, parent, false);
        return new CommentViewHolder(view);
    }

    /**
     * Binds the comment data to the views held by the ViewHolder.
     * @param holder   the ViewHolder that should be updated
     * @param position the position of the comment in the dataset
     */
    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = comments.get(position);
        holder.commentUser.setText(comment.getUsername());
        holder.commentDate.setText(comment.getTime());
        holder.commentText.setText(comment.getText());
    }

    /**
     * Returns the total number of comments in the dataset.
     * @return the number of comments
     */
    @Override
    public int getItemCount() {
        return comments.size();
    }

    /**
     * ViewHolder class representing a single comment item within the RecyclerView.
     * Holds references to the username, date, and text views.
     */
    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView commentText, commentUser, commentDate;

        /**
         * Constructs a ViewHolder for a comment item.
         * Initializes the TextViews for username, date, and comment text.
         * @param itemView the view representing the comment item
         */
        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            commentUser = itemView.findViewById(R.id.comment_user);
            commentDate = itemView.findViewById(R.id.comment_date);
            commentText = itemView.findViewById(R.id.comment_text);
        }
    }
}
