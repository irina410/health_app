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
        public final java.util.List<String> diseases;

        public Patient(String email, String name, String surname, String card, java.util.List<String> diseases) {
            this.email = email;
            this.name = name;
            this.surname = surname;
            this.card = card;
            this.diseases = diseases;
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
                                    
                                    java.util.List<String> diseases = new java.util.ArrayList<>();
                                    Object dObj = data.get("diseases");
                                    if (dObj instanceof List) {
                                        for (Object o : (List<?>) dObj) {
                                            if (o != null) diseases.add(String.valueOf(o));
                                        }
                                    }
                                    
                                    result.add(new Patient(email, name, surname, card, diseases));
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

    public interface VoidCallback {
        void onSuccess();
        void onError(String msg);
    }

    public void addPatient(Patient patient, VoidCallback cb) {
        java.util.HashMap<String, Object> map = new java.util.HashMap<>();
        map.put("email", patient.email);
        map.put("name", patient.name);
        map.put("surname", patient.surname);
        map.put("card", patient.card);
        map.put("diseases", patient.diseases == null ? new java.util.ArrayList<>() : patient.diseases);
        db.collection(COLLECTION)
                .document(patient.email)
                .set(map)
                .addOnSuccessListener(aVoid -> cb.onSuccess())
                .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    private String safe(Object o) {
        return o == null ? "" : String.valueOf(o);
    }
}
