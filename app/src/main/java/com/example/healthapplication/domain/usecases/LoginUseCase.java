package com.example.healthapplication.domain.usecases;

import com.example.healthapplication.data.firebase.AuthRepository;

public class LoginUseCase {
    private final AuthRepository repo;

    public interface Callback {
        void onSuccess();
        void onError(String msg);
    }

    public LoginUseCase(AuthRepository repo) {
        this.repo = repo;
    }

    public void execute(String email, String password, Callback cb) {
        repo.signIn(email, password, new AuthRepository.AuthCallback() {
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
