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
import java.util.concurrent.ConcurrentHashMap;

public class UserRepository {
    private final FirebaseFirestore db;
    private final String USERS_COLLECTION = "users";
    private final Map<String, User> cache = new ConcurrentHashMap<>();
    private final Map<String, Long> cacheTimestamps = new ConcurrentHashMap<>();
    private static final long CACHE_DURATION = 5 * 60 * 1000; // 5 minutes

    public UserRepository(FirebaseFirestore db) {
        this.db = db;
    }

    public interface Callback {
        void onSuccess(User user);
        void onError(String message);
    }

    public interface VoidCallback {
        void onSuccess();
        void onError(String message);
    }

    /**
     * Ищем пользователя по полю "email" в коллекции "users".
     * Возвращаем первый найденный документ.
     */
    public void getUserByEmail(String email, Callback cb) {
        // Check cache first
        if (isCacheValid(email)) {
            cb.onSuccess(cache.get(email));
            return;
        }

        // Try direct document access first (faster)
        db.collection(USERS_COLLECTION).document(email)
                .get()
                .addOnSuccessListener(ds -> {
                    if (ds != null && ds.exists()) {
                        try {
                            User user = mapDocumentToUser(ds.getData());
                            updateCache(email, user);
                            cb.onSuccess(user);
                        } catch (Exception e) {
                            cb.onError("Ошибка парсинга профиля: " + e.getMessage());
                        }
                    } else {
                        // Fallback to query if document doesn't exist
                        fallbackQuery(email, cb);
                    }
                })
                .addOnFailureListener(e -> {
                    // Fallback to query on failure
                    fallbackQuery(email, cb);
                });
    }

    private void fallbackQuery(String email, Callback cb) {
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
                            updateCache(email, user);
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

    /**
     * Создаёт или обновляет документ пользователя по email как docId.
     */
    public void upsertUser(@NonNull User user, @NonNull VoidCallback cb) {
        if (user == null || user.getEmail() == null || user.getEmail().isEmpty()) {
            cb.onError("Email обязателен");
            return;
        }
        db.collection(USERS_COLLECTION)
                .document(user.getEmail())
                .set(mapUserToDocument(user))
                .addOnSuccessListener(aVoid -> {
                    updateCache(user.getEmail(), user);
                    cb.onSuccess();
                })
                .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    private User mapDocumentToUser(Map<String, Object> data) {
        if (data == null) return null;
        String email = safeString(data.get("email"));
        String name = safeString(data.get("name"));
        String surname = safeString(data.get("surname"));
        String patronymic = safeString(data.get("patronymic"));
        String role = safeString(data.get("role"));
        String card = safeString(data.get("card"));
        String phone = safeString(data.get("phone"));
        String gender = safeString(data.get("gender"));
        String birthDate = safeString(data.get("birthDate"));
        String medicalHistory = safeString(data.get("medicalHistory"));
        String specialty = safeString(data.get("specialty"));
        String avatarUri = safeString(data.get("avatarUri"));

        List<String> diseases = new ArrayList<>();
        Object dObj = data.get("diseases");
        if (dObj instanceof List) {
            for (Object o : (List<?>) dObj) {
                if (o != null) diseases.add(String.valueOf(o));
            }
        }

        return new User(email, name, surname, patronymic, role, card, diseases,
                phone, gender, birthDate, medicalHistory, specialty, avatarUri);
    }

    private Map<String, Object> mapUserToDocument(User user) {
        java.util.HashMap<String, Object> map = new java.util.HashMap<>();
        map.put("email", user.getEmail());
        map.put("name", user.getName());
        map.put("surname", user.getSurname());
        map.put("patronymic", user.getPatronymic());
        map.put("role", user.getRole());
        map.put("card", user.getCard());
        map.put("diseases", user.getDiseases() == null ? new java.util.ArrayList<>() : user.getDiseases());
        map.put("phone", user.getPhone());
        map.put("gender", user.getGender());
        map.put("birthDate", user.getBirthDate());
        map.put("medicalHistory", user.getMedicalHistory());
        map.put("specialty", user.getSpecialty());
        map.put("avatarUri", user.getAvatarUri());
        return map;
    }

    private String safeString(Object o) {
        return o == null ? "" : String.valueOf(o);
    }

    // Cache management methods
    private boolean isCacheValid(String email) {
        Long timestamp = cacheTimestamps.get(email);
        if (timestamp == null) return false;
        return (System.currentTimeMillis() - timestamp) < CACHE_DURATION;
    }

    private void updateCache(String email, User user) {
        cache.put(email, user);
        cacheTimestamps.put(email, System.currentTimeMillis());
    }

    public void clearCache() {
        cache.clear();
        cacheTimestamps.clear();
    }

    public void clearCacheForUser(String email) {
        cache.remove(email);
        cacheTimestamps.remove(email);
    }
}
