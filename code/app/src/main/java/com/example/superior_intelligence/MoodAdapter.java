package com.example.superior_intelligence;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;

import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;


import androidx.recyclerview.widget.RecyclerView;
import com.example.superior_intelligence.R;
import com.example.superior_intelligence.Mood;

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
public class MoodAdapter extends RecyclerView.Adapter<MoodAdapter.MoodViewHolder> {
    private final List<Mood> moodList;

    /**
     * Constructor.
     * @param moodList A list of Mood objects to display.
     */
    public MoodAdapter(List<Mood> moodList) {
        this.moodList = moodList;
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
        Mood mood = moodList.get(position);
        String moodTitle = (mood.getMood() != null ? mood.getMood() : "Unknown Mood");
        String postTitle = (mood.getMoodTitle() != null ? mood.getMoodTitle() : "");
        holder.titleText.setText(moodTitle + (postTitle.isEmpty() ? "" : " - " + postTitle));
        holder.dateText.setText("Date: " + (mood.getDate() != null ? mood.getDate() : "N/A"));
        holder.situationText.setText("Situation: " + (mood.getSituation() != null ? mood.getSituation() : "N/A"));
        holder.userText.setText("User: " + (mood.getPostUser() != null ? mood.getPostUser() : "N/A"));
        if (mood.getOverlayColor() != null && !mood.getOverlayColor().isEmpty()) {
            try {
                int color = Color.parseColor(mood.getOverlayColor().toString());
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
        return moodList.size();
    }
    /**
     * Clears the adapter's data and adds a fresh list of mood events.
     * Useful for reloading data from Firestore.
     */
    public void updateData(List<Mood> newMoods) {
        moodList.clear();
        moodList.addAll(newMoods);
        notifyDataSetChanged();
    }

    /**
     * MoodViewHolder holds the view references for each item in item_mood_event.xml.
     */
    public static class MoodViewHolder extends RecyclerView.ViewHolder {
        TextView titleText, dateText, situationText, userText;

        public MoodViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.item_mood_title);
            dateText = itemView.findViewById(R.id.item_mood_date);
            situationText = itemView.findViewById(R.id.item_mood_situation);
            userText = itemView.findViewById(R.id.item_mood_user);
        }
    }
}