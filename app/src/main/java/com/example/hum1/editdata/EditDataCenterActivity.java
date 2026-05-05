package com.example.hum1.editdata;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hum1.LocaleUtil;
import com.example.hum1.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Активность для редактирования данных центра.
 * Позволяет пользователю (центру) обновлять информацию о себе,
 * такую как имя центра, адрес, ФИО, время работы, номер телефона и поля для отправления заявки.
 */
public class EditDataCenterActivity extends AppCompatActivity {

    FirebaseAuth auth;
    EditText center_nameV, addressV, fioV, work_timeV, work_startV, work_endV, appointment_intervalV, working_daysV, phone_numberV, docV;
    String userId = "", center_name = "", address = "", fio = "", work_time = "", work_start = "", work_end = "", appointment_interval = "", working_days = "", phone_number = "", doc = "";
    Button saveB;
    private final boolean[] selectedDays = new boolean[]{false, true, true, true, true, true, false};
    private final String[] dayKeys = new String[]{"sun", "mon", "tue", "wed", "thu", "fri", "sat"};
    private DatabaseReference mDatabase;
    FirebaseUser user;
    FirebaseAuth mAuth;

    /**
     * Метод жизненного цикла, вызываемый при создании активности.
     * Загружает текущие данные пользователя из Firebase и инициализирует поля интерфейса.
     *
     * @param savedInstanceState Состояние активности, если она пересоздаётся
     */
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleUtil.initAppLocale(this);
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_edit_data_center);

        // Инициализация полей
        docV = findViewById(R.id.doc);
        center_nameV = findViewById(R.id.center_name);
        addressV = findViewById(R.id.address);
        fioV = findViewById(R.id.fio);
        work_timeV = findViewById(R.id.work_time);
        work_startV = findViewById(R.id.work_start);
        work_endV = findViewById(R.id.work_end);
        appointment_intervalV = findViewById(R.id.appointment_interval);
        working_daysV = findViewById(R.id.working_days);
        working_daysV.setFocusable(false);
        working_daysV.setOnClickListener(v -> showWorkingDaysDialog());
        phone_numberV = findViewById(R.id.phone_number);
        saveB = findViewById(R.id.save);

        // Получение ссылки на БД и текущего пользователя
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        assert user != null;
        userId = user.getUid();

        // Загрузка данных пользователя из Firebase
        mDatabase.child("Users").child(userId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    DataSnapshot snapshot = task.getResult();
                    if (snapshot.exists()) {
                        fio = snapshot.child("fio").getValue(String.class);
                        work_time = snapshot.child("work_time").getValue(String.class);
                        work_start = snapshot.child("work_start").getValue(String.class);
                        work_end = snapshot.child("work_end").getValue(String.class);
                        appointment_interval = snapshot.child("appointment_interval_minutes").getValue(String.class);
                        working_days = snapshot.child("working_days").getValue(String.class);
                        phone_number = snapshot.child("phone_number").getValue(String.class);
                        address = snapshot.child("address").getValue(String.class);
                        center_name = snapshot.child("center_name").getValue(String.class);
                        doc = snapshot.child("doc").getValue(String.class);

                        if ((work_start == null || work_start.isEmpty()) && work_time != null && work_time.contains("-")) {
                            String[] parts = work_time.split("-");
                            if (parts.length >= 2) {
                                work_start = parts[0].trim();
                                work_end = parts[1].trim();
                            }
                        }
                        if (appointment_interval == null || appointment_interval.isEmpty()) {
                            appointment_interval = "30";
                        }
                        if (working_days == null || working_days.isEmpty()) {
                            working_days = "mon,tue,wed,thu,fri";
                        }

                        // Отображение данных в интерфейсе
                        center_nameV.setText(center_name);
                        addressV.setText(address);
                        fioV.setText(fio);
                        work_timeV.setText(work_time);
                        work_startV.setText(work_start);
                        work_endV.setText(work_end);
                        appointment_intervalV.setText(appointment_interval);
                        applyWorkingDaysToChecks(working_days);
                        working_daysV.setText(formatSelectedDays());
                        phone_numberV.setText(phone_number);
                        docV.setText(doc);
                    }
                }
            }
        });

        // Сохранение изменений при нажатии кнопки
        saveB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fio = fioV.getText().toString();
                work_start = work_startV.getText().toString();
                work_end = work_endV.getText().toString();
                appointment_interval = appointment_intervalV.getText().toString();
                working_days = working_daysV.getText().toString();
                work_time = work_start + "-" + work_end;
                phone_number = phone_numberV.getText().toString();
                center_name = center_nameV.getText().toString();
                address = addressV.getText().toString();
                doc = docV.getText().toString();

                // Обновление данных в Firebase
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                FirebaseDatabase db = FirebaseDatabase.getInstance();
                db.getReference().child("Users").child(uid).child("center_name").setValue(center_name);
                db.getReference().child("Users").child(uid).child("address").setValue(address);
                db.getReference().child("Users").child(uid).child("fio").setValue(fio);
                db.getReference().child("Users").child(uid).child("work_time").setValue(work_time);
                db.getReference().child("Users").child(uid).child("work_start").setValue(work_start);
                db.getReference().child("Users").child(uid).child("work_end").setValue(work_end);
                db.getReference().child("Users").child(uid).child("appointment_interval_minutes").setValue(appointment_interval);
                db.getReference().child("Users").child(uid).child("working_days").setValue(working_days);
                db.getReference().child("Users").child(uid).child("phone_number").setValue(phone_number);
                db.getReference().child("Users").child(uid).child("doc").setValue(doc);
                Toast.makeText(
                        EditDataCenterActivity.this,
                        getString(R.string.changes_saved),
                        Toast.LENGTH_SHORT
                ).show();

                finish();
            }
        });
    }

    private void showWorkingDaysDialog() {
        String[] labels = new String[]{
                getString(R.string.day_sun),
                getString(R.string.day_mon),
                getString(R.string.day_tue),
                getString(R.string.day_wed),
                getString(R.string.day_thu),
                getString(R.string.day_fri),
                getString(R.string.day_sat)
        };
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.schedule_days_select))
                .setMultiChoiceItems(labels, selectedDays, (dialog, which, isChecked) -> selectedDays[which] = isChecked)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> working_daysV.setText(formatSelectedDays()))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void applyWorkingDaysToChecks(String rawDays) {
        for (int i = 0; i < selectedDays.length; i++) {
            selectedDays[i] = false;
        }
        if (rawDays == null || rawDays.trim().isEmpty()) {
            rawDays = "mon,tue,wed,thu,fri";
        }
        String normalized = "," + rawDays.toLowerCase() + ",";
        for (int i = 0; i < dayKeys.length; i++) {
            selectedDays[i] = normalized.contains("," + dayKeys[i] + ",");
        }
    }

    private String formatSelectedDays() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < dayKeys.length; i++) {
            if (selectedDays[i]) {
                if (builder.length() > 0) {
                    builder.append(",");
                }
                builder.append(dayKeys[i]);
            }
        }
        return builder.toString();
    }

}
