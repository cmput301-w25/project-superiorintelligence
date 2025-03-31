package com.example.superior_intelligence;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

/**
 * Dialog fragment for selecting post visibility (public/private).
 * Displays a checkbox for public/private selection and notifies the host activity
 * of the user's choice. Supports both new posts and editing existing posts.
 */

public class PostStatusFragment extends DialogFragment {

    /**
     * Callback interface for post status selection events.
     */
    interface PostStatusDialogListener{
        /**
         * Called when user selects post visibility.
         * @param post_status true for public, false for private
         */
        void public_status(boolean post_status);
    }
    private PostStatusDialogListener listener;
    private Event existingEvent;

    /**
     * Creates a new PostStatusFragment instance.
     * @param existingEvent The event being edited, or null for new posts
     */
    public PostStatusFragment(Event existingEvent){
        this.existingEvent = existingEvent;
    }

    /**
     * Attaches the fragment to its host activity.
     * @param context The host activity context
     * @throws RuntimeException if host doesn't implement PostStatusDialogListener
     */
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof PostStatusDialogListener){
            listener = (PostStatusDialogListener) context;
        }
        else {
            throw new RuntimeException("Implement listener");
        }
    }

    /**
     * Creates and configures the post status selection dialog.
     * @param savedInstanceState Saved instance state or null
     * @return Configured AlertDialog for post status selection
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = getLayoutInflater().inflate(R.layout.post_status_fragment, null);
        CheckBox public_checkbox = view.findViewById(R.id.public_checkbox);
        AlertDialog dialog;
        if (existingEvent != null && existingEvent.isPublic_status()){
            public_checkbox.setChecked(true);
            dialog = new AlertDialog.Builder(requireContext(), R.style.DialogTheme)
                    .setView(view)
                    .setPositiveButton("CONFIRM", (dialogInterface, which) -> {
                        if (public_checkbox.isChecked()){
                            listener.public_status(true);
                        } else {
                            listener.public_status(false);
                        }
                    })
                    .create();
        } else {
            dialog = new AlertDialog.Builder(requireContext(), R.style.DialogTheme)
                    .setView(view)
                    .setPositiveButton("POST", (dialogInterface, which) -> {
                        if (public_checkbox.isChecked()) {
                            listener.public_status(true);
                        } else {
                            listener.public_status(false);
                        }
                    })
                    .create();
        }

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
        });
        return dialog;

    }
}


