package com.example.healthapplication.presentation.patients;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.healthapplication.R;
import com.example.healthapplication.data.firebase.PatientRepository;
import com.example.healthapplication.di.ServiceLocator;

import java.util.List;

public class PatientsFragment extends Fragment {
    private PatientsViewModel vm;
    private ProgressBar progressBar;
    private LinearLayout container;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_patients, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        progressBar = v.findViewById(R.id.progress);
        container = v.findViewById(R.id.patients_list);

        vm = new ViewModelProvider(this, ServiceLocator.providePatientsFactory())
                .get(PatientsViewModel.class);

        vm.getState().observe(getViewLifecycleOwner(), state -> {
            progressBar.setVisibility(state.loading ? View.VISIBLE : View.GONE);
            if (state.error != null) {
                Toast.makeText(getContext(), state.error, Toast.LENGTH_SHORT).show();
            }
            if (state.patients != null) renderPatients(state.patients);
        });

        vm.processIntent(new PatientsIntent.LoadAll());
    }

    private void renderPatients(List<PatientRepository.Patient> list) {
        container.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext());
        for (PatientRepository.Patient p : list) {
            View item = inflater.inflate(R.layout.item_patient, container, false);
            ((TextView) item.findViewById(R.id.tv_name)).setText(p.surname + " " + p.name);
            ((TextView) item.findViewById(R.id.tv_email)).setText(p.email);
            ((TextView) item.findViewById(R.id.tv_card)).setText("Карта: " + p.card);
            container.addView(item);
        }
    }
}
