package com.example.hum1;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Главный экран центра. Нижнее меню переключает заявки, календарь записей,
 * сообщения и настройки, не выбрасывая пользователя в отдельный раздел.
 */
public class CenterActivity extends AppCompatActivity {

    /**
     * Настраивает язык, нижнюю навигацию и стартовый раздел заявок.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleUtil.initAppLocale(this);
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_center);
        LanguageToggleHelper.setup(this);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new CenterApplicationsFragment())
                .commit();

        bottomNav.setSelectedItemId(R.id.navigation_see);
    }

    /**
     * Выбирает фрагмент по пункту нижнего меню центра.
     */
    private final BottomNavigationView.OnNavigationItemSelectedListener navListener = item -> {
        Fragment selectedFragment = null;

        if (item.getItemId() == R.id.navigation_see) {
            selectedFragment = new CenterApplicationsFragment();
        } else if (item.getItemId() == R.id.navigation_schedule) {
            selectedFragment = new CenterScheduleFragment();
        } else if (item.getItemId() == R.id.navigation_chats) {
            selectedFragment = new CenterChatsFragment();
        } else if (item.getItemId() == R.id.navigation_setting) {
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
