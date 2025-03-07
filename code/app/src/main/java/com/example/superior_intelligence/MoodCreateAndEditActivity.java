package com.example.superior_intelligence;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class MoodCreateAndEditActivity extends AppCompatActivity {
    private CardView emotionSelector, socialSituationSelector;
    private Spinner emotionDropdown, emotionDropdown2;
    private TextView emotionText, socialSituationText;
    private boolean isEmotionSelected = false;
    private boolean isSocialSituationSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.creating_new_mood_event);

        // Initialize UI components for first spinner (Emotional State)
        emotionSelector = findViewById(R.id.cardView);
        emotionDropdown = findViewById(R.id.emotion_dropdown);
        emotionText = findViewById(R.id.emotional_state_text);

        // Initialize UI components for second spinner (Social Situation)
        socialSituationSelector = findViewById(R.id.social_situation_banner);
        emotionDropdown2 = findViewById(R.id.emotion_dropdown2);
        socialSituationText = findViewById(R.id.social_situation_text);

        // Load emotions into the first Spinner
        ArrayAdapter<CharSequence> emotionAdapter = new ArrayAdapter<>(this,
                R.layout.emotional_state_options,
                getResources().getStringArray(R.array.emotional_state_list));
        emotionAdapter.setDropDownViewResource(R.layout.emotional_state_options);
        emotionDropdown.setAdapter(emotionAdapter);

        // Load social situations into the second Spinner
        ArrayAdapter<CharSequence> socialSituationAdapter = new ArrayAdapter<>(this,
                R.layout.emotional_state_options,  // Using same layout for styling
                getResources().getStringArray(R.array.social_situation_list));
        socialSituationAdapter.setDropDownViewResource(R.layout.emotional_state_options);
        emotionDropdown2.setAdapter(socialSituationAdapter);

        // Hide spinners initially
        emotionDropdown.setVisibility(View.GONE);
        emotionDropdown2.setVisibility(View.GONE);

        // Toggle first dropdown when clicking "Emotional State"
        emotionSelector.setOnClickListener(v -> {
            if (emotionDropdown.getVisibility() == View.GONE) {
                emotionDropdown.setVisibility(View.VISIBLE);
            } else {
                emotionDropdown.setVisibility(View.GONE);
            }
        });

        // Toggle second dropdown when clicking "Social Situation"
        socialSituationSelector.setOnClickListener(v -> {
            if (emotionDropdown2.getVisibility() == View.GONE) {
                emotionDropdown2.setVisibility(View.VISIBLE);
            } else {
                emotionDropdown2.setVisibility(View.GONE);
            }
        });

        // Handle selection for first dropdown (Emotional State)
        emotionDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedEmotion = parent.getItemAtPosition(position).toString();

                if (isEmotionSelected && emotionText.getText().toString().equals(selectedEmotion)) {
                    emotionText.setText("Emotional State");
                    isEmotionSelected = false;
                } else {
                    emotionText.setText(selectedEmotion);
                    isEmotionSelected = true;
                }
                emotionDropdown.setVisibility(View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                emotionText.setText("Emotional State");
                isEmotionSelected = false;
            }
        });

        // Handle selection for second dropdown (Social Situation)
        emotionDropdown2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedSocialSituation = parent.getItemAtPosition(position).toString();

                if (isSocialSituationSelected && socialSituationText.getText().toString().equals(selectedSocialSituation)) {
                    socialSituationText.setText("Current social situation");
                    isSocialSituationSelected = false;
                } else {
                    socialSituationText.setText(selectedSocialSituation);
                    isSocialSituationSelected = true;
                }
                emotionDropdown2.setVisibility(View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                socialSituationText.setText("Current social situation");
                isSocialSituationSelected = false;
            }
        });
    }
}
