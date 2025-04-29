package com.example.hum1;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity3 extends AppCompatActivity {

    FirebaseAuth auth;
    Button appl;

    TextView center_nameV;
    EditText editTextDate, editTextTime, editTextList, editTextFam;
    private RecyclerView recyclerView, recyclerView2;

    FirebaseUser user;
    String userId = "", role = "", email = "", phone_number = "", fio = "", birth = "";
    private ArrayList<Map<String, String>> listC;
    private ListAdapter2 adapter1;
    List<ListU2> listU;
    private ListU2Adapter adapter2;
    private DatabaseReference mDatabase;
    ArrayAdapter<String> adapter;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_main3);

        auth = FirebaseAuth.getInstance();
        appl = findViewById(R.id.appl);

        center_nameV = findViewById(R.id.center_name);
        user = auth.getCurrentUser();
        editTextDate = findViewById(R.id.date);
        editTextTime = findViewById(R.id.time);
        editTextList = findViewById(R.id.list);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        userId = user.getUid();

        listC = new ArrayList<>();
        adapter1 = new ListAdapter2(listC);
        recyclerView = findViewById(R.id.recyclerView_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter1);

        listU = new ArrayList<>();
        adapter2 = new ListU2Adapter(listU);
        recyclerView2 = findViewById(R.id.recyclerView_list2);
        recyclerView2.setLayoutManager(new LinearLayoutManager(this));
        recyclerView2.setAdapter(adapter2);
        recyclerView2.setNestedScrollingEnabled(false);

        Bundle bundle = getIntent().getExtras();
        assert bundle != null;
        String center_name = bundle.getString("center_name");
        String center_id = bundle.getString("id");
        center_nameV.setText("Подача заявки в центр " + center_name);

        loadListData(center_id);
        loadListUData(center_id);

        editTextDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog();
            }
        });

        editTextTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimePickerDialog();
            }
        });

        mDatabase.child("Users").child(userId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {

                    DataSnapshot snapshot = task.getResult();
                    if (snapshot.exists()) {
                        role = snapshot.child("role").getValue(String.class);

                        fio = snapshot.child("fio").getValue(String.class);
                        email = snapshot.child("email").getValue(String.class);
                        birth = snapshot.child("birth").getValue(String.class);
                        phone_number = snapshot.child("phone_number").getValue(String.class);
                    }
                }
            }
        });

        if (user == null) {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        }



        appl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String time = editTextTime.getText().toString();
                String date = editTextDate.getText().toString();


                if (TextUtils.isEmpty(time)) {
                    Toast.makeText(MainActivity3.this, "Введите время", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(date)) {
                    Toast.makeText(MainActivity3.this, "Введите дату", Toast.LENGTH_SHORT).show();
                    return;
                }



                HashMap<String, Object> applicationInfo = new HashMap<>();
                applicationInfo.put("email", email);
                applicationInfo.put("fio", fio);
                applicationInfo.put("phone_number", phone_number);
                applicationInfo.put("birth", birth);
                applicationInfo.put("date", date);

                applicationInfo.put("time", time);
                applicationInfo.put("center", center_name);
                applicationInfo.put("id", userId);
                applicationInfo.put("status", "Рассматривается");
                applicationInfo.put("comment", "");

                Map<String, Integer> selectedItems = adapter1.getSelectedQuantities();
                applicationInfo.put("selected_items", selectedItems);
                editTextDate.setText("");
                editTextTime.setText("");

                editTextFam.setText("");

                DatabaseReference newApplicationRef = FirebaseDatabase.getInstance()
                        .getReference("Applications").push();
                String applicationId = newApplicationRef.getKey();
                applicationInfo.put("id_appl", applicationId);

                newApplicationRef.setValue(applicationInfo).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(MainActivity3.this, "Заявка подана", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MainActivity3.this, MyApplications.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(MainActivity3.this, "Ошибка при подаче заявки", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });



    }




    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear; // Месяцы начинаются с 0
            editTextDate.setText(selectedDate);
        }, year, month, day);

        datePickerDialog.show();
    }



    private void showTimePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, selectedHour, selectedMinute) -> {
            String selectedTime = selectedHour + ":" + String.format("%02d", selectedMinute);
            editTextTime.setText(selectedTime);
        }, hour, minute, true);

        timePickerDialog.show();
    }

    private void loadListData(String centerId) {
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
                adapter1.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                }
        });
    }

    private void loadListUData(String centerId){
        mDatabase.child("Users").child(centerId).child("list_u").addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listU.clear();
                for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                    String item = (String) itemSnapshot.getValue();
                    if (item != null) {
                        listU.add(new ListU2(item));
                    }
                }
                adapter2.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                }
        });
    }

}