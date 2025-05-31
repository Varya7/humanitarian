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
 * Активность для модератора с нижней навигацией.
 * Позволяет переключаться между фрагментами просмотра списка и настройками.
 */
public class ModeratorActivity extends AppCompatActivity {


    /**
     * Метод жизненного цикла onCreate.
     * Выполняет инициализацию интерфейса, включая включение Edge-to-Edge отображения,
     * скрытие ActionBar, настройку нижней навигации и установку начального фрагмента.
     *
     * @param savedInstanceState сохранённое состояние активности (если есть)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_moderator);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new ModeratorListFragment())
                .commit();

        bottomNav.setSelectedItemId(R.id.navigation_see);


    }

    /**
     * Обработчик выбора элементов нижней навигации.
     * В зависимости от выбранного пункта переключает отображаемый фрагмент:
     * - Список модератора (ModeratorListFragment)
     * - Настройки модератора (SettingMFragment)
     */
    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            item -> {
                Fragment selectedFragment = null;

                if (item.getItemId() == R.id.navigation_see) {
                    selectedFragment = new ModeratorListFragment();
                }

                else if (item.getItemId() == R.id.navigation_setting) {
                    selectedFragment = new SettingMFragment();
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