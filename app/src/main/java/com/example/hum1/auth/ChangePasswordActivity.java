package com.example.hum1.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hum1.R;
import com.example.hum1.SettingCFragment;
import com.example.hum1.SettingFragment;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Активность для изменения пароля текущего пользователя.
 * Пользователь должен ввести свой старый пароль для повторной аутентификации,
 * после чего сможет установить новый пароль.
 */
public class ChangePasswordActivity extends AppCompatActivity {

    EditText old_passwordV;
    EditText new_passwordV;
    private FirebaseUser user;
    private String role;

    /**
     * Метод инициализации активности. Прячет ActionBar,
     * находит элементы интерфейса и задаёт обработчик кнопки сохранения.
     *
     * @param savedInstanceState сохранённое состояние активности (если есть)
     */
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

    /**
     * Метод, выполняющий процесс смены пароля.
     * Сначала происходит повторная аутентификация пользователя,
     * после чего, при успехе, вызывается {@link #updatePassword(String)}.
     */
    void changePassword() {
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

    /**
     * Обновляет пароль пользователя в Firebase Authentication.
     *
     * @param newPassword новый пароль, который будет установлен
     */
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

    /**
     * Перенаправляет пользователя на соответствующий экран настроек
     * в зависимости от его роли (user или center).
     */
    private void redirectToSettings() {
        Class<?> targetClass = "user".equals(role) ?
                SettingFragment.class : SettingCFragment.class;

        startActivity(new Intent(this, targetClass));
        finish();
    }


}
