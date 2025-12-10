package com.example.hum1;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hum1.auth.LoginFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;


/**
 * Активность аутентификации, которая служит точкой входа в приложение.
 * Определяет роль авторизированного пользователя и перенаправляет на соответствующую активность.
 */
public class AuthActivity extends AppCompatActivity {

    /**
     * Инициализирует активность при создании.
     * Проверяет текущего авторизованного пользователя и выполняет перенаправление
     * или показывает фрагмент входа в зависимости от состояния аутентификации.
     *
     * @param savedInstanceState сохраненное состояние активности (может быть null)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleUtil.initAppLocale(this);
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_auth);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            checkUserRoleAndRedirect(currentUser.getUid());
            forceGetFCMToken();
        } else {
            showLoginFragment();
        }
    }

    /**
     * Проверяет роль пользователя в базе данных Firebase и перенаправляет
     * на соответствующую активность в зависимости от роли.
     *
     * @param userId уникальный идентификатор пользователя в Firebase
     */
    private void checkUserRoleAndRedirect(String userId) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                .child("Users").child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String role = snapshot.child("role").getValue(String.class);
                    if ("user".equals(role)) {
                        redirectToMyApplications();
                    }
                    else if ("center".equals(role)){
                        redirectToMainActivity2();
                    }
                    else if ("moderator".equals(role)){
                        redirectToMainActivity3();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    /**
     * Перенаправляет пользователя на активность обычного пользователя.
     * Завершает текущую активность после перенаправления.
     */
    void redirectToMyApplications() {
        startActivity(new Intent(this, UserActivity.class));
        finish();
    }

    /**
     * Перенаправляет пользователя на активность центра.
     * Завершает текущую активность после перенаправления.
     */
    void redirectToMainActivity2() {
        startActivity(new Intent(this, CenterActivity.class));
        finish();
    }

    /**
     * Перенаправляет пользователя на активность модератора.
     * Завершает текущую активность после перенаправления.
     */
    void redirectToMainActivity3() {
        startActivity(new Intent(this, ModeratorActivity.class));
        finish();
    }

    /**
     * Отображает фрагмент входа в систему.
     * Заменяет текущий фрагмент в контейнере R.id.fragment_container.
     */
    private void showLoginFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new LoginFragment())
                .commit();
    }

    private void forceGetFCMToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        //Log.w("FCM_TOKEN", "Ошибка получения токена", task.getException());
                        return;
                    }

                    String token = task.getResult();
                    //Log.d("FCM_TOKEN", "Токен принудительно получен: " + token);

                    saveTokenToDatabase(token);
                });
    }

    private void saveTokenToDatabase(String token) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            FirebaseDatabase.getInstance().getReference("Users")
                    .child(userId)
                    .child("fcmToken")
                    .setValue(token)
                    .addOnSuccessListener(aVoid -> {
                    })
                    .addOnFailureListener(e -> {
                    });
        }
    }
}