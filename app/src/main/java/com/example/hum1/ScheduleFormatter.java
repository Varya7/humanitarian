package com.example.hum1;

import android.content.Context;

import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Форматирует график центра из рабочих дней и диапазона времени.
 */
public final class ScheduleFormatter {
    private ScheduleFormatter() {}

    public static String fromSnapshot(Context context, DataSnapshot snapshot) {
        String days = formatDays(context, snapshot.child("working_days").getValue(String.class));
        String start = valueOrDefault(snapshot.child("work_start").getValue(String.class), "09:00");
        String end = valueOrDefault(snapshot.child("work_end").getValue(String.class), "18:00");
        return days + ", " + start + "-" + end;
    }

    public static String fromValues(Context context, String workingDays, String start, String end) {
        return formatDays(context, workingDays) + ", " + valueOrDefault(start, "09:00") + "-" + valueOrDefault(end, "18:00");
    }

    private static String formatDays(Context context, String rawDays) {
        String normalized = rawDays == null || rawDays.trim().isEmpty()
                ? "mon,tue,wed,thu,fri"
                : rawDays.toLowerCase(Locale.ROOT);
        String[][] labels = new String[][]{
                {"mon", context.getString(R.string.day_mon)},
                {"tue", context.getString(R.string.day_tue)},
                {"wed", context.getString(R.string.day_wed)},
                {"thu", context.getString(R.string.day_thu)},
                {"fri", context.getString(R.string.day_fri)},
                {"sat", context.getString(R.string.day_sat)},
                {"sun", context.getString(R.string.day_sun)}
        };
        List<String> selected = new ArrayList<>();
        for (String[] label : labels) {
            if (("," + normalized + ",").contains("," + label[0] + ",")) {
                selected.add(label[1]);
            }
        }
        return selected.isEmpty() ? context.getString(R.string.schedule_days_select) : String.join(", ", selected);
    }

    private static String valueOrDefault(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }
}
