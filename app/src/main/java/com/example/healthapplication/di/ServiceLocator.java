package com.example.healthapplication.di;

import androidx.annotation.NonNull;
import android.content.Context;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.healthapplication.data.firebase.AuthRepository;
import com.example.healthapplication.data.firebase.UserRepository;
import com.example.healthapplication.domain.usecases.GetUserProfileUseCase;
import com.example.healthapplication.domain.usecases.UpsertUserUseCase;
import com.example.healthapplication.domain.usecases.LoginUseCase;
import com.example.healthapplication.domain.usecases.SignUpUseCase;
import com.example.healthapplication.presentation.profile.ProfileViewModel;
import com.example.healthapplication.presentation.signin.SignInViewModel;
import com.example.healthapplication.presentation.signup.SignUpViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import com.example.healthapplication.data.firebase.DiseaseRepository;
import com.example.healthapplication.domain.usecases.GetDiseasesUseCase;
import com.example.healthapplication.presentation.diseases.DiseasesViewModel;

import com.example.healthapplication.data.firebase.PatientRepository;
import com.example.healthapplication.domain.usecases.GetPatientsUseCase;
import com.example.healthapplication.presentation.patients.PatientsViewModel;
import com.example.healthapplication.domain.usecases.AddPatientUseCase;
import com.example.healthapplication.data.firebase.DoctorPatientRepository;
import com.example.healthapplication.domain.usecases.GetDoctorPatientsUseCase;
import com.example.healthapplication.domain.usecases.LinkPatientToDoctorUseCase;

public class ServiceLocator {
    private static final FirebaseAuth auth = FirebaseAuth.getInstance();
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static com.example.healthapplication.data.local.AppDatabase appDb;

    // Repositories
    private static final AuthRepository authRepo = new AuthRepository(auth, db);
    private static final UserRepository userRepo = new UserRepository(db);

    // UseCases
    private static final LoginUseCase loginUseCase = new LoginUseCase(authRepo);
    private static final SignUpUseCase signUpUseCase = new SignUpUseCase(authRepo);
    private static final GetUserProfileUseCase getUserProfileUseCase = new GetUserProfileUseCase(userRepo);
    private static final UpsertUserUseCase upsertUserUseCase = new UpsertUserUseCase(userRepo);


    private static final DiseaseRepository diseaseRepo = new DiseaseRepository(db);
    private static final GetDiseasesUseCase getDiseasesUseCase = new GetDiseasesUseCase(diseaseRepo);


    private static final PatientRepository patientRepo = new PatientRepository(db);
    private static final GetPatientsUseCase getPatientsUseCase = new GetPatientsUseCase(patientRepo);
    private static final AddPatientUseCase addPatientUseCase = new AddPatientUseCase(patientRepo);
    private static final DoctorPatientRepository doctorPatientRepo = new DoctorPatientRepository(db);
    private static final GetDoctorPatientsUseCase getDoctorPatientsUseCase = new GetDoctorPatientsUseCase(doctorPatientRepo);
    private static final LinkPatientToDoctorUseCase linkPatientToDoctorUseCase = new LinkPatientToDoctorUseCase(doctorPatientRepo);

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

    // SignUp factory
    public static ViewModelProvider.Factory provideSignUpFactory() {
        return new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                if (modelClass.isAssignableFrom(SignUpViewModel.class)) {
                    return (T) new SignUpViewModel(signUpUseCase, upsertUserUseCase);
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
                    return (T) new PatientsViewModel(getPatientsUseCase, userRepo, getDoctorPatientsUseCase, linkPatientToDoctorUseCase);
                }
                throw new IllegalArgumentException("Unknown ViewModel class");
            }
        };
    }

    public static AddPatientUseCase provideAddPatientUseCase() {
        return addPatientUseCase;
    }

    public static GetDoctorPatientsUseCase provideGetDoctorPatientsUseCase() {
        return getDoctorPatientsUseCase;
    }

    public static LinkPatientToDoctorUseCase provideLinkPatientToDoctorUseCase() {
        return linkPatientToDoctorUseCase;
    }

    public static synchronized void init(@NonNull Context context) {
        if (appDb == null) {
            appDb = androidx.room.Room.databaseBuilder(
                    context.getApplicationContext(),
                    com.example.healthapplication.data.local.AppDatabase.class,
                    "health_app.db"
            ).fallbackToDestructiveMigration().build();
        }
    }

    public static com.example.healthapplication.data.local.AppDatabase db() {
        return appDb;
    }

    public static ViewModelProvider.Factory provideEditProfileFactory() {
        return new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                if (modelClass.getName().equals("com.example.healthapplication.presentation.editprofile.EditProfileViewModel")
                        || modelClass.getSimpleName().equals("EditProfileViewModel")) {
                    return (T) new com.example.healthapplication.presentation.editprofile.EditProfileViewModel(upsertUserUseCase, userRepo);
                }
                throw new IllegalArgumentException("Unknown ViewModel class");
            }
        };
    }
}
