package com.example.healthapplication.presentation.diseases;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.healthapplication.data.firebase.DiseaseRepository;
import com.example.healthapplication.domain.usecases.GetDiseasesUseCase;

import java.util.List;

public class DiseasesViewModel extends ViewModel {
    private final GetDiseasesUseCase getDiseasesUseCase;
    private final MutableLiveData<DiseasesState> state = new MutableLiveData<>(DiseasesState.idle());

    public DiseasesViewModel(GetDiseasesUseCase getDiseasesUseCase) {
        this.getDiseasesUseCase = getDiseasesUseCase;
    }

    public LiveData<DiseasesState> getState() {
        return state;
    }

    public void processIntent(Object intent) {
        if (intent instanceof DiseasesIntent.LoadAll) {
            loadAll();
        }
    }

    private void loadAll() {
        state.postValue(DiseasesState.loading());
        getDiseasesUseCase.execute(new GetDiseasesUseCase.Callback() {
            @Override
            public void onSuccess(List<DiseaseRepository.Disease> list) {
                state.postValue(DiseasesState.success(list));
            }

            @Override
            public void onError(String msg) {
                state.postValue(DiseasesState.error(msg));
            }
        });
    }
}
