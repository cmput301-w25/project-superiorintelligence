package com.example.superior_intelligence;

import com.example.superior_intelligence.User;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class Userbase {
    private final FirebaseFirestore db;

    public Userbase() {
        db = FirebaseFirestore.getInstance();
    }

    public void checkUserExists(String username, UserCheckCallback callback) {
        db.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        String name = document.getString("name");
                        callback.onUserChecked(true, name, username);
                    } else {
                        callback.onUserChecked(false, null, null);
                    }
                })
                .addOnFailureListener(e -> {
                    callback.onUserChecked(false, null, null);
                });
    }

    public void createUser(String name, String username, UserCreationCallback callback) {
        // In this example, we use a simple User (or HelperClass) object.
        // You can also rename HelperClass to something like SignUpUser if you prefer.
        HelperClass userData = new HelperClass(name, username);
        db.collection("users")
                .add(userData)
                .addOnSuccessListener(documentReference -> callback.onUserCreated(true))
                .addOnFailureListener(e -> callback.onUserCreated(false));
    }

    public interface UserCheckCallback {
        void onUserChecked(boolean exists, String name, String username);
    }

    public interface UserCreationCallback {
        void onUserCreated(boolean success);
    }
}
