package com.example.healthapplication.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface PatientDao {
    @Query("SELECT * FROM patients ORDER BY surname ASC, name ASC")
    List<PatientEntity> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(PatientEntity entity);

    @Query("DELETE FROM patients")
    void clear();
}


