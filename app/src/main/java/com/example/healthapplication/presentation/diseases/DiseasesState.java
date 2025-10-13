package com.example.healthapplication.presentation.diseases;

import com.example.healthapplication.data.firebase.DiseaseRepository;
import java.util.List;

public class DiseasesState {
    public final boolean loading;
    public final List<DiseaseRepository.Disease> diseases;
    public final String error;

    public DiseasesState(boolean loading, List<DiseaseRepository.Disease> diseases, String error) {
        this.loading = loading;
        this.diseases = diseases;
        this.error = error;
    }

    public static DiseasesState idle() { return new DiseasesState(false, null, null); }
    public static DiseasesState loading() { return new DiseasesState(true, null, null); }
    public static DiseasesState success(List<DiseaseRepository.Disease> list) { return new DiseasesState(false, list, null); }
    public static DiseasesState error(String msg) { return new DiseasesState(false, null, msg); }
}
