package com.example.superior_intelligence;

import android.content.Context;
import android.graphics.Movie;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class MoodArrayAdapter extends ArrayAdapter<Mood> {
    private ArrayList<Mood> moods;
    private Context context;

    public MoodArrayAdapter(Context context, ArrayList<Mood> moods){
        super(context, 0, moods);
        this.moods = moods;
        this.context = context;
    }

    @NonNull
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
        View view = convertView;
        if (view == null){
            view = LayoutInflater.from(context).inflate(R.layout.list_event, parent, false);
        }

        Mood mood = moods.get(position);

        TextView moodTitle = view.findViewById(R.id.event_title);
        //TextView eventDate = view.findViewById(R.id.event_date);

        moodTitle.setText(mood.getMoodTitle());
        //eventDate.setText(mood.getDate);
        return view;
    }
}
