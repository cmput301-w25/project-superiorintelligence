package com.example.superior_intelligence;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Manages home events including filtering, sorting, adding, updating, and removing events.
 */
public class HomeManager {

    /**
     * Filters events by reason, including exact and partial matches.
     * Exact matches are events where the keyword appears as a whole word,
     * while partial matches are events where the keyword appears as part of a word.
     * Results are sorted by timestamp in descending order with exact matches first.
     *
     * @param keyword The search term to filter events by
     * @param events The list of events to filter
     * @return A filtered list of events containing exact matches followed by partial matches,
     *         sorted by timestamp in descending order
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
     * Filters events by mood. Only events matching one of the specified moods are included.
     * Results are sorted by timestamp in descending order.
     *
     * @param moods The list of moods to filter by
     * @param events The list of events to filter
     * @return A filtered list of events containing only events with matching moods,
     *         sorted by timestamp in descending order
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
     * Gets the most recent 3 events from the list.
     * The input list is assumed to be already sorted by timestamp in descending order.
     *
     * @param events The list of events to filter
     * @return A list containing at most 3 most recent events
     */
    public static List<Event> recentThree(List<Event> events) {
        List<Event> result = new ArrayList<>();
        int count = Math.min(3, events.size());
        for (int i = 0; i < count; i++) {
            result.add(events.get(i));
        }
        return result;
    }

    /**
     * Filters events from the last 7 days (including today).
     * Events are parsed from their date string representation and compared against
     * the current date. Results are sorted by timestamp in descending order.
     *
     * @param events The list of events to filter
     * @return A list of events from the last 7 days, sorted by timestamp in descending order
     */
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
     * Adds or updates an event in the list. If an event with the same ID already exists,
     * it will be replaced with the new event. Otherwise, the new event will be added to the list.
     *
     * @param events The list of events to modify
     * @param newEvent The event to add or update
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
     * Removes an event from the list by its ID. Does nothing if no event with
     * the specified ID exists or if the eventId is null.
     *
     * @param events The list of events to modify
     * @param eventId The ID of the event to remove
     */
    public static void removeEventById(List<Event> events, String eventId) {
        if (eventId == null) return;
        events.removeIf(e -> e.getID().equals(eventId));
    }

}
