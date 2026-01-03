package com.example.healthapplication.data.firebase;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DoctorPatientRepository {
    private final FirebaseFirestore db;
    private static final String COLLECTION = "doctor_patients";

    public DoctorPatientRepository(FirebaseFirestore db) {
        this.db = db;
    }

    public interface VoidCallback {
        void onSuccess();
        void onError(String msg);
    }

    public interface ListCallback {
        void onSuccess(List<String> patientEmails);
        void onError(String msg);
    }

    public void addLink(@NonNull String doctorEmail, @NonNull String patientEmail, @NonNull VoidCallback cb) {
        String id = doctorEmail + "__" + patientEmail;
        Map<String, Object> map = new HashMap<>();
        map.put("doctorEmail", doctorEmail);
        map.put("patientEmail", patientEmail);
        db.collection(COLLECTION)
                .document(id)
                .set(map)
                .addOnSuccessListener(aVoid -> cb.onSuccess())
                .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    public void getPatientsForDoctor(@NonNull String doctorEmail, @NonNull ListCallback cb) {
        db.collection(COLLECTION)
                .whereEqualTo("doctorEmail", doctorEmail)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot snapshots) {
                        List<String> emails = new ArrayList<>();
                        if (snapshots != null) {
                            for (DocumentSnapshot ds : snapshots.getDocuments()) {
                                Object e = ds.get("patientEmail");
                                if (e != null) emails.add(String.valueOf(e));
                            }
                        }
                        cb.onSuccess(emails);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        cb.onError(e.getMessage());
                    }
                });
    }
}


