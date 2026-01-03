package com.example.healthapplication.presentation.signup;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.healthapplication.R;
import com.example.healthapplication.di.ServiceLocator;
import com.example.healthapplication.MainActivity;

public class SignUpFragment extends Fragment {
    private SignUpViewModel vm;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.sign_up, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        EditText email = v.findViewById(R.id.email);
        EditText password = v.findViewById(R.id.password);
        EditText confirmPassword = v.findViewById(R.id.confirm_password);
        Button btnSignUp = v.findViewById(R.id.btn_sign_up);
        TextView linkSignIn = v.findViewById(R.id.link_sign_in);

        vm = new ViewModelProvider(this, ServiceLocator.provideSignUpFactory())
                .get(SignUpViewModel.class);

        vm.getState().observe(getViewLifecycleOwner(), state -> {
            btnSignUp.setEnabled(!state.loading);
            if (state.loading) {
                btnSignUp.setText("Регистрация...");
            } else {
                btnSignUp.setText("Зарегистрироваться");
            }
            
            if (state.message != null) {
                Toast.makeText(getContext(), state.message, Toast.LENGTH_SHORT).show();
            }
            
            if (state.success) {
                // После успешной регистрации показываем аутентифицированный UI
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).showAuthenticatedUi();
                }
            }
        });

        btnSignUp.setOnClickListener(view ->
                vm.processIntent(new SignUpIntent.Submit(
                        email.getText().toString().trim(),
                        password.getText().toString().trim(),
                        confirmPassword.getText().toString().trim()
                )));

        linkSignIn.setOnClickListener(view -> {
            // Переход к экрану входа
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new com.example.healthapplication.presentation.signin.SignInFragment())
                    .commit();
        });
    }
}
