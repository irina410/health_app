package com.example.healthapplication.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "patients")
public class PatientEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String email;
    public String name;
    public String surname;
    public String card;
}


