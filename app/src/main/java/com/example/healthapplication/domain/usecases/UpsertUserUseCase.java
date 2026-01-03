package com.example.healthapplication.domain.usecases;

import androidx.annotation.NonNull;

import com.example.healthapplication.data.firebase.UserRepository;
import com.example.healthapplication.domain.models.User;

public class UpsertUserUseCase {
    private final UserRepository repo;

    public interface Callback {
        void onSuccess();
        void onError(String msg);
    }

    public UpsertUserUseCase(UserRepository repo) {
        this.repo = repo;
    }

    public void execute(@NonNull User user, @NonNull Callback cb) {
        repo.upsertUser(user, new UserRepository.VoidCallback() {
            @Override
            public void onSuccess() {
                cb.onSuccess();
            }

            @Override
            public void onError(String message) {
                cb.onError(message);
            }
        });
    }
}


