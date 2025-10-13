package com.example.healthapplication.presentation.diseases;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.healthapplication.R;
import com.example.healthapplication.di.ServiceLocator;
import com.example.healthapplication.data.firebase.DiseaseRepository;

import java.util.List;

public class DiseasesFragment extends Fragment {
    private DiseasesViewModel vm;
    private ProgressBar progressBar;
    private LinearLayout container;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_diseases, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        progressBar = v.findViewById(R.id.progress);
        container = v.findViewById(R.id.disease_list);

        vm = new ViewModelProvider(this, ServiceLocator.provideDiseasesFactory())
                .get(DiseasesViewModel.class);

        vm.getState().observe(getViewLifecycleOwner(), state -> {
            progressBar.setVisibility(state.loading ? View.VISIBLE : View.GONE);
            if (state.error != null) {
                Toast.makeText(getContext(), state.error, Toast.LENGTH_SHORT).show();
            }
            if (state.diseases != null) renderList(state.diseases);
        });

        vm.processIntent(new DiseasesIntent.LoadAll());
    }

    private void renderList(List<DiseaseRepository.Disease> list) {
        container.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext());
        for (DiseaseRepository.Disease d : list) {
            View item = inflater.inflate(R.layout.item_disease, container, false);
            ((TextView) item.findViewById(R.id.tv_name)).setText(d.name);
            ((TextView) item.findViewById(R.id.tv_description)).setText(d.description);
            container.addView(item);
        }
    }
}
