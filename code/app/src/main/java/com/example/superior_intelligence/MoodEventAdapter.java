package com.example.superior_intelligence;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;

import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;


import androidx.recyclerview.widget.RecyclerView;
import com.example.superior_intelligence.R;
import com.example.superior_intelligence.MoodEvent;

import java.util.List;
/**
 * MoodEventAdapter
 *
 * <p>
 *     A RecyclerView.Adapter that binds MoodEvent data to the item_mood_event layout.
 * </p>
 *
 * <p>References:
 * <a href="https://developer.android.com/guide/topics/ui/layout/recyclerview">RecyclerView Docs</a>
 * </p>
 */



public class MoodEventAdapter extends RecyclerView.Adapter<MoodEventAdapter.MoodViewHolder> {
    private final List<MoodEvent> moodEvents;
    /**
     * Constructor
     * @param moodEvents A list of MoodEvent objects to display
     */
    public MoodEventAdapter(List<MoodEvent> moodEvents) {
        this.moodEvents = moodEvents;
    }

    @NonNull
    @Override
    public MoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mood_event, parent, false);
        return new MoodViewHolder(view);}

    @Override
    public void onBindViewHolder(@NonNull MoodViewHolder holder, int position)
    {
        MoodEvent event = moodEvents.get(position);
        String moodTitle = (event.getMood() != null ? event.getMood() : "Unknown Mood");
        String postTitle = (event.getTitle() != null ? event.getTitle() : "");
        holder.titleText.setText(moodTitle + (postTitle.isEmpty() ? "" : " - " + postTitle));
        holder.dateText.setText("Date: " + (event.getDate() != null ? event.getDate() : "N/A"));
        holder.situationText.setText("Situation: " + (event.getSituation() != null ? event.getSituation() : "N/A"));
        holder.userText.setText("User: " + (event.getPostUser() != null ? event.getPostUser() : "N/A"));
        if (event.getOverlayColor() != null && !event.getOverlayColor().isEmpty()) {
            try {
                int color = Color.parseColor(event.getOverlayColor());
                holder.itemView.setBackgroundColor(color);
            } catch (IllegalArgumentException e) {
                // If overlayColor is invalid, fallback to default background
                holder.itemView.setBackgroundResource(R.color.backgroundGreen);
            }
        } else {
            // Default background if overlayColor is not provided
            holder.itemView.setBackgroundResource(R.color.backgroundGreen);
        }
    }

    @Override
    public int getItemCount() {
        return moodEvents.size();
    }
    /**
     * Clears the adapter's data and adds a fresh list of mood events.
     * Useful for reloading data from Firestore.
     */
    public void updateData(List<MoodEvent> newEvents) {
        moodEvents.clear();
        moodEvents.addAll(newEvents);
        notifyDataSetChanged();}
    /**
     * MoodViewHolder
     *
     * <p>
     *     Holds references to the views in each item layout (item_mood_event.xml).
     * </p>
     */
    public static class MoodViewHolder extends RecyclerView.ViewHolder {

        TextView titleText, dateText, situationText, userText;

        public MoodViewHolder(@NonNull View itemView)

        {
            super(itemView);
            titleText = itemView.findViewById(R.id.item_mood_title);
            dateText = itemView.findViewById(R.id.item_mood_date);
            situationText = itemView.findViewById(R.id.item_mood_situation);
            userText = itemView.findViewById(R.id.item_mood_user);
        }

    }}