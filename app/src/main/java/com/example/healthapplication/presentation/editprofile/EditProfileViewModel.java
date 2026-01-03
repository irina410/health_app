package com.example.healthapplication.presentation.editprofile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.healthapplication.domain.models.User;
import com.example.healthapplication.domain.usecases.UpsertUserUseCase;
import com.example.healthapplication.data.firebase.UserRepository;

import java.util.ArrayList;
import java.util.Arrays;

public class EditProfileViewModel extends ViewModel {
    private final UpsertUserUseCase upsertUserUseCase;
    private final UserRepository userRepository;
    private final MutableLiveData<EditProfileState> state = new MutableLiveData<>(EditProfileState.idle());

    public EditProfileViewModel(UpsertUserUseCase upsertUserUseCase, UserRepository userRepository) {
        this.upsertUserUseCase = upsertUserUseCase;
        this.userRepository = userRepository;
    }

    public LiveData<EditProfileState> getState() { return state; }

    public void processIntent(EditProfileIntent intent) {
        if (intent instanceof EditProfileIntent.Submit) {
            EditProfileIntent.Submit s = (EditProfileIntent.Submit) intent;
            submit(s);
        } else if (intent instanceof EditProfileIntent.LoadUser) {
            EditProfileIntent.LoadUser l = (EditProfileIntent.LoadUser) intent;
            loadUser(l);
        }
    }

    private void submit(EditProfileIntent.Submit s) {
        if (s.email == null || s.email.isEmpty()) {
            state.postValue(EditProfileState.error("Email обязателен"));
            return;
        }
        if (s.name == null || s.name.trim().isEmpty()) {
            state.postValue(EditProfileState.error("Имя обязательно"));
            return;
        }
        if (s.surname == null || s.surname.trim().isEmpty()) {
            state.postValue(EditProfileState.error("Фамилия обязательна"));
            return;
        }
        state.postValue(EditProfileState.loading());
        java.util.List<String> diseases = new ArrayList<>();
        if (s.diseasesCsv != null && !s.diseasesCsv.trim().isEmpty()) {
            diseases = new ArrayList<>(Arrays.asList(s.diseasesCsv.split(",")));
            for (int i = 0; i < diseases.size(); i++) diseases.set(i, diseases.get(i).trim());
        }
        User user = new User(
                s.email,
                s.name,
                s.surname,
                s.patronymic,
                s.role,
                s.role != null && s.role.equals("Пациент") ? s.card : "",
                s.role != null && s.role.equals("Пациент") ? diseases : new ArrayList<>(),
                s.role != null && s.role.equals("Пациент") ? s.phone : "",
                s.role != null && s.role.equals("Пациент") ? s.gender : "",
                s.role != null && s.role.equals("Пациент") ? s.birthDate : "",
                s.role != null && s.role.equals("Пациент") ? s.medicalHistory : "",
                s.role != null && s.role.equals("Врач") ? s.specialty : "",
                s.avatarUri
        );
        upsertUserUseCase.execute(user, new UpsertUserUseCase.Callback() {
            @Override
            public void onSuccess() {
                state.postValue(EditProfileState.success("Профиль сохранён"));
            }

            @Override
            public void onError(String msg) {
                state.postValue(EditProfileState.error(msg));
            }
        });
    }
    
    private void loadUser(EditProfileIntent.LoadUser l) {
        state.postValue(EditProfileState.loading());
        userRepository.getUserByEmail(l.email, new UserRepository.Callback() {
            @Override
            public void onSuccess(User user) {
                state.postValue(EditProfileState.userLoaded(user));
            }

            @Override
            public void onError(String error) {
                state.postValue(EditProfileState.error(error));
            }
        });
    }
}


