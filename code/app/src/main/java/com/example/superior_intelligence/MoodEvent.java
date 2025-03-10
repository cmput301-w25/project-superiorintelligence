package com.example.superior_intelligence;

/**
 * MoodEvent
 *
 * <p>
 *     Represents a mood event document from Firestore. Fields match the
 *     Firestore structure in the 'MyPosts' collection:
 *     <ul>
 *         <li>date</li>
 *         <li>imageUrl</li>
 *         <li>isPost</li>
 *         <li>mood</li>
 *         <li>overlayColor</li>
 *         <li>postUser</li>
 *         <li>situation</li>
 *         <li>title</li>
 *     </ul>
 * </p>
 *
 * <p>
 *     This class is used for deserialization when fetching documents via
 *     Firestore (doc.toObject(MoodEvent.class)).
 * </p>
 *
 * <p>References:
 * <a href="https://firebase.google.com/docs/firestore/manage-data/add-data">Firestore - Add Data</a>
 * </p>
 */
public class MoodEvent {

    private  String date;


    private String imageUrl;
    private boolean  isPost;

    private String mood;
    private String overlayColor;
    private String  postUser;
    private   String situation;
    private  String title;

    /**
     * No-argument constructor needed for Firestore to deserialize.
     */
    public MoodEvent() {
    }
    public  String getDate() {
        return date;}

    public void setDate(String date) {this.date = date;}

    public String getImageUrl() {
        return imageUrl;}




    public void setImageUrl(String imageUrl)
    {
        this.imageUrl = imageUrl;
    }
    public boolean isPost() {
        return isPost;
    }

    public void  setPost(boolean post) {
        isPost = post;}
    public String  getMood() {
        return mood;}
    public void setMood(String mood) {
        this.mood = mood;
    }
    public String getOverlayColor() {
        return overlayColor;}



    public void setOverlayColor(String overlayColor)
    {
        this.overlayColor = overlayColor;
    }
    public String getPostUser() {
        return postUser;
    }
    public void setPostUser(String postUser) {
        this.postUser = postUser;
    }
    public String getSituation()
    {
        return situation;}


    public void setSituation
            (String situation)
    {this.situation = situation;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }}