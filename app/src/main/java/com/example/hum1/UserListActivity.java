package com.example.hum1;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Активность для регистрации нового центра и управления списком пользователей.
 * Позволяет добавлять динамические поля для ввода данных,
 * регистрировать пользователя в Firebase и сохранять все данные в базе данных.
 */
public class UserListActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    private Button btnReg, btnAddRow;
    LinearLayout containerFields;
    private List<String> dataList = new ArrayList<>();
    List<Map<String, String>> listC = new ArrayList<>();
    private String centerName, address, email, password, fio, work_time, phone_number, doc;
    private double latitude, longitude;

    /**
     * Инициализирует активность, получает данные из Intent и настраивает UI элементы.
     *
     * @param savedInstanceState Сохраненное состояние активности (если есть)
     */
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleUtil.initAppLocale(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list2);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        mAuth = FirebaseAuth.getInstance();

        String listCJson = getIntent().getStringExtra("list_c");
        if (listCJson != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<Map<String, String>>>() {}.getType();
            listC = gson.fromJson(listCJson, type);
        }
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

        btnReg = findViewById(R.id.btn_register);
        btnAddRow = findViewById(R.id.btn_add_row);
        containerFields = findViewById(R.id.container_fields);

        btnAddRow.setOnClickListener(v -> addRow());

        btnReg.setOnClickListener(v -> {
            if (validateAndCollectData()) {
                registerUser();
            }
        });
    }

    /**
     * Добавляет новую строку с полем для ввода данных.
     * Каждая строка содержит одно текстовое поле.
     */
    void addRow() {
        LinearLayout rowLayout = new LinearLayout(this);
        rowLayout.setOrientation(LinearLayout.HORIZONTAL);
        rowLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        EditText etMargin = new EditText(this);
        etMargin.setHint("Поле");
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        etMargin.setLayoutParams(params);

        rowLayout.addView(etMargin);
        containerFields.addView(rowLayout);
    }


    /**
     * Проверяет заполненность всех полей и собирает введенные данные в список.
     *
     * @return true если все поля заполнены, false если есть пустые поля
     */
    boolean validateAndCollectData() {
        dataList.clear();
        for (int i = 0; i < containerFields.getChildCount(); i++) {
            View row = containerFields.getChildAt(i);
            if (row instanceof LinearLayout) {
                LinearLayout rowLayout = (LinearLayout) row;
                EditText etMargin = (EditText) rowLayout.getChildAt(0);

                String margin = etMargin.getText().toString().trim();
                if (margin.isEmpty()) {
                    Toast.makeText(
                            this,
                            getString(R.string.error_fill_all_fields_short),
                            Toast.LENGTH_SHORT
                    ).show();
                    return false;
                }

                dataList.add(margin);
            }
        }
        return true;
    }

    /**
     * Регистрирует нового пользователя в Firebase Authentication.
     * При успешной регистрации сохраняет данные в Realtime Database.
     */
    private void registerUser() {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            saveDataToDatabase(firebaseUser.getUid());
                        }
                    } else {
                        String msg = getString(R.string.error_registration_prefix)
                                + ": " + task.getException().getMessage();
                        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                    }
                });
    }


    /**
     * Сохраняет все данные центра в Firebase Realtime Database.
     * Сохраняемые данные включают:
     * - Основную информацию о центре (название, адрес, контакты)
     * - Учетные данные (email, пароль)
     * - Геопозицию (широту и долготу)
     * - Списки данных (list_u и list_c)
     * - Статус и роль пользователя
     *
     * @param uid Уникальный идентификатор пользователя в Firebase
     */
    private void saveDataToDatabase(String uid) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);

        Map<String, Object> userData = new HashMap<>();
        userData.put("center_name", centerName);
        userData.put("address", address);
        userData.put("email", email);
        userData.put("password", password);
        userData.put("fio", fio);
        userData.put("work_time", work_time);
        userData.put("phone_number", phone_number);
        userData.put("doc", doc);
        userData.put("role", "center");
        userData.put("status", "Рассматривается");
        userData.put("comment", "");
        userData.put("latitude", latitude);
        userData.put("longitude", longitude);
        userData.put("list_u", dataList);
        userData.put("list_c", listC);
        userData.put("id", uid);

        userRef.setValue(userData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(
                                this,
                                getString(R.string.center_registered_success),
                                Toast.LENGTH_SHORT
                        ).show();
                        startActivity(new Intent(this, CenterActivity.class));
                        finish();
                    } else {
                        String msg = getString(R.string.error_save_prefix)
                                + ": " + task.getException().getMessage();
                        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                    }
                });

    }
}
