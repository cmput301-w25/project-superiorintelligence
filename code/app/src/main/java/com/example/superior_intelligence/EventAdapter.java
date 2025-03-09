package com.example.superior_intelligence;

import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
        currentList = newList != null ? newList : new ArrayList<>();
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

        // Format timestamp as a readable date & time
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy - HH:mm", Locale.getDefault());
        String formattedDate = sdf.format(event.getDate()); // Convert Date to readable format


        // Set title & date
        holder.eventTitle.setText(event.getTitle());
        holder.eventDate.setText(formattedDate);

        // Set overlay color
        String colorStr = (event.getOverlayColor() != null && !event.getOverlayColor().isEmpty())
                ? event.getOverlayColor() : "#99FFFFFF";
        holder.eventOverlay.setCardBackgroundColor(Color.parseColor(colorStr));
        holder.eventOverlay.getBackground().setAlpha(200);

        // Set image or default background
        if (event.getImageUrl() != null && !event.getImageUrl().isEmpty()) {
            holder.eventImage.setImageURI(Uri.parse(event.getImageUrl()));
        } else {
            holder.eventImage.setBackgroundResource(R.color.secondaryGreen);
        }

        // Only show emoji if user selected it
        if (event.getEmojiResource() != 0) {
            holder.eventEmoticon.setVisibility(View.VISIBLE);
            holder.eventEmoticon.setImageResource(event.getEmojiResource());
        } else {
            holder.eventEmoticon.setVisibility(View.GONE);
        }

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
