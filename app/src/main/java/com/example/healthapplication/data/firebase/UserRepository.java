package com.example.healthapplication.data.firebase;

import androidx.annotation.NonNull;

import com.example.healthapplication.domain.models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UserRepository {
    private final FirebaseFirestore db;
    private final String USERS_COLLECTION = "users";

    public UserRepository(FirebaseFirestore db) {
        this.db = db;
    }

    public interface Callback {
        void onSuccess(User user);
        void onError(String message);
    }

    /**
     * Ищем пользователя по полю "email" в коллекции "users".
     * Возвращаем первый найденный документ.
     */
    public void getUserByEmail(String email, Callback cb) {
        CollectionReference ref = db.collection(USERS_COLLECTION);
        ref.whereEqualTo("email", email)
                .limit(1)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots == null || queryDocumentSnapshots.isEmpty()) {
                            cb.onError("Пользователь не найден");
                            return;
                        }
                        DocumentSnapshot ds = queryDocumentSnapshots.getDocuments().get(0);
                        try {
                            User user = mapDocumentToUser(ds.getData());
                            cb.onSuccess(user);
                        } catch (Exception e) {
                            cb.onError("Ошибка парсинга профиля: " + e.getMessage());
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        cb.onError(e.getMessage());
                    }
                });
    }

    private User mapDocumentToUser(Map<String, Object> data) {
        if (data == null) return null;
        String email = safeString(data.get("email"));
        String name = safeString(data.get("name"));
        String surname = safeString(data.get("surname"));
        String patronymic = safeString(data.get("patronymic"));
        String role = safeString(data.get("role"));
        String card = safeString(data.get("card"));

        List<String> diseases = new ArrayList<>();
        Object dObj = data.get("diseases");
        if (dObj instanceof List) {
            for (Object o : (List<?>) dObj) {
                if (o != null) diseases.add(String.valueOf(o));
            }
        }

        return new User(email, name, surname, patronymic, role, card, diseases);
    }

    private String safeString(Object o) {
        return o == null ? "" : String.valueOf(o);
    }
}
