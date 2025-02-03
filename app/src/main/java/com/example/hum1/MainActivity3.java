package com.example.hum1;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class MainActivity3 extends AppCompatActivity {

    FirebaseAuth auth;
    Button appl;

    TextView center_nameV;
    EditText editTextDate, editTextTime, editTextList, editTextFam;
    FirebaseUser user;
    String userId = "", role = "", email = "", phone_number = "", fio = "", birth = "";
    String center;
    private DatabaseReference mDatabase;
    ArrayAdapter<String> adapter;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_main3);

        auth = FirebaseAuth.getInstance();
        appl = findViewById(R.id.appl);

        center_nameV = findViewById(R.id.center_name);
        user = auth.getCurrentUser();
        editTextDate = findViewById(R.id.date);
        editTextTime = findViewById(R.id.time);
        editTextList = findViewById(R.id.list);
        editTextFam = findViewById(R.id.fam);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        userId = user.getUid();

        Bundle bundle = getIntent().getExtras();
        assert bundle != null;
        String center_name = bundle.getString("center_name");

        center_nameV.setText("Подача заявки в центр " + center_name);

        editTextDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog();
            }
        });

        editTextTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimePickerDialog();
            }
        });

        mDatabase.child("Users").child(userId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                } else {
                    DataSnapshot snapshot = task.getResult();
                    if (snapshot.exists()) {
                        // Извлекаем роль
                        role = snapshot.child("role").getValue(String.class);

                        fio = snapshot.child("fio").getValue(String.class);
                        email = snapshot.child("email").getValue(String.class);
                        birth = snapshot.child("birth").getValue(String.class);
                        phone_number = snapshot.child("phone_number").getValue(String.class);
                        Log.d("firebase", "Role: " + role);
                    } else {
                        Log.e("firebase", "No data found");
                    }
                }
            }
        });

        if (user == null) {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        }


        appl.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick (View view){
                String fam, time, date, list;

                fam = String.valueOf(editTextFam.getText());
                time = String.valueOf(editTextTime.getText());
                date = String.valueOf(editTextDate.getText());
                list = String.valueOf(editTextList.getText());


                if (TextUtils.isEmpty(fam)) {
                    Toast.makeText(MainActivity3.this, "Enter family information", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(time)) {
                    Toast.makeText(MainActivity3.this, "Enter time", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(date)) {
                    Toast.makeText(MainActivity3.this, "Enter date", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(list)) {
                    Toast.makeText(MainActivity3.this, "Enter list", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(center)) {
                    Toast.makeText(MainActivity3.this, "Enter center", Toast.LENGTH_SHORT).show();
                    return;
                }

                HashMap<String, String> applicationInfo = new HashMap<>();
                applicationInfo.put("email", email);
                applicationInfo.put("fio", fio);
                applicationInfo.put("phone_number", phone_number);
                applicationInfo.put("birth", birth);
                applicationInfo.put("family_members", fam);
                applicationInfo.put("date", date);
                applicationInfo.put("time", time);
                applicationInfo.put("list", list);
                applicationInfo.put("center", center_name);
                applicationInfo.put("id", userId);
                applicationInfo.put("status", "Рассматривается");
                applicationInfo.put("comment", "");

                editTextDate.setText("");
                editTextTime.setText("");
                editTextList.setText("");
                editTextFam.setText("");

                DatabaseReference newApplicationRef = FirebaseDatabase.getInstance().getReference().child("Applications").push();
// Получение уникального ключа (id) новой заявки
                String applicationId = newApplicationRef.getKey();
                applicationInfo.put("id_appl", applicationId);

// Сохранение данных в Realtime Database
                newApplicationRef.setValue(applicationInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Заявка успешно добавлена
                            Log.d("Firebase", "Application submitted with ID: " + applicationId);
                        } else {
                            // Ошибка при добавлении заявки
                            Log.e("Firebase", "Failed to submit application", task.getException());
                        }
                    }
                });



                Toast.makeText(MainActivity3.this, "Заявка подана", Toast.LENGTH_SHORT).show();
/*
            FirebaseDatabase.getInstance().getReference().child("Applications")
                .push()
                .setValue(applicationInfo);
*/
                Intent intent = new Intent(MainActivity3.this, MyApplications.class);
                startActivity(intent);
                finish();
            }
        });

    }




    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Создаём DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            // Устанавливаем выбранную дату в EditText
            String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear; // Месяцы начинаются с 0
            editTextDate.setText(selectedDate);
        }, year, month, day);

        datePickerDialog.show();
    }



    private void showTimePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, selectedHour, selectedMinute) -> {
            String selectedTime = selectedHour + ":" + String.format("%02d", selectedMinute);
            editTextTime.setText(selectedTime);
        }, hour, minute, true);

        timePickerDialog.show();
    }

}