package com.example.hum1;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Класс {@code ViewApplicC} представляет экран просмотра и управления заявкой
 * от имени сотрудника центра.
 * Отображает данные заявки, список выбранных позиций, дополнительную информацию,
 * а также предоставляет возможность одобрить или отклонить заявку с комментарием.
 *
 * Использует Firebase Realtime Database для получения и изменения данных заявки.
 */
public class ViewApplicC extends AppCompatActivity {

    DatabaseReference mDatabase;

    private ArrayList<Map<String, String>> listC;
    Button statusT;
    RecyclerView recyclerView;
    RecyclerView recyclerView2;
    ListU3Adapter adapter2;
    private ArrayList<ListU3> listU3List;
    EditText comV;
    ListAdapter adapter;
    TextView statusF, dateV, timeV, emailV, fioV, phone_numberV, birthV, listV;
    String id, date, time, email, fio, phone_number, birth, status;

    /**
     * Метод {@code onCreate} вызывается при создании Activity.
     * Инициализирует компоненты интерфейса, загружает данные заявки из Firebase
     * и настраивает обработчики кнопок изменения статуса заявки.
     *
     * @param savedInstanceState Состояние, сохраненное при предыдущем запуске (если есть).
     */
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_view_applic_c);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        dateV = findViewById(R.id.date);
        timeV = findViewById(R.id.time);
        emailV = findViewById(R.id.email);
        fioV = findViewById(R.id.fio);
        phone_numberV = findViewById(R.id.phone_number);
        birthV = findViewById(R.id.birth);
        listV = findViewById(R.id.list);
        statusT = findViewById(R.id.statusT);
        statusF = findViewById(R.id.statusF);
        comV = findViewById(R.id.comm);

        listC = new ArrayList<>();
        adapter = new ListAdapter(listC);
        recyclerView = findViewById(R.id.recyclerView_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        recyclerView.setNestedScrollingEnabled(false);

        listU3List = new ArrayList<>();
        adapter2 = new ListU3Adapter(listU3List);
        recyclerView2 = findViewById(R.id.recyclerView_list2);
        recyclerView2.setLayoutManager(new LinearLayoutManager(this));
        recyclerView2.setAdapter(adapter2);

        Bundle bundle = getIntent().getExtras();
        id = bundle.getString("id");

        mDatabase.child("Applications").child(id).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                } else {
                    DataSnapshot snapshot = task.getResult();
                    if (snapshot.exists()) {
                        email = snapshot.child("email").getValue(String.class);
                        fio = snapshot.child("fio").getValue(String.class);

                        phone_number = snapshot.child("phone_number").getValue(String.class);
                        birth = snapshot.child("birth").getValue(String.class);
                        date = snapshot.child("date").getValue(String.class);
                        time = snapshot.child("time").getValue(String.class);
                        status = snapshot.child("status").getValue(String.class);
                        dateV.setText(date);
                        timeV.setText(time);
                        emailV.setText(email);
                        fioV.setText(fio);
                        phone_numberV.setText(phone_number);
                        birthV.setText(birth);
                        if (status.equals("Одобрено")){
                            statusT.setText("Заявка одобрена!");
                        }

                    }
                }
            }
        });

        loadListData();
        loadListU3Data();

        statusT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDatabase.child("Applications").child(id).child("status").setValue("Одобрено");
                if (! String.valueOf(comV.getText()).equals("")){
                    mDatabase.child("Applications").child(id).child("comment").setValue(String.valueOf(comV.getText()));
                }
                Toast.makeText(ViewApplicC.this, "Статус заявки изменен на Одобрено", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ViewApplicC.this, CenterApplicationsFragment.class);
                startActivity(intent);
                finish();
            }
        });


        statusF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDatabase.child("Applications").child(id).child("status").setValue("Отклонено");
                if (! String.valueOf(comV.getText()).equals("")){
                    mDatabase.child("Applications").child(id).child("comment").setValue(String.valueOf(comV.getText()));
                }
                Toast.makeText(ViewApplicC.this, "Статус заявки изменен на Отклонено", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ViewApplicC.this, CenterApplicationsFragment.class);
                startActivity(intent);
                finish();
            }
        });

    }

    /**
     * Загружает список выбранных предметов из заявки (selected_items) из Firebase
     * и отображает их в {@link RecyclerView}.
     */
    private void loadListData() {
        mDatabase.child("Applications").child(id).child("selected_items").addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listC.clear();
                for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                    String itemName = itemSnapshot.getKey();
                    Object itemValue = itemSnapshot.getValue();
                    if (itemValue instanceof Long && (Long)itemValue == 0) {
                        continue;
                    }
                    if (itemValue instanceof String && "0".equals(itemValue)) {
                        continue;
                    }

                    Map<String, String> item = new HashMap<>();
                    item.put("name", itemName);

                    if (itemValue instanceof Long) {
                        item.put("quantity", String.valueOf((Long) itemValue));
                    } else if (itemValue instanceof String) {
                        item.put("quantity", (String) itemValue);
                    }

                    listC.add(item);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                  }
        });
    }

    /**
     * Загружает дополнительную информацию о пользователе из заявки в Firebase
     * и отображает её в {@link RecyclerView}.
     */
    private void loadListU3Data() {
        mDatabase.child("Applications").child(id).child("list_u").addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listU3List.clear();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    String label = childSnapshot.getKey();
                    String value = childSnapshot.getValue(String.class);
                    if (label != null && value != null) {
                        listU3List.add(new ListU3(label, value));
                    }
                }
                adapter2.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                  }
        });
    }



}