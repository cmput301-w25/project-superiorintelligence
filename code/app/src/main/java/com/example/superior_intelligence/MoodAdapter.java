package com.example.superior_intelligence;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;

import java.util.List;
import java.util.Locale;
public class MoodAdapter extends RecyclerView.Adapter<MoodAdapter.MoodViewHolder> {
    private final List<Mood> moodList;
    public MoodAdapter(List<Mood> moodList) {
        this.moodList = moodList;}
    @NonNull
    @Override
    public MoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mood, parent, false);
        return new MoodViewHolder(view);}
    @Override
    public void onBindViewHolder(@NonNull MoodViewHolder holder, int position)

    {
        Mood mood = moodList.get(position);
        holder.moodText.setText(mood.getMoodExplaination());

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy - HH:mm", Locale.getDefault());
        holder.dateText.setText(sdf.format(mood.getTimestamp()));}
    @Override
    public int getItemCount() {
        return moodList.size();}
    static class MoodViewHolder extends RecyclerView.ViewHolder {
        TextView moodText, dateText;
        public MoodViewHolder(@NonNull View itemView)

        {
            super(itemView);
            moodText = itemView.findViewById(R.id.moodText);
            dateText = itemView.findViewById(R.id.dateText);}
    }
}
