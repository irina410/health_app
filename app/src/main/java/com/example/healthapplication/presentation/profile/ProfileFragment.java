package com.example.healthapplication.presentation.profile;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.healthapplication.R;
import com.example.healthapplication.di.ServiceLocator;
import com.example.healthapplication.domain.models.User;

public class ProfileFragment extends Fragment {
    private ProfileViewModel vm;
    private TextView tvName, tvEmail, tvRole, tvCard, tvDiseases;
    private ProgressBar progressBar;

    // При создании фрагмента передай email либо через args, либо возьми от Auth (подключи FirebaseAuth если нужно)
    public static ProfileFragment newInstance(String email) {
        ProfileFragment f = new ProfileFragment();
        Bundle b = new Bundle();
        b.putString("email", email);
        f.setArguments(b);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        tvName = v.findViewById(R.id.tv_name);
        tvEmail = v.findViewById(R.id.tv_email);
        tvRole = v.findViewById(R.id.tv_role);
        tvCard = v.findViewById(R.id.tv_card);
        tvDiseases = v.findViewById(R.id.tv_diseases);
        progressBar = v.findViewById(R.id.progress);

        vm = new ViewModelProvider(this, ServiceLocator.provideProfileFactory())
                .get(ProfileViewModel.class);

        vm.getState().observe(getViewLifecycleOwner(), state -> {
            progressBar.setVisibility(state.loading ? View.VISIBLE : View.GONE);
            if (state.user != null) renderUser(state.user);
            if (state.error != null) {
                Toast.makeText(getContext(), state.error, Toast.LENGTH_SHORT).show();
            }
        });

        String email = null;
        if (getArguments() != null) email = getArguments().getString("email");
        if (TextUtils.isEmpty(email)) {
            // Альтернатива: взять текущий email из FirebaseAuth
            Toast.makeText(getContext(), "Email для профиля не передан", Toast.LENGTH_SHORT).show();
            return;
        }

        vm.processIntent(new ProfileIntent.LoadProfile(email));
    }

    private void renderUser(User user) {
        tvName.setText(String.format("%s %s %s", safe(user.getSurname()), safe(user.getName()), safe(user.getPatronymic())).trim());
        tvEmail.setText(safe(user.getEmail()));
        tvRole.setText(safe(user.getRole()));
        tvCard.setText(safe(user.getCard()));

        if (user.getDiseases() != null && !user.getDiseases().isEmpty()) {
            tvDiseases.setText(android.text.TextUtils.join(", ", user.getDiseases()));
        } else {
            tvDiseases.setText("Нет данных");
        }
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}
