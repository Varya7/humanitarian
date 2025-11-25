package com.example.hum1;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import android.app.TimePickerDialog;


import com.example.hum1.adapters.ListAdapter2;
import com.example.hum1.adapters.ListU2Adapter;
import com.example.hum1.classes.ListU2;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Активность подачи заявки.
 * Отвечает за отображение интерфейса выбора центра, даты и времени,
 * а также за создание и отправку заявки в Firebase Realtime Database.
 */
public class MainActivity extends AppCompatActivity {


    FirebaseAuth auth;
    Button appl;
    Spinner spinner;
    Integer currentId = 0;
    private RecyclerView recyclerView, recyclerView2;

    EditText editTextDate, editTextTime;
    FirebaseUser user;
    String userId = "", role = "", email = "", phone_number = "", fio = "", birth = "", idC = "";
    String[] centers;
    private ListAdapter2 adapter1;
    private ListU2Adapter adapter2;
    String center;
    private DatabaseReference mDatabase;
    private ArrayList<Map<String, String>> listC;
    ArrayAdapter<String> adapter;
    List<String> centerNames, centersId;
    View view;
    List<ListU2> listU;

    /**
     * Инициализация активности, установка обработчиков и загрузка данных.
     * Выполняется при создании активности.
     *
     * @param savedInstanceState сохраненное состояние активности (если есть)
     */
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_main);
        centerNames = new ArrayList<>();
        centersId = new ArrayList<>();
        spinner = findViewById(R.id.center);
        auth = FirebaseAuth.getInstance();
        appl = findViewById(R.id.appl);

        user = auth.getCurrentUser();

        editTextDate = findViewById(R.id.date);
        editTextTime = findViewById(R.id.time);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        userId = user.getUid();

        listC = new ArrayList<>();
        adapter1 = new ListAdapter2(listC);
        recyclerView = findViewById(R.id.recyclerView_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter1);


        listU = new ArrayList<>();
        adapter2 = new ListU2Adapter(listU);
        recyclerView2 = findViewById(R.id.recyclerView_list2);
        recyclerView2.setLayoutManager(new LinearLayoutManager(this));
        recyclerView2.setAdapter(adapter2);
        recyclerView2.setNestedScrollingEnabled(false);



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

                        role = snapshot.child("role").getValue(String.class);

                        fio = snapshot.child("fio").getValue(String.class);
                        email = snapshot.child("email").getValue(String.class);
                        birth = snapshot.child("birth").getValue(String.class);
                        phone_number = snapshot.child("phone_number").getValue(String.class);

                    }
                }
            }
        });

        mDatabase.child("Users").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {

                if (task.isSuccessful()) {
                    DataSnapshot snapshot = task.getResult();
                    if (snapshot.exists()) {
                        for (DataSnapshot centerSnapshot : snapshot.getChildren()) {
                            String name = centerSnapshot.child("center_name").getValue(String.class);
                            if (name != null) {
                                String status = centerSnapshot.child("status").getValue(String.class);
                                if (status != null && status.equals("Одобрено")) {
                                    String id = centerSnapshot.child("id").getValue(String.class);
                                    centerNames.add(name);
                                    centersId.add(id);
                                }
                            }
                        }
                        adapter.notifyDataSetChanged();


                        }
                }
            }
        });

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, centerNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        if (user == null) {
            Intent intent = new Intent(getApplicationContext(), AuthActivity.class);
            startActivity(intent);
            finish();
        }

        AdapterView.OnItemSelectedListener itemSelectedListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = (String) parent.getItemAtPosition(position);
                center = item;
                currentId = position;
                if (!centersId.isEmpty() && currentId < centersId.size()) {
                    loadListData(centersId.get(currentId));
                    loadListUData(centersId.get(currentId));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };
        spinner.setOnItemSelectedListener(itemSelectedListener);


        if (!centerNames.isEmpty()) {

            loadListData(centersId.get(currentId));
            loadListUData(centersId.get(currentId));
        }

        appl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String time = editTextTime.getText().toString();
                String date = editTextDate.getText().toString();


                if (TextUtils.isEmpty(time)) {
                    Toast.makeText(MainActivity.this, "Введите время", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(date)) {
                    Toast.makeText(MainActivity.this, "Введите дату", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(center)) {
                    Toast.makeText(MainActivity.this, "Выберите центр", Toast.LENGTH_SHORT).show();
                    return;
                }

                HashMap<String, String> listUData = new HashMap<>();

                for (int i = 0; i < recyclerView2.getChildCount(); i++) {
                    View itemView = recyclerView2.getChildAt(i);
                    EditText editText = itemView.findViewById(R.id.margin);
                    if (editText != null) {
                        String text = editText.getText().toString().trim();
                        if (text.isEmpty()) {
                            Toast.makeText(MainActivity.this, "Заполните все поля!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        String fieldName = listU.get(i).getMargin();
                        listUData.put(fieldName, text);
                    }
                }

                HashMap<String, Object> applicationInfo = new HashMap<>();
                applicationInfo.put("email", email);
                applicationInfo.put("fio", fio);
                applicationInfo.put("phone_number", phone_number);
                applicationInfo.put("birth", birth);
                applicationInfo.put("date", date);
                applicationInfo.put("time", time);
                applicationInfo.put("center", center);
                applicationInfo.put("id", userId);
                applicationInfo.put("status", "Рассматривается");
                applicationInfo.put("comment", "");
                applicationInfo.put("list_u", listUData);

                Map<String, Integer> selectedItems = adapter1.getSelectedQuantities();
                applicationInfo.put("selected_items", selectedItems);

                editTextDate.setText("");
                editTextTime.setText("");

                DatabaseReference newApplicationRef = FirebaseDatabase.getInstance()
                        .getReference("Applications").push();
                String applicationId = newApplicationRef.getKey();
                applicationInfo.put("id_appl", applicationId);

                newApplicationRef.setValue(applicationInfo).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(MainActivity.this, "Заявка подана", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MainActivity.this, UserActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(MainActivity.this, "Ошибка при подаче заявки", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });


    }

    /**
     * Отображает Dialog выбора даты.
     * При выборе даты устанавливает значение в поле ввода даты.
     */
    void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {

            String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear; // Месяцы начинаются с 0
            editTextDate.setText(selectedDate);
        }, year, month, day);

        datePickerDialog.show();
    }

    /**
     * Отображает Dialog выбора времени.
     * При выборе времени устанавливает значение в поле ввода времени.
     */
    void showTimePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, selectedHour, selectedMinute) -> {
            String selectedTime = selectedHour + ":" + String.format("%02d", selectedMinute);
            editTextTime.setText(selectedTime);
        }, hour, minute, true);

        timePickerDialog.show();
    }

    /**
     * Загружает список данных "list_c" с названиями и количествами вещей центра с заданным ID из базы данных Firebase.
     * Обновляет адаптер RecyclerView после загрузки данных.
     *
     * @param centerId уникальный идентификатор центра
     */
    private void loadListData(String centerId) {

        mDatabase.child("Users").child(centerId).child("list_c").addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listC.clear();
                for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                    Map<String, String> item = (Map<String, String>) itemSnapshot.getValue();
                    if (item != null && item.containsKey("name") && item.containsKey("quantity")) {
                        listC.add(item);
                    }
                }
                adapter1.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }


    /**
     * Загружает список данных "list_u" с полями, необходимыми для подачи заявки в конкретный центр центра  из базы данных Firebase.
     * Обновляет адаптер RecyclerView после загрузки данных.
     *
     * @param centerId уникальный идентификатор центра
     */
    private void loadListUData(String centerId){
        mDatabase.child("Users").child(centerId).child("list_u").addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listU.clear();
                for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                    String item = (String) itemSnapshot.getValue();
                    if (item != null) {
                        listU.add(new ListU2(item));
                    }
                }
                adapter2.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                }
        });
    }




}