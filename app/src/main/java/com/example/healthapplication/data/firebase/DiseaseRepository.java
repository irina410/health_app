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

public class DiseaseRepository {
    private final FirebaseFirestore db;
    private static final String COLLECTION = "diseases";

    public DiseaseRepository(FirebaseFirestore db) {
        this.db = db;
    }

    public interface Callback {
        void onSuccess(List<Disease> list);
        void onError(String msg);
    }

    public static class Disease {
        public final String name;
        public final String description;

        public Disease(String name, String description) {
            this.name = name;
            this.description = description;
        }
    }

    public void getAllDiseases(Callback cb) {
        db.collection(COLLECTION)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot snapshots) {
                        List<Disease> result = new ArrayList<>();
                        if (snapshots != null) {
                            for (DocumentSnapshot ds : snapshots.getDocuments()) {
                                Map<String, Object> data = ds.getData();
                                if (data != null) {
                                    String name = safe(data.get("name"));
                                    String description = safe(data.get("description"));
                                    result.add(new Disease(name, description));
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
