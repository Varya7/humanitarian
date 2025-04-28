package com.example.hum1;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ViewCenter extends AppCompatActivity {

    private TextView center_nameV, addressV, phone_numberV, emailV, fioV, work_timeV, docV;
    private Button appl, mapB;
    private String userId, centerId;
    private ListAdapter adapter;
    private ArrayList<Map<String, String>> listC;
    private Double latitude, longitude;
    private FirebaseAuth auth;
    private RecyclerView recyclerView;
    private DatabaseReference mDatabase;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_view_center);

        initViews();

        auth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        userId = auth.getCurrentUser().getUid();

        centerId = getIntent().getStringExtra("id");
        if (centerId == null || centerId.isEmpty()) {
            Toast.makeText(this, "Ошибка: ID центра не получен", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        listC = new ArrayList<>();
        adapter = new ListAdapter(listC);
        recyclerView = findViewById(R.id.recyclerView_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadCenterData(centerId);
        loadListData();
        setupButtonListeners();
    }

    private void initViews() {
        center_nameV = findViewById(R.id.center_name);
        addressV = findViewById(R.id.address);
        phone_numberV = findViewById(R.id.phone_number);
        emailV = findViewById(R.id.email);
        fioV = findViewById(R.id.fio);
        work_timeV = findViewById(R.id.work_time);
        docV = findViewById(R.id.doc);
        mapB = findViewById(R.id.route);
        appl = findViewById(R.id.appl);
    }

    private void loadCenterData(String centerId) {
        mDatabase.child("Users").child(centerId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(ViewCenter.this, "Центр не найден", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                try {
                    String centerName = getStringValue(snapshot, "center_name");
                    String address = getStringValue(snapshot, "address");
                    String phone = getStringValue(snapshot, "phone_number");
                    String email = getStringValue(snapshot, "email");
                    String fio = getStringValue(snapshot, "fio");
                    String work_time = getStringValue(snapshot, "work_time");
                    String doc = getStringValue(snapshot, "doc");
                    center_nameV.setText(centerName);
                    addressV.setText(address);
                    phone_numberV.setText(phone);
                    emailV.setText(email);
                    fioV.setText(fio);
                    work_timeV.setText(work_time);
                    docV.setText(doc);
                    latitude = getDoubleValue(snapshot, "latitude");
                    longitude = getDoubleValue(snapshot, "longitude");

                } catch (Exception e) {
                    Toast.makeText(ViewCenter.this, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Ошибка чтения: " + error.getMessage());
                Toast.makeText(ViewCenter.this, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getStringValue(DataSnapshot snapshot, String key) {
        return snapshot.child(key).exists() ? snapshot.child(key).getValue(String.class) : "";
    }

    private Double getDoubleValue(DataSnapshot snapshot, String key) {
        return snapshot.child(key).exists() ? snapshot.child(key).getValue(Double.class) : null;
    }

    private String formatText(String label, String value) {
        return value != null && !value.isEmpty() ? label + ": " + value : label + ": не указано";
    }

    private void setupButtonListeners() {
        appl.setOnClickListener(v -> {
            String centerName = center_nameV.getText().toString();
            Intent intent = new Intent(ViewCenter.this, MainActivity3.class);
            intent.putExtra("center_name", centerName);
            intent.putExtra("id", centerId);
            startActivity(intent);
        });

        mapB.setOnClickListener(v -> {
            if (latitude != null && longitude != null) {
                Intent intent = new Intent(ViewCenter.this, MapActivity.class);
                intent.putExtra("latitude", latitude);
                intent.putExtra("longitude", longitude);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Координаты не доступны", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadListData() {
        mDatabase.child("Users").child(centerId).child("list_c").addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listC.clear();
                for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                    Map<String, String> item = (Map<String, String>) itemSnapshot.getValue();
                    if (item != null && item.containsKey("name") && item.containsKey("quantity")) {
                        listC.add(item);
                    }
                }
                adapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("FirebaseError", "Ошибка чтения списка", databaseError.toException());
            }
        });
    }
}