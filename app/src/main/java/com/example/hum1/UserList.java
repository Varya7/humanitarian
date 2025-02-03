package com.example.hum1;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
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

public class UserList extends AppCompatActivity {

    ArrayList<Center> centers = new ArrayList<Center>();

    private DatabaseReference mDatabase;
    FirebaseUser user;
    FirebaseAuth auth;
private CenterAdapter adapter;
    private String id, userId, role,  center_name, address, email, fio, work_time, phone_number, list;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_user_list);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);

        SearchView searchView = findViewById(R.id.searchView);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        assert user != null;
        userId = user.getUid();
        //setInitialData();
         RecyclerView recyclerView = findViewById(R.id.list);

        CenterAdapter.OnCenterClickListener centerClickListener = new CenterAdapter.OnCenterClickListener() {
            @Override
            public void onCenterClick(Center center, int position) {
                Intent intent = new Intent(getApplicationContext(), ViewCenter.class);
                intent.putExtra("id", center.getId());
                startActivity(intent);

            }
        };


         adapter = new CenterAdapter(this, centers, centerClickListener);

        recyclerView.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);


        mDatabase.child("Users").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @SuppressLint("NotifyDataSetChanged")
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
                            role = applicationSnapshot.child("role").getValue(String.class);
                            if (role.equals("center")) {
                                center_name = applicationSnapshot.child("center_name").getValue(String.class);
                                address = applicationSnapshot.child("address").getValue(String.class);
                                email = applicationSnapshot.child("email").getValue(String.class);
                                fio = applicationSnapshot.child("fio").getValue(String.class);
                                work_time = applicationSnapshot.child("work_time").getValue(String.class);
                                phone_number = applicationSnapshot.child("phone_number").getValue(String.class);

                                list = applicationSnapshot.child("list_c").getValue(String.class);//Application application = new Application(date, time, email, name, surname, phone_number, birth, family_members, list);
                                id = applicationSnapshot.child("id").getValue(String.class);
                                centers.add(new Center(id, center_name, address, email, fio, work_time, phone_number, list));
                            }
                            //    setInitialData();
                            // Добавляем в список
                        }

                        adapter.notifyDataSetChanged();
                    }
                }
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterList(newText);
                return true;
            }
        });


    }



    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                    if (item.getItemId()==R.id.navigation_centers){


                        return true;
                    }

                    else if (item.getItemId()== R.id.navigation_see){
                        Intent intent = new Intent(UserList.this, MyApplications.class);
                        startActivity(intent);
                        return true;
                    }
                    else if (item.getItemId() == R.id.navigation_setting){
                        Intent intent = new Intent(UserList.this, SettingUser.class);
                        startActivity(intent);
                        return true;
                    }
                    return false;
                }
            };

    private void setInitialData(){
        centers.add(new Center(id, center_name, address, email, fio, work_time, phone_number, list));
    }

    private void filterList(String text) {
        ArrayList<Center> filteredList = new ArrayList<>();
        for (Center center : centers) {
            if (center.getCenter_name().toLowerCase().contains(text.toLowerCase())||
                    center.getAddress().toLowerCase().contains(text.toLowerCase()) ||
                    center.getFIO().toLowerCase().contains(text.toLowerCase()) ||
                    center.getList().toLowerCase().contains(text.toLowerCase()))
            {
                filteredList.add(center);
            }
        }
        adapter.updateList(filteredList);
    }

}