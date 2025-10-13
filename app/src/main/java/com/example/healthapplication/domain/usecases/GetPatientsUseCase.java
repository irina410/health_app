package com.example.healthapplication.domain.usecases;

import com.example.healthapplication.data.firebase.PatientRepository;
import java.util.List;

public class GetPatientsUseCase {
    private final PatientRepository repo;

    public interface Callback {
        void onSuccess(List<PatientRepository.Patient> list);
        void onError(String msg);
    }

    public GetPatientsUseCase(PatientRepository repo) {
        this.repo = repo;
    }

    public void execute(Callback cb) {
        repo.getAllPatients(new PatientRepository.Callback() {
            @Override
            public void onSuccess(List<PatientRepository.Patient> list) {
                cb.onSuccess(list);
            }

            @Override
            public void onError(String msg) {
                cb.onError(msg);
            }
        });
    }
}
