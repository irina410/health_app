package com.example.healthapplication.presentation.editprofile;

public class EditProfileState {
    public final boolean loading;
    public final boolean success;
    public final String message;
    public final com.example.healthapplication.domain.models.User user;

    public EditProfileState(boolean loading, boolean success, String message, com.example.healthapplication.domain.models.User user) {
        this.loading = loading;
        this.success = success;
        this.message = message;
        this.user = user;
    }

    public static EditProfileState idle() { return new EditProfileState(false, false, null, null); }
    public static EditProfileState loading() { return new EditProfileState(true, false, null, null); }
    public static EditProfileState success(String msg) { return new EditProfileState(false, true, msg, null); }
    public static EditProfileState error(String msg) { return new EditProfileState(false, false, msg, null); }
    public static EditProfileState userLoaded(com.example.healthapplication.domain.models.User user) { return new EditProfileState(false, false, null, user); }
}


