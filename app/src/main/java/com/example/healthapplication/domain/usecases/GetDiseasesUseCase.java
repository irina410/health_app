package com.example.healthapplication.domain.usecases;

import com.example.healthapplication.data.firebase.DiseaseRepository;
import java.util.List;

public class GetDiseasesUseCase {
    private final DiseaseRepository repo;

    public interface Callback {
        void onSuccess(List<DiseaseRepository.Disease> list);
        void onError(String msg);
    }

    public GetDiseasesUseCase(DiseaseRepository repo) {
        this.repo = repo;
    }

    public void execute(Callback cb) {
        repo.getAllDiseases(new DiseaseRepository.Callback() {
            @Override
            public void onSuccess(List<DiseaseRepository.Disease> list) {
                cb.onSuccess(list);
            }

            @Override
            public void onError(String msg) {
                cb.onError(msg);
            }
        });
    }
}
