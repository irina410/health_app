package com.example.healthapplication.domain.usecases;

import androidx.annotation.NonNull;

import com.example.healthapplication.data.firebase.DoctorPatientRepository;

public class LinkPatientToDoctorUseCase {
    private final DoctorPatientRepository repo;

    public interface Callback {
        void onSuccess();
        void onError(String msg);
    }

    public LinkPatientToDoctorUseCase(DoctorPatientRepository repo) {
        this.repo = repo;
    }

    public void execute(@NonNull String doctorEmail, @NonNull String patientEmail, @NonNull Callback cb) {
        repo.addLink(doctorEmail, patientEmail, new DoctorPatientRepository.VoidCallback() {
            @Override
            public void onSuccess() { cb.onSuccess(); }
            @Override
            public void onError(String msg) { cb.onError(msg); }
        });
    }
}


