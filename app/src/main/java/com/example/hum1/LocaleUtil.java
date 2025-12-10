package com.example.hum1;

import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

public final class LocaleUtil {
    private LocaleUtil() {}

    public static void applyAppLocale(Context ctx, String langTag) {
        LangPrefs.saveLang(ctx, langTag);
        LocaleListCompat appLocale = LocaleListCompat.forLanguageTags(langTag);
        AppCompatDelegate.setApplicationLocales(appLocale);
    }

    public static void initAppLocale(Context ctx) {
        String langTag = LangPrefs.loadLang(ctx);
        LocaleListCompat appLocale = LocaleListCompat.forLanguageTags(langTag);
        AppCompatDelegate.setApplicationLocales(appLocale);
    }
}

