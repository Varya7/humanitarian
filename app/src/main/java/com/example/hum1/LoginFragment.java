package com.example.hum1;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Фрагмент для авторизации пользователя через Firebase.
 * Предоставляет UI для ввода электронной почты и пароля,
 * а также перенаправляет пользователя в зависимости от его роли:
 * {@code center}, {@code moderator} или обычный пользователь {@code user}.
 */
public class LoginFragment extends Fragment {

    EditText editTextEmail;
    EditText editTextPassword;
    private Button buttonLogin;
    private TextView textViewRegister;
    private ProgressBar progressBar;

    FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private String userId = "", role = "";

    /**
     * Метод вызывается при создании представления фрагмента.
     * Инициализирует Firebase, UI-элементы и обработчики событий.
     *
     * @param inflater           Инфлейтер макета.
     * @param container          Контейнер ViewGroup.
     * @param savedInstanceState Сохранённое состояние.
     * @return Представление фрагмента.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        editTextEmail = view.findViewById(R.id.email);
        editTextPassword = view.findViewById(R.id.password);
        buttonLogin = view.findViewById(R.id.btn_login);
        progressBar = view.findViewById(R.id.progressBar);
        textViewRegister = view.findViewById(R.id.registerNow);

        checkCurrentUser();

        textViewRegister.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new RegisterFragment())
                    .addToBackStack(null)
                    .commit();
        });

        buttonLogin.setOnClickListener(v -> loginUser());

        return view;
    }

    /**
     * Проверяет, авторизован ли пользователь, и если да —
     * определяет его роль и перенаправляет в соответствующую активность.
     */
    void checkCurrentUser() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String idU = currentUser.getUid();
            progressBar.setVisibility(View.VISIBLE);

            mDatabase.child("Users").child(idU).get().addOnCompleteListener(task -> {
                progressBar.setVisibility(View.GONE);
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Ошибка получения данных", task.getException());
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

    /**
     * Проводит валидацию полей и выполняет попытку авторизации
     * через Firebase с указанными данными.
     */
    void loginUser() {
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

    /**
     * Получает из базы данных роль пользователя по его ID
     * и перенаправляет его в соответствующую активность.
     *
     * @param userId ID текущего пользователя.
     */
    private void checkUserRole(String userId) {
        progressBar.setVisibility(View.VISIBLE);
        mDatabase.child("Users").child(userId).get().addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);
            if (!task.isSuccessful()) {
                Log.e("firebase", "Ошибка получения данных", task.getException());
            } else {
                DataSnapshot snapshot = task.getResult();
                if (snapshot.exists()) {
                    role = snapshot.child("role").getValue(String.class);
                    navigateToRoleSpecificActivity(role);
                }
            }
        });
    }

    /**
     * Перенаправляет пользователя в активность в зависимости от его роли.
     * Если роль — "center", открывается {@code CenterApplicationsFragment}.
     * Если "moderator" — {@code ModeratorActivity},
     * иначе, если "user" — {@code UserActivity}.
     *
     * @param role Роль пользователя в системе.
     */
    private void navigateToRoleSpecificActivity(String role) {
        Intent intent;
        if (role == null) {
            return;
        }

        switch (role) {
            case "center":
                intent = new Intent(getActivity(), CenterActivity.class);
                break;
            case "moderator":
                intent = new Intent(getActivity(), ModeratorActivity.class);
                break;
            case "user":
                intent = new Intent(getActivity(), UserActivity.class);
                break;
            default:
                intent = new Intent(getActivity(), AuthActivity.class);
                break;
        }

        startActivity(intent);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}
