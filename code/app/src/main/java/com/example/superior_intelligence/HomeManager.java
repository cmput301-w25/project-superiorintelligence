package com.example.superior_intelligence;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeManager {

    /**
     * Filters events by reason, including exact and partial matches.
     */
    public static List<Event> filterByReason(String keyword, List<Event> events) {
        String lowerKeyword = keyword.toLowerCase();
        List<Event> exactMatches = new ArrayList<>();
        List<Event> partialMatches = new ArrayList<>();

        for (Event e : events) {
            String reason = e.getMoodExplanation() != null ? e.getMoodExplanation().toLowerCase() : "";
            if (reason.matches(".*\\b" + java.util.regex.Pattern.quote(lowerKeyword) + "\\b.*")) {
                exactMatches.add(e);
            } else if (reason.contains(lowerKeyword)) {
                partialMatches.add(e);
            }
        }

        exactMatches.sort((e1, e2) -> Long.compare(e2.getTimestamp(), e1.getTimestamp()));
        partialMatches.sort((e1, e2) -> Long.compare(e2.getTimestamp(), e1.getTimestamp()));

        List<Event> filteredList = new ArrayList<>(exactMatches);
        filteredList.addAll(partialMatches);
        return filteredList;
    }

    /**
     * Filters events by mood.
     */
    public static List<Event> filterByMood(List<String> moods, List<Event> events) {
        List<Event> result = new ArrayList<>();
        for (Event e : events) {
            if (moods.contains(e.getMood())) {
                result.add(e);
            }
        }
        result.sort((e1, e2) -> Long.compare(e2.getTimestamp(), e1.getTimestamp()));
        return result;
    }

    /**
     * Filters the most recent 3 events.
     */
    public static List<Event> recentThree(List<Event> events) {
        List<Event> result = new ArrayList<>();
        int count = Math.min(3, events.size());
        for (int i = 0; i < count; i++) {
            result.add(events.get(i));
        }
        return result;
    }

    public static List<Event> filterRecentWeek(List<Event> events) {
        /*Stackoverflow:
        https://stackoverflow.com/questions/16982056/how-to-get-the-date-7-days-earlier-date-from-current-date-in-java
         */
        List<Event> recentWeekEvents = new ArrayList<>();

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        Date currentDate = cal.getTime();

        cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - 6); // last 7 days total
        Date recentWeekDate = cal.getTime();

        for (Event e : events) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
                Date eventDate = sdf.parse(e.getDate());
                if (eventDate != null && !eventDate.before(recentWeekDate) && !eventDate.after(currentDate)) {
                    recentWeekEvents.add(e);
                }
            } catch (ParseException ex) {
                // Skip invalid date formats
            }
        }

        recentWeekEvents.sort((e1, e2) -> Long.compare(e2.getTimestamp(), e1.getTimestamp()));
        return recentWeekEvents;
    }

    /**
     * Add or update an event in the list based on matching ID.
     */
    public static void upsertEvent(List<Event> events, Event newEvent) {
        if (newEvent == null) return;

        boolean found = false;
        for (int i = 0; i < events.size(); i++) {
            if (events.get(i).getID().equals(newEvent.getID())) {
                events.set(i, newEvent);
                found = true;
                break;
            }
        }
        if (!found) {
            events.add(newEvent);
        }
    }

    /**
     * Remove an event from the list by ID.
     */
    public static void removeEventById(List<Event> events, String eventId) {
        if (eventId == null) return;
        events.removeIf(e -> e.getID().equals(eventId));
    }

}
