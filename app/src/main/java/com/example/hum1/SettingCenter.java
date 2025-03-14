package com.example.hum1;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SettingCenter extends AppCompatActivity {
    FirebaseAuth auth;
    FirebaseUser user;
    String userId ="", center_name="", address="",  email = "", fio = "", work_time="", phone_number ="", list="";
    private DatabaseReference mDatabase;
    TextView emailV, fioV, work_timeV, phone_numberV, logoutV, deleteV, listV, center_nameV, addressV;
    Button edit_dataB, edit_passwordB, edit_listB;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_setting_center);

        emailV = findViewById(R.id.email);
        fioV = findViewById(R.id.fio);
        listV = findViewById(R.id.list);
        work_timeV = findViewById(R.id.work_time);
        phone_numberV = findViewById(R.id.phone_number);
        edit_dataB = findViewById(R.id.btn_edit_data);
        edit_passwordB = findViewById(R.id.btn_edit_password);
        edit_listB = findViewById(R.id.btn_edit_list);
        center_nameV = findViewById(R.id.center_name);
        addressV = findViewById(R.id.address);
        logoutV = findViewById(R.id.logout);
        deleteV = findViewById(R.id.delete);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        assert user != null;
        userId = user.getUid();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);

        mDatabase.child("Users").child(userId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
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



        logoutV.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(SettingCenter.this, Login.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

            }
        });
        deleteV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(SettingCenter.this)
                        .setTitle("Подтверждение удаления")
                        .setMessage("Вы хотите удалить свой аккаунт?")
                        .setPositiveButton("Удалить", (dialog, which) -> {
                            user.delete()
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            Log.d("Firebase", "User account deleted successfully");
                                            Toast.makeText(SettingCenter.this, "Аккаунт удален", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(SettingCenter.this, Login.class));
                                            finish();
                                        } else {
                                            Log.w("Firebase", "Error deleting user account", task1.getException());
                                            Toast.makeText(SettingCenter.this, "Ошибка удаления аккаунта", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        })
                        .setNegativeButton("Отмена", null)
                        .show();
            }
        });
        edit_dataB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingCenter.this, EditDataCenterActivity.class);
                startActivity(intent);
            }
        });
        edit_passwordB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingCenter.this, ChangePasswordActivity.class);
                startActivity(intent);
            }
        });
edit_listB.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        Intent intent = new Intent(SettingCenter.this, EditListActivity.class);
        startActivity(intent);
    }
});


    }


    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    if (item.getItemId()== R.id.navigation_see){
                        Intent intent = new Intent(SettingCenter.this, MainActivity2.class);
                        startActivity(intent);
                        return true;
                    }

                    else if (item.getItemId() == R.id.navigation_setting){
                        return true;
                    }
                    return false;
                }
            };

}