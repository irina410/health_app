package com.example.healthapplication.presentation.patients;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.healthapplication.R;
import com.example.healthapplication.data.firebase.PatientRepository;
import com.example.healthapplication.di.ServiceLocator;
import com.example.healthapplication.domain.models.User;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class PatientsFragment extends Fragment {
    private PatientsViewModel vm;
    private ProgressBar progressBar;
    private LinearLayout container;
    private FloatingActionButton fabAddPatient;
    private User currentUser;
    private final java.util.Map<String, View> emailToItem = new java.util.HashMap<>();
    private com.google.firebase.firestore.ListenerRegistration linksRegistration;
    private final java.util.Map<String, com.google.firebase.firestore.ListenerRegistration> userRegistrations = new java.util.HashMap<>();

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
        fabAddPatient = v.findViewById(R.id.fab_add_patient);

        vm = new ViewModelProvider(this, ServiceLocator.providePatientsFactory())
                .get(PatientsViewModel.class);

        vm.getState().observe(getViewLifecycleOwner(), state -> {
            progressBar.setVisibility(state.loading ? View.VISIBLE : View.GONE);
            if (state.error != null) {
                Toast.makeText(getContext(), state.error, Toast.LENGTH_SHORT).show();
            }
            if (state.patients != null) renderPatients(state.patients);
            if (state.user != null) {
                currentUser = state.user;
                updateFabVisibility();
                updateTabTitle();
            }
            // Handle success message for linking
            if (state.loading == false && state.error == null && state.patients == null && state.user == null) {
                Toast.makeText(getContext(), "Пациент добавлен", Toast.LENGTH_SHORT).show();
            }
        });

        vm.processIntent(new PatientsIntent.LoadAll());
        loadCurrentUser();

        fabAddPatient.setOnClickListener(view -> openLinkPatientDialog());
    }

    private void renderPatients(List<PatientRepository.Patient> list) {
        container.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext());
        for (PatientRepository.Patient p : list) {
            View item = inflater.inflate(R.layout.item_patient, container, false);
            ((TextView) item.findViewById(R.id.tv_name)).setText(p.surname + " " + p.name);
            ((TextView) item.findViewById(R.id.tv_email)).setText(p.email);
            ((TextView) item.findViewById(R.id.tv_card)).setText("Карта: " + p.card);

            item.setOnClickListener(v -> openPatientProfile(p.email));

            container.addView(item);
        }
    }

    private void openAddPatientDialog() {
        android.app.AlertDialog.Builder b = new android.app.AlertDialog.Builder(requireContext());
        View dialog = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_patient, null);
        b.setView(dialog);

        final android.app.AlertDialog d = b.create();
        dialog.findViewById(R.id.btn_cancel).setOnClickListener(v -> d.dismiss());
        dialog.findViewById(R.id.btn_save).setOnClickListener(v -> {
            String email = ((android.widget.EditText) dialog.findViewById(R.id.et_email)).getText().toString().trim();
            String name = ((android.widget.EditText) dialog.findViewById(R.id.et_name)).getText().toString().trim();
            String surname = ((android.widget.EditText) dialog.findViewById(R.id.et_surname)).getText().toString().trim();
            String card = ((android.widget.EditText) dialog.findViewById(R.id.et_card)).getText().toString().trim();
            String diseases = ((android.widget.EditText) dialog.findViewById(R.id.et_diseases)).getText().toString().trim();
            if (email.isEmpty()) { android.widget.Toast.makeText(getContext(), "Email обязателен", android.widget.Toast.LENGTH_SHORT).show(); return; }

            java.util.List<String> diseasesList = new java.util.ArrayList<>();
            if (!diseases.isEmpty()) {
                String[] diseasesArray = diseases.split(",");
                for (String disease : diseasesArray) {
                    diseasesList.add(disease.trim());
                }
            }
            
            com.example.healthapplication.domain.usecases.AddPatientUseCase useCase = ServiceLocator.provideAddPatientUseCase();
            useCase.execute(new PatientRepository.Patient(email, name, surname, card, diseasesList), new com.example.healthapplication.domain.usecases.AddPatientUseCase.Callback() {
                @Override
                public void onSuccess() {
                    requireActivity().runOnUiThread(() -> {
                        android.widget.Toast.makeText(getContext(), "Пациент добавлен", android.widget.Toast.LENGTH_SHORT).show();
                        vm.processIntent(new PatientsIntent.LoadAll());
                        d.dismiss();
                    });
                }

                @Override
                public void onError(String msg) {
                    requireActivity().runOnUiThread(() -> android.widget.Toast.makeText(getContext(), msg, android.widget.Toast.LENGTH_SHORT).show());
                }
            });
        });
        d.show();
    }

    private void openLinkPatientDialog() {
        // FAB виден только врачу, поэтому не блокируем по currentUser
        String doctorEmail = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getEmail() : null;
        android.app.AlertDialog.Builder b = new android.app.AlertDialog.Builder(requireContext());
        // Simple dialog with single EditText for patient email
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        layout.setPadding(pad, pad, pad, pad);
        final android.widget.EditText et = new android.widget.EditText(getContext());
        et.setHint("Email пациента");
        layout.addView(et);
        b.setView(layout);
        b.setPositiveButton("Добавить", (dialog, which) -> {
            String patientEmail = et.getText().toString().trim();
            if (patientEmail.isEmpty()) {
                Toast.makeText(getContext(), "Введите email", Toast.LENGTH_SHORT).show();
                return;
            }
            if (doctorEmail == null || doctorEmail.isEmpty()) {
                Toast.makeText(getContext(), "Не найден email врача", Toast.LENGTH_SHORT).show();
                return;
            }
            vm.processIntent(new PatientsIntent.LinkPatient(doctorEmail, patientEmail));
        });
        b.setNegativeButton("Отмена", null);
        b.show();
    }
    
    private void loadCurrentUser() {
        String email = FirebaseAuth.getInstance().getCurrentUser() != null ? 
                FirebaseAuth.getInstance().getCurrentUser().getEmail() : null;
        if (email != null) {
            vm.processIntent(new PatientsIntent.LoadUser(email));
        }
    }
    
    private void updateFabVisibility() {
        if (currentUser != null && "Врач".equals(currentUser.getRole())) {
            fabAddPatient.setVisibility(View.VISIBLE);
            // Load linked patients for doctor view
            startDoctorPatientsRealtime();
        } else {
            fabAddPatient.setVisibility(View.GONE);
            stopDoctorPatientsRealtime();
            // For patients, load their doctors
            if ("Пациент".equals(currentUser.getRole())) {
                startPatientDoctorsRealtime();
            }
        }
    }

    private void updateTabTitle() {
        if (currentUser != null && "Пациент".equals(currentUser.getRole())) {
            // For patients, this tab should show "Врачи"
            if (getActivity() != null) {
                getActivity().setTitle("Врачи");
            }
        }
    }
    
    private void openPatientProfile(String patientEmail) {
        // Создаем диалог для просмотра профиля пациента
        android.app.AlertDialog.Builder b = new android.app.AlertDialog.Builder(requireContext());
        View dialog = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_patient_profile, null);
        b.setView(dialog);
        
        final android.app.AlertDialog d = b.create();
        ((TextView) dialog.findViewById(R.id.tv_patient_email)).setText(patientEmail);

        // Подгружаем профиль пользователя с кэшированием
        com.example.healthapplication.data.firebase.UserRepository userRepo = new com.example.healthapplication.data.firebase.UserRepository(com.google.firebase.firestore.FirebaseFirestore.getInstance());
        userRepo.getUserByEmail(patientEmail, new com.example.healthapplication.data.firebase.UserRepository.Callback() {
            @Override
            public void onSuccess(com.example.healthapplication.domain.models.User user) {
                if (!isAdded()) return;
                TextView tvName = dialog.findViewById(R.id.tv_patient_name);
                tvName.setText((user.getSurname() + " " + user.getName()).trim());
                TextView tvDis = dialog.findViewById(R.id.tv_patient_diseases);
                if (user.getDiseases() != null && !user.getDiseases().isEmpty()) {
                    tvDis.setText(android.text.TextUtils.join(", ", user.getDiseases()));
                } else tvDis.setText("Нет данных");
                TextView tvHist = dialog.findViewById(R.id.tv_patient_history);
                tvHist.setText(user.getMedicalHistory() == null ? "" : user.getMedicalHistory());
                android.widget.ImageView iv = dialog.findViewById(R.id.iv_patient_avatar);
                if (user.getAvatarUri() != null && !user.getAvatarUri().isEmpty()) {
                    try {
                        iv.setImageURI(android.net.Uri.parse(user.getAvatarUri()));
                    } catch (Exception e) {
                        android.util.Log.e("PatientsFragment", "Error loading patient avatar: " + e.getMessage());
                        iv.setImageResource(R.mipmap.ic_launcher_round);
                    }
                }
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) return;
                TextView tvName = dialog.findViewById(R.id.tv_patient_name);
                tvName.setText("Профиль не найден");
            }
        });

        // Кнопка редактирования (только для врачей)
        View btnEdit = dialog.findViewById(R.id.btn_edit_patient);
        if (currentUser != null && "Врач".equals(currentUser.getRole())) {
            btnEdit.setVisibility(View.VISIBLE);
            btnEdit.setOnClickListener(v -> {
                d.dismiss();
                openEditPatientMedicalDialog(patientEmail);
            });
        } else {
            btnEdit.setVisibility(View.GONE);
        }
        
        // Кнопка закрытия
        dialog.findViewById(R.id.btn_close).setOnClickListener(v -> d.dismiss());
        
        d.show();
    }

    private void openEditPatientMedicalDialog(String patientEmail) {
        android.app.AlertDialog.Builder b = new android.app.AlertDialog.Builder(requireContext());
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        layout.setPadding(pad, pad, pad, pad);
        final android.widget.EditText etDiseases = new android.widget.EditText(requireContext());
        etDiseases.setHint("Диагнозы (через запятую)");
        final android.widget.EditText etHistory = new android.widget.EditText(requireContext());
        etHistory.setHint("История болезни");
        layout.addView(etDiseases);
        layout.addView(etHistory);
        b.setView(layout);

        // prefill from user profile
        com.example.healthapplication.data.firebase.UserRepository ur = new com.example.healthapplication.data.firebase.UserRepository(com.google.firebase.firestore.FirebaseFirestore.getInstance());
        ur.getUserByEmail(patientEmail, new com.example.healthapplication.data.firebase.UserRepository.Callback() {
            @Override
            public void onSuccess(com.example.healthapplication.domain.models.User user) {
                if (user.getDiseases() != null && !user.getDiseases().isEmpty()) {
                    etDiseases.setText(android.text.TextUtils.join(", ", user.getDiseases()));
                }
                if (user.getMedicalHistory() != null) etHistory.setText(user.getMedicalHistory());
            }

            @Override
            public void onError(String message) { }
        });

        b.setPositiveButton("Сохранить", (dialog, which) -> {
            java.util.List<String> diseasesList = new java.util.ArrayList<>();
            String diseases = etDiseases.getText().toString().trim();
            if (!diseases.isEmpty()) {
                String[] arr = diseases.split(",");
                for (String s : arr) diseasesList.add(s.trim());
            }
            String history = etHistory.getText().toString().trim();

            ur.getUserByEmail(patientEmail, new com.example.healthapplication.data.firebase.UserRepository.Callback() {
                @Override
                public void onSuccess(com.example.healthapplication.domain.models.User user) {
                    com.example.healthapplication.domain.models.User updated = new com.example.healthapplication.domain.models.User(
                            user.getEmail(), user.getName(), user.getSurname(), user.getPatronymic(), user.getRole(), user.getCard(), diseasesList,
                            user.getPhone(), user.getGender(), user.getBirthDate(), history, user.getSpecialty(), user.getAvatarUri());
                    new com.example.healthapplication.domain.usecases.UpsertUserUseCase(ur).execute(updated, new com.example.healthapplication.domain.usecases.UpsertUserUseCase.Callback() {
                        @Override
                        public void onSuccess() {
                            if (!isAdded()) return;
                            android.widget.Toast.makeText(requireContext(), "Обновлено", android.widget.Toast.LENGTH_SHORT).show();
                            vm.processIntent(new PatientsIntent.LoadAll());
                        }

                        @Override
                        public void onError(String msg) {
                            if (!isAdded()) return;
                            android.widget.Toast.makeText(requireContext(), msg, android.widget.Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onError(String message) {
                    if (!isAdded()) return;
                    android.widget.Toast.makeText(requireContext(), "Профиль не найден", android.widget.Toast.LENGTH_SHORT).show();
                }
            });
        });
        b.setNegativeButton("Отмена", null);
        b.show();
    }

    private void startDoctorPatientsRealtime() {
        if (currentUser == null || currentUser.getEmail() == null) return;
        stopDoctorPatientsRealtime();
        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
        linksRegistration = db.collection("doctor_patients")
                .whereEqualTo("doctorEmail", currentUser.getEmail())
                .addSnapshotListener((snapshots, e) -> {
                    if (!isAdded()) return;
                    if (e != null) return;
                    container.removeAllViews();
                    emailToItem.clear();
                    for (com.google.firebase.firestore.DocumentSnapshot ds : snapshots.getDocuments()) {
                        String email = String.valueOf(ds.get("patientEmail"));
                        View item = LayoutInflater.from(requireContext()).inflate(R.layout.item_doctor_patient, container, false);
                        ((TextView) item.findViewById(R.id.tv_subtitle)).setText(email);
                    item.setOnClickListener(v -> openPatientProfile(email));
                    item.setOnLongClickListener(v -> {
                        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                            .setMessage("Хотите удалить пациента из списка?")
                            .setPositiveButton("Да", (dialog, which) -> removePatientFromDoctor(email))
                            .setNegativeButton("Нет", null)
                            .show();
                        return true;
                    });
                    container.addView(item);
                    emailToItem.put(email, item);

                        // attach per-user profile listener
                        attachUserListener(email);
                    }
                });
    }

    private void attachUserListener(String email) {
        if (userRegistrations.containsKey(email)) return;
        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
        com.google.firebase.firestore.ListenerRegistration reg = db.collection("users")
                .document(email)
                .addSnapshotListener((ds, e) -> {
                    if (!isAdded()) return;
                    View item = emailToItem.get(email);
                    if (item == null) return;
                    if (e != null || ds == null || !ds.exists()) {
                        ((TextView) item.findViewById(R.id.tv_title)).setText("");
                        return;
                    }
                    Object name = ds.get("name");
                    Object surname = ds.get("surname");
                    Object avatar = ds.get("avatarUri");
                    ((TextView) item.findViewById(R.id.tv_title)).setText(((surname == null ? "" : surname.toString()) + " " + (name == null ? "" : name.toString())).trim());
                    android.widget.ImageView iv = item.findViewById(R.id.iv_avatar);
                    if (avatar != null && !String.valueOf(avatar).isEmpty()) {
                        try {
                            iv.setImageURI(android.net.Uri.parse(String.valueOf(avatar)));
                        } catch (Exception e) {
                            android.util.Log.e("PatientsFragment", "Error loading avatar: " + e.getMessage());
                            iv.setImageResource(R.mipmap.ic_launcher_round);
                        }
                    }
                });
        userRegistrations.put(email, reg);
    }

    private void stopDoctorPatientsRealtime() {
        if (linksRegistration != null) { linksRegistration.remove(); linksRegistration = null; }
        for (com.google.firebase.firestore.ListenerRegistration r : userRegistrations.values()) { r.remove(); }
        userRegistrations.clear();
        emailToItem.clear();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopDoctorPatientsRealtime();
    }

    private void startPatientDoctorsRealtime() {
        if (currentUser == null || currentUser.getEmail() == null) return;
        String patientEmail = currentUser.getEmail();
        
        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
        linksRegistration = db.collection("doctor_patients")
                .whereEqualTo("patientEmail", patientEmail)
                .addSnapshotListener((snapshots, e) -> {
                    if (!isAdded()) return;
                    if (e != null) {
                        Toast.makeText(requireContext(), "Ошибка загрузки врачей: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (snapshots == null) return;
                    
                    LinearLayout container = getView().findViewById(R.id.patients_list);
                    if (container == null) return;
                    
                    container.removeAllViews();
                    userRegistrations.clear();
                    emailToItem.clear();
                    
                    for (com.google.firebase.firestore.DocumentSnapshot ds : snapshots.getDocuments()) {
                        String doctorEmail = String.valueOf(ds.get("doctorEmail"));
                        View item = LayoutInflater.from(requireContext()).inflate(R.layout.item_doctor_patient, container, false);
                        ((TextView) item.findViewById(R.id.tv_subtitle)).setText(doctorEmail);
                        
                        item.setOnClickListener(v -> openDoctorProfile(doctorEmail));
                        // No long press for patients - they can't delete doctors
                        
                        container.addView(item);
                        emailToItem.put(doctorEmail, item);
                        
                        // Attach per-doctor profile listener
                        attachDoctorListener(doctorEmail);
                    }
                });
    }

    private void attachDoctorListener(String doctorEmail) {
        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
        com.google.firebase.firestore.ListenerRegistration reg = db.collection("users")
                .document(doctorEmail)
                .addSnapshotListener((ds, e) -> {
                    if (!isAdded()) return;
                    View item = emailToItem.get(doctorEmail);
                    if (item == null) return;
                    if (e != null || ds == null || !ds.exists()) {
                        ((TextView) item.findViewById(R.id.tv_title)).setText("");
                        return;
                    }
                    Object name = ds.get("name");
                    Object surname = ds.get("surname");
                    Object specialty = ds.get("specialty");
                    Object avatar = ds.get("avatarUri");
                    ((TextView) item.findViewById(R.id.tv_title)).setText(((surname == null ? "" : surname.toString()) + " " + (name == null ? "" : name.toString())).trim());
                    ((TextView) item.findViewById(R.id.tv_subtitle)).setText(doctorEmail + (specialty != null ? " - " + specialty.toString() : ""));
                    android.widget.ImageView iv = item.findViewById(R.id.iv_avatar);
                    if (avatar != null && !String.valueOf(avatar).isEmpty()) {
                        try {
                            iv.setImageURI(android.net.Uri.parse(String.valueOf(avatar)));
                        } catch (Exception e) {
                            android.util.Log.e("PatientsFragment", "Error loading avatar: " + e.getMessage());
                            iv.setImageResource(R.mipmap.ic_launcher_round);
                        }
                    }
                });
        userRegistrations.put(doctorEmail, reg);
    }

    private void openDoctorProfile(String doctorEmail) {
        // Создаем диалог для просмотра профиля врача
        android.app.AlertDialog.Builder b = new android.app.AlertDialog.Builder(requireContext());
        View dialog = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_doctor_profile, null);
        b.setView(dialog);
        
        final android.app.AlertDialog d = b.create();
        ((TextView) dialog.findViewById(R.id.tv_doctor_email)).setText(doctorEmail);

        // Подгружаем профиль врача с кэшированием
        com.example.healthapplication.data.firebase.UserRepository userRepo = new com.example.healthapplication.data.firebase.UserRepository(com.google.firebase.firestore.FirebaseFirestore.getInstance());
        userRepo.getUserByEmail(doctorEmail, new com.example.healthapplication.data.firebase.UserRepository.Callback() {
            @Override
            public void onSuccess(com.example.healthapplication.domain.models.User user) {
                if (!isAdded()) return;
                TextView tvName = dialog.findViewById(R.id.tv_doctor_name);
                tvName.setText((user.getSurname() + " " + user.getName()).trim());
                TextView tvSpecialty = dialog.findViewById(R.id.tv_doctor_specialty);
                tvSpecialty.setText(user.getSpecialty() == null ? "Не указано" : user.getSpecialty());
                android.widget.ImageView iv = dialog.findViewById(R.id.iv_doctor_avatar);
                if (user.getAvatarUri() != null && !user.getAvatarUri().isEmpty()) {
                    try {
                        iv.setImageURI(android.net.Uri.parse(user.getAvatarUri()));
                    } catch (Exception e) {
                        android.util.Log.e("PatientsFragment", "Error loading doctor avatar: " + e.getMessage());
                        iv.setImageResource(R.mipmap.ic_launcher_round);
                    }
                }
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) return;
                TextView tvName = dialog.findViewById(R.id.tv_doctor_name);
                tvName.setText("Профиль не найден");
            }
        });

        d.show();
    }

    private void removePatientFromDoctor(String patientEmail) {
        if (currentUser == null || currentUser.getEmail() == null) return;
        String doctorEmail = currentUser.getEmail();
        String docId = doctorEmail + "__" + patientEmail;
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("doctor_patients")
                .document(docId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    if (!isAdded()) return;
                    Toast.makeText(requireContext(), "Пациент удален", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    Toast.makeText(requireContext(), "Ошибка удаления: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
