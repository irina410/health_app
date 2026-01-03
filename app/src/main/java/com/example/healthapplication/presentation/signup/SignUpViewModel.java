package com.example.healthapplication.presentation.signup;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.healthapplication.domain.usecases.SignUpUseCase;
import com.example.healthapplication.domain.usecases.UpsertUserUseCase;
import com.example.healthapplication.domain.models.User;

public class SignUpViewModel extends ViewModel {
    private final SignUpUseCase signUpUseCase;
    private final UpsertUserUseCase upsertUserUseCase;
    private final MutableLiveData<SignUpState> state = new MutableLiveData<>(SignUpState.idle());

    public SignUpViewModel(SignUpUseCase signUpUseCase, UpsertUserUseCase upsertUserUseCase) {
        this.signUpUseCase = signUpUseCase;
        this.upsertUserUseCase = upsertUserUseCase;
    }

    public LiveData<SignUpState> getState() {
        return state;
    }

    public void processIntent(SignUpIntent intent) {
        if (intent instanceof SignUpIntent.Submit) {
            SignUpIntent.Submit s = (SignUpIntent.Submit) intent;
            doSignUp(s.email, s.password, s.confirmPassword);
        }
    }

    private void doSignUp(String email, String password, String confirmPassword) {
        // Валидация
        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            state.postValue(SignUpState.error("Все поля должны быть заполнены"));
            return;
        }

        if (!password.equals(confirmPassword)) {
            state.postValue(SignUpState.error("Пароли не совпадают"));
            return;
        }

        if (password.length() < 6) {
            state.postValue(SignUpState.error("Пароль должен содержать минимум 6 символов"));
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            state.postValue(SignUpState.error("Неверный формат email"));
            return;
        }

        state.postValue(SignUpState.loading());
        signUpUseCase.execute(email, password, new SignUpUseCase.Callback() {
            @Override
            public void onSuccess() {
                // создаём базовую запись пользователя в Firestore
                User user = new User(email, "", "", "", "", "", new java.util.ArrayList<>());
                upsertUserUseCase.execute(user, new UpsertUserUseCase.Callback() {
                    @Override
                    public void onSuccess() {
                        state.postValue(SignUpState.success("Регистрация успешна"));
                    }

                    @Override
                    public void onError(String msg) {
                        state.postValue(SignUpState.error("Регистрация прошла, но профиль не создан: " + msg));
                    }
                });
            }

            @Override
            public void onError(String msg) {
                state.postValue(SignUpState.error(msg));
            }
        });
    }
}
