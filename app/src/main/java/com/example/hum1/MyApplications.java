package com.example.hum1;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

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

import java.util.ArrayList;
import java.util.List;

public class MyApplications extends AppCompatActivity {

    ArrayList<ApplicationU> applications = new ArrayList<ApplicationU>();

    private DatabaseReference mDatabase;
    FirebaseUser user;
    FirebaseAuth auth;
    Button appl, cen;
    List<String> a1;
    Spinner spinner;
    ArrayAdapter<String> adapter1;
    private String id_appl, userId, status, center, center_name, id, date, time, email, fio, phone_number, birth, family_members, list;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_my_applications);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        auth = FirebaseAuth.getInstance();
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        setupBottomNavigation();
        bottomNavigationView.setSelectedItemId(R.id.navigation_see);

        spinner = findViewById(R.id.spinner);
        spinner.setEnabled(true);

        user = auth.getCurrentUser();
        appl = findViewById(R.id.appl);
        assert user != null;
        userId = user.getUid();
        RecyclerView recyclerView = findViewById(R.id.list);


        appl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });


        AppAdapterU.OnAppClickListener appClickListener = new AppAdapterU.OnAppClickListener() {
            @Override
            public void onAppClick(ApplicationU app, int position) {
                Intent intent = new Intent(getApplicationContext(), ViewApplic.class);
                intent.putExtra("id", app.getId_appl());
                startActivity(intent);

            }
        };

        AppAdapterU adapter = new AppAdapterU(this, applications, appClickListener);
        recyclerView.setAdapter(adapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

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
            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
                String item = (String) parent.getItemAtPosition(position);
                applications.clear();
                mDatabase.child("Applications").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if (task.isSuccessful()) {

                            DataSnapshot snapshot = task.getResult();

                            if (snapshot.exists()) {
                                for (DataSnapshot applicationSnapshot : snapshot.getChildren()) {
                                    id = applicationSnapshot.child("id").getValue(String.class);
                                    status = applicationSnapshot.child("status").getValue(String.class);
                                    if (id.equals(userId) && status.equals(item)) {
                                        center = applicationSnapshot.child("center").getValue(String.class);
                                        date = applicationSnapshot.child("date").getValue(String.class);
                                        time = applicationSnapshot.child("time").getValue(String.class);
                                        email = applicationSnapshot.child("email").getValue(String.class);
                                        fio = applicationSnapshot.child("fio").getValue(String.class);
                                        phone_number = applicationSnapshot.child("phone_number").getValue(String.class);
                                        birth = applicationSnapshot.child("birth").getValue(String.class);
                                        family_members = applicationSnapshot.child("family_members").getValue(String.class);
                                        list = applicationSnapshot.child("list").getValue(String.class);
                                        id_appl = applicationSnapshot.child("id_appl").getValue(String.class);
                                        applications.add(new ApplicationU(id_appl, date, time, email, fio, phone_number, birth, family_members, list, status, center));
                                    }

                                    adapter.notifyDataSetChanged();
                                }

                            }
                        }
                    }
                });

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }

        };
        spinner.setOnItemSelectedListener(itemSelectedListener);
}


    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    if (item.getItemId() == R.id.navigation_centers) {
                        startActivity(new Intent(MyApplications.this, UserList.class));
                        finish();
                        return true;
                    }
                    else if (item.getItemId() == R.id.navigation_see) {
                        return true;
                    }
                    else if (item.getItemId() == R.id.navigation_setting) {
                        startActivity(new Intent(MyApplications.this, SettingUser.class));
                        finish();
                        return true;
                    }
                    return false;
                }
            };

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);
        bottomNavigationView.setSelectedItemId(R.id.navigation_see);
    }

    private void setInitialData(){
        applications.add(new ApplicationU(id_appl, date, time, email, fio, phone_number, birth, family_members, list, status, center));
    }



}