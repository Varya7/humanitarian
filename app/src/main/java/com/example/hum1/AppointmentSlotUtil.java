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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * РЈС‚РёР»РёС‚Р° РґР»СЏ СЂР°СЃРїРёСЃР°РЅРёСЏ С†РµРЅС‚СЂР°: С…СЂР°РЅРёС‚ СЂР°Р±РѕС‡РµРµ РІСЂРµРјСЏ РєР°Рє РґРёР°РїР°Р·РѕРЅ,
 * РіРµРЅРµСЂРёСЂСѓРµС‚ СЃРІРѕР±РѕРґРЅС‹Рµ РѕРєРЅР° РЅР° РІС‹Р±СЂР°РЅРЅС‹Р№ РґРµРЅСЊ Рё Р±СЂРѕРЅРёСЂСѓРµС‚ РѕРєРЅРѕ РїСЂРё РїРѕРґР°С‡Рµ Р·Р°СЏРІРєРё.
 */
public final class AppointmentSlotUtil {

    private AppointmentSlotUtil() {
    }

    public interface SlotsCallback {
        void onLoaded(List<Slot> slots);

        void onError();
    }

    /**
     * РћРґРЅРѕ РѕРєРЅРѕ Р·Р°РїРёСЃРё С†РµРЅС‚СЂР°.
     */
    public static class Slot {
        public String key;
        public String time;
        public boolean available;
        public String applicationId;
        public String userId;
        public String fio;
        public String status;
        public String pickupCode;
        public boolean manual;
    }

    /**
     * Р—Р°РіСЂСѓР¶Р°РµС‚ СЃР»РѕС‚С‹ РґРЅСЏ. Р•СЃР»Рё СЃР»РѕС‚РѕРІ РµС‰С‘ РЅРµС‚, СЃРѕР·РґР°С‘С‚ РёС… РёР· РіСЂР°С„РёРєР° С†РµРЅС‚СЂР°.
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
                boolean workingDay = isWorkingDay(centerSnapshot, displayDate);
                if (slotsSnapshot.exists()) {
                    if (callback != null) callback.onLoaded(readSlots(slotsSnapshot, true, workingDay));
                    return;
                }

                List<String> generated = generateTimes(centerSnapshot, displayDate);
                Map<String, Object> updates = new HashMap<>();
                for (String time : generated) {
                    String slotKey = toSlotKey(time);
                    Map<String, Object> slot = new HashMap<>();
                    slot.put("time", time);
                    slot.put("available", true);
                    slot.put("manual", false);
                    slot.put("created_by", "auto");
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
                                        if (callback != null) callback.onLoaded(readSlots(snapshot, true, workingDay));
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
     * Р‘СЂРѕРЅРёСЂСѓРµС‚ РІС‹Р±СЂР°РЅРЅРѕРµ РѕРєРЅРѕ Р·Р° Р·Р°СЏРІРєРѕР№.
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
        updates.put("status", InventoryReservationUtil.STATUS_APPROVED);
        updates.put("booked_at", ServerValue.TIMESTAMP);
        centerRef.child("appointment_slots").child(dateKey).child(slotKey).updateChildren(updates);
    }

    /**
     * РџСЂРµРѕР±СЂР°Р·СѓРµС‚ РѕС‚РѕР±СЂР°Р¶Р°РµРјСѓСЋ РґР°С‚Сѓ d/M/yyyy РІ Р±РµР·РѕРїР°СЃРЅС‹Р№ РєР»СЋС‡ yyyyMMdd.
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
        return readSlots(slotsSnapshot, onlyAvailable, true);
    }

    public static List<Slot> readSlots(DataSnapshot slotsSnapshot, boolean onlyAvailable, boolean workingDay) {
        List<Slot> slots = new ArrayList<>();
        for (DataSnapshot child : slotsSnapshot.getChildren()) {
            Slot slot = new Slot();
            slot.key = child.getKey();
            slot.time = child.child("time").getValue(String.class);
            Object available = child.child("available").getValue();
            slot.available = available == null || truthyDayValue(available);
            slot.applicationId = child.child("applicationId").getValue(String.class);
            slot.userId = child.child("userId").getValue(String.class);
            slot.fio = child.child("fio").getValue(String.class);
            slot.status = child.child("status").getValue(String.class);
            slot.pickupCode = child.child("pickupCode").getValue(String.class);
            String createdBy = child.child("created_by").getValue(String.class);
            String source = child.child("source").getValue(String.class);
            slot.manual = "center".equals(createdBy)
                    || "manual_center".equals(source);
            boolean oldBookedSlotWithoutStatus = !slot.available
                    && TextUtils.isEmpty(slot.status)
                    && !TextUtils.isEmpty(slot.applicationId);
            if (!slot.available && !isScheduledStatus(slot.status) && !oldBookedSlotWithoutStatus) {
                slot.available = true;
                slot.applicationId = null;
                slot.userId = null;
                slot.fio = null;
                slot.pickupCode = null;
            }
            if (!workingDay && slot.available && !slot.manual) {
                continue;
            }
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

    public static boolean isWorkingDay(DataSnapshot centerSnapshot, String displayDate) {
        Calendar date = parseDate(displayDate);
        if (date == null) return true;

        DataSnapshot daysSnapshot = centerSnapshot.child("working_days");
        int day = date.get(Calendar.DAY_OF_WEEK);
        String key = dayKey(day);
        if (!daysSnapshot.exists()) return isDefaultWorkingDay(key);


        if (daysSnapshot.child(key).exists()) {
            return truthyDayValue(daysSnapshot.child(key).getValue());
        }

        WorkingDays parsed = parseWorkingDays(daysSnapshot);
        if (parsed.hasExplicitSelected) {
            return parsed.selected.contains(key);
        }
        if (parsed.hasExplicitClosed) {
            return true;
        }

        return isDefaultWorkingDay(key);
    }

    private static boolean isScheduledStatus(String status) {
        return InventoryReservationUtil.STATUS_APPROVED.equals(status)
                || InventoryReservationUtil.STATUS_ISSUED.equals(status);
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

    private static boolean isDefaultWorkingDay(String key) {
        return !"sat".equals(key) && !"sun".equals(key);
    }

    private static WorkingDays parseWorkingDays(DataSnapshot daysSnapshot) {
        WorkingDays result = new WorkingDays();
        Object raw = daysSnapshot.getValue();
        if (raw instanceof String) {
            addDaysFromText(result, (String) raw);
            return result;
        }

        for (DataSnapshot child : daysSnapshot.getChildren()) {
            String keyDay = normalizeDay(child.getKey());
            String valueDay = normalizeDay(child.getValue());
            if (!TextUtils.isEmpty(valueDay)) {
                result.selected.add(valueDay);
                result.hasExplicitSelected = true;
            } else if (!TextUtils.isEmpty(keyDay)) {
                if (truthyDayValue(child.getValue())) {
                    result.selected.add(keyDay);
                    result.hasExplicitSelected = true;
                } else {
                    result.hasExplicitClosed = true;
                }
            }
        }
        return result;
    }

    private static void addDaysFromText(WorkingDays result, String value) {
        if (value == null) return;
        String[] parts = value.toLowerCase(Locale.ROOT).split("[,\\s;|]+");
        for (String part : parts) {
            String day = normalizeDay(part);
            if (!TextUtils.isEmpty(day)) {
                result.selected.add(day);
                result.hasExplicitSelected = true;
            }
        }
    }

    private static String normalizeDay(Object value) {
        if (value == null) return "";
        String text = String.valueOf(value).trim().toLowerCase(Locale.ROOT);
        switch (text) {
            case "1":
            case "mon":
            case "monday":
            case "\u043f\u043d":
            case "\u043f\u043e\u043d\u0435\u0434\u0435\u043b\u044c\u043d\u0438\u043a":
                return "mon";
            case "2":
            case "tue":
            case "tuesday":
            case "\u0432\u0442":
            case "\u0432\u0442\u043e\u0440\u043d\u0438\u043a":
                return "tue";
            case "3":
            case "wed":
            case "wednesday":
            case "\u0441\u0440":
            case "\u0441\u0440\u0435\u0434\u0430":
                return "wed";
            case "4":
            case "thu":
            case "thursday":
            case "\u0447\u0442":
            case "\u0447\u0435\u0442\u0432\u0435\u0440\u0433":
                return "thu";
            case "5":
            case "fri":
            case "friday":
            case "\u043f\u0442":
            case "\u043f\u044f\u0442\u043d\u0438\u0446\u0430":
                return "fri";
            case "6":
            case "sat":
            case "saturday":
            case "\u0441\u0431":
            case "\u0441\u0443\u0431\u0431\u043e\u0442\u0430":
                return "sat";
            case "0":
            case "7":
            case "sun":
            case "sunday":
            case "\u0432\u0441":
            case "\u0432\u043e\u0441\u043a\u0440\u0435\u0441\u0435\u043d\u044c\u0435":
                return "sun";
            default:
                return "";
        }
    }
    private static boolean truthyDayValue(Object value) {
        if (value == null) return false;
        if (value instanceof Boolean) return (Boolean) value;
        String text = String.valueOf(value).trim().toLowerCase(Locale.ROOT);
        return !"false".equals(text) && !"0".equals(text) && !"null".equals(text);
    }

    private static class WorkingDays {
        final Set<String> selected = new HashSet<>();
        boolean hasExplicitSelected;
        boolean hasExplicitClosed;
    }

    private static String[] parseLegacyWorkTime(String value) {
        if (value == null) return new String[]{"09:00", "18:00"};
        String normalized = value.replace("вЂ”", "-").replace("вЂ“", "-");
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

