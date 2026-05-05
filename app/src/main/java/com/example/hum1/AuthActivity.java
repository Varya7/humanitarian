package com.example.hum1;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

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

    private static final int MAX_RETRY_COUNT = 3;
    private int retryCount = 0;
    private Button retryButton;
    private boolean isShowingNoInternet = false;

    /**
     * Инициализирует активность при создании.
     * Проверяет текущего авторизованного пользователя и выполняет перенаправление
     * или показывает фрагмент входа в зависимости от состояния аутентификации.
     *
     * @param savedInstanceState сохраненное состояние активности (может быть null)
     */
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleUtil.initAppLocale(this);
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_auth);

        retryButton = findViewById(R.id.btn_retry);
        if (retryButton != null) {
            retryButton.setOnClickListener(v -> retryConnection());
        }
        LanguageToggleHelper.setup(this);

        checkAndProceed();
    }

    private void checkAndProceed() {
        if (!isNetworkAvailable()) {
            showNoInternetMessage();
            return;
        }

        hideNoInternetMessage();

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            checkUserRoleAndRedirect(currentUser.getUid());
            forceGetFCMToken();
        } else {
            showLoginFragment();
        }
    }

    private void retryConnection() {
        if (isNetworkAvailable()) {
            hideNoInternetMessage();
            retryCount = 0;
            checkAndProceed();
        } else {
            Toast.makeText(this, getString(R.string.auth_internet_still_unavailable), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (connectivityManager == null) return false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
            if (capabilities == null) return false;

            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
        } else {
            android.net.NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }
    }

    private void showNoInternetMessage() {
        if (isShowingNoInternet) return;

        isShowingNoInternet = true;

        android.view.View noInternetView = findViewById(R.id.no_internet_container);
        android.view.View fragmentContainer = findViewById(R.id.fragment_container);

        if (noInternetView != null) {
            noInternetView.setVisibility(android.view.View.VISIBLE);
        }
        if (fragmentContainer != null) {
            fragmentContainer.setVisibility(android.view.View.GONE);
        }

        Toast.makeText(this, getString(R.string.auth_no_internet_toast), Toast.LENGTH_LONG).show();
    }

    private void hideNoInternetMessage() {
        isShowingNoInternet = false;

        android.view.View noInternetView = findViewById(R.id.no_internet_container);
        android.view.View fragmentContainer = findViewById(R.id.fragment_container);

        if (noInternetView != null) {
            noInternetView.setVisibility(android.view.View.GONE);
        }
        if (fragmentContainer != null) {
            fragmentContainer.setVisibility(android.view.View.VISIBLE);
        }
    }

    private void showNetworkErrorMessage() {
        Toast.makeText(this, getString(R.string.auth_network_error), Toast.LENGTH_SHORT).show();
    }

    /**
     * Проверяет роль пользователя в базе данных Firebase и перенаправляет
     * на соответствующую активность в зависимости от роли.
     *
     * @param userId уникальный идентификатор пользователя в Firebase
     */
    private void checkUserRoleAndRedirect(String userId) {
        if (!isNetworkAvailable()) {
            showNetworkErrorMessage();
            showLoginFragment();
            return;
        }

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                .child("Users").child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                retryCount = 0;

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
                } else {
                    showLoginFragment();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (error.getCode() == DatabaseError.DISCONNECTED ||
                        error.getCode() == DatabaseError.NETWORK_ERROR) {

                    if (retryCount < MAX_RETRY_COUNT) {
                        retryCount++;
                        findViewById(android.R.id.content).postDelayed(() -> {
                            if (isNetworkAvailable()) {
                                checkUserRoleAndRedirect(userId);
                            } else {
                                showNetworkErrorMessage();
                                showLoginFragment();
                            }
                        }, 2000);
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(AuthActivity.this,
                                    getString(R.string.auth_server_unavailable),
                                    Toast.LENGTH_LONG).show();
                            showLoginFragment();
                        });
                        retryCount = 0;
                    }
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(AuthActivity.this,
                                getString(R.string.auth_user_check_error, error.getMessage()),
                                Toast.LENGTH_SHORT).show();
                        showLoginFragment();
                    });
                }
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
        if (!isNetworkAvailable()) {
            return;
        }

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        return;
                    }

                    String token = task.getResult();
                    saveTokenToDatabase(token);
                });
    }

    private void saveTokenToDatabase(String token) {
        if (!isNetworkAvailable()) {
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            FirebaseDatabase.getInstance().getReference("Users")
                    .child(userId)
                    .child("fcmToken")
                    .setValue(token)
                    .addOnSuccessListener(aVoid -> {})
                    .addOnFailureListener(e -> {});
        }
    }
}
