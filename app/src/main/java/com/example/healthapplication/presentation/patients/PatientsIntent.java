package com.example.healthapplication.presentation.patients;

public interface PatientsIntent {
    class LoadAll implements PatientsIntent {}
    class LoadUser implements PatientsIntent {
        public final String email;
        
        public LoadUser(String email) {
            this.email = email;
        }
    }
    class LinkPatient implements PatientsIntent {
        public final String doctorEmail;
        public final String patientEmail;
        public LinkPatient(String doctorEmail, String patientEmail) {
            this.doctorEmail = doctorEmail;
            this.patientEmail = patientEmail;
        }
    }
}
