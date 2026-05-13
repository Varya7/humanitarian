package com.example.hum1;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.hum1.views.ViewApplicC;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Вкладка календаря центра: показывает свободные и занятые окна записи.
 * Занятые записи открываются по нажатию, свободные можно удалить или добавить вручную.
 */
public class CenterScheduleFragment extends Fragment {

    private EditText dateV;
    private EditText newSlotTimeV;
    private LinearLayout slotsContainer;
    private DatabaseReference centerRef;
    private String selectedDate = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_center_schedule, container, false);
        init(view);
        return view;
    }

    @SuppressLint("MissingInflatedId")
    private void init(View view) {
        String userId = FirebaseAuth.getInstance().getCurrentUser() == null
                ? ""
                : FirebaseAuth.getInstance().getCurrentUser().getUid();
        centerRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        dateV = view.findViewById(R.id.date);
        newSlotTimeV = view.findViewById(R.id.new_slot_time);
        slotsContainer = view.findViewById(R.id.slots_container);
        Button addSlotB = view.findViewById(R.id.add_slot);

        selectedDate = todayDisplayDate();
        dateV.setText(selectedDate);
        loadSlots();

        dateV.setOnClickListener(v -> showDatePicker());
        addSlotB.setOnClickListener(v -> addSlot());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(requireContext(), (view, year, month, day) -> {
            selectedDate = day + "/" + (month + 1) + "/" + year;
            dateV.setText(selectedDate);
            loadSlots();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void loadSlots() {
        AppointmentSlotUtil.loadOrCreateSlots(centerRef, selectedDate, new AppointmentSlotUtil.SlotsCallback() {
            @Override
            public void onLoaded(List<AppointmentSlotUtil.Slot> slots) {
                renderSlots();
            }

            @Override
            public void onError() {
                if (isAdded()) {
                    Toast.makeText(requireContext(), getString(R.string.error_load_data), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void renderSlots() {
        if (!isAdded()) return;
        slotsContainer.removeAllViews();
        String dateKey = AppointmentSlotUtil.toDateKey(selectedDate);
        centerRef
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot centerSnapshot) {
                        if (!isAdded() || getView() == null) return;
                        DataSnapshot slotsSnapshot = centerSnapshot.child("appointment_slots").child(dateKey);
                        boolean workingDay = AppointmentSlotUtil.isWorkingDay(centerSnapshot, selectedDate);
                        List<AppointmentSlotUtil.Slot> allSlots = AppointmentSlotUtil.readSlots(slotsSnapshot, false, workingDay);
                        if (allSlots.isEmpty()) {
                            slotsContainer.addView(createSlotCard(getString(R.string.schedule_no_slots), false, null));
                            return;
                        }

                        for (AppointmentSlotUtil.Slot slot : allSlots) {
                            slotsContainer.addView(createSlotCard(formatSlot(slot), !slot.available, slot));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        if (isAdded()) Toast.makeText(requireContext(), getString(R.string.error_load_data), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private View createSlotCard(String textValue, boolean clickableBooking, AppointmentSlotUtil.Slot slot) {
        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setMinimumHeight(dp(clickableBooking ? 76 : 58));
        card.setPadding(dp(18), dp(clickableBooking ? 18 : 14), dp(18), dp(clickableBooking ? 18 : 14));
        card.setBackgroundResource(clickableBooking ? R.drawable.comment_background : R.drawable.bg_white_card);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, 12);
        card.setLayoutParams(cardParams);

        TextView text = createSlotText(textValue, clickableBooking);
        card.addView(text, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        if (slot != null && slot.available) {
            Button delete = new Button(requireContext());
            delete.setText(getString(R.string.btn_delete));
            delete.setOnClickListener(v -> deleteSlot(slot));
            card.addView(delete);
        } else if (clickableBooking && slot != null && !TextUtils.isEmpty(slot.applicationId)) {
            TextView hint = createSlotText(getString(R.string.schedule_open_booking), true);
            hint.setTextSize(12f);
            card.addView(hint);
            card.setOnClickListener(v -> openApplication(slot.applicationId));
        }
        return card;
    }

    private void openApplication(String applicationId) {
        Intent intent = new Intent(requireContext(), ViewApplicC.class);
        intent.putExtra("id", applicationId);
        startActivity(intent);
    }

    private String formatSlot(AppointmentSlotUtil.Slot slot) {
        if (slot.available) {
            return slot.time + " - " + getString(R.string.schedule_free);
        }
        String fio = TextUtils.isEmpty(slot.fio) ? "-" : slot.fio;
        return slot.time + " - " + fio;
    }

    private TextView createSlotText(String text, boolean accent) {
        TextView view = new TextView(requireContext());
        view.setText(text);
        view.setTextSize(15f);
        view.setTextColor(getResources().getColor(accent ? android.R.color.black : android.R.color.darker_gray));
        view.setGravity(Gravity.CENTER_VERTICAL);
        return view;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private void addSlot() {
        if (!isAdded()) return;
        String time = newSlotTimeV.getText().toString().trim();
        if (TextUtils.isEmpty(time)) {
            Toast.makeText(requireContext(), getString(R.string.error_enter_time), Toast.LENGTH_SHORT).show();
            return;
        }

        String dateKey = AppointmentSlotUtil.toDateKey(selectedDate);
        String slotKey = AppointmentSlotUtil.toSlotKey(time);
        Map<String, Object> slot = new HashMap<>();
        slot.put("time", time);
        slot.put("available", true);
        slot.put("manual", true);
        slot.put("created_by", "center");
        slot.put("source", "manual_center");
        centerRef.child("appointment_slots").child(dateKey).child(slotKey).setValue(slot)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(requireContext(), getString(R.string.schedule_slot_saved), Toast.LENGTH_SHORT).show();
                    newSlotTimeV.setText("");
                    loadSlots();
                })
                .addOnFailureListener(error -> Toast.makeText(requireContext(), getString(R.string.error_save_data), Toast.LENGTH_SHORT).show());
    }

    private void deleteSlot(AppointmentSlotUtil.Slot slot) {
        String dateKey = AppointmentSlotUtil.toDateKey(selectedDate);
        centerRef.child("appointment_slots").child(dateKey).child(slot.key).removeValue()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(requireContext(), getString(R.string.schedule_slot_deleted), Toast.LENGTH_SHORT).show();
                    loadSlots();
                })
                .addOnFailureListener(error -> Toast.makeText(requireContext(), getString(R.string.error_save_data), Toast.LENGTH_SHORT).show());
    }

    private String todayDisplayDate() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.DAY_OF_MONTH) + "/"
                + (calendar.get(Calendar.MONTH) + 1) + "/"
                + calendar.get(Calendar.YEAR);
    }
}
