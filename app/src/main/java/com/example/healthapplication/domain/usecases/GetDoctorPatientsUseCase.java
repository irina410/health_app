package com.example.healthapplication.domain.usecases;

import androidx.annotation.NonNull;

import com.example.healthapplication.data.firebase.DoctorPatientRepository;

import java.util.List;

public class GetDoctorPatientsUseCase {
    private final DoctorPatientRepository repo;

    public interface Callback {
        void onSuccess(List<String> patientEmails);
        void onError(String msg);
    }

    public GetDoctorPatientsUseCase(DoctorPatientRepository repo) {
        this.repo = repo;
    }

    public void execute(@NonNull String doctorEmail, @NonNull Callback cb) {
        repo.getPatientsForDoctor(doctorEmail, new DoctorPatientRepository.ListCallback() {
            @Override
            public void onSuccess(List<String> patientEmails) { cb.onSuccess(patientEmails); }
            @Override
            public void onError(String msg) { cb.onError(msg); }
        });
    }
}


