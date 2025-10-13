package com.example.healthapplication.presentation.profile;

import com.example.healthapplication.domain.models.User;

public class ProfileState {
    public final boolean loading;
    public final User user;
    public final String error;

    public ProfileState(boolean loading, User user, String error) {
        this.loading = loading;
        this.user = user;
        this.error = error;
    }

    public static ProfileState idle() { return new ProfileState(false, null, null); }
    public static ProfileState loading() { return new ProfileState(true, null, null); }
    public static ProfileState success(User user) { return new ProfileState(false, user, null); }
    public static ProfileState error(String msg) { return new ProfileState(false, null, msg); }
}
