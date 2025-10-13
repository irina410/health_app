package com.example.healthapplication.domain.usecases;

import com.example.healthapplication.data.firebase.UserRepository;
import com.example.healthapplication.domain.models.User;

public class GetUserProfileUseCase {
    private final UserRepository repo;

    public interface Callback {
        void onSuccess(User user);
        void onError(String msg);
    }

    public GetUserProfileUseCase(UserRepository repo) {
        this.repo = repo;
    }

    public void execute(String email, Callback cb) {
        repo.getUserByEmail(email, new UserRepository.Callback() {
            @Override
            public void onSuccess(User user) {
                cb.onSuccess(user);
            }

            @Override
            public void onError(String message) {
                cb.onError(message);
            }
        });
    }
}
