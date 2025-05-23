package com.example.hum1;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginFragment extends Fragment {

    private EditText editTextEmail, editTextPassword;
    private Button buttonLogin;
    private TextView textViewRegister;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String userId = "", role = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        // Инициализация Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Инициализация UI элементов
        editTextEmail = view.findViewById(R.id.email);
        editTextPassword = view.findViewById(R.id.password);
        buttonLogin = view.findViewById(R.id.btn_login);
        progressBar = view.findViewById(R.id.progressBar);
        textViewRegister = view.findViewById(R.id.registerNow);

        // Проверка авторизации при старте
        checkCurrentUser();

        // Обработчики событий
        textViewRegister.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new RegisterFragment())
                    .addToBackStack(null)
                    .commit();
        });

        buttonLogin.setOnClickListener(v -> loginUser());

        return view;
    }

    private void checkCurrentUser() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String idU = currentUser.getUid();
            progressBar.setVisibility(View.VISIBLE);

            mDatabase.child("Users").child(idU).get().addOnCompleteListener(task -> {
                progressBar.setVisibility(View.GONE);
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                } else {
                    DataSnapshot snapshot = task.getResult();
                    if (snapshot.exists()) {
                        role = snapshot.child("role").getValue(String.class);
                        navigateToRoleSpecificActivity(role);
                    }
                }
            });
        }
    }

    private void loginUser() {
        progressBar.setVisibility(View.VISIBLE);
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getContext(), "Введите адрес электронной почты", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getContext(), "Введите пароль", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Авторизация прошла успешно", Toast.LENGTH_SHORT).show();
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            userId = user.getUid();
                            checkUserRole(userId);
                        }
                    } else {
                        Toast.makeText(getContext(), "Ошибка авторизации", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUserRole(String userId) {
        progressBar.setVisibility(View.VISIBLE);
        mDatabase.child("Users").child(userId).get().addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);
            if (!task.isSuccessful()) {
                Log.e("firebase", "Error getting data", task.getException());
            } else {
                DataSnapshot snapshot = task.getResult();
                if (snapshot.exists()) {
                    role = snapshot.child("role").getValue(String.class);
                    navigateToRoleSpecificActivity(role);
                }
            }
        });
    }

    private void navigateToRoleSpecificActivity(String role) {
        Intent intent;
        if (role == null) {
            return;
        }

        switch (role) {
            case "center":
                intent = new Intent(getActivity(), CenterApplicationsFragment.class);
                break;
            case "moderator":
                intent = new Intent(getActivity(), ModeratorActivity.class);
                break;
            default:
                intent = new Intent(getActivity(), UserActivity.class);
                break;
        }

        startActivity(intent);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}