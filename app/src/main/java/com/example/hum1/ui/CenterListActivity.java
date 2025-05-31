package com.example.hum1.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hum1.R;
import com.example.hum1.UserListActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;


import android.widget.LinearLayout;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Активность для создания и управления списком центров.
 * Позволяет добавлять динамические строки с информацией о центрах,
 * валидировать данные и сохранять их в Firebase.
 */
public class CenterListActivity extends AppCompatActivity {

    private FirebaseUser user;
    private FirebaseAuth mAuth;
    private Button btnReg, btnAddRow;
    private LinearLayout containerFields;
    private Double latitude, longitude;
    String centerName, address, email, password, fio, work_time, phone_number, doc;
    private List<Map<String, String>> dataList = new ArrayList<>();

    /**
     * Инициализирует активность, устанавливает обработчики событий
     * и получает данные из предыдущей активности.
     *
     * @param savedInstanceState Сохраненное состояние активности
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_center_list);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        btnReg = findViewById(R.id.btn_register);
        btnAddRow = findViewById(R.id.btn_add_row);
        containerFields = findViewById(R.id.container_fields);

        centerName = getIntent().getStringExtra("center_name");
        address = getIntent().getStringExtra("address");
        email = getIntent().getStringExtra("email");
        password = getIntent().getStringExtra("password");
        fio = getIntent().getStringExtra("fio");
        work_time = getIntent().getStringExtra("work_time");
        phone_number = getIntent().getStringExtra("phone_number");
        doc = getIntent().getStringExtra("doc");
        latitude = getIntent().getDoubleExtra("latitude", 0.0);
        longitude = getIntent().getDoubleExtra("longitude", 0.0);

        addRow();

        btnAddRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addRow();
            }
        });

        btnReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateAndCollectData()) {
                    saveData();
                }
            }
        });
    }

    /**
     * Добавляет новую строку с полями для ввода информации о центре.
     * Каждая строка содержит:
     * - Поле для названия
     * - Поле для количества (только числа)
     */
    private void addRow() {
        LinearLayout rowLayout = new LinearLayout(this);
        rowLayout.setOrientation(LinearLayout.HORIZONTAL);
        rowLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        EditText etName = new EditText(this);
        etName.setHint("Название");
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        etName.setLayoutParams(params);

        EditText etQuantity = new EditText(this);
        etQuantity.setHint("Количество");
        etQuantity.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        etQuantity.setLayoutParams(params);

        rowLayout.addView(etName);
        rowLayout.addView(etQuantity);

        containerFields.addView(rowLayout);
    }

    /**
     * Проверяет корректность введенных данных и собирает их в коллекцию.
     *
     * @return true если все данные валидны, false если есть ошибки
     */
    private boolean validateAndCollectData() {
        dataList.clear();
        for (int i = 0; i < containerFields.getChildCount(); i++) {
            View row = containerFields.getChildAt(i);
            if (row instanceof LinearLayout) {
                LinearLayout rowLayout = (LinearLayout) row;
                EditText etName = (EditText) rowLayout.getChildAt(0);
                EditText etQuantity = (EditText) rowLayout.getChildAt(1);

                String name = etName.getText().toString().trim();
                String quantity = etQuantity.getText().toString().trim();

                if (name.isEmpty() || quantity.isEmpty()) {
                    Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
                    return false;
                }

                Map<String, String> data = new HashMap<>();
                data.put("name", name);
                data.put("quantity", quantity);
                dataList.add(data);
            }
        }
        return true;
    }

    /**
     * Сохраняет собранные данные и передает их в следующую активность.
     * Преобразует список данных в JSON-формат перед передачей.
     * Передает следующие данные:
     * - Название центра
     * - Адрес
     * - Электронную почту
     * - Пароль
     * - ФИО ответственного
     * - Время работы
     * - Номер телефона
     * - Список документов
     * - Координаты (широта и долгота)
     * - Список данных в JSON-формате
     */
    private void saveData() {
        Gson gson = new Gson();
        String listCJson = gson.toJson(dataList);
        //Map<String, Object> centerInfo = new HashMap<>();
        Intent intent = new Intent(getApplicationContext(), UserListActivity.class);
        intent.putExtra("center_name", centerName);
        intent.putExtra("address", address);
        intent.putExtra("email", email);
        intent.putExtra("password", password);
        intent.putExtra("fio", fio);
        intent.putExtra("work_time", work_time);
        intent.putExtra("phone_number", phone_number);
        intent.putExtra("doc", doc);
        intent.putExtra("latitude", latitude);
        intent.putExtra("longitude", longitude);
        intent.putExtra("list_c", listCJson);

        Toast.makeText(this, "Данные сохранены", Toast.LENGTH_SHORT).show();

        startActivity(intent);
        finish();
    }

}