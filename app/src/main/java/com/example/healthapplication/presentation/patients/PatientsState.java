package com.example.healthapplication.presentation.patients;

import com.example.healthapplication.data.firebase.PatientRepository;
import java.util.List;

public class PatientsState {
    public final boolean loading;
    public final List<PatientRepository.Patient> patients;
    public final String error;

    public PatientsState(boolean loading, List<PatientRepository.Patient> patients, String error) {
        this.loading = loading;
        this.patients = patients;
        this.error = error;
    }

    public static PatientsState idle() { return new PatientsState(false, null, null); }
    public static PatientsState loading() { return new PatientsState(true, null, null); }
    public static PatientsState success(List<PatientRepository.Patient> list) { return new PatientsState(false, list, null); }
    public static PatientsState error(String msg) { return new PatientsState(false, null, msg); }
}
