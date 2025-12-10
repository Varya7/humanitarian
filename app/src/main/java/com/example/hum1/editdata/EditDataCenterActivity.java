package com.example.hum1.editdata;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
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
    EditText center_nameV, addressV, fioV, work_timeV, phone_numberV, docV;
    String userId = "", center_name = "", address = "", fio = "", work_time = "", phone_number = "", doc = "";
    Button saveB;
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
        EdgeToEdge.enable(this);
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
                        phone_number = snapshot.child("phone_number").getValue(String.class);
                        address = snapshot.child("address").getValue(String.class);
                        center_name = snapshot.child("center_name").getValue(String.class);
                        doc = snapshot.child("doc").getValue(String.class);

                        // Отображение данных в интерфейсе
                        center_nameV.setText(center_name);
                        addressV.setText(address);
                        fioV.setText(fio);
                        work_timeV.setText(work_time);
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
                work_time = work_timeV.getText().toString();
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
}
