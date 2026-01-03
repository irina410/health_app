package com.example.healthapplication.domain.usecases;

import com.example.healthapplication.data.firebase.PatientRepository;
import com.example.healthapplication.data.local.PatientEntity;
import com.example.healthapplication.di.ServiceLocator;

public class AddPatientUseCase {
    private final PatientRepository repo;

    public interface Callback {
        void onSuccess();
        void onError(String msg);
    }

    public AddPatientUseCase(PatientRepository repo) {
        this.repo = repo;
    }

    public void execute(PatientRepository.Patient patient, Callback cb) {
        // Сначала в Firestore
        repo.addPatient(patient, new PatientRepository.VoidCallback() {
            @Override
            public void onSuccess() {
                // Затем локально в Room
                new Thread(() -> {
                    try {
                        PatientEntity e = new PatientEntity();
                        e.email = patient.email;
                        e.name = patient.name;
                        e.surname = patient.surname;
                        e.card = patient.card;
                        ServiceLocator.db().patientDao().insert(e);
                        cb.onSuccess();
                    } catch (Exception ex) {
                        cb.onError(ex.getMessage());
                    }
                }).start();
            }

            @Override
            public void onError(String msg) {
                cb.onError(msg);
            }
        });
    }
}


