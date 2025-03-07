package com.example.superior_intelligence;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Movie;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

/**
 * This MoodEventTitleFragment class is a dialog for user to input the mood event title
 */
public class MoodEventTitleFragment extends DialogFragment {

    interface MoodEventDialogListener{
        void addMoodTitle(String title, Boolean addStatus);
    }

    private MoodEventDialogListener listener;

    public static MoodEventTitleFragment newInstance(Mood mood){
        Bundle args = new Bundle();
        args.putSerializable("Mood Event", mood);

        MoodEventTitleFragment fragment = new MoodEventTitleFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MoodEventDialogListener){
            listener = (MoodEventDialogListener) context;
        }
        else {
            throw new RuntimeException("Implement listener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = getLayoutInflater().inflate(R.layout.mood_title_fragment, null);
        EditText editTextTitle = view.findViewById(R.id.mood_title);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder
                .setView(view)
                .setTitle("Give your mood event a name!")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Continue", (dialog, which) -> {
                    String title = editTextTitle.getText().toString();
                    listener.addMoodTitle(title, true);
                });

        return builder.create();
    }
}
