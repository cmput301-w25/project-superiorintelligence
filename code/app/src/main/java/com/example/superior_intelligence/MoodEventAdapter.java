package com.example.superior_intelligence;
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
        this.moodEvents = moodEvents;}

    public MoodEventAdapter(List<com.example.superior_intelligence.MoodEvent> moodEventsList, List<MoodEvent> moodEvents) {
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

        holder.titleText.setText(event.getMood() + " - " + event.getTitle());
        holder.dateText.setText("Date: " + event.getDate());
        holder.situationText.setText("Situation: " + event.getSituation());
        holder.userText.setText("User: " + event.getPostUser());}
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