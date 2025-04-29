package com.example.hum1;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ChangePasswordActivity extends AppCompatActivity {
    private EditText old_passwordV, new_passwordV;
    private FirebaseUser user;
    private String role;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_change_password);
        old_passwordV = findViewById(R.id.old_password);
        new_passwordV = findViewById(R.id.new_password);
        Button saveB = findViewById(R.id.save);
        user = FirebaseAuth.getInstance().getCurrentUser();
        role = getIntent().getStringExtra("role");

        saveB.setOnClickListener(v -> changePassword());
    }

    private void changePassword() {
        String oldPassword = old_passwordV.getText().toString().trim();
        String newPassword = new_passwordV.getText().toString().trim();

        if (newPassword.length() < 6) {
            Toast.makeText(this, "Пароль должен содержать минимум 6 символов", Toast.LENGTH_SHORT).show();
            return;
        }

        AuthCredential credential = EmailAuthProvider
                .getCredential(user.getEmail(), oldPassword);

        user.reauthenticate(credential)
                .addOnCompleteListener(reauthTask -> {
                    if (reauthTask.isSuccessful()) {
                        updatePassword(newPassword);
                    } else {
                        Toast.makeText(this,
                                "Ошибка аутентификации: " + reauthTask.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updatePassword(String newPassword) {
        user.updatePassword(newPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Пароль успешно изменен", Toast.LENGTH_SHORT).show();
                        redirectToSettings();
                    } else {
                        Toast.makeText(this,
                                "Ошибка изменения пароля: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void redirectToSettings() {
        Class<?> targetClass = "user".equals(role) ?
                SettingUser.class : SettingCenter.class;

        startActivity(new Intent(this, targetClass));
        finish();
    }
}