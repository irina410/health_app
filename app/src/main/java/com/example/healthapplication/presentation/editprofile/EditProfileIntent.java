package com.example.healthapplication.presentation.editprofile;

public interface EditProfileIntent {
    class Submit implements EditProfileIntent {
        public final String email;
        public final String name;
        public final String surname;
        public final String patronymic;
        public final String role;
        public final String card; // patient only
        public final String diseasesCsv; // patient only (read-only for patient UI)
        public final String phone; // patient only
        public final String gender; // patient only
        public final String birthDate; // patient only
        public final String medicalHistory; // patient only (read-only for patient UI)
        public final String specialty; // doctor only
        public final String avatarUri; // both

        public Submit(String email, String name, String surname, String patronymic, String role,
                       String card, String diseasesCsv, String phone, String gender, String birthDate,
                       String medicalHistory, String specialty, String avatarUri) {
            this.email = email;
            this.name = name;
            this.surname = surname;
            this.patronymic = patronymic;
            this.role = role;
            this.card = card;
            this.diseasesCsv = diseasesCsv;
            this.phone = phone;
            this.gender = gender;
            this.birthDate = birthDate;
            this.medicalHistory = medicalHistory;
            this.specialty = specialty;
            this.avatarUri = avatarUri;
        }
    }
    
    class LoadUser implements EditProfileIntent {
        public final String email;
        
        public LoadUser(String email) {
            this.email = email;
        }
    }
}


