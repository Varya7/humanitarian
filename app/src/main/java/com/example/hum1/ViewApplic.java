package com.example.hum1;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class ViewApplic extends AppCompatActivity {

    private DatabaseReference mDatabase;

    TextView centerV, statusV, dateV, timeV, emailV, fioV, phone_numberV, birthV, family_membersV, listV, comV;
    String date, time, email, fio, phone_number, birth, family_members, list, status, com;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_view_applic);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        dateV = findViewById(R.id.date);
        timeV = findViewById(R.id.time);
        emailV = findViewById(R.id.email);
        fioV = findViewById(R.id.fio);
        phone_numberV = findViewById(R.id.phone_number);
        birthV = findViewById(R.id.birth);
        family_membersV = findViewById(R.id.family_members);
        listV = findViewById(R.id.list);
        centerV = findViewById(R.id.center);
        statusV = findViewById(R.id.status);
        comV = findViewById(R.id.comm);

        Bundle bundle = getIntent().getExtras();
        assert bundle != null;
        String id = bundle.getString("id");

        assert id != null;
        mDatabase.child("Applications").child(id).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                } else {
                    DataSnapshot snapshot = task.getResult();
                    if (snapshot.exists()) {
                        email = snapshot.child("email").getValue(String.class);
                        fio = snapshot.child("fio").getValue(String.class);

                        phone_number = snapshot.child("phone_number").getValue(String.class);
                        birth = snapshot.child("birth").getValue(String.class);
                        date = snapshot.child("date").getValue(String.class);
                        time = snapshot.child("time").getValue(String.class);
                        family_members = snapshot.child("family_members").getValue(String.class);
                        list = snapshot.child("list").getValue(String.class);
                        status = snapshot.child("status").getValue(String.class);
                        com = snapshot.child("comment").getValue(String.class);
                        String center = snapshot.child("center").getValue(String.class);

                        Log.d("firebase", "Email: " + email);

                        Log.d("firebase", "Phone Number: " + phone_number);
                        Log.d("firebase", "Birth: " + birth);
                        Log.d("firebase", "Date: " + date);
                        Log.d("firebase", "Time: " + time);
                        Log.d("firebase", "Family Members: " + family_members);
                        Log.d("firebase", "List: " + list);
                        Log.d("firebase", "Status:" + status);
                        dateV.setText("Дата: "+ date);
                        timeV.setText("Время: "+ time);
                        emailV.setText("Email: " + email);
                        fioV.setText("ФИО: "+ fio);

                        phone_numberV.setText("Номер телефона: " + phone_number);
                        birthV.setText("Дата рождения: " + birth);
                        centerV.setText("Центр: " + center);
                        family_membersV.setText("Количество членов семьи: " +family_members);
                        listV.setText("Список вещей: " +list);
                        if (status.equals("Рассматривается")){
                            statusV.setText("Статус: На рассмотрении");
                        }
                        else if (status.equals("Одобрено")){
                            statusV.setText("Статус: Одобрено");
                        }
                        else{
                            statusV.setText("Статус: Отклонено");
                        }
                        if (com.equals("")){
                            comV.setText(" ");
                        }
                        else{
                            comV.setText("Комментарий: " + com);
                        }
                    } else {
                        Log.d("firebase", "No data available");
                    }
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //TextView headerView = findViewById(R.id.selectedMenuItem);
        if (id== R.id.action_logout){
            //FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, SettingUser.class);
            //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return true;
        }

        //headerView.setText(item.getTitle());
        return super.onOptionsItemSelected(item);
    }

}