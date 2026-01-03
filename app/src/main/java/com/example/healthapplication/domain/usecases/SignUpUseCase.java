package com.example.healthapplication.domain.usecases;

import com.example.healthapplication.data.firebase.AuthRepository;

public class SignUpUseCase {
    private final AuthRepository repo;

    public interface Callback {
        void onSuccess();
        void onError(String msg);
    }

    public SignUpUseCase(AuthRepository repo) {
        this.repo = repo;
    }

    public void execute(String email, String password, Callback cb) {
        repo.signUp(email, password, new AuthRepository.AuthCallback() {
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
