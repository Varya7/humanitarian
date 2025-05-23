package com.example.hum1;

import android.content.Intent;
import android.os.Bundle;
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

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SettingFragment extends Fragment {

    private FirebaseAuth auth;
    private FirebaseUser user;
    private DatabaseReference mDatabase;
    private String userId, email, fio, birth, phone_number;

    private TextView emailV, fioV, birthV, phone_numberV, logoutV, deleteV;
    private Button edit_dataB, edit_passwordB;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        assert user != null;
        userId = user.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        emailV = view.findViewById(R.id.email);
        fioV = view.findViewById(R.id.fio);
        birthV = view.findViewById(R.id.birth);
        phone_numberV = view.findViewById(R.id.phone_number);
        logoutV = view.findViewById(R.id.logout);
        deleteV = view.findViewById(R.id.delete);
        edit_dataB = view.findViewById(R.id.btn_edit_data);
        edit_passwordB = view.findViewById(R.id.btn_edit_password);

        loadUserData();

        logoutV.setOnClickListener(v -> {
            auth.signOut();
            Intent intent = new Intent(getContext(), AuthActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        deleteV.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Подтверждение удаления")
                    .setMessage("Вы хотите удалить свой аккаунт?")
                    .setPositiveButton("Удалить", (dialog, which) -> deleteAccount())
                    .setNegativeButton("Отмена", null)
                    .show();
        });

        edit_dataB.setOnClickListener(v -> startActivity(new Intent(getContext(), EditDataUserActivity.class)));

        edit_passwordB.setOnClickListener(v -> startActivity(new Intent(getContext(), ChangePasswordActivity.class)));

        return view;
    }

    private void loadUserData() {
        mDatabase.child("Users").child(userId).get().addOnCompleteListener((Task<DataSnapshot> task) -> {
            if (task.isSuccessful()) {
                DataSnapshot snapshot = task.getResult();
                if (snapshot.exists()) {
                    fio = snapshot.child("fio").getValue(String.class);
                    email = snapshot.child("email").getValue(String.class);
                    birth = snapshot.child("birth").getValue(String.class);
                    phone_number = snapshot.child("phone_number").getValue(String.class);

                    fioV.setText(fio);
                    birthV.setText(birth);
                    emailV.setText(email);
                    phone_numberV.setText(phone_number);
                }
            }
        });
    }

    private void deleteAccount() {
        mDatabase.child("Users").child(userId).removeValue().addOnCompleteListener(dbTask -> {
            if (dbTask.isSuccessful()) {
                user.delete().addOnCompleteListener(authTask -> {
                    if (authTask.isSuccessful()) {
                        Toast.makeText(getContext(), "Аккаунт удален", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getContext(), AuthActivity.class));
                        requireActivity().finish();
                    } else {
                        Toast.makeText(getContext(), "Ошибка удаления аккаунта", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(getContext(), "Ошибка удаления данных", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
