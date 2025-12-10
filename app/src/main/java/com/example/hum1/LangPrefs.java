package com.example.hum1;

import android.content.Context;

public final class LangPrefs {
    private static final String PREFS = "app_settings";
    private static final String KEY_LANG = "lang";

    public static void saveLang(Context ctx, String tag) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_LANG, tag)
                .apply();
    }

    public static String loadLang(Context ctx) {
        return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(KEY_LANG, "ru");
    }
}
