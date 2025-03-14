package com.example.hum1;

import static android.app.PendingIntent.getActivity;
import static com.example.hum1.R.id;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.view.Menu;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity2 extends AppCompatActivity {

    //ArrayList<com.example.hum1.Application> applications = new ArrayList<>();
    ArrayList<Application> applications = new ArrayList<Application>();
    List<String> a1;
    private DatabaseReference mDatabase;
    FirebaseUser user;
    FirebaseAuth auth;
    Spinner spinner;
    AppAdapter adapter;
    ImageButton scannerB;
    ArrayAdapter<String> adapter1;
    private String id_appl, userId, center, center_name, id, date, time, email, fio, phone_number, birth, family_members, list, status;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_main2);
        spinner = findViewById(R.id.spinner);
        scannerB = findViewById(R.id.scanner);
        spinner.setEnabled(true);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);


        mDatabase = FirebaseDatabase.getInstance().getReference();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        assert user != null;
        userId = user.getUid();

        scannerB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity2.this, ScanActivity.class);
                startActivity(intent);

            }
        });


        //setInitialData();
        RecyclerView recyclerView = findViewById(R.id.list);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        AppAdapter.OnAppClickListener appClickListener = new AppAdapter.OnAppClickListener() {
            @Override
            public void onAppClick(Application app, int position) {
                Intent intent = new Intent(getApplicationContext(), ViewApplicC.class);
                intent.putExtra("id", app.getId_appl());
                startActivity(intent);
                //finish();
                //Toast.makeText(getApplicationContext(), "Был выбран пункт " + app.getName(),
                //Toast.LENGTH_SHORT).show();
            }
        };

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


        // создаем адаптер
        adapter = new AppAdapter(this, applications, appClickListener);
        // устанавливаем для списка адаптер
        recyclerView.setAdapter(adapter);

        mDatabase.child("Applications").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                } else {
                    DataSnapshot snapshot = task.getResult();
                    //textView.setText(snapshot.toString());
                    //заполнить лист, с помощью конструктора создать объекты и заполнить массив
                    //
                    if (snapshot.exists()) {
                        for (DataSnapshot applicationSnapshot : snapshot.getChildren()) {

                            center = applicationSnapshot.child("center").getValue(String.class);
                            status = applicationSnapshot.child("status").getValue(String.class);
                            if (center.equals(center_name) && status.equals("Рассматривается")) {
                                // Извлекаем данные из snapshot
                                //id = applicationSnapshot.child("id").getValue(String.class);
                                date = applicationSnapshot.child("date").getValue(String.class);
                                time = applicationSnapshot.child("time").getValue(String.class);
                                email = applicationSnapshot.child("email").getValue(String.class);
                                fio = applicationSnapshot.child("fio").getValue(String.class);

                                phone_number = applicationSnapshot.child("phone_number").getValue(String.class);
                                birth = applicationSnapshot.child("birth").getValue(String.class);
                                family_members = applicationSnapshot.child("family_members").getValue(String.class);
                                list = applicationSnapshot.child("list").getValue(String.class);//Application application = new Application(date, time, email, name, surname, phone_number, birth, family_members, list);
                                id_appl = applicationSnapshot.child("id_appl").getValue(String.class);

                                applications.add(new Application(id_appl, date, time, email, fio, phone_number, birth, family_members, list));
                            }
                            //    setInitialData();
                            // Добавляем в список
                        }
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        });

        a1 = new ArrayList<>();
        a1.add("Рассматривается");
        a1.add("Одобрено");
        a1.add("Отклонено");
        a1.add("Выдано");
        adapter1 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, a1);
            adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter1);

        AdapterView.OnItemSelectedListener itemSelectedListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                // Получаем выбранный объект
                String item = (String) parent.getItemAtPosition(position);
                applications.clear();
                mDatabase.child("Applications").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if (!task.isSuccessful()) {
                            Log.e("firebase", "Error getting data", task.getException());
                        } else {
                            DataSnapshot snapshot = task.getResult();
                            //textView.setText(snapshot.toString());
                            //заполнить лист, с помощью конструктора создать объекты и заполнить массив
                            //
                            if (snapshot.exists()) {
                                for (DataSnapshot applicationSnapshot : snapshot.getChildren()) {

                                    center = applicationSnapshot.child("center").getValue(String.class);
                                    status = applicationSnapshot.child("status").getValue(String.class);
                                    if (center.equals(center_name) && status.equals(item)) {
                                        // Извлекаем данные из snapshot
                                        //id = applicationSnapshot.child("id").getValue(String.class);
                                        date = applicationSnapshot.child("date").getValue(String.class);
                                        time = applicationSnapshot.child("time").getValue(String.class);
                                        email = applicationSnapshot.child("email").getValue(String.class);
                                        fio = applicationSnapshot.child("fio").getValue(String.class);

                                        phone_number = applicationSnapshot.child("phone_number").getValue(String.class);
                                        birth = applicationSnapshot.child("birth").getValue(String.class);
                                        family_members = applicationSnapshot.child("family_members").getValue(String.class);
                                        list = applicationSnapshot.child("list").getValue(String.class);//Application application = new Application(date, time, email, name, surname, phone_number, birth, family_members, list);
                                        id_appl = applicationSnapshot.child("id_appl").getValue(String.class);

                                        applications.add(new Application(id_appl, date, time, email, fio, phone_number, birth, family_members, list));
                                    }
                                    //    setInitialData();
                                    // Добавляем в список
                                }
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };
        spinner.setOnItemSelectedListener(itemSelectedListener);
    }





    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    if (item.getItemId()== R.id.navigation_see){
                        return true;
                    }
                    else if (item.getItemId() == R.id.navigation_setting){
                        Intent intent = new Intent(MainActivity2.this, SettingCenter.class);
                        startActivity(intent);
                        return true;
                    }
                    return false;
                }
            };


    private void setInitialData(){
applications.add(new Application(id_appl, date, time, email, fio, phone_number, birth, family_members, list));


    }


}


