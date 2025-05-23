package com.example.hum1;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.hum1.ui.CenterListActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class EditDataUserActivity extends AppCompatActivity {
    FirebaseAuth auth;
EditText fioV, birthV, phone_numberV;
String userId="", fio="", birth="", phone_number="";
Button saveB;
    private DatabaseReference mDatabase;
    FirebaseUser user;
    FirebaseAuth mAuth;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_edit_data_user);

        fioV = findViewById(R.id.fio);
        birthV = findViewById(R.id.birth);
        phone_numberV = findViewById(R.id.phone_number);
        saveB = findViewById(R.id.save);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        assert user != null;
        userId = user.getUid();

        birthV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBirthPickerDialog();
            }
        });

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
                        birthV.setText(birth);phone_numberV.setText(phone_number);
                    }
                }
            }
        });




        saveB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fio = String.valueOf(fioV.getText());
                birth = String.valueOf(birthV.getText());
                phone_number = String.valueOf(phone_numberV.getText());

                FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child("fio").setValue(fio);
                FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child("birth").setValue(birth);
                FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child("phone_number").setValue(phone_number);

                Toast.makeText(EditDataUserActivity.this, "Изменения сохранены",
                        Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(EditDataUserActivity.this, SettingFragment.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void showBirthPickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {

            String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
            birthV.setText(selectedDate);
        }, year, month, day);

        datePickerDialog.show();
    }
}