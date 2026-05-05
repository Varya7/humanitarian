package com.example.hum1;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Утилита для расписания центра: хранит рабочее время как диапазон,
 * генерирует свободные окна на выбранный день и бронирует окно при подаче заявки.
 */
public final class AppointmentSlotUtil {

    private AppointmentSlotUtil() {
    }

    public interface SlotsCallback {
        void onLoaded(List<Slot> slots);

        void onError();
    }

    /**
     * Одно окно записи центра.
     */
    public static class Slot {
        public String key;
        public String time;
        public boolean available;
        public String applicationId;
        public String userId;
        public String fio;
    }

    /**
     * Загружает слоты дня. Если слотов ещё нет, создаёт их из графика центра.
     */
    public static void loadOrCreateSlots(DatabaseReference centerRef, String displayDate, SlotsCallback callback) {
        if (centerRef == null || TextUtils.isEmpty(displayDate)) {
            if (callback != null) callback.onError();
            return;
        }

        String dateKey = toDateKey(displayDate);
        if (TextUtils.isEmpty(dateKey)) {
            if (callback != null) callback.onError();
            return;
        }

        centerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot centerSnapshot) {
                DataSnapshot slotsSnapshot = centerSnapshot.child("appointment_slots").child(dateKey);
                if (slotsSnapshot.exists()) {
                    if (callback != null) callback.onLoaded(readSlots(slotsSnapshot, true));
                    return;
                }

                List<String> generated = generateTimes(centerSnapshot, displayDate);
                Map<String, Object> updates = new HashMap<>();
                for (String time : generated) {
                    String slotKey = toSlotKey(time);
                    Map<String, Object> slot = new HashMap<>();
                    slot.put("time", time);
                    slot.put("available", true);
                    slot.put("created_at", ServerValue.TIMESTAMP);
                    updates.put("appointment_slots/" + dateKey + "/" + slotKey, slot);
                }

                if (updates.isEmpty()) {
                    if (callback != null) callback.onLoaded(new ArrayList<>());
                    return;
                }

                centerRef.updateChildren(updates)
                        .addOnSuccessListener(unused -> centerRef.child("appointment_slots").child(dateKey)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (callback != null) callback.onLoaded(readSlots(snapshot, true));
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        if (callback != null) callback.onError();
                                    }
                                }))
                        .addOnFailureListener(error -> {
                            if (callback != null) callback.onError();
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (callback != null) callback.onError();
            }
        });
    }

    /**
     * Бронирует выбранное окно за заявкой.
     */
    public static void bookSlot(
            DatabaseReference centerRef,
            String displayDate,
            String time,
            String applicationId,
            String userId,
            String fio
    ) {
        String dateKey = toDateKey(displayDate);
        String slotKey = toSlotKey(time);
        if (centerRef == null || TextUtils.isEmpty(dateKey) || TextUtils.isEmpty(slotKey)) return;

        Map<String, Object> updates = new HashMap<>();
        updates.put("available", false);
        updates.put("applicationId", applicationId);
        updates.put("userId", userId);
        updates.put("fio", fio);
        updates.put("booked_at", ServerValue.TIMESTAMP);
        centerRef.child("appointment_slots").child(dateKey).child(slotKey).updateChildren(updates);
    }

    /**
     * Преобразует отображаемую дату d/M/yyyy в безопасный ключ yyyyMMdd.
     */
    public static String toDateKey(String displayDate) {
        Calendar calendar = parseDate(displayDate);
        if (calendar == null) return "";
        return String.format(Locale.US, "%04d%02d%02d",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH));
    }

    public static String toSlotKey(String time) {
        if (time == null) return "";
        return time.trim().replace(":", "_").replace(".", "_");
    }

    public static List<Slot> readSlots(DataSnapshot slotsSnapshot, boolean onlyAvailable) {
        List<Slot> slots = new ArrayList<>();
        for (DataSnapshot child : slotsSnapshot.getChildren()) {
            Slot slot = new Slot();
            slot.key = child.getKey();
            slot.time = child.child("time").getValue(String.class);
            Boolean available = child.child("available").getValue(Boolean.class);
            slot.available = available == null || available;
            slot.applicationId = child.child("applicationId").getValue(String.class);
            slot.userId = child.child("userId").getValue(String.class);
            slot.fio = child.child("fio").getValue(String.class);
            if (!TextUtils.isEmpty(slot.time) && (!onlyAvailable || slot.available)) {
                slots.add(slot);
            }
        }
        slots.sort((a, b) -> a.time.compareTo(b.time));
        return slots;
    }

    public static List<String> generateTimes(DataSnapshot centerSnapshot, String displayDate) {
        List<String> result = new ArrayList<>();
        if (!isWorkingDay(centerSnapshot, displayDate)) return result;

        String start = centerSnapshot.child("work_start").getValue(String.class);
        String end = centerSnapshot.child("work_end").getValue(String.class);
        if (TextUtils.isEmpty(start) || TextUtils.isEmpty(end)) {
            String legacy = centerSnapshot.child("work_time").getValue(String.class);
            String[] parsed = parseLegacyWorkTime(legacy);
            start = TextUtils.isEmpty(start) ? parsed[0] : start;
            end = TextUtils.isEmpty(end) ? parsed[1] : end;
        }

        int interval = parsePositiveInt(centerSnapshot.child("appointment_interval_minutes").getValue(), 30);
        int startMinutes = parseMinutes(start, 9 * 60);
        int endMinutes = parseMinutes(end, 18 * 60);
        if (endMinutes <= startMinutes) return result;

        for (int minute = startMinutes; minute < endMinutes; minute += interval) {
            result.add(String.format(Locale.getDefault(), "%02d:%02d", minute / 60, minute % 60));
        }
        return result;
    }

    private static boolean isWorkingDay(DataSnapshot centerSnapshot, String displayDate) {
        Calendar date = parseDate(displayDate);
        if (date == null) return true;

        DataSnapshot daysSnapshot = centerSnapshot.child("working_days");
        if (!daysSnapshot.exists()) return true;

        int day = date.get(Calendar.DAY_OF_WEEK);
        String key = dayKey(day);

        if (daysSnapshot.child(key).exists()) {
            Boolean value = daysSnapshot.child(key).getValue(Boolean.class);
            return value == null || value;
        }

        Object raw = daysSnapshot.getValue();
        if (raw instanceof String) {
            String text = ((String) raw).toLowerCase(Locale.ROOT);
            return text.contains(key) || text.contains(String.valueOf(day));
        }

        return true;
    }

    private static Calendar parseDate(String displayDate) {
        if (displayDate == null) return null;
        String[] parts = displayDate.trim().split("[./-]");
        if (parts.length < 3) return null;
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setLenient(false);
            calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(parts[0]));
            calendar.set(Calendar.MONTH, Integer.parseInt(parts[1]) - 1);
            calendar.set(Calendar.YEAR, Integer.parseInt(parts[2]));
            calendar.getTime();
            return calendar;
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String dayKey(int calendarDay) {
        switch (calendarDay) {
            case Calendar.MONDAY:
                return "mon";
            case Calendar.TUESDAY:
                return "tue";
            case Calendar.WEDNESDAY:
                return "wed";
            case Calendar.THURSDAY:
                return "thu";
            case Calendar.FRIDAY:
                return "fri";
            case Calendar.SATURDAY:
                return "sat";
            case Calendar.SUNDAY:
            default:
                return "sun";
        }
    }

    private static String[] parseLegacyWorkTime(String value) {
        if (value == null) return new String[]{"09:00", "18:00"};
        String normalized = value.replace("—", "-").replace("–", "-");
        String[] parts = normalized.split("-");
        if (parts.length >= 2) {
            return new String[]{parts[0].trim(), parts[1].trim()};
        }
        return new String[]{"09:00", "18:00"};
    }

    private static int parseMinutes(String time, int fallback) {
        if (time == null) return fallback;
        String[] parts = time.trim().split("[:.]");
        try {
            int hours = Integer.parseInt(parts[0]);
            int minutes = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
            return hours * 60 + minutes;
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static int parsePositiveInt(Object raw, int fallback) {
        try {
            int value = Integer.parseInt(String.valueOf(raw));
            return value > 0 ? value : fallback;
        } catch (Exception ignored) {
            return fallback;
        }
    }
}
