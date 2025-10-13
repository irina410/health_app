package com.example.healthapplication.presentation.profile;

public interface ProfileIntent {
    class LoadProfile implements ProfileIntent {
        public final String email;
        public LoadProfile(String email) { this.email = email; }
    }
    // Можно добавить другие интенты (Refresh, Update) позже
}
