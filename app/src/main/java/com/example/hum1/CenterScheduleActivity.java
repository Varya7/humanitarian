package com.example.hum1;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
 * Календарь центра для просмотра занятых и свободных окон записи.
 * Центр может выбрать день, увидеть кто записан, добавить свободное окно
 * или удалить свободное окно, если расписание на день изменилось.
 */
public class CenterScheduleActivity extends AppCompatActivity {

    private EditText dateV;
    private EditText newSlotTimeV;
    private LinearLayout slotsContainer;
    private DatabaseReference centerRef;
    private String selectedDate = "";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleUtil.initAppLocale(this);
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_center_schedule);

        String userId = FirebaseAuth.getInstance().getCurrentUser() == null
                ? ""
                : FirebaseAuth.getInstance().getCurrentUser().getUid();
        centerRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        dateV = findViewById(R.id.date);
        newSlotTimeV = findViewById(R.id.new_slot_time);
        slotsContainer = findViewById(R.id.slots_container);
        Button addSlotB = findViewById(R.id.add_slot);

        selectedDate = todayDisplayDate();
        dateV.setText(selectedDate);
        loadSlots();

        dateV.setOnClickListener(v -> showDatePicker());
        addSlotB.setOnClickListener(v -> addSlot());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, day) -> {
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
                renderSlots(slots);
            }

            @Override
            public void onError() {
                Toast.makeText(CenterScheduleActivity.this, getString(R.string.error_load_data), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void renderSlots(List<AppointmentSlotUtil.Slot> availableSlots) {
        slotsContainer.removeAllViews();
        String dateKey = AppointmentSlotUtil.toDateKey(selectedDate);
        centerRef
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot centerSnapshot) {
                        DataSnapshot slotsSnapshot = centerSnapshot.child("appointment_slots").child(dateKey);
                        boolean workingDay = AppointmentSlotUtil.isWorkingDay(centerSnapshot, selectedDate);
                        List<AppointmentSlotUtil.Slot> allSlots = AppointmentSlotUtil.readSlots(slotsSnapshot, false, workingDay);
                        if (allSlots.isEmpty()) {
                            TextView empty = createSlotText(getString(R.string.schedule_no_slots));
                            slotsContainer.addView(empty);
                            return;
                        }

                        for (AppointmentSlotUtil.Slot slot : allSlots) {
                            LinearLayout row = new LinearLayout(CenterScheduleActivity.this);
                            row.setOrientation(LinearLayout.HORIZONTAL);
                            row.setGravity(Gravity.CENTER_VERTICAL);
                            row.setPadding(0, 8, 0, 8);

                            TextView text = createSlotText(formatSlot(slot));
                            row.addView(text, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

                            if (slot.available) {
                                Button delete = new Button(CenterScheduleActivity.this);
                                delete.setText(getString(R.string.btn_delete));
                                delete.setOnClickListener(v -> deleteSlot(slot));
                                row.addView(delete);
                            }
                            slotsContainer.addView(row);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(CenterScheduleActivity.this, getString(R.string.error_load_data), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String formatSlot(AppointmentSlotUtil.Slot slot) {
        if (slot.available) {
            return slot.time + " - " + getString(R.string.schedule_free);
        }
        String fio = TextUtils.isEmpty(slot.fio) ? "-" : slot.fio;
        return slot.time + " - " + fio;
    }

    private TextView createSlotText(String text) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(15f);
        view.setTextColor(getResources().getColor(android.R.color.black));
        return view;
    }

    private void addSlot() {
        String time = newSlotTimeV.getText().toString().trim();
        if (TextUtils.isEmpty(time)) {
            Toast.makeText(this, getString(R.string.error_enter_time), Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(this, getString(R.string.schedule_slot_saved), Toast.LENGTH_SHORT).show();
                    newSlotTimeV.setText("");
                    loadSlots();
                })
                .addOnFailureListener(error ->
                        Toast.makeText(this, getString(R.string.error_save_data), Toast.LENGTH_SHORT).show());
    }

    private void deleteSlot(AppointmentSlotUtil.Slot slot) {
        String dateKey = AppointmentSlotUtil.toDateKey(selectedDate);
        centerRef.child("appointment_slots").child(dateKey).child(slot.key).removeValue()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, getString(R.string.schedule_slot_deleted), Toast.LENGTH_SHORT).show();
                    loadSlots();
                })
                .addOnFailureListener(error ->
                        Toast.makeText(this, getString(R.string.error_save_data), Toast.LENGTH_SHORT).show());
    }

    private String todayDisplayDate() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.DAY_OF_MONTH) + "/"
                + (calendar.get(Calendar.MONTH) + 1) + "/"
                + calendar.get(Calendar.YEAR);
    }
}
