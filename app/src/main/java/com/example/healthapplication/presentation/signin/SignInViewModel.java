package com.example.healthapplication.presentation.signin;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.healthapplication.domain.usecases.LoginUseCase;

public class SignInViewModel extends ViewModel {
    private final LoginUseCase loginUseCase;
    private final MutableLiveData<SignInState> state = new MutableLiveData<>(SignInState.idle());

    public SignInViewModel(LoginUseCase loginUseCase) {
        this.loginUseCase = loginUseCase;
    }

    public LiveData<SignInState> getState() {
        return state;
    }

    public void processIntent(SignInIntent intent) {
        if (intent instanceof SignInIntent.Submit) {
            SignInIntent.Submit s = (SignInIntent.Submit) intent;
            doLogin(s.email, s.password);
        }
    }

    private void doLogin(String email, String password) {
        state.postValue(SignInState.loading());
        loginUseCase.execute(email, password, new LoginUseCase.Callback() {
            @Override
            public void onSuccess() {
                state.postValue(SignInState.success("Вход выполнен"));
            }

            @Override
            public void onError(String msg) {
                state.postValue(SignInState.error(msg));
            }
        });
    }
}
