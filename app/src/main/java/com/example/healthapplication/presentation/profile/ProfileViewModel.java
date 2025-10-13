package com.example.healthapplication.presentation.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.healthapplication.domain.models.User;
import com.example.healthapplication.domain.usecases.GetUserProfileUseCase;

public class ProfileViewModel extends ViewModel {
    private final GetUserProfileUseCase getUserProfileUseCase;
    private final MutableLiveData<ProfileState> state = new MutableLiveData<>(ProfileState.idle());

    public ProfileViewModel(GetUserProfileUseCase getUserProfileUseCase) {
        this.getUserProfileUseCase = getUserProfileUseCase;
    }

    public LiveData<ProfileState> getState() {
        return state;
    }

    public void processIntent(Object intent) {
        if (intent instanceof ProfileIntent.LoadProfile) {
            ProfileIntent.LoadProfile li = (ProfileIntent.LoadProfile) intent;
            loadProfile(li.email);
        }
    }

    private void loadProfile(String email) {
        state.postValue(ProfileState.loading());
        getUserProfileUseCase.execute(email, new GetUserProfileUseCase.Callback() {
            @Override
            public void onSuccess(User user) {
                state.postValue(ProfileState.success(user));
            }

            @Override
            public void onError(String msg) {
                state.postValue(ProfileState.error(msg));
            }
        });
    }
}
