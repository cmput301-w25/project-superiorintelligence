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

import java.util.concurrent.atomic.AtomicBoolean;

public class PostStatusFragment extends DialogFragment {
    interface PostStatusDialogListener{
        public void public_status(boolean post_status);
    }
    private PostStatusDialogListener listener;
    private Event existingEvent;

    public PostStatusFragment(Event existingEvent){
        this.existingEvent = existingEvent;
    }
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


