package com.example.healthapplication.domain.models;

import java.util.List;

public class User {
    private String email;
    private String name;
    private String surname;
    private String patronymic;
    private String role;
    private String card;
    private List<String> diseases;
    // Optional, role-specific and UI fields
    private String phone;
    private String gender; // "М" / "Ж" or custom text
    private String birthDate; // ISO date string or any formatted date
    private String medicalHistory; // История болезни (text)
    private String specialty; // Должность врача (терапевт/лор/...)
    private String avatarUri; // content:// or https://

    public User() {}

    public User(String email, String name, String surname, String patronymic, String role, String card, List<String> diseases) {
        this.email = email;
        this.name = name;
        this.surname = surname;
        this.patronymic = patronymic;
        this.role = role;
        this.card = card;
        this.diseases = diseases;
    }

    public User(String email,
                String name,
                String surname,
                String patronymic,
                String role,
                String card,
                List<String> diseases,
                String phone,
                String gender,
                String birthDate,
                String medicalHistory,
                String specialty,
                String avatarUri) {
        this.email = email;
        this.name = name;
        this.surname = surname;
        this.patronymic = patronymic;
        this.role = role;
        this.card = card;
        this.diseases = diseases;
        this.phone = phone;
        this.gender = gender;
        this.birthDate = birthDate;
        this.medicalHistory = medicalHistory;
        this.specialty = specialty;
        this.avatarUri = avatarUri;
    }

    // region getters
    public String getEmail() { return email; }
    public String getName() { return name; }
    public String getSurname() { return surname; }
    public String getPatronymic() { return patronymic; }
    public String getRole() { return role; }
    public String getCard() { return card; }
    public List<String> getDiseases() { return diseases; }
    public String getPhone() { return phone; }
    public String getGender() { return gender; }
    public String getBirthDate() { return birthDate; }
    public String getMedicalHistory() { return medicalHistory; }
    public String getSpecialty() { return specialty; }
    public String getAvatarUri() { return avatarUri; }
    // endregion
}
