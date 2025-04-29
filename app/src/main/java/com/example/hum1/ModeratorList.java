package com.example.hum1;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

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

public class ModeratorList extends AppCompatActivity {

    ArrayList<CenterApp> centers = new ArrayList<CenterApp>();
    List<String> a1;
    private DatabaseReference mDatabase;
    FirebaseUser user;
    FirebaseAuth auth;
    Spinner spinner;
    CenterAppAdapter adapter;
    BottomNavigationView bottomNavigationView;
    ArrayAdapter<String> adapter1;
    private String id_appl, userId, center, role,  status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_moderator_list);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        spinner = findViewById(R.id.spinner);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);


        mDatabase = FirebaseDatabase.getInstance().getReference();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        assert user != null;
        userId = user.getUid();

        RecyclerView recyclerView = findViewById(R.id.list);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        CenterAppAdapter.OnCenterAppClickListener appClickListener = new CenterAppAdapter.OnCenterAppClickListener() {
            @Override
            public void onCenterAppClick(CenterApp centerApp, int position) {
                Intent intent = new Intent(getApplicationContext(), ViewCenterApp.class);
                intent.putExtra("id", centerApp.getId_appl());
                startActivity(intent);
            }


        };

        adapter = new CenterAppAdapter(this, centers, appClickListener);
        recyclerView.setAdapter(adapter);
        a1 = new ArrayList<>();
        a1.add("Рассматривается");
        a1.add("Одобрено");
        a1.add("Отклонено");
        adapter1 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, a1);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter1);

        AdapterView.OnItemSelectedListener itemSelectedListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String item = (String) parent.getItemAtPosition(position);
                centers.clear();
                mDatabase.child("Users").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if (task.isSuccessful()) {

                            DataSnapshot snapshot = task.getResult();

                            if (snapshot.exists()) {
                                for (DataSnapshot applicationSnapshot : snapshot.getChildren()) {
                                    role = applicationSnapshot.child("role").getValue(String.class);
                                    status = applicationSnapshot.child("status").getValue(String.class);
                                    if (role != null && status != null && item != null &&
                                            status.equals(item)) {
                                        center = applicationSnapshot.child("center_name").getValue(String.class);
                                        id_appl = applicationSnapshot.getKey();
                                        centers.add(new CenterApp(center, id_appl));
                                    }

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


    @Override
    protected void onResume() {
        super.onResume();
        bottomNavigationView.setSelectedItemId(R.id.navigation_see);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    if (item.getItemId()== R.id.navigation_see){
                        return true;
                    }
                    else if (item.getItemId() == R.id.navigation_setting){
                        Intent intent = new Intent(ModeratorList.this, SettingModerator.class);
                        startActivity(intent);
                        finish();
                        return true;

                    }
                    return false;
                }

            };

}