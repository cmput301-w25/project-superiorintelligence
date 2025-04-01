package com.example.superior_intelligence;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MoodTrendActivity extends AppCompatActivity {

    private boolean lineChartBuilt = false;
    private boolean barChartBuilt = false;
    private LineChart lineChart;
    private BarChart barChart;
    private Button lineChartButton, barChartButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mood_trend);

        // UI refs
        lineChart = findViewById(R.id.mood_line_chart);
        barChart = findViewById(R.id.mood_bar_chart);
        lineChartButton = findViewById(R.id.line_chart_button);
        barChartButton = findViewById(R.id.bar_chart_button);

        User currentUser = User.getInstance();
        Database.getInstance().loadEventsFromFirebase(currentUser, (myPosts, explore, followed) -> {
            if (myPosts == null || myPosts.isEmpty()) return;

            // Sort events by time
            Collections.sort(myPosts, Comparator.comparingLong(Event::getTimestamp));

            List<Entry> lineEntries = new ArrayList<>();
            List<BarEntry> barEntries = new ArrayList<>();
            List<String> xLabels = new ArrayList<>();
            List<String> moodLabels = new ArrayList<>();

            // Build dataset
            for (int i = 0; i < myPosts.size(); i++) {
                Event e = myPosts.get(i);
                int moodScore = moodToScore(e.getMood());

                lineEntries.add(new Entry(i, moodScore));
                barEntries.add(new BarEntry(i, moodScore));

                xLabels.add(e.getDate());
                moodLabels.add(e.getMood());
            }

            // Create charts
            setupLineChart(lineEntries, xLabels, moodLabels);
            setupBarChart(barEntries, xLabels, moodLabels);

            // Toggle logic
            lineChartButton.setOnClickListener(v -> {
                lineChart.setVisibility(View.VISIBLE);
                barChart.setVisibility(View.GONE);
                if (!lineChartBuilt) {
                    setupLineChart(lineEntries, xLabels, moodLabels);
                    lineChartBuilt = true;
                }
            });

            barChartButton.setOnClickListener(v -> {
                barChart.setVisibility(View.VISIBLE);
                lineChart.setVisibility(View.GONE);
                if (!barChartBuilt) {
                    setupBarChart(barEntries, xLabels, moodLabels);
                    barChartBuilt = true;
                }
            });

        });

        // Back button
        ImageButton backButton = findViewById(R.id.profile_back_button);
        backButton.setOnClickListener(v -> {
            startActivity(new Intent(MoodTrendActivity.this, ProfileActivity.class));
            finish();
        });
    }

    /**
     * Line Chart with colored circles.
     */
    private void setupLineChart(List<Entry> entries, List<String> xLabels, List<String> moodLabels) {
        LineDataSet dataSet = new LineDataSet(entries, "Mood Over Time");

        List<Integer> circleColors = new ArrayList<>();
        for (String mood : moodLabels) {
            circleColors.add(getColorFromMood(mood));
        }

        dataSet.setCircleColors(circleColors);
        dataSet.setColor(Color.DKGRAY); // neutral line
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineData data = new LineData(dataSet);
        lineChart.setData(data);
        lineChart.animateXY(1000, 1000);
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xLabels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelRotationAngle(-45);
        xAxis.setTextColor(Color.DKGRAY);

        YAxis yAxis = lineChart.getAxisLeft();
        yAxis.setAxisMinimum(0f);
        yAxis.setAxisMaximum(11f);
        yAxis.setLabelCount(6, true);

        lineChart.getAxisRight().setEnabled(false);
        lineChart.getDescription().setEnabled(false);
        lineChart.setTouchEnabled(true);
        lineChart.setPinchZoom(true);
        lineChart.invalidate();

    }

    /**
     * Bar chart with each bar colored by mood.
     */
    private void setupBarChart(List<BarEntry> entries, List<String> xLabels, List<String> moodLabels) {
        BarDataSet dataSet = new BarDataSet(entries, "Mood Over Time");

        List<Integer> moodColors = new ArrayList<>();
        for (String mood : moodLabels) {
            moodColors.add(getColorFromMood(mood));
        }

        dataSet.setColors(moodColors);
        dataSet.setValueTextColor(Color.BLACK);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.9f);

        barChart.setData(data);
        barChart.animateY(1000);
        XAxis xAxis = barChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xLabels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelRotationAngle(-45);
        xAxis.setTextColor(Color.DKGRAY);

        YAxis yAxis = barChart.getAxisLeft();
        yAxis.setAxisMinimum(0f);
        yAxis.setAxisMaximum(11f);
        yAxis.setLabelCount(6, true);

        barChart.getAxisRight().setEnabled(false);
        barChart.getDescription().setEnabled(false);
        barChart.setTouchEnabled(true);
        barChart.setPinchZoom(true);
        barChart.setFitBars(true);
        barChart.invalidate();
    }

    /**
     * Mood → numeric value (for Y-axis).
     */
    private int moodToScore(String mood) {
        if (mood == null) return 5;
        switch (mood.toLowerCase()) {
            case "happiness": return 10;
            case "surprise": return 9;
            case "confusion": return 6;
            case "shame": return 4;
            case "sadness": return 3;
            case "disgust": return 2;
            case "anger": return 1;
            default: return 5;
        }
    }

    /**
     * Convert hex string from EventManager to int color.
     */
    private int getColorFromMood(String mood) {
        try {
            String hex = EventManager.getOverlayColorForMood(mood);
            return Color.parseColor(hex);
        } catch (Exception e) {
            return Color.GRAY;
        }
    }
}
