package com.example.healthapplication;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.healthapplication.presentation.profile.ProfileFragment;
import com.example.healthapplication.presentation.diseases.DiseasesFragment;
import com.example.healthapplication.presentation.patients.PatientsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnItemSelectedListener(this::onNavSelected);

        // дефолтно показываем профиль
        if (savedInstanceState == null) {
            loadFragment(createProfileFragment());
            bottomNav.setSelectedItemId(R.id.menu_profile);
        }
    }

    private boolean onNavSelected(@NonNull MenuItem item) {
        Fragment fragment;
        int id = item.getItemId();

        if (id == R.id.menu_profile) {
            fragment = createProfileFragment();
        } else if (id == R.id.menu_diseases) {
            fragment = new DiseasesFragment();
        } else if (id == R.id.menu_patients) {
            fragment = new PatientsFragment();
        } else {
            return false;
        }

        loadFragment(fragment);
        return true;
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private ProfileFragment createProfileFragment() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String email = auth.getCurrentUser() != null ? auth.getCurrentUser().getEmail() : null;
        return ProfileFragment.newInstance(email != null ? email : "");
    }
}
