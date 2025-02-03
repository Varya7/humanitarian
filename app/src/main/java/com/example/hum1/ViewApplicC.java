package com.example.hum1;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ViewApplicC extends AppCompatActivity {

    private DatabaseReference mDatabase;

    Button statusT;
    EditText comV;
    TextView textView, statusF, dateV, timeV, emailV, fioV, phone_numberV, birthV, family_membersV, listV;
    String date, time, email, fio, phone_number, birth, family_members, list, status;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_view_applic_c);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        dateV = findViewById(R.id.date);
        timeV = findViewById(R.id.time);
        emailV = findViewById(R.id.email);
        fioV = findViewById(R.id.fio);
        phone_numberV = findViewById(R.id.phone_number);
        birthV = findViewById(R.id.birth);
        family_membersV = findViewById(R.id.family_members);
        listV = findViewById(R.id.list);
        statusT = findViewById(R.id.statusT);
        statusF = findViewById(R.id.statusF);
        comV = findViewById(R.id.comm);
        Bundle bundle = getIntent().getExtras();

       // assert bundle != null;
        String id = bundle.getString("id");

       // assert id != null;
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
                        // Логируем полученные данные
                        Log.d("firebase", "Email: " + email);

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
                        family_membersV.setText("Количество членов семьи: " +family_members);
                        listV.setText("Список вещей: " +list);
                        if (status.equals("Одобрено")){
                            statusT.setText("Заявка одобрена!");
                        }

                    } else {
                        Log.d("firebase", "No data available");
                    }
                }
            }
        });


        statusT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDatabase.child("Applications").child(id).child("status").setValue("Одобрено");
                if (! String.valueOf(comV.getText()).equals("")){
                    mDatabase.child("Applications").child(id).child("comment").setValue(String.valueOf(comV.getText()));
                }
                Toast.makeText(ViewApplicC.this, "Статус заявки изменен на Одобрено", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ViewApplicC.this, MainActivity2.class);
                startActivity(intent);
                finish();
            }
        });


        statusF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDatabase.child("Applications").child(id).child("status").setValue("Отклонено");
                if (! String.valueOf(comV.getText()).equals("")){
                    mDatabase.child("Applications").child(id).child("comment").setValue(String.valueOf(comV.getText()));
                }
                Toast.makeText(ViewApplicC.this, "Статус заявки изменен на Отклонено", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ViewApplicC.this, MainActivity2.class);
                startActivity(intent);
                finish();
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
            Intent intent = new Intent(this, SettingCenter.class);
            //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return true;
        }

        //headerView.setText(item.getTitle());
        return super.onOptionsItemSelected(item);
    }

}