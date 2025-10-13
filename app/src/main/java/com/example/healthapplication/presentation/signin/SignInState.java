package com.example.healthapplication.presentation.signin;

public class SignInState {
    public final boolean loading;
    public final boolean success;
    public final String message;

    public SignInState(boolean loading, boolean success, String message) {
        this.loading = loading;
        this.success = success;
        this.message = message;
    }

    public static SignInState idle() { return new SignInState(false, false, null); }
    public static SignInState loading() { return new SignInState(true, false, null); }
    public static SignInState success(String msg) { return new SignInState(false, true, msg); }
    public static SignInState error(String msg) { return new SignInState(false, false, msg); }
}
