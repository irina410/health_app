package com.example.healthapplication.presentation.signin;

public interface SignInIntent {
    class Submit implements SignInIntent {
        public final String email;
        public final String password;

        public Submit(String email, String password) {
            this.email = email;
            this.password = password;
        }
    }
}
