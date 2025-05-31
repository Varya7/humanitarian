package com.example.hum1;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


/**
 * Фрагмент настроек модератора.
 * Отображает информацию об учетной записи, предоставляет функции выхода,
 * смены пароля и удаления аккаунта.
 */
public class SettingMFragment extends Fragment {

    FirebaseAuth auth;
    private FirebaseUser user;
    private DatabaseReference userRef;
    private String userId = "";
    DatabaseReference mDatabase;

    private TextView emailV, logoutV, deleteV;
    private Button edit_passwordB;

    /**
     * Вызывается при создании интерфейса фрагмента.
     * Инициализирует элементы интерфейса и загружает данные пользователя из Firebase.
     *
     * @param inflater           Инфлейтер для создания представлений из XML.
     * @param container          Родительский контейнер для фрагмента.
     * @param savedInstanceState Сохранённое состояние, если оно есть.
     * @return View, соответствующий интерфейсу фрагмента.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting_m, container, false);

        emailV = view.findViewById(R.id.email);
        logoutV = view.findViewById(R.id.logout);
        deleteV = view.findViewById(R.id.delete);
        edit_passwordB = view.findViewById(R.id.edit_password);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        if (user != null) {
            userId = user.getUid();
            mDatabase = FirebaseDatabase.getInstance().getReference();
            userRef = mDatabase.child("Users").child(userId);
            loadUserData();
            setupButtons();
        }


        return view;
    }

    /**
     * Загружает и отображает email текущего пользователя из базы данных Firebase.
     */
    private void loadUserData() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    emailV.setText(snapshot.child("email").getValue(String.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("SettingModerator", "Database error", error.toException());
            }
        });
    }

    /**
     * Устанавливает обработчики нажатий на кнопки: выход, изменение пароля и удаление аккаунта.
     */
    private void setupButtons() {
        logoutV.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getActivity(), AuthActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        });

        deleteV.setOnClickListener(v -> showDeleteConfirmationDialog());

        edit_passwordB.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), ChangePasswordActivity.class)));
    }


    /**
     * Отображает диалог подтверждения перед удалением аккаунта.
     */
    void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Подтверждение удаления")
                .setMessage("Вы хотите удалить свой аккаунт?")
                .setPositiveButton("Удалить", (dialog, which) -> deleteAccount())
                .setNegativeButton("Отмена", null)
                .show();
    }

    /**
     * Удаляет аккаунт пользователя из Firebase Authentication и Realtime Database.
     * После успешного удаления перенаправляет пользователя на экран авторизации.
     */
    private void deleteAccount() {
        userRef.removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        user.delete()
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        Toast.makeText(getContext(), "Аккаунт удалён", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(getActivity(), AuthActivity.class));
                                        requireActivity().finish();
                                    } else {
                                        Toast.makeText(getContext(), "Ошибка удаления аккаунта: " + task1.getException(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(getContext(), "Ошибка удаления данных из базы: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}