package com.example.healthapplication.presentation.signup;

public class SignUpState {
    public final boolean loading;
    public final boolean success;
    public final String message;

    public SignUpState(boolean loading, boolean success, String message) {
        this.loading = loading;
        this.success = success;
        this.message = message;
    }

    public static SignUpState idle() { 
        return new SignUpState(false, false, null); 
    }
    
    public static SignUpState loading() { 
        return new SignUpState(true, false, null); 
    }
    
    public static SignUpState success(String msg) { 
        return new SignUpState(false, true, msg); 
    }
    
    public static SignUpState error(String msg) { 
        return new SignUpState(false, false, msg); 
    }
}
