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

    // region getters
    public String getEmail() { return email; }
    public String getName() { return name; }
    public String getSurname() { return surname; }
    public String getPatronymic() { return patronymic; }
    public String getRole() { return role; }
    public String getCard() { return card; }
    public List<String> getDiseases() { return diseases; }
    // endregion
}
