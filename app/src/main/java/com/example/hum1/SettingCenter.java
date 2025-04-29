package com.example.hum1;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class SettingCenter extends AppCompatActivity {
    FirebaseAuth auth;
    FirebaseUser user;
    private DatabaseReference userRef;
    private ArrayList<Map<String, String>> listC;
    private RecyclerView recyclerView;
    private ListAdapter adapter;
    String userId = "";
    private DatabaseReference mDatabase;
    LinearLayout commentLayout;
    BottomNavigationView bottomNavigationView;
    TextView  commV, statusV, emailV, fioV, work_timeV, phone_numberV, logoutV, deleteV, center_nameV, addressV, docV;
    Button edit_dataB, edit_passwordB, edit_listB, edit_listU;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_setting_center);

        initViews();

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        assert user != null;
        userId = user.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        userRef = mDatabase.child("Users").child(userId);

        listC = new ArrayList<>();
        adapter = new ListAdapter(listC);
        recyclerView = findViewById(R.id.recyclerView_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadUserData();
        loadListData();

        setupButtons();

        setupBottomNavigation();



        deleteV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(SettingCenter.this)
                        .setTitle("Подтверждение удаления")
                        .setMessage("Вы хотите удалить свой аккаунт?")
                        .setPositiveButton("Удалить", (dialog, which) -> {
                            mDatabase.child("Users").child(userId).removeValue()
                                    .addOnCompleteListener(dbTask -> {
                                        if (dbTask.isSuccessful()) {
                                            user.delete()
                                                    .addOnCompleteListener(authTask -> {
                                                        if (authTask.isSuccessful()) {
                                                            Toast.makeText(SettingCenter.this, "Аккаунт удален", Toast.LENGTH_SHORT).show();
                                                            startActivity(new Intent(SettingCenter.this, Login.class));
                                                            finish();
                                                        } else {
                                                            Toast.makeText(SettingCenter.this, "Ошибка удаления аккаунта", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        } else {
                                            Toast.makeText(SettingCenter.this, "Ошибка удаления данных", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        })
                        .setNegativeButton("Отмена", null)
                        .show();
            }
        });
    }



    private void initViews() {
        emailV = findViewById(R.id.email);
        fioV = findViewById(R.id.fio);
        work_timeV = findViewById(R.id.work_time);
        phone_numberV = findViewById(R.id.phone_number);
        edit_dataB = findViewById(R.id.btn_edit_data);
        edit_passwordB = findViewById(R.id.btn_edit_password);
        edit_listB = findViewById(R.id.btn_edit_list);
        center_nameV = findViewById(R.id.center_name);
        addressV = findViewById(R.id.address);
        logoutV = findViewById(R.id.logout);
        deleteV = findViewById(R.id.delete);
        docV = findViewById(R.id.doc);
        commV = findViewById(R.id.comm);
        statusV = findViewById(R.id.status);
        commentLayout = findViewById(R.id.commentLayout);
        edit_listU = findViewById(R.id.btn_edit_list_user);
    }

    private void loadUserData() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    center_nameV.setText(snapshot.child("center_name").getValue(String.class));
                    addressV.setText(snapshot.child("address").getValue(String.class));
                    fioV.setText(snapshot.child("fio").getValue(String.class));
                    work_timeV.setText(snapshot.child("work_time").getValue(String.class));
                    emailV.setText(snapshot.child("email").getValue(String.class));
                    phone_numberV.setText(snapshot.child("phone_number").getValue(String.class));
                    docV.setText(snapshot.child("doc").getValue(String.class));
                    String status = snapshot.child("status").getValue(String.class);
                    if (status.equals("Рассматривается")){
                        statusV.setText("Заявка на регистрацию центра рассматривается. Пользователи пока не могут отправлять заявки центр. Пожалуйста, дождитесь решения модератора.");
                    }
                    else if (status.equals("Одобрено")){
                        statusV.setText("Заявка на регистрацию центра одобрена. Пользователи могут отправлять заявки в центр.");
                    }
                    else{
                        statusV.setText("Заявка на регистрацию центра отклонена.");
                    }
                    String com = snapshot.child("comment").getValue(String.class);
                    if (com.equals("")){
                        commentLayout.setVisibility(View.GONE);
                    }
                    else{
                        commV.setText(com);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
             }
        });
    }

    private void loadListData() {
        userRef.child("list_c").addValueEventListener(new ValueEventListener() {
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

                updateRecyclerViewHeight();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
             }
        });
    }

    private void updateRecyclerViewHeight() {
        if (adapter.getItemCount() > 0) {

            int heightInDp = adapter.getItemCount() * 56;
            int heightInPx = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, heightInDp, getResources().getDisplayMetrics());

            ViewGroup.LayoutParams params = recyclerView.getLayoutParams();
            params.height = heightInPx;
            recyclerView.setLayoutParams(params);
        }
    }

    private String formatText(String label, String value) {
        return value != null ? label + ": " + value : label + ": не указано";
    }

    private void setupButtons() {
        logoutV.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, Login.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        });

        edit_dataB.setOnClickListener(v ->
                startActivity(new Intent(this, EditDataCenterActivity.class)));

        edit_passwordB.setOnClickListener(v ->
                startActivity(new Intent(this, ChangePasswordActivity.class)));

        edit_listB.setOnClickListener(v ->
                startActivity(new Intent(this, EditListActivity.class)));
        edit_listU.setOnClickListener(v ->
                startActivity(new Intent(this, EditListUActivity.class)));
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Подтверждение удаления")
                .setMessage("Вы хотите удалить свой аккаунт?")
                .setPositiveButton("Удалить", (dialog, which) -> deleteAccount())
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void deleteAccount() {
        userRef.removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        user.delete()
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        Toast.makeText(this, "Аккаунт удалён", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(this, Login.class));
                                        finish();
                                    } else {
                                        Toast.makeText(this, "Ошибка удаления аккаунта: " + task1.getException(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(this, "Ошибка удаления данных из базы: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void setupBottomNavigation() {


        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_setting);
        bottomNavigationView.invalidate();
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navigation_see) {
                startActivity(new Intent(this, MainActivity2.class));
                finish();
                return true;
            }
            return false;
        });

    }

}