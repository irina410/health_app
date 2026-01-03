package com.example.healthapplication.data.firebase;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class AuthRepository {
    private final FirebaseAuth auth;
    private final FirebaseFirestore db;

    public interface AuthCallback {
        void onSuccess();
        void onError(String message);
    }

    public AuthRepository(FirebaseAuth auth, FirebaseFirestore db) {
        this.auth = auth;
        this.db = db;
    }

    public void signIn(String email, String password, AuthCallback cb) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> cb.onSuccess())
                .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    public void signUp(String email, String password, AuthCallback cb) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> cb.onSuccess())
                .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    public FirebaseFirestore getDb() {
        return db;
    }
}
