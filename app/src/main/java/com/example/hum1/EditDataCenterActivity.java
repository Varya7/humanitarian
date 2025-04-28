package com.example.hum1;

import android.annotation.SuppressLint;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditDataCenterActivity extends AppCompatActivity {
    FirebaseAuth auth;
    EditText center_nameV, addressV, fioV, work_timeV, phone_numberV;
    String userId="", center_name="", address="", fio="", work_time="", phone_number="";
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
        setContentView(R.layout.activity_edit_data_center);
        center_nameV = findViewById(R.id.center_name);
        addressV = findViewById(R.id.address);
        fioV = findViewById(R.id.fio);
        work_timeV = findViewById(R.id.work_time);
        phone_numberV = findViewById(R.id.phone_number);
        saveB = findViewById(R.id.save);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        assert user != null;
        userId = user.getUid();


        mDatabase.child("Users").child(userId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                } else {
                    DataSnapshot snapshot = task.getResult();
                    if (snapshot.exists()) {
                        fio = snapshot.child("fio").getValue(String.class);
                        work_time = snapshot.child("work_time").getValue(String.class);
                        phone_number = snapshot.child("phone_number").getValue(String.class);
                        address = snapshot.child("address").getValue(String.class);
                        center_name = snapshot.child("center_name").getValue(String.class);
                        center_nameV.setText(center_name);
                        addressV.setText(address);
                        fioV.setText(fio);
                        work_timeV.setText(work_time);
                        phone_numberV.setText(phone_number);
                    }
                }
            }
        });


        saveB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fio = String.valueOf(fioV.getText());
                work_time = String.valueOf(work_timeV.getText());
                phone_number = String.valueOf(phone_numberV.getText());
                center_name = String.valueOf(center_nameV.getText());
                address = String.valueOf(addressV.getText());

                FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child("center_name").setValue(center_name);
                FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child("address").setValue(address);
                FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child("fio").setValue(fio);
                FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child("work_time").setValue(work_time);
                FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child("phone_number").setValue(phone_number);

                Toast.makeText(EditDataCenterActivity.this, "Изменения сохранены",
                        Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(EditDataCenterActivity.this, SettingCenter.class);
                startActivity(intent);
                finish();
            }
        });

    }
}