package com.example.superior_intelligence;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

/**
 * A DialogFragment that provides a confirmation dialog for deleting a mood entry.
 * It notifies the attached listener when the user confirms deletion.
 */
public class DeleteMoodFragment extends DialogFragment {

    /**
     * Interface to handle the delete confirmation response.
     */
    interface DeleteDialogListener {
        /**
         * Called when the user selects an option in the delete dialog.
         * @param delete_status true if the user confirmed deletion, false otherwise.
         */
        void delete(boolean delete_status);
    }
    private DeleteDialogListener listener;

    /**
     * Attaches the fragment to the hosting activity and ensures it implements the listener.
     * @param context the context of the hosting activity.
     */
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

    /**
     * Creates the delete confirmation dialog.
     * @param savedInstanceState the previously saved state of the fragment, if any.
     * @return a new AlertDialog instance with delete confirmation options.
     */
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
