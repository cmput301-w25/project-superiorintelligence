package com.example.superior_intelligence;
import androidx.annotation.NonNull;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
/**
 * RecentMoodEventRepository
 *
 * Fetches Event objects from "MyPosts" in Firestore, ordering by "dateTime" descending.
 * Maps Firestore fields manually to the existing Event class, avoiding any changes to Event.
 */
public class RecentMoodEventRepository {
    private final FirebaseFirestore db;
    public RecentMoodEventRepository() {db = FirebaseFirestore.getInstance();}
    /**
     * Fetches Event documents from "MyPosts" in descending order of "dateTime" (a Timestamp).
     *
     * @param onSuccess A Consumer of an ArrayList of Event objects on success
     * @param onError   A Consumer of an Exception if something goes wrong
     *
     * NOTE: This uses java.util.function.Consumer, so ensure you have Java 8 enabled OR
     *       switch to a custom callback interface if you prefer.
     */
    public void getRecentMoodEvents(@NonNull Consumer<ArrayList<Event>> onSuccess, @NonNull Consumer<Exception> onError)
    {db.collection("MyPosts")
                .orderBy("dateTime", Query.Direction.DESCENDING)

                .get()
                .addOnSuccessListener(queryDocumentSnapshots ->{
                    ArrayList<Event> events = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots){
                        Event event = new Event();
                        event.setID(doc.getId());
                        Timestamp ts = doc.getTimestamp("dateTime");
                        if (ts != null) {
                            event.setTimestamp(ts.toDate().getTime());
                            String formattedDate = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(ts.toDate());
                            event.setDate(formattedDate);
                        }
                        event.setMood(doc.getString("mood")) ;
                        event.setTitle(doc.getString("title"));
                        event.setOverlayColor(doc.getString("overlayColor" ));

                        event.setImageUrl(doc.getString("imageUrl") );
                        Long emojiResLong = doc.getLong("emojiResource");
                        if (emojiResLong != null) {event.setEmojiResource(emojiResLong.intValue());}
                        Boolean isFollowed = doc.getBoolean("isFollowed");

                        if (isFollowed != null) {event.setFollowed(isFollowed);}
                        Boolean isMyPost = doc.getBoolean("isMyPost");
                        if (isMyPost != null) {event.setMyPost(isMyPost);}
                        event.setMoodExplanation(doc.getString("moodExplanation"));
                        event.setSituation(doc.getString("situation"));
                        event.setUser(doc.getString("user"));
                        Double lat = doc.getDouble("lat");

                        Double lng = doc.getDouble("lng");
                        if (lat != null)event.setLat(lat);
                        if(lng != null) event.setLng(lng);
                        List<Comment> commentList = (List<Comment>) doc.get("comments");
                        if (commentList != null){event.setComments(commentList);}
                        events.add(event);}
                    onSuccess.accept(events);
                } ).addOnFailureListener(onError::accept);}}