package com.example.healthapplication.data.firebase;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PatientRepository {
    private final FirebaseFirestore db;
    private static final String COLLECTION = "patients";

    public PatientRepository(FirebaseFirestore db) {
        this.db = db;
    }

    public static class Patient {
        public final String email;
        public final String name;
        public final String surname;
        public final String card;

        public Patient(String email, String name, String surname, String card) {
            this.email = email;
            this.name = name;
            this.surname = surname;
            this.card = card;
        }
    }

    public interface Callback {
        void onSuccess(List<Patient> list);
        void onError(String msg);
    }

    public void getAllPatients(Callback cb) {
        db.collection(COLLECTION)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot snapshots) {
                        List<Patient> result = new ArrayList<>();
                        if (snapshots != null) {
                            for (DocumentSnapshot ds : snapshots.getDocuments()) {
                                Map<String, Object> data = ds.getData();
                                if (data != null) {
                                    String email = safe(data.get("email"));
                                    String name = safe(data.get("name"));
                                    String surname = safe(data.get("surname"));
                                    String card = safe(data.get("card"));
                                    result.add(new Patient(email, name, surname, card));
                                }
                            }
                        }
                        cb.onSuccess(result);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        cb.onError(e.getMessage());
                    }
                });
    }

    private String safe(Object o) {
        return o == null ? "" : String.valueOf(o);
    }
}
