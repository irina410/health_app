package com.example.healthapplication.data.local;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {PatientEntity.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract PatientDao patientDao();
}


