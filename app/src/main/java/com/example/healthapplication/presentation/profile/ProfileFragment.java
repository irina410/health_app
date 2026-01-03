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
import com.example.healthapplication.MainActivity;
import com.example.healthapplication.di.ServiceLocator;
import com.example.healthapplication.domain.models.User;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileFragment extends Fragment {
    private ProfileViewModel vm;
    private TextView tvName, tvEmail, tvRole, tvCard, tvDiseases;
    private TextView tvPhone, tvGenderBirth, tvMedHistory, tvSpecialty;
    private View sectionPatient, sectionDoctor;
    private android.widget.ImageView ivAvatar;
    private ProgressBar progressBar;
    private View btnLogout;

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
        tvPhone = v.findViewById(R.id.tv_phone);
        tvGenderBirth = v.findViewById(R.id.tv_gender_birth);
        tvMedHistory = v.findViewById(R.id.tv_med_history);
        tvSpecialty = v.findViewById(R.id.tv_specialty);
        sectionPatient = v.findViewById(R.id.section_patient);
        sectionDoctor = v.findViewById(R.id.section_doctor);
        ivAvatar = v.findViewById(R.id.iv_avatar);
        progressBar = v.findViewById(R.id.progress);
        btnLogout = v.findViewById(R.id.btn_logout);
        View btnEdit = v.findViewById(R.id.btn_edit);

        vm = new ViewModelProvider(this, ServiceLocator.provideProfileFactory())
                .get(ProfileViewModel.class);

        vm.getState().observe(getViewLifecycleOwner(), state -> {
            progressBar.setVisibility(state.loading ? View.VISIBLE : View.GONE);
            if (state.user != null) renderUser(state.user);
            if (state.error != null) {
                // Если профиль не найден — предлагаем создать/отредактировать
                if ("Пользователь не найден".equals(state.error)) {
                    Toast.makeText(getContext(), "Профиль отсутствует, заполните данные", Toast.LENGTH_SHORT).show();
                    // Открываем экран редактирования профиля
                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new com.example.healthapplication.presentation.editprofile.EditProfileFragment())
                            .addToBackStack(null)
                            .commit();
                } else {
                    Toast.makeText(getContext(), state.error, Toast.LENGTH_SHORT).show();
                }
            }
        });

        String email = null;
        if (getArguments() != null) email = getArguments().getString("email");
        if (TextUtils.isEmpty(email)) {
            // Берём email из FirebaseAuth; если нет пользователя — уводим на логин
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).showLoginUi(true);
                }
                return;
            } else {
                email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getContext(), "Не удалось определить email пользователя", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }

        btnLogout.setOnClickListener(v1 -> {
            FirebaseAuth.getInstance().signOut();
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).showLoginUi(true);
            }
        });

        btnEdit.setOnClickListener(v12 -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new com.example.healthapplication.presentation.editprofile.EditProfileFragment())
                    .addToBackStack(null)
                    .commit();
        });

        vm.processIntent(new ProfileIntent.LoadProfile(email));
    }

    private void renderUser(User user) {
        tvName.setText(String.format("%s %s %s", safe(user.getSurname()), safe(user.getName()), safe(user.getPatronymic())).trim());
        tvEmail.setText(safe(user.getEmail()));
        tvRole.setText(safe(user.getRole()));

        boolean isDoctor = "Врач".equals(user.getRole());
        sectionDoctor.setVisibility(isDoctor ? View.VISIBLE : View.GONE);
        sectionPatient.setVisibility(!isDoctor ? View.VISIBLE : View.GONE);

        if (isDoctor) {
            if (!safe(user.getSpecialty()).isEmpty()) {
                tvSpecialty.setText(safe(user.getSpecialty()));
            } else {
                tvSpecialty.setVisibility(View.GONE);
            }
        } else {
            // Phone
            if (!safe(user.getPhone()).isEmpty()) {
                tvPhone.setText(safe(user.getPhone()));
            } else {
                tvPhone.setVisibility(View.GONE);
            }
            
            // Gender/Birth
            String gender = safe(user.getGender());
            String birth = safe(user.getBirthDate());
            if (!gender.isEmpty() || !birth.isEmpty()) {
                tvGenderBirth.setText(String.format("%s%s%s",
                        gender,
                        (gender.isEmpty() || birth.isEmpty()) ? "" : " / ",
                        birth));
            } else {
                tvGenderBirth.setVisibility(View.GONE);
            }
            
            // Card removed for patients
            
            // Diseases
            if (user.getDiseases() != null && !user.getDiseases().isEmpty()) {
                tvDiseases.setText(android.text.TextUtils.join(", ", user.getDiseases()));
            } else {
                tvDiseases.setVisibility(View.GONE);
            }
            
            // Medical History
            if (!safe(user.getMedicalHistory()).isEmpty()) {
                tvMedHistory.setText(safe(user.getMedicalHistory()));
            } else {
                tvMedHistory.setVisibility(View.GONE);
            }
        }

        if (user.getAvatarUri() != null && !user.getAvatarUri().isEmpty()) {
            try {
                android.net.Uri uri = android.net.Uri.parse(user.getAvatarUri());
                ivAvatar.setImageURI(uri);
            } catch (Exception e) {
                android.util.Log.e("ProfileFragment", "Error loading avatar: " + e.getMessage());
                ivAvatar.setImageResource(com.example.healthapplication.R.mipmap.ic_launcher_round);
            }
        } else {
            ivAvatar.setImageResource(com.example.healthapplication.R.mipmap.ic_launcher_round);
        }
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}
