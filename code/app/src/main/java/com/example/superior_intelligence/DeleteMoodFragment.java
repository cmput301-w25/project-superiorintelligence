/**
 * This fragment is a dialog pop up to confirm if user wants to delete the event
 */
package com.example.superior_intelligence;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Movie;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class DeleteMoodFragment extends DialogFragment {

    interface DeleteDialogListener{
        void delete(boolean delete_status);
    }
    private DeleteDialogListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof DeleteDialogListener){
            listener = (DeleteDialogListener) context;
        }
        else {
            throw new RuntimeException("Implement listener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = getLayoutInflater().inflate(R.layout.delete_mood_fragment, null);

        AlertDialog dialog = new AlertDialog.Builder(requireContext(), R.style.DialogTheme)
                .setView(view)
                .setNegativeButton("CANCEL", null)
                .setPositiveButton("DELETE", (dialogInterface, which) -> {
                    listener.delete(true);
                })
                .create();
        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
        });
        return dialog;
    }
}
