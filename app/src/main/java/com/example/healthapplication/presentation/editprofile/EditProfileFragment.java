package com.example.healthapplication.presentation.editprofile;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.healthapplication.R;
import com.example.healthapplication.di.ServiceLocator;
import com.example.healthapplication.domain.models.User;
import com.google.firebase.auth.FirebaseAuth;

public class EditProfileFragment extends Fragment {
    private EditProfileViewModel vm;
    private ActivityResultLauncher<String> imagePicker;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imagePicker = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (getView() == null) return;
            View ivAvatar = getView().findViewById(R.id.iv_avatar);
            if (uri != null && ivAvatar instanceof android.widget.ImageView) {
                try {
                    // Grant persistent permission to the URI
                    requireContext().getContentResolver().takePersistableUriPermission(uri, 
                        android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    ((android.widget.ImageView) ivAvatar).setImageURI(uri);
                    ivAvatar.setTag(uri.toString());
                } catch (Exception e) {
                    android.util.Log.e("ImagePicker", "Error loading image: " + e.getMessage());
                    // Fallback to default image
                    ((android.widget.ImageView) ivAvatar).setImageResource(R.mipmap.ic_launcher_round);
                    ivAvatar.setTag(null);
                }
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        EditText etName = v.findViewById(R.id.et_name);
        EditText etSurname = v.findViewById(R.id.et_surname);
        EditText etPatronymic = v.findViewById(R.id.et_patronymic);
        Switch switchRole = v.findViewById(R.id.switch_role);
        TextView tvRoleDisplay = v.findViewById(R.id.tv_role_display);
        EditText etPhone = v.findViewById(R.id.et_phone);
        android.widget.Switch swGender = v.findViewById(R.id.sw_gender);
        TextView tvGenderLabel = v.findViewById(R.id.tv_gender_label);
        EditText etBirthDate = v.findViewById(R.id.et_birthdate);
        // Add date mask for birth date
        etBirthDate.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(android.text.Editable s) {
                String text = s.toString().replaceAll("[^0-9]", "");
                if (text.length() >= 8) {
                    text = text.substring(0, 8);
                    String formatted = text.substring(0, 2) + "." + text.substring(2, 4) + "." + text.substring(4, 8);
                    if (!s.toString().equals(formatted)) {
                        etBirthDate.setText(formatted);
                        etBirthDate.setSelection(formatted.length());
                    }
                }
            }
        });
        EditText etDiseases = v.findViewById(R.id.et_diseases);
        EditText etMedHistory = v.findViewById(R.id.et_med_history);
        EditText etSpecialty = v.findViewById(R.id.et_specialty);
        View ivAvatar = v.findViewById(R.id.iv_avatar);
        Button btnSave = v.findViewById(R.id.btn_save);

        vm = new ViewModelProvider(this, ServiceLocator.provideEditProfileFactory())
                .get(EditProfileViewModel.class);

        // Настройка ползунка роли
        switchRole.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                tvRoleDisplay.setText("Врач");
                etDiseases.setVisibility(View.GONE);
                etMedHistory.setVisibility(View.GONE);
                etSpecialty.setVisibility(View.VISIBLE);
                etPhone.setVisibility(View.GONE);
                swGender.setVisibility(View.GONE);
                tvGenderLabel.setVisibility(View.GONE);
                etBirthDate.setVisibility(View.GONE);
            } else {
                tvRoleDisplay.setText("Пациент");
                etDiseases.setVisibility(View.VISIBLE);
                etMedHistory.setVisibility(View.VISIBLE);
                etSpecialty.setVisibility(View.GONE);
                etPhone.setVisibility(View.VISIBLE);
                swGender.setVisibility(View.VISIBLE);
                tvGenderLabel.setVisibility(View.VISIBLE);
                etBirthDate.setVisibility(View.VISIBLE);
            }
        });

        // Update gender label when switch changes
        swGender.setOnCheckedChangeListener((buttonView, isChecked) -> {
            tvGenderLabel.setText(isChecked ? "Женский" : "Мужской");
        });

        ivAvatar.setOnClickListener(v1 -> {
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setMessage("Хотите изменить аватарку?")
                .setPositiveButton("Да", (d, which) -> imagePicker.launch("image/*"))
                .setNegativeButton("Нет", null)
                .show();
        });

        vm.getState().observe(getViewLifecycleOwner(), state -> {
            btnSave.setEnabled(!state.loading);
            if (state.loading) btnSave.setText("Сохранение..."); else btnSave.setText("Сохранить");
            if (state.message != null) Toast.makeText(getContext(), state.message, Toast.LENGTH_SHORT).show();
            if (state.success) {
                requireActivity().onBackPressed();
            }
            if (state.user != null) {
                // Предзаполнение данных
                etName.setText(state.user.getName() != null ? state.user.getName() : "");
                etSurname.setText(state.user.getSurname() != null ? state.user.getSurname() : "");
                etPatronymic.setText(state.user.getPatronymic() != null ? state.user.getPatronymic() : "");
                
                boolean isDoctor = "Врач".equals(state.user.getRole());
                switchRole.setChecked(isDoctor);
                tvRoleDisplay.setText(isDoctor ? "Врач" : "Пациент");
                
                if (isDoctor) {
                    etDiseases.setVisibility(View.GONE);
                    etMedHistory.setVisibility(View.GONE);
                    etSpecialty.setVisibility(View.VISIBLE);
                } else {
                    etDiseases.setVisibility(View.VISIBLE);
                    etMedHistory.setVisibility(View.VISIBLE);
                    etSpecialty.setVisibility(View.GONE);
                    if (state.user.getDiseases() != null && !state.user.getDiseases().isEmpty()) {
                        etDiseases.setText(android.text.TextUtils.join(", ", state.user.getDiseases()));
                    }
                }

                // Prefill common optional fields
                etPhone.setText(state.user.getPhone() != null ? state.user.getPhone() : "");
                swGender.setChecked("Ж".equalsIgnoreCase(state.user.getGender()));
                tvGenderLabel.setText(swGender.isChecked() ? "Женский" : "Мужской");
                etBirthDate.setText(state.user.getBirthDate() != null ? state.user.getBirthDate() : "");
                etMedHistory.setText(state.user.getMedicalHistory() != null ? state.user.getMedicalHistory() : "");
                etSpecialty.setText(state.user.getSpecialty() != null ? state.user.getSpecialty() : "");
                if (state.user.getAvatarUri() != null && !state.user.getAvatarUri().isEmpty() && ivAvatar instanceof android.widget.ImageView) {
                    ((android.widget.ImageView) ivAvatar).setImageURI(android.net.Uri.parse(state.user.getAvatarUri()));
                    ivAvatar.setTag(state.user.getAvatarUri());
                }
            }
        });

        btnSave.setOnClickListener(view -> {
            String email = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getEmail() : null;
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(getContext(), "Не найден email", Toast.LENGTH_SHORT).show();
                return;
            }
            
            String role = switchRole.isChecked() ? "Врач" : "Пациент";
            String card = ""; // карта удалена для пациента
            String diseases = switchRole.isChecked() ? "" : etDiseases.getText().toString().trim();
            String phone = switchRole.isChecked() ? "" : etPhone.getText().toString().trim();
            String gender = switchRole.isChecked() ? "" : (swGender.isChecked() ? "Ж" : "М");
            String birth = switchRole.isChecked() ? "" : etBirthDate.getText().toString().trim();
            String medHistory = switchRole.isChecked() ? "" : etMedHistory.getText().toString().trim();
            String specialty = switchRole.isChecked() ? etSpecialty.getText().toString().trim() : "";
            String avatar = ivAvatar.getTag() instanceof String ? (String) ivAvatar.getTag() : "";

            vm.processIntent(new EditProfileIntent.Submit(
                    email,
                    etName.getText().toString().trim(),
                    etSurname.getText().toString().trim(),
                    etPatronymic.getText().toString().trim(),
                    role,
                    card,
                    diseases,
                    phone,
                    gender,
                    birth,
                    medHistory,
                    specialty,
                    avatar
            ));
        });

        // Загружаем текущие данные пользователя
        String email = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getEmail() : null;
        if (!TextUtils.isEmpty(email)) {
            vm.processIntent(new EditProfileIntent.LoadUser(email));
        }
    }
}


