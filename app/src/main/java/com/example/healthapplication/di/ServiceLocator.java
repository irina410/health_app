package com.example.healthapplication.di;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.healthapplication.data.firebase.AuthRepository;
import com.example.healthapplication.data.firebase.UserRepository;
import com.example.healthapplication.domain.usecases.GetUserProfileUseCase;
import com.example.healthapplication.domain.usecases.LoginUseCase;
import com.example.healthapplication.presentation.profile.ProfileViewModel;
import com.example.healthapplication.presentation.signin.SignInViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import com.example.healthapplication.data.firebase.DiseaseRepository;
import com.example.healthapplication.domain.usecases.GetDiseasesUseCase;
import com.example.healthapplication.presentation.diseases.DiseasesViewModel;

import com.example.healthapplication.data.firebase.PatientRepository;
import com.example.healthapplication.domain.usecases.GetPatientsUseCase;
import com.example.healthapplication.presentation.patients.PatientsViewModel;

public class ServiceLocator {
    private static final FirebaseAuth auth = FirebaseAuth.getInstance();
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Repositories
    private static final AuthRepository authRepo = new AuthRepository(auth, db);
    private static final UserRepository userRepo = new UserRepository(db);

    // UseCases
    private static final LoginUseCase loginUseCase = new LoginUseCase(authRepo);
    private static final GetUserProfileUseCase getUserProfileUseCase = new GetUserProfileUseCase(userRepo);


    private static final DiseaseRepository diseaseRepo = new DiseaseRepository(db);
    private static final GetDiseasesUseCase getDiseasesUseCase = new GetDiseasesUseCase(diseaseRepo);


    private static final PatientRepository patientRepo = new PatientRepository(db);
    private static final GetPatientsUseCase getPatientsUseCase = new GetPatientsUseCase(patientRepo);

    // SignIn factory (уже был)
    public static ViewModelProvider.Factory provideSignInFactory() {
        return new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                if (modelClass.isAssignableFrom(SignInViewModel.class)) {
                    return (T) new SignInViewModel(loginUseCase);
                }
                throw new IllegalArgumentException("Unknown ViewModel class");
            }
        };
    }

    // Profile factory (новая)
    public static ViewModelProvider.Factory provideProfileFactory() {
        return new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                if (modelClass.isAssignableFrom(ProfileViewModel.class)) {
                    return (T) new ProfileViewModel(getUserProfileUseCase);
                }
                throw new IllegalArgumentException("Unknown ViewModel class");
            }
        };
    }

    public static ViewModelProvider.Factory provideDiseasesFactory() {
        return new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                if (modelClass.isAssignableFrom(DiseasesViewModel.class)) {
                    return (T) new DiseasesViewModel(getDiseasesUseCase);
                }
                throw new IllegalArgumentException("Unknown ViewModel class");
            }
        };
    }

    public static ViewModelProvider.Factory providePatientsFactory() {
        return new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                if (modelClass.isAssignableFrom(PatientsViewModel.class)) {
                    return (T) new PatientsViewModel(getPatientsUseCase);
                }
                throw new IllegalArgumentException("Unknown ViewModel class");
            }
        };
    }

}
