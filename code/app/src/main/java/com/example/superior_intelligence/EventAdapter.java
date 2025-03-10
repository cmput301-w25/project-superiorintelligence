package com.example.superior_intelligence;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {

    private final List<Event> exploreEvents;
    private final List<Event> followedEvents;
    private final List<Event> myPostsEvents;
    private List<Event> currentList;

    private final Handler handler = new Handler(Looper.getMainLooper());

    public interface OnFollowToggleListener {
        void onFollowToggled(Event event, boolean isFollowed);
    }
    private final OnFollowToggleListener followToggleListener;

    public EventAdapter(
            List<Event> exploreEvents,
            List<Event> followedEvents,
            List<Event> myPostsEvents,
            OnFollowToggleListener followToggleListener
    ) {
        this.exploreEvents = (exploreEvents != null) ? exploreEvents : new ArrayList<>();
        this.followedEvents = (followedEvents != null) ? followedEvents : new ArrayList<>();
        this.myPostsEvents = (myPostsEvents != null) ? myPostsEvents : new ArrayList<>();

        this.currentList = this.exploreEvents; // default tab
        this.followToggleListener = followToggleListener;
    }

    public void setEvents(List<Event> newList) {
        // Ensure only MyPosts appear in MyPosts tab
        if (currentList == myPostsEvents) {
            currentList = new ArrayList<>();
            for (Event event : newList) {
                if (event.isMyPost()) {
                    currentList.add(event);
                }
            }
        } else {
            currentList = newList;
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_event, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = currentList.get(position);

        // Set title & date
        holder.eventTitle.setText(event.getTitle());
        holder.eventDate.setText(event.getDate());

        // Set overlay color
        String colorStr = (event.getOverlayColor() != null && !event.getOverlayColor().isEmpty())
                ? event.getOverlayColor() : "#99FFFFFF";
        holder.eventOverlay.setCardBackgroundColor(Color.parseColor(colorStr));
        holder.eventOverlay.getBackground().setAlpha(200);

        // Load image from Firestore if exists
        if (event.getImageUrl() != null && !event.getImageUrl().isEmpty()) {
            fetchImageFromFirestore(event.getImageUrl(), new ImageLoadCallback() {
                @Override
                public void onImageLoaded(Bitmap bitmap) {
                    holder.eventImage.setImageBitmap(bitmap);
                }

                @Override
                public void onImageLoadFailed(String error) {
                    Log.e("EventAdapter", "Failed to load image: " + error);
                    holder.eventImage.setBackgroundResource(R.color.secondaryGreen); // Default background
                }
            });
        } else {
            holder.eventImage.setBackgroundResource(R.color.secondaryGreen); // Default if no image
        }

        // Only show emoji if user selected it
        if (event.getEmojiResource() != 0) {
            holder.eventEmoticon.setVisibility(View.VISIBLE);
            holder.eventEmoticon.setImageResource(event.getEmojiResource());
        } else {
            holder.eventEmoticon.setVisibility(View.GONE);
        }

        // Click listener to open EventDetailsActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), EventDetailsActivity.class);
            intent.putExtra("title", event.getTitle());
            intent.putExtra("mood", event.getMood());
            intent.putExtra("reason", event.getMoodExplanation());
            intent.putExtra("situation", event.getSituation());
            intent.putExtra("imageUrl", event.getImageUrl());
            intent.putExtra("overlayColor", event.getOverlayColor());
            intent.putExtra("emojiResource", event.getEmojiResource());
            v.getContext().startActivity(intent);
        });

        // Completely hide follow options for MyPosts
        if (event.isMyPost()) {
            holder.followText.setVisibility(View.GONE);
            holder.followCheckbox.setVisibility(View.GONE);
        } else {
            holder.followText.setVisibility(View.VISIBLE);
            holder.followCheckbox.setVisibility(View.VISIBLE);

            // Ensure follow text & checkbox reflect actual state
            holder.followText.setText(event.isFollowed() ? "Following" : "Follow");
            holder.followCheckbox.setOnCheckedChangeListener(null); // Remove listener before updating state
            holder.followCheckbox.setChecked(event.isFollowed());

            // Follow/unfollow logic
            holder.followCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                event.setFollowed(isChecked);
                holder.followText.setText(isChecked ? "Following" : "Follow");

                // Move event between Explore & Followed lists
                if (isChecked) {
                    if (!followedEvents.contains(event)) {
                        followedEvents.add(event);
                        exploreEvents.remove(event);
                    }
                } else {
                    if (!exploreEvents.contains(event)) {
                        exploreEvents.add(event);
                        followedEvents.remove(event);
                    }
                }

                // Notify HomeActivity of follow status change
                if (followToggleListener != null) {
                    followToggleListener.onFollowToggled(event, isChecked);
                }

                // Ensure RecyclerView refreshes properly
                handler.post(() -> {
                    if (currentList == exploreEvents) {
                        setEvents(exploreEvents);
                    } else if (currentList == followedEvents) {
                        setEvents(followedEvents);
                    } else {
                        setEvents(myPostsEvents);
                    }
                });
            });
        }
    }

    // Fetch image from Firestore using document ID
    private void fetchImageFromFirestore(String documentId, ImageLoadCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("images").document(documentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String base64Image = documentSnapshot.getString("imgData");
                        if (base64Image != null && !base64Image.isEmpty()) {
                            Bitmap bitmap = base64ToBitmap(base64Image);
                            callback.onImageLoaded(bitmap); // Return the bitmap
                        } else {
                            callback.onImageLoadFailed("No image data found");
                        }
                    } else {
                        callback.onImageLoadFailed("Document does not exist");
                    }
                })
                .addOnFailureListener(e -> callback.onImageLoadFailed("Failed to retrieve image: " + e.getMessage()));
    }

    // Convert Base64 string to Bitmap
    private Bitmap base64ToBitmap(String base64Str) {
        try {
            byte[] decodedBytes = Base64.decode(base64Str, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (Exception e) {
            Log.e("EventAdapter", "Error decoding Base64", e);
            return null;
        }
    }

    // Callback interface for image loading
    public interface ImageLoadCallback {
        void onImageLoaded(Bitmap bitmap);
        void onImageLoadFailed(String error);
    }

    @Override
    public int getItemCount() {
        return currentList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView eventTitle, eventDate, followText;
        CardView eventOverlay;
        ImageView eventImage, eventEmoticon;
        CheckBox followCheckbox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            eventTitle = itemView.findViewById(R.id.event_title);
            eventDate = itemView.findViewById(R.id.event_date);
            eventOverlay = itemView.findViewById(R.id.event_overlay);
            eventImage = itemView.findViewById(R.id.event_image);
            eventEmoticon = itemView.findViewById(R.id.event_emoticon);
            followText = itemView.findViewById(R.id.follow_text);
            followCheckbox = itemView.findViewById(R.id.follow_checkbox);
        }
    }
}
