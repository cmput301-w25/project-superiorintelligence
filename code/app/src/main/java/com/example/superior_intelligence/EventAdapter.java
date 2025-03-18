package com.example.superior_intelligence;
import com.example.superior_intelligence.Photobase;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.content.Context;
import android.os.Looper;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying a list of events in a RecyclerView.
 * Handles event display, selection, and follow/unfollow logic.
 */
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {

    private List<Event> currentList = new ArrayList<>();
    private final Context context;

    public EventAdapter(@NonNull Context context) {
        this.context = context;
    }

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

        // Set emoji if present
        if (event.getEmojiResource() != 0) {
            holder.eventEmoticon.setVisibility(View.VISIBLE);
            holder.eventEmoticon.setImageResource(event.getEmojiResource());
        } else {
            holder.eventEmoticon.setVisibility(View.GONE); // Hide emoji if none
        }

        // Handle click to open details
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, EventDetailsActivity.class);
            intent.putExtra("event", event); // Pass entire event object
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return currentList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // Declare all required views here
        TextView eventTitle, eventDate;
        CardView eventOverlay;
        ImageView eventEmoticon, eventImage, commentIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize all views here
            eventTitle = itemView.findViewById(R.id.event_title);
            eventDate = itemView.findViewById(R.id.event_date);
            eventOverlay = itemView.findViewById(R.id.event_overlay);
            eventEmoticon = itemView.findViewById(R.id.event_emoticon);
            eventImage = itemView.findViewById(R.id.event_image);
            commentIcon = itemView.findViewById(R.id.comment_icon);
        }
    }

}
