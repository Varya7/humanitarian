package com.example.hum1;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Главная активность для центра (организации) в приложении.
 * Предоставляет навигацию между разделами через нижнее меню и управление фрагментами.
 */
public class CenterActivity extends AppCompatActivity {

    /**
     * Инициализирует активность при создании.
     * устанавливает нижнюю навигацию и начальный фрагмент.
     *
     * @param savedInstanceState сохраненное состояние активности (может быть null)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleUtil.initAppLocale(this);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_center);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new CenterApplicationsFragment())
                .commit();

        bottomNav.setSelectedItemId(R.id.navigation_see);
    }

    /**
     * Слушатель выбора пунктов нижнего меню навигации.
     * Определяет какой фрагмент нужно отобразить в зависимости от выбранного пункта меню.
     */
    BottomNavigationView.OnNavigationItemSelectedListener navListener =
            item -> {
                Fragment selectedFragment = null;

                if (item.getItemId() == R.id.navigation_see) {
                    selectedFragment = new CenterApplicationsFragment();
                }

                else if (item.getItemId() == R.id.navigation_setting) {
                    selectedFragment = new SettingCFragment();
                }

                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, selectedFragment)
                            .commit();
                    return true;
                }
                return false;
            };
}