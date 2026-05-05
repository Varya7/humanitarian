package com.example.hum1;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

/**
 * Небольшой общий помощник для кнопки смены языка в верхней панели.
 * Используется на экранах входа и основных активностях всех ролей.
 */
public final class LanguageToggleHelper {

    private LanguageToggleHelper() {
    }

    /**
     * Подключает кнопку с id btn_language_toggle к смене языка ru/en.
     *
     * @param activity активность, где находится кнопка
     */
    public static void setup(Activity activity) {
        if (activity == null) return;
        View view = activity.findViewById(R.id.btn_language_toggle);
        if (!(view instanceof TextView)) return;

        TextView button = (TextView) view;
        updateLabel(activity, button);
        button.setOnClickListener(v -> {
            String current = LangPrefs.loadLang(activity);
            String next = current.startsWith("en") ? "ru" : "en";
            LocaleUtil.applyAppLocale(activity, next);
            activity.recreate();
        });
    }

    private static void updateLabel(Activity activity, TextView button) {
        String current = LangPrefs.loadLang(activity);
        button.setText(current.startsWith("en") ? "EN" : "RU");
    }
}
