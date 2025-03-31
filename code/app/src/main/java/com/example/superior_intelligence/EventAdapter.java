package com.example.superior_intelligence;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    private List<Event> currentList = new ArrayList<>();
    private Context context;

    /**
     * Interface for handling event detail view clicks.
     */
    public interface ViewDetailsListener {
        void onViewDetails(Event event);
    }

    private ViewDetailsListener viewDetailsListener;

    /**
     * Constructor for the EventAdapter.
     * @param context the application context
     * @param listener the listener for event detail clicks
     */
    public EventAdapter(@NonNull Context context, ViewDetailsListener listener) {
        this.context = context;
        this.viewDetailsListener = listener;
    }

    public EventAdapter(ArrayList<Event> eventList) {

    }

    /**
     * Sets a new list of events and updates the RecyclerView.
     * @param newList the new list of events
     */
    public void setEvents(List<Event> newList) {
        this.currentList = newList; // Directly set the list
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

        // Set title and date
        holder.eventTitle.setText(event.getTitle());
        holder.eventDate.setText(event.getDate());

        // Set overlay color dynamically
        String colorStr = event.getOverlayColor();
        if (colorStr != null && !colorStr.isEmpty()) {
            holder.eventOverlay.setCardBackgroundColor(Color.parseColor(colorStr));
        } else {
            holder.eventOverlay.setCardBackgroundColor(Color.parseColor("#99FFFFFF")); // Default fallback color
        }

        // reset both views to ensure they don’t carry previous states
        holder.privateIcon.setVisibility(View.GONE);
        holder.publicIcon.setVisibility(View.GONE);

        System.out.println("Event Status: " + event.isPublic_status());

        // Set public/private status
        if (!event.isPublic_status()) {
            holder.privateIcon.setVisibility(View.VISIBLE);
            System.out.println("Private icon should be visible");
        } else {
            holder.publicIcon.setVisibility(View.VISIBLE);
            System.out.println("Public icon should be visible");
        }

        // Set emoji if present
        if (event.getEmojiResource() != 0) {
            holder.eventEmoticon.setVisibility(View.VISIBLE);
            holder.eventEmoticon.setImageResource(event.getEmojiResource());
        } else {
            holder.eventEmoticon.setVisibility(View.GONE); // Hide emoji if none
        }

        String imageDocId = event.getImageUrl();
        if (imageDocId != null && !imageDocId.isEmpty()) {
            Photobase photobase = new Photobase(context);
            photobase.loadImage(imageDocId, new Photobase.ImageLoadCallback() {
                @Override
                public void onImageLoaded(Bitmap bitmap) {
                    holder.eventImage.setVisibility(View.VISIBLE);
                    holder.eventImage.setImageBitmap(bitmap);
                }

                @Override
                public void onImageLoadFailed(String error) {
                    Log.e("EventAdapter", "Failed to load image: " + error);
                    holder.eventImage.setVisibility(View.VISIBLE);
                    holder.eventImage.setImageResource(R.color.secondaryGreen);

                }
            });
        } else {
            holder.eventImage.setVisibility(View.VISIBLE);
            holder.eventImage.setImageResource(R.color.secondaryGreen);
        }

        // Handle click to open details
        holder.itemView.setOnClickListener(v -> {
            if (viewDetailsListener != null) {
                viewDetailsListener.onViewDetails(event);
            }
        });
    }

    @Override
    public int getItemCount() {
        return currentList.size();
    }

    /**
     * ViewHolder class representing a single event item in the RecyclerView.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // Declare all required views here
        TextView eventTitle, eventDate;
        CardView eventOverlay;
        ImageView eventEmoticon, eventImage, commentIcon, publicIcon, privateIcon;

        /**
         * Constructs the ViewHolder and initializes its views.
         * @param itemView the view representing a single item
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize all views here
            eventTitle = itemView.findViewById(R.id.event_title);
            eventDate = itemView.findViewById(R.id.event_date);
            eventOverlay = itemView.findViewById(R.id.event_overlay);
            eventEmoticon = itemView.findViewById(R.id.event_emoticon);
            eventImage = itemView.findViewById(R.id.event_image);
            commentIcon = itemView.findViewById(R.id.comment_icon);
            publicIcon = itemView.findViewById(R.id.public_status_detail);
            privateIcon = itemView.findViewById(R.id.private_status_detail);
        }
    }

}
