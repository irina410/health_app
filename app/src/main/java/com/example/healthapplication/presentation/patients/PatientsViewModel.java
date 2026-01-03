package com.example.healthapplication.presentation.patients;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.healthapplication.data.firebase.PatientRepository;
import com.example.healthapplication.data.firebase.UserRepository;
import com.example.healthapplication.domain.usecases.GetPatientsUseCase;
import com.example.healthapplication.domain.usecases.GetDoctorPatientsUseCase;
import com.example.healthapplication.domain.usecases.LinkPatientToDoctorUseCase;

import java.util.List;

public class PatientsViewModel extends ViewModel {
    private final GetPatientsUseCase getPatientsUseCase;
    private final UserRepository userRepository;
    private final GetDoctorPatientsUseCase getDoctorPatientsUseCase;
    private final LinkPatientToDoctorUseCase linkPatientToDoctorUseCase;
    private final MutableLiveData<PatientsState> state = new MutableLiveData<>(PatientsState.idle());

    public PatientsViewModel(GetPatientsUseCase getPatientsUseCase, UserRepository userRepository,
                             GetDoctorPatientsUseCase getDoctorPatientsUseCase,
                             LinkPatientToDoctorUseCase linkPatientToDoctorUseCase) {
        this.getPatientsUseCase = getPatientsUseCase;
        this.userRepository = userRepository;
        this.getDoctorPatientsUseCase = getDoctorPatientsUseCase;
        this.linkPatientToDoctorUseCase = linkPatientToDoctorUseCase;
    }

    public LiveData<PatientsState> getState() {
        return state;
    }

    public void processIntent(Object intent) {
        if (intent instanceof PatientsIntent.LoadAll) {
            loadAllPatients();
        } else if (intent instanceof PatientsIntent.LoadUser) {
            PatientsIntent.LoadUser l = (PatientsIntent.LoadUser) intent;
            loadUser(l);
        } else if (intent instanceof PatientsIntent.LinkPatient) {
            PatientsIntent.LinkPatient lp = (PatientsIntent.LinkPatient) intent;
            linkPatient(lp.doctorEmail, lp.patientEmail);
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
    
    private void loadUser(PatientsIntent.LoadUser l) {
        userRepository.getUserByEmail(l.email, new UserRepository.Callback() {
            @Override
            public void onSuccess(com.example.healthapplication.domain.models.User user) {
                state.postValue(PatientsState.userLoaded(user));
            }

            @Override
            public void onError(String error) {
                state.postValue(PatientsState.error(error));
            }
        });
    }

    private void linkPatient(String doctorEmail, String patientEmail) {
        state.postValue(PatientsState.loading());
        linkPatientToDoctorUseCase.execute(doctorEmail, patientEmail, new LinkPatientToDoctorUseCase.Callback() {
            @Override
            public void onSuccess() {
                // Success message and stop loading
                state.postValue(PatientsState.success("Пациент добавлен"));
            }

            @Override
            public void onError(String msg) {
                state.postValue(PatientsState.error(msg));
            }
        });
    }
}
