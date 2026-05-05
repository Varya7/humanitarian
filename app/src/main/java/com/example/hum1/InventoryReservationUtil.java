package com.example.hum1;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Общая логика склада центра: доступные вещи, забронированные вещи и выдача.
 * При одобрении заявка резервирует выбранные позиции, а при выдаче списание
 * идёт уже из резерва, чтобы пользователи видели только реально доступный остаток.
 */
public final class InventoryReservationUtil {

    public static final String STATUS_REVIEWING = "Рассматривается";
    public static final String STATUS_APPROVED = "Одобрено";
    public static final String STATUS_REJECTED = "Отклонено";
    public static final String STATUS_ISSUED = "Выдано";

    private InventoryReservationUtil() {
    }

    /**
     * Колбэк для экранов, которым нужно отреагировать на результат операции.
     */
    public interface Completion {
        void onSuccess();

        void onError(String message);
    }

    /**
     * Одобряет заявку и переносит выбранные вещи из доступных в забронированные.
     * Если доступного остатка не хватает, сначала показывает диалог подтверждения.
     */
    public static void approveApplication(
            AppCompatActivity activity,
            DatabaseReference rootRef,
            String centerId,
            String applicationId,
            String comment,
            boolean allowShortage,
            Completion completion
    ) {
        if (activity == null || rootRef == null || TextUtils.isEmpty(centerId) || TextUtils.isEmpty(applicationId)) {
            notifyError(completion, activity == null ? "" : activity.getString(R.string.error_load_data));
            return;
        }

        rootRef.child("Applications").child(applicationId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot appSnapshot) {
                        if (!appSnapshot.exists()) {
                            notifyError(completion, activity.getString(R.string.error_get_application));
                            return;
                        }

                        Boolean reservationApplied = appSnapshot.child("reservation_applied").getValue(Boolean.class);
                        if (Boolean.TRUE.equals(reservationApplied)) {
                            Map<String, Object> appUpdates = new HashMap<>();
                            appUpdates.put("status", STATUS_APPROVED);
                            if (!TextUtils.isEmpty(comment)) appUpdates.put("comment", comment);
                            ensurePickupCode(rootRef, applicationId, appSnapshot, appUpdates);
                            rootRef.child("Applications").child(applicationId).updateChildren(appUpdates)
                                    .addOnSuccessListener(unused -> notifySuccess(completion))
                                    .addOnFailureListener(error -> notifyError(completion, activity.getString(R.string.error_save_data)));
                            return;
                        }

                        Map<String, Integer> selectedItems = readQuantityMap(appSnapshot.child("selected_items"));
                        if (selectedItems.isEmpty()) {
                            Map<String, Object> updates = new HashMap<>();
                            updates.put("Applications/" + applicationId + "/status", STATUS_APPROVED);
                            updates.put("Applications/" + applicationId + "/reservation_applied", true);
                            if (!TextUtils.isEmpty(comment)) updates.put("Applications/" + applicationId + "/comment", comment);
                            ensurePickupCode(rootRef, applicationId, appSnapshot, updates, "Applications/" + applicationId + "/");
                            rootRef.updateChildren(updates)
                                    .addOnSuccessListener(unused -> notifySuccess(completion))
                                    .addOnFailureListener(error -> notifyError(completion, activity.getString(R.string.error_save_data)));
                            return;
                        }

                        rootRef.child("Users").child(centerId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot centerSnapshot) {
                                        InventoryState inventory = readInventoryState(centerSnapshot.child("list_c"));
                                        Map<String, Integer> reserved = readQuantityMap(centerSnapshot.child("reserved_items"));

                                        List<String> shortages = findShortages(selectedItems, inventory.availableByName);
                                        if (!shortages.isEmpty() && !allowShortage) {
                                            showShortageDialog(activity, rootRef, centerId, applicationId, comment, shortages, completion);
                                            return;
                                        }

                                        List<Map<String, String>> updatedAvailable = applyReservationToAvailable(
                                                inventory.items,
                                                selectedItems
                                        );
                                        for (Map.Entry<String, Integer> entry : selectedItems.entrySet()) {
                                            reserved.put(entry.getKey(), reserved.getOrDefault(entry.getKey(), 0) + entry.getValue());
                                        }

                                        Map<String, Object> updates = new HashMap<>();
                                        updates.put("Users/" + centerId + "/list_c", updatedAvailable);
                                        updates.put("Users/" + centerId + "/reserved_items", reserved);
                                        updates.put("Applications/" + applicationId + "/status", STATUS_APPROVED);
                                        updates.put("Applications/" + applicationId + "/reservation_applied", true);
                                        updates.put("Applications/" + applicationId + "/reserved_items", selectedItems);
                                        updates.put("Applications/" + applicationId + "/reserved_at", ServerValue.TIMESTAMP);
                                        if (!TextUtils.isEmpty(comment)) {
                                            updates.put("Applications/" + applicationId + "/comment", comment);
                                        }
                                        ensurePickupCode(rootRef, applicationId, appSnapshot, updates, "Applications/" + applicationId + "/");

                                        rootRef.updateChildren(updates)
                                                .addOnSuccessListener(unused -> notifySuccess(completion))
                                                .addOnFailureListener(error -> notifyError(completion, activity.getString(R.string.error_save_data)));
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        notifyError(completion, activity.getString(R.string.error_load_data));
                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        notifyError(completion, activity.getString(R.string.error_load_data));
                    }
                });
    }

    /**
     * Отмечает заявку выданной и списывает выбранные вещи из резерва центра.
     * Для старых заявок без резерва оставлен мягкий fallback: остаток вычитается из доступных.
     */
    public static void issueApplication(
            AppCompatActivity activity,
            DatabaseReference rootRef,
            String centerId,
            String applicationId,
            Completion completion
    ) {
        if (activity == null || rootRef == null || TextUtils.isEmpty(centerId) || TextUtils.isEmpty(applicationId)) {
            notifyError(completion, activity == null ? "" : activity.getString(R.string.error_load_data));
            return;
        }

        rootRef.child("Applications").child(applicationId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot appSnapshot) {
                        if (!appSnapshot.exists()) {
                            notifyError(completion, activity.getString(R.string.error_get_application));
                            return;
                        }

                        Map<String, Integer> toIssue = readQuantityMap(appSnapshot.child("reserved_items"));
                        if (toIssue.isEmpty()) {
                            toIssue = readQuantityMap(appSnapshot.child("selected_items"));
                        }

                        Map<String, Integer> finalToIssue = toIssue;
                        rootRef.child("Users").child(centerId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot centerSnapshot) {
                                        Map<String, Integer> reserved = readQuantityMap(centerSnapshot.child("reserved_items"));
                                        InventoryState available = readInventoryState(centerSnapshot.child("list_c"));
                                        boolean legacyAvailableChanged = false;

                                        for (Map.Entry<String, Integer> entry : finalToIssue.entrySet()) {
                                            String item = entry.getKey();
                                            int qty = entry.getValue();
                                            int reservedQty = reserved.getOrDefault(item, 0);
                                            if (reservedQty > 0) {
                                                reserved.put(item, Math.max(0, reservedQty - qty));
                                            } else {
                                                legacyAvailableChanged = true;
                                                subtractFromAvailable(available.items, item, qty);
                                            }
                                        }

                                        Map<String, Object> updates = new HashMap<>();
                                        updates.put("Users/" + centerId + "/reserved_items", reserved);
                                        if (legacyAvailableChanged) {
                                            updates.put("Users/" + centerId + "/list_c", available.items);
                                        }
                                        updates.put("Applications/" + applicationId + "/status", STATUS_ISSUED);
                                        updates.put("Applications/" + applicationId + "/issued_at", ServerValue.TIMESTAMP);

                                        rootRef.updateChildren(updates)
                                                .addOnSuccessListener(unused -> notifySuccess(completion))
                                                .addOnFailureListener(error -> notifyError(completion, activity.getString(R.string.error_save_data)));
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        notifyError(completion, activity.getString(R.string.error_load_data));
                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        notifyError(completion, activity.getString(R.string.error_load_data));
                    }
                });
    }

    private static void showShortageDialog(
            AppCompatActivity activity,
            DatabaseReference rootRef,
            String centerId,
            String applicationId,
            String comment,
            List<String> shortages,
            Completion completion
    ) {
        StringBuilder message = new StringBuilder(activity.getString(R.string.inventory_shortage_message));
        message.append("\n\n");
        for (String shortage : shortages) {
            message.append(shortage).append("\n");
        }

        new AlertDialog.Builder(activity)
                .setTitle(activity.getString(R.string.inventory_shortage_title))
                .setMessage(message.toString().trim())
                .setPositiveButton(activity.getString(R.string.inventory_reserve_anyway), (dialog, which) ->
                        approveApplication(activity, rootRef, centerId, applicationId, comment, true, completion))
                .setNegativeButton(activity.getString(R.string.cancel), null)
                .show();
    }

    private static List<String> findShortages(Map<String, Integer> selected, Map<String, Integer> available) {
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : selected.entrySet()) {
            int availableQty = available.getOrDefault(entry.getKey(), 0);
            if (availableQty < entry.getValue()) {
                result.add(String.format(Locale.getDefault(), "%s: %d / %d", entry.getKey(), availableQty, entry.getValue()));
            }
        }
        return result;
    }

    private static List<Map<String, String>> applyReservationToAvailable(
            List<Map<String, String>> currentItems,
            Map<String, Integer> selectedItems
    ) {
        List<Map<String, String>> updated = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        for (Map<String, String> item : currentItems) {
            Map<String, String> copy = new HashMap<>(item);
            String name = copy.get("name");
            if (!TextUtils.isEmpty(name)) {
                int currentQty = parseQuantity(copy.get("quantity"));
                int requested = selectedItems.getOrDefault(name, 0);
                copy.put("quantity", String.valueOf(Math.max(0, currentQty - requested)));
                seen.add(name);
            }
            updated.add(copy);
        }

        for (String itemName : selectedItems.keySet()) {
            if (!seen.contains(itemName)) {
                Map<String, String> missing = new HashMap<>();
                missing.put("name", itemName);
                missing.put("quantity", "0");
                updated.add(missing);
            }
        }

        return updated;
    }

    private static void subtractFromAvailable(List<Map<String, String>> availableItems, String itemName, int quantity) {
        for (Map<String, String> item : availableItems) {
            if (itemName.equals(item.get("name"))) {
                int current = parseQuantity(item.get("quantity"));
                item.put("quantity", String.valueOf(Math.max(0, current - quantity)));
                return;
            }
        }
    }

    private static InventoryState readInventoryState(DataSnapshot listSnapshot) {
        InventoryState state = new InventoryState();
        for (DataSnapshot itemSnapshot : listSnapshot.getChildren()) {
            Object raw = itemSnapshot.getValue();
            Map<String, String> item = new HashMap<>();

            if (raw instanceof Map) {
                Map<?, ?> rawMap = (Map<?, ?>) raw;
                Object name = rawMap.get("name");
                Object quantity = rawMap.get("quantity");
                if (name != null) item.put("name", String.valueOf(name));
                if (quantity != null) item.put("quantity", String.valueOf(quantity));
            } else if (itemSnapshot.getKey() != null) {
                item.put("name", itemSnapshot.getKey());
                item.put("quantity", String.valueOf(raw == null ? 0 : raw));
            }

            String name = item.get("name");
            if (!TextUtils.isEmpty(name)) {
                int quantity = parseQuantity(item.get("quantity"));
                item.put("quantity", String.valueOf(Math.max(0, quantity)));
                state.availableByName.put(name, Math.max(0, quantity));
                state.items.add(item);
            }
        }
        return state;
    }

    private static Map<String, Integer> readQuantityMap(DataSnapshot snapshot) {
        Map<String, Integer> result = new HashMap<>();
        if (snapshot == null || !snapshot.exists()) return result;

        for (DataSnapshot child : snapshot.getChildren()) {
            String name = child.getKey();
            if (TextUtils.isEmpty(name)) continue;
            int quantity = parseQuantity(child.getValue());
            if (quantity > 0) {
                result.put(name, result.getOrDefault(name, 0) + quantity);
            }
        }
        return result;
    }

    private static int parseQuantity(Object value) {
        if (value == null) return 0;
        if (value instanceof Number) {
            return Math.max(0, (int) Math.round(((Number) value).doubleValue()));
        }
        try {
            return Math.max(0, (int) Math.round(Double.parseDouble(String.valueOf(value).replace(",", "."))));
        } catch (Exception ignored) {
            return 0;
        }
    }

    private static void ensurePickupCode(
            DatabaseReference rootRef,
            String applicationId,
            DataSnapshot appSnapshot,
            Map<String, Object> updates,
            String applicationPathPrefix
    ) {
        String existing = appSnapshot.child("pickup_code").getValue(String.class);
        String code = TextUtils.isEmpty(existing) ? PickupCodeUtil.generate(applicationId) : existing;
        updates.put(applicationPathPrefix + "pickup_code", code);
        updates.put("PickupCodes/" + code, applicationId);
    }

    private static void ensurePickupCode(
            DatabaseReference rootRef,
            String applicationId,
            DataSnapshot appSnapshot,
            Map<String, Object> appUpdates
    ) {
        String existing = appSnapshot.child("pickup_code").getValue(String.class);
        String code = TextUtils.isEmpty(existing) ? PickupCodeUtil.generate(applicationId) : existing;
        appUpdates.put("pickup_code", code);
        rootRef.child("PickupCodes").child(code).setValue(applicationId);
    }

    private static void notifySuccess(Completion completion) {
        if (completion != null) completion.onSuccess();
    }

    private static void notifyError(Completion completion, String message) {
        if (completion != null) completion.onError(message);
    }

    private static class InventoryState {
        final List<Map<String, String>> items = new ArrayList<>();
        final Map<String, Integer> availableByName = new HashMap<>();
    }
}
