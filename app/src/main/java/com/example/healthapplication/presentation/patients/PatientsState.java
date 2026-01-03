package com.example.healthapplication.presentation.patients;

import com.example.healthapplication.data.firebase.PatientRepository;
import java.util.List;

public class PatientsState {
    public final boolean loading;
    public final List<PatientRepository.Patient> patients;
    public final String error;
    public final com.example.healthapplication.domain.models.User user;

    public PatientsState(boolean loading, List<PatientRepository.Patient> patients, String error, com.example.healthapplication.domain.models.User user) {
        this.loading = loading;
        this.patients = patients;
        this.error = error;
        this.user = user;
    }

    public static PatientsState idle() { return new PatientsState(false, null, null, null); }
    public static PatientsState loading() { return new PatientsState(true, null, null, null); }
    public static PatientsState success(List<PatientRepository.Patient> list) { return new PatientsState(false, list, null, null); }
    public static PatientsState success(String message) { return new PatientsState(false, null, null, null); }
    public static PatientsState error(String msg) { return new PatientsState(false, null, msg, null); }
    public static PatientsState userLoaded(com.example.healthapplication.domain.models.User user) { return new PatientsState(false, null, null, user); }
}
