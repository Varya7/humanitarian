package com.example.hum1.editdata;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hum1.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

/**
 * Активность для редактирования персональных данных пользователя.
 * Позволяет изменить ФИО, дату рождения и номер телефона.
 */
public class EditDataUserActivity extends AppCompatActivity {

    FirebaseAuth auth;
    EditText fioV, birthV, phone_numberV;
    String userId = "", fio = "", birth = "", phone_number = "";
    Button saveB;
    DatabaseReference mDatabase;
    FirebaseUser user;
    FirebaseAuth mAuth;

    /**
     * Метод жизненного цикла активности. Загружает текущие пользовательские данные из Firebase,
     * инициализирует поля ввода и кнопку сохранения. Устанавливает обработчик для выбора даты рождения.
     *
     * @param savedInstanceState Состояние, переданное системе Android при восстановлении активности
     */
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_edit_data_user);

        // Инициализация UI-компонентов
        fioV = findViewById(R.id.fio);
        birthV = findViewById(R.id.birth);
        phone_numberV = findViewById(R.id.phone_number);
        saveB = findViewById(R.id.save);

        // Инициализация Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        user = mAuth.getCurrentUser();

        assert user != null;
        userId = user.getUid();

        // Обработка выбора даты рождения
        birthV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBirthPickerDialog();
            }
        });

        // Загрузка существующих данных пользователя
        mDatabase.child("Users").child(userId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    DataSnapshot snapshot = task.getResult();
                    if (snapshot.exists()) {
                        fio = snapshot.child("fio").getValue(String.class);
                        birth = snapshot.child("birth").getValue(String.class);
                        phone_number = snapshot.child("phone_number").getValue(String.class);

                        fioV.setText(fio);
                        birthV.setText(birth);
                        phone_numberV.setText(phone_number);
                    }
                }
            }
        });

        // Сохранение изменений
        saveB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fio = fioV.getText().toString();
                birth = birthV.getText().toString();
                phone_number = phone_numberV.getText().toString();

                DatabaseReference userRef = FirebaseDatabase.getInstance()
                        .getReference().child("Users").child(userId);
                userRef.child("fio").setValue(fio);
                userRef.child("birth").setValue(birth);
                userRef.child("phone_number").setValue(phone_number);

                Toast.makeText(EditDataUserActivity.this, "Изменения сохранены", Toast.LENGTH_SHORT).show();


                finish();
            }
        });
    }

    /**
     * Показывает диалог выбора даты и устанавливает выбранную дату в поле birthV.
     */
    void showBirthPickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    birthV.setText(selectedDate);
                },
                year, month, day);

        datePickerDialog.show();
    }
}
