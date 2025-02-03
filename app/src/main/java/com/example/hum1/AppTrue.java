package com.example.hum1;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hum1.databinding.ActivityAppTrueBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class AppTrue extends AppCompatActivity {

    private ActivityAppTrueBinding binding;

    ArrayList<Application> applications = new ArrayList<Application>();

    private DatabaseReference mDatabase;
    FirebaseUser user;
    FirebaseAuth auth;
    private String id_appl, userId, center, center_name, id, date, time, email, fio, phone_number, birth, family_members, list, status;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_app_true);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        assert user != null;
        userId = user.getUid();

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
        AppAdapter adapter = new AppAdapter(this, applications, appClickListener);
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
                            if (center.equals(center_name) && status.equals("Одобрено")) {
                                // Извлекаем данные из snapshot
                                id = applicationSnapshot.child("id").getValue(String.class);
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



    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                    if (item.getItemId()==R.id.navigation_true){

                        return true;
                    }
                    else if (item.getItemId()== R.id.navigation_see){
                        Intent intent = new Intent(AppTrue.this, MainActivity2.class);
                        startActivity(intent);
                        return true;
                    }
                    else if (item.getItemId()== R.id.navigation_false){
                        Intent intent = new Intent(AppTrue.this, AppFalse.class);
                        startActivity(intent);
                        return true;
                    }
                    else if (item.getItemId() == R.id.navigation_setting){
                        Intent intent = new Intent(AppTrue.this, SettingCenter.class);
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