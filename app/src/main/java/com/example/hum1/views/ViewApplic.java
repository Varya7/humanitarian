package com.example.hum1.views;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hum1.LocaleUtil;
import com.example.hum1.QRcodeActivity;
import com.example.hum1.R;
import com.example.hum1.adapters.ListAdapter;
import com.example.hum1.adapters.ListU3Adapter;
import com.example.hum1.classes.ListU3;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Активность ViewApplic отображает подробную информацию о заявке,
 * включая данные пользователя, статус, выбранные элементы и возможность
 * генерации QR-кода, если заявка одобрена.
 *
 * Использует Firebase Realtime Database для получения данных.
 *
 * Компоненты:
 * - Текстовые поля для отображения email, ФИО, номера телефона, даты и времени, центра, комментария и т.д.
 * - RecyclerView для отображения выбранных предметов и дополнительной информации.
 * - Кнопка генерации QR-кода, доступная при одобренной заявке.
 */
public class ViewApplic extends AppCompatActivity {

    DatabaseReference mDatabase;

    TextView centerV, statusV, dateV, timeV, emailV, fioV, phone_numberV, birthV, comV;
    String id, date, time, email, fio, phone_number, birth, status, com;
    Button qrcode;
    LinearLayout commentLayout;
    private ListAdapter adapter;
    ListU3Adapter adapter2;
    private ArrayList<ListU3> listU3List;
    private ArrayList<Map<String, String>> listC;
    private RecyclerView recyclerView, recyclerView2;

    /**
     * Инициализация активности, установка UI, загрузка данных заявки.
     *
     * @param savedInstanceState состояние активности, если она восстанавливается
     */
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleUtil.initAppLocale(this);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_view_applic);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        dateV = findViewById(R.id.date);
        timeV = findViewById(R.id.time);
        emailV = findViewById(R.id.email);
        fioV = findViewById(R.id.fio);
        phone_numberV = findViewById(R.id.phone_number);
        birthV = findViewById(R.id.birth);
        centerV = findViewById(R.id.center);
        statusV = findViewById(R.id.status);
        comV = findViewById(R.id.comm);
        qrcode = findViewById(R.id.qrcode);
        commentLayout = findViewById(R.id.commentLayout);

        listC = new ArrayList<>();

        adapter = new ListAdapter(listC);
        recyclerView = findViewById(R.id.recyclerView_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        listU3List = new ArrayList<>();
        adapter2 = new ListU3Adapter(listU3List);
        recyclerView2 = findViewById(R.id.recyclerView_list2);
        recyclerView2.setLayoutManager(new LinearLayoutManager(this));
        recyclerView2.setAdapter(adapter2);



        Bundle bundle = getIntent().getExtras();
        assert bundle != null;
        id = bundle.getString("id");

        assert id != null;
        mDatabase.child("Applications").child(id).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {

                    DataSnapshot snapshot = task.getResult();
                    if (snapshot.exists()) {
                        email = snapshot.child("email").getValue(String.class);
                        fio = snapshot.child("fio").getValue(String.class);

                        phone_number = snapshot.child("phone_number").getValue(String.class);
                        birth = snapshot.child("birth").getValue(String.class);
                        date = snapshot.child("date").getValue(String.class);
                        time = snapshot.child("time").getValue(String.class);
                        status = snapshot.child("status").getValue(String.class);
                        com = snapshot.child("comment").getValue(String.class);
                        String center = snapshot.child("center").getValue(String.class);
                        dateV.setText(date);
                        timeV.setText(time);
                        emailV.setText(email);
                        fioV.setText(fio);
                        phone_numberV.setText(phone_number);
                        birthV.setText(birth);
                        centerV.setText(center);
                        if ("Одобрено".equals(status)) {
                            qrcode.setVisibility(View.VISIBLE);
                        } else {
                            qrcode.setVisibility(View.GONE);
                        }

                        if ("Рассматривается".equals(status)) {
                            statusV.setText(getString(R.string.status_pending));
                        } else if ("Одобрено".equals(status)) {
                            statusV.setText(getString(R.string.status_approved));
                        } else if ("Отклонено".equals(status)) {
                            statusV.setText(getString(R.string.status_rejected));
                        } else if ("Выдано".equals(status)) {
                            statusV.setText(getString(R.string.status_issued));
                        } else {
                            statusV.setText(status);
                        }

                        if (com.equals("")){
                            commentLayout.setVisibility(View.GONE);
                        }
                        else{
                            comV.setText(com);
                        }
                    }
                }
            }
        });


        loadListData();
        loadListU3Data();

        qrcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ("Одобрено".equals(status)) {
                    Intent intent = new Intent(getApplicationContext(), QRcodeActivity.class);
                    intent.putExtra("id", id);
                    startActivity(intent);
                } else {
                    Toast.makeText(
                            ViewApplic.this,
                            getString(R.string.qr_after_approval),
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }
        });


    }


    /**
     * Загрузка списка выбранных предметов из Firebase и обновление RecyclerView.
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
     * Загрузка дополнительной пользовательской информации из Firebase.
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