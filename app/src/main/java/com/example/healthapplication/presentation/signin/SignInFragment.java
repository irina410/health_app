package com.example.healthapplication.presentation.signin;

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

public class SignInFragment extends Fragment {
    private SignInViewModel vm;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.sign_in, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        EditText email = v.findViewById(R.id.email);
        EditText password = v.findViewById(R.id.password);
        Button button = v.findViewById(R.id.go_to_main);
        TextView linkSignUp = v.findViewById(R.id.link_sign_up);

        vm = new ViewModelProvider(this, ServiceLocator.provideSignInFactory())
                .get(SignInViewModel.class);

        vm.getState().observe(getViewLifecycleOwner(), state -> {
            button.setEnabled(!state.loading);
            if (state.loading) {
                button.setText("Вход...");
            } else {
                button.setText("Войти");
            }
            
            if (state.message != null) {
                Toast.makeText(getContext(), state.message, Toast.LENGTH_SHORT).show();
            }
            if (state.success) {
                // Показываем аутентифицированный UI без перезапуска Activity
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).showAuthenticatedUi();
                }
            }
        });

        button.setOnClickListener(view ->
                vm.processIntent(new SignInIntent.Submit(
                        email.getText().toString().trim(),
                        password.getText().toString().trim()
                )));

        linkSignUp.setOnClickListener(view -> {
            // Переход к экрану регистрации
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new com.example.healthapplication.presentation.signup.SignUpFragment())
                    .commit();
        });
    }
}
