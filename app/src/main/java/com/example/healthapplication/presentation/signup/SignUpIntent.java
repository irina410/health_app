package com.example.healthapplication.presentation.signup;

public interface SignUpIntent {
    class Submit implements SignUpIntent {
        public final String email;
        public final String password;
        public final String confirmPassword;

        public Submit(String email, String password, String confirmPassword) {
            this.email = email;
            this.password = password;
            this.confirmPassword = confirmPassword;
        }
    }
}
