package com.example.superior_intelligence;
import com.example.superior_intelligence.Photobase;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.content.Context;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying a list of events in a RecyclerView.
 * Handles event display, selection, and follow/unfollow logic.
 */
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {

    private final List<Event> exploreEvents;
    private final List<Event> followedEvents;
    private final List<Event> myPostsEvents;
    private List<Event> currentList;

    private final Handler handler = new Handler(Looper.getMainLooper());

    /**
     * Interface for handling follow/unfollow actions.
     */
    public interface OnFollowToggleListener {
        void onFollowToggled(Event event, boolean isFollowed);
    }
    private final OnFollowToggleListener followToggleListener;
    private final Context context;
    private final Photobase photobase;
    private final Userbase userbase;

    /**
     * Constructs an `EventAdapter` with a list of events and a listener for follow toggles.
     *
     * @param exploreEvents The list of events to be displayed.
     * @param followedEvents List of followed events.
     * @param myPostsEvents List of followed events.
     * @param followToggleListener The listener handling follow/unfollow actions.
     */
    public EventAdapter(
            @NonNull Context context,
            List<Event> exploreEvents,
            List<Event> followedEvents,
            List<Event> myPostsEvents,
            OnFollowToggleListener followToggleListener
    ) {
        this.context = context;
        this.exploreEvents = (exploreEvents != null) ? exploreEvents : new ArrayList<>();
        this.followedEvents = (followedEvents != null) ? followedEvents : new ArrayList<>();
        this.myPostsEvents = (myPostsEvents != null) ? myPostsEvents : new ArrayList<>();

        this.currentList = this.exploreEvents; // default tab
        this.followToggleListener = followToggleListener;
        this.photobase = new Photobase(context);
        this.userbase = Userbase.getInstance();
    }

    /**
     * Updates the adapter's dataset and refreshes the list.
     *
     * @param newList The updated list of events.
     */
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
        String loggedInUsername = User.getInstance().getUsername();

        // Set title & date
        holder.eventTitle.setText(event.getTitle());
        holder.eventDate.setText(event.getDate());

        // Set overlay color
        String colorStr = (event.getOverlayColor() != null && !event.getOverlayColor().isEmpty())
                ? event.getOverlayColor() : "#99FFFFFF";
        holder.eventOverlay.setCardBackgroundColor(Color.parseColor(colorStr));
        holder.eventOverlay.getBackground().setAlpha(200);

        // Load image using photobase if available
        if (event.getImageUrl() != null && !event.getImageUrl().isEmpty()) {
            photobase.loadImage(event.getImageUrl(), new Photobase.ImageLoadCallback() {
                @Override
                public void onImageLoaded(Bitmap bitmap) {
                    holder.eventImage.setImageBitmap(bitmap);
                }

                @Override
                public void onImageLoadFailed(String error) {
                    holder.eventImage.setBackgroundResource(R.color.secondaryGreen);
                }
            });
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
            intent.putExtra("date", event.getDate()); 
            intent.putExtra("user", event.getUser());
            v.getContext().startActivity(intent);
        });

        // Check follow status and update UI
        userbase.checkFollowStatus(loggedInUsername, event.getUser(), isFollowing -> {
            holder.followCheckbox.setChecked(isFollowing);
            holder.followText.setText(isFollowing ? "Following" : "Follow");
        });

        // Follow/unfollow logic
        holder.followCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                userbase.followUser(loggedInUsername, event.getUser(), success -> {
                    if (success) holder.followText.setText("Following");
                });
            } else {
                userbase.unfollowUser(loggedInUsername, event.getUser(), success -> {
                    if (success) holder.followText.setText("Follow");
                });
            }
        });

    }

    /**
     * Returns the total number of items in the list.
     * @return The size of the event list.
     */
    @Override
    public int getItemCount() {
        return currentList.size();
    }

    /**
     * ViewHolder class representing an individual item in the RecyclerView.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView eventTitle, eventDate, followText;
        CardView eventOverlay;
        ImageView eventImage, eventEmoticon;
        CheckBox followCheckbox;

        /**
         * Constructor for initializing the ViewHolder with views.
         * @param itemView The view representing an individual list item.
         */
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
