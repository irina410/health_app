package com.example.healthapplication.presentation.patients;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.healthapplication.data.firebase.PatientRepository;
import com.example.healthapplication.domain.usecases.GetPatientsUseCase;

import java.util.List;

public class PatientsViewModel extends ViewModel {
    private final GetPatientsUseCase getPatientsUseCase;
    private final MutableLiveData<PatientsState> state = new MutableLiveData<>(PatientsState.idle());

    public PatientsViewModel(GetPatientsUseCase getPatientsUseCase) {
        this.getPatientsUseCase = getPatientsUseCase;
    }

    public LiveData<PatientsState> getState() {
        return state;
    }

    public void processIntent(Object intent) {
        if (intent instanceof PatientsIntent.LoadAll) {
            loadAllPatients();
        }
    }

    private void loadAllPatients() {
        state.postValue(PatientsState.loading());
        getPatientsUseCase.execute(new GetPatientsUseCase.Callback() {
            @Override
            public void onSuccess(List<PatientRepository.Patient> list) {
                state.postValue(PatientsState.success(list));
            }

            @Override
            public void onError(String msg) {
                state.postValue(PatientsState.error(msg));
            }
        });
    }
}
