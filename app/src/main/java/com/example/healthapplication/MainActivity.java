package com.example.healthapplication;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.healthapplication.presentation.profile.ProfileFragment;
import com.example.healthapplication.presentation.diseases.DiseasesFragment;
import com.example.healthapplication.presentation.patients.PatientsFragment;
import com.example.healthapplication.presentation.signin.SignInFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        com.example.healthapplication.di.ServiceLocator.init(this);

        auth = FirebaseAuth.getInstance();
        bottomNav = findViewById(R.id.bottom_nav);
        
        // Проверяем, залогинен ли пользователь
        if (auth.getCurrentUser() == null) {
            showLoginUi(savedInstanceState == null);
        } else {
            showAuthenticatedUi(savedInstanceState == null);
        }
    }

    public void showAuthenticatedUi(boolean initial) {
        bottomNav.setVisibility(View.VISIBLE);
        bottomNav.setOnItemSelectedListener(this::onNavSelected);
        if (initial) {
            loadFragment(createProfileFragment());
            bottomNav.setSelectedItemId(R.id.menu_profile);
        }
    }

    public void showAuthenticatedUi() {
        showAuthenticatedUi(true);
    }

    public void showLoginUi(boolean initial) {
        bottomNav.setVisibility(View.GONE);
        if (initial) {
            loadFragment(new SignInFragment());
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
