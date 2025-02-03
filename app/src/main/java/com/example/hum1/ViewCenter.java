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

public class ViewCenter extends AppCompatActivity {

    TextView center_nameV, addressV, phone_numberV, emailV, fioV, listV, work_timeV;
    String userId="", center_name, address, phone_number, email, fio, work_time, list;
    Double latitude, longitude;
    Button appl, mapB;
    FirebaseAuth auth;
    FirebaseUser user;
    private DatabaseReference mDatabase;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_view_center);
        center_nameV = findViewById(R.id.center_name);
        addressV = findViewById(R.id.address);
        phone_numberV = findViewById(R.id.phone_number);
        emailV = findViewById(R.id.email);
        fioV = findViewById(R.id.fio);
        work_timeV = findViewById(R.id.work_time);
        listV = findViewById(R.id.list);
        mapB = findViewById(R.id.route);
        appl = findViewById(R.id.appl);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        assert user != null;
        userId = user.getUid();

        Bundle bundle = getIntent().getExtras();
        assert bundle != null;
        String id = bundle.getString("id");

        mDatabase.child("Users").child(id).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                } else {
                    DataSnapshot snapshot = task.getResult();
                    if (snapshot.exists()) {
                        fio = snapshot.child("fio").getValue(String.class);
                        email = snapshot.child("email").getValue(String.class);
                        work_time = snapshot.child("work_time").getValue(String.class);
                        phone_number = snapshot.child("phone_number").getValue(String.class);
                        address = snapshot.child("address").getValue(String.class);
                        center_name = snapshot.child("center_name").getValue(String.class);
                        list = snapshot.child("list_c").getValue(String.class);
                        latitude = Double.valueOf(snapshot.child("latitude").getValue(String.class));
                        longitude = Double.valueOf(snapshot.child("longitude").getValue(String.class));
                        fioV.setText("ФИО представителя: " + fio);
                        work_timeV.setText("Часы работы: " + work_time);
                        emailV.setText("Email: " + email);
                        addressV.setText("Адрес: " + address);
                        center_nameV.setText("Название центра: " + center_name);
                        phone_numberV.setText("Номер телефона: " + phone_number);
                        listV.setText(list);
                    } else {
                        Log.e("firebase", "No data found");
                    }
                }
            }
        });

        appl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ViewCenter.this, MainActivity3.class);
                intent.putExtra("center_name", center_name);
                startActivity(intent);
            }
        });

        mapB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ViewCenter.this, MapActivity.class);
                intent.putExtra("latitude", latitude);
                intent.putExtra("longitude", longitude);
                startActivity(intent);
            }
        });
    }

}