package com.example.hum1;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.hum1.databinding.ActivityViewApplicQrBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ViewApplicQR extends AppCompatActivity {
    private DatabaseReference mDatabase;
    FirebaseAuth auth;
    FirebaseUser user;
    Button StatusB;
    TextView dateV, timeV, emailV, fioV, phone_numberV, birthV, family_membersV, listV;
    String userId, center_name, date, time, email, fio, phone_number, birth, family_members, list, status;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_view_applic_qr);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        assert user != null;
        userId = user.getUid();
        dateV = findViewById(R.id.date);
        timeV = findViewById(R.id.time);
        emailV = findViewById(R.id.email);
        fioV = findViewById(R.id.fio);
        phone_numberV = findViewById(R.id.phone_number);
        birthV = findViewById(R.id.birth);
        family_membersV = findViewById(R.id.family_members);
        listV = findViewById(R.id.list);
        StatusB = findViewById(R.id.status);

        Bundle bundle = getIntent().getExtras();

        // assert bundle != null;
        String id = bundle.getString("id");

        mDatabase.child("Users").child(userId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                } else {
                    DataSnapshot snapshot = task.getResult();
                    if (snapshot.exists()) {
                        center_name = snapshot.child("center_name").getValue(String.class);
                    } else {
                        Log.e("firebase", "No data found");
                    }
                }
            }
        });

        // assert id != null;
        mDatabase.child("Applications").child(id).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                } else {
                    DataSnapshot snapshot = task.getResult();
                    if (snapshot.exists()) {
                        String center = snapshot.child("center").getValue(String.class);
                        if (! center_name.equals(center)){
                            dateV.setText("Заявка была отправлена в другой центр");
                        }
                        else {
                            email = snapshot.child("email").getValue(String.class);
                            fio = snapshot.child("fio").getValue(String.class);

                            phone_number = snapshot.child("phone_number").getValue(String.class);
                            birth = snapshot.child("birth").getValue(String.class);
                            date = snapshot.child("date").getValue(String.class);
                            time = snapshot.child("time").getValue(String.class);
                            family_members = snapshot.child("family_members").getValue(String.class);
                            list = snapshot.child("list").getValue(String.class);
                            status = snapshot.child("status").getValue(String.class);
                            // Логируем полученные данные
                            Log.d("firebase", "Email: " + email);

                            Log.d("firebase", "Birth: " + birth);
                            Log.d("firebase", "Date: " + date);
                            Log.d("firebase", "Time: " + time);
                            Log.d("firebase", "Family Members: " + family_members);
                            Log.d("firebase", "List: " + list);
                            Log.d("firebase", "Status:" + status);
                            dateV.setText("Дата: " + date);
                            timeV.setText("Время: " + time);
                            emailV.setText("Email: " + email);
                            fioV.setText("ФИО: " + fio);

                            phone_numberV.setText("Номер телефона: " + phone_number);
                            birthV.setText("Дата рождения: " + birth);
                            family_membersV.setText("Количество членов семьи: " + family_members);
                            listV.setText("Список вещей: " + list);
                        }

                    } else {
                        Log.d("firebase", "No data available");
                    }
                }
            }
        });


        StatusB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDatabase.child("Applications").child(id).child("status").setValue("Выдано");
                Toast.makeText(ViewApplicQR.this, "Статус заявки изменен на Выдано", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ViewApplicQR.this, MainActivity2.class);
                startActivity(intent);
                finish();
            }
        });



    }

}