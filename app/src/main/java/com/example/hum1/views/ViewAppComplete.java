package com.example.hum1.views;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hum1.R;
import com.example.hum1.adapters.ListAdapter;
import com.example.hum1.adapters.ListU3Adapter;
import com.example.hum1.classes.ListU3;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Активность для отображения информации о выданной заявке.
 * Показывает основную информацию о заявке, списки выбранных предметов и дополнительную информацию.
 */
public class ViewAppComplete extends AppCompatActivity {

    DatabaseReference mDatabase;

    ArrayList<Map<String, String>> listC;
    private RecyclerView recyclerView, recyclerView2;
    ListU3Adapter adapter2;
    ArrayList<ListU3> listU3List;
    LinearLayout commentLayout;
    //EditText comV;
    ListAdapter adapter;
    TextView dateV, timeV, emailV, fioV, phone_numberV, birthV, comV;
    String id, com, date, time, email, fio, phone_number, birth, status;

    /**
     * Метод вызывается при создании активности.
     * Инициализирует интерфейс, подключение к Firebase и загружает данные о заявке.
     *
     * @param savedInstanceState Сохранённое состояние активности
     */
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_view_app_complete);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        dateV = findViewById(R.id.date);
        timeV = findViewById(R.id.time);
        emailV = findViewById(R.id.email);
        fioV = findViewById(R.id.fio);
        comV = findViewById(R.id.comm);
        phone_numberV = findViewById(R.id.phone_number);
        commentLayout = findViewById(R.id.commentLayout);
        birthV = findViewById(R.id.birth);
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
        id = bundle.getString("id");

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
                        dateV.setText(date);
                        timeV.setText(time);
                        emailV.setText(email);
                        fioV.setText(fio);

                        phone_numberV.setText(phone_number);
                        birthV.setText(birth);

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

    }


    /**
     * Загружает список выбранных пользователем предметов из узла "selected_items"
     * и отображает их в RecyclerView.
     */
    void loadListData() {
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
     * Загружает дополнительную информацию пользователя из узла "list_u"
     * и отображает её в виде списка.
     */
    void loadListU3Data() {
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