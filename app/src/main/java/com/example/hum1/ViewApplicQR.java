package com.example.hum1;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hum1.databinding.ActivityViewApplicQrBinding;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Класс ViewApplicQR представляет активность для просмотра данных заявки по QR-коду.
 * Показывает информацию о заявке, включая дату, время, контактные данные, список запрошенных товаров и дополнительную информацию.
 * Позволяет изменить статус заявки на "Выдано" и обновить количество товаров в центре.
 *
 * Использует Firebase Realtime Database для загрузки и обновления данных.
 *
 **/
public class ViewApplicQR extends AppCompatActivity {
    ArrayList<Map<String, String>> listC;
    private ArrayList<Map<String, String>> listC2;
    DatabaseReference mDatabase;
    private DatabaseReference userRef;
    private AlertDialog messageDialog;
    FirebaseAuth auth;
    LinearLayout linearLayoutError, linearLayoutDate, linearLayoutTime, linearLayoutEmail, linearLayoutFio, linearLayoutPhone, linearLayoutBirth;
    private ViewGroup rootView;

    FirebaseUser user;
    private AlertDialog loadingDialog;
    ListAdapter adapter;
    RecyclerView recyclerView;
    RecyclerView recyclerView2;
    ListU3Adapter adapter2;
    private ArrayList<ListU3> listU3List;
    Button StatusB;
    TextView dateV, timeV, emailV, fioV, phone_numberV, birthV, errorV;
    String id, userId, center_name, date, time, email, fio, phone_number, birth, status;

    /**
     * Метод onCreate вызывается при создании активности.
     * Загружает данные из Firebase, проверяет центр и статус заявки, обновляет интерфейс.
     * Инициализирует элементы интерфейса и задаёт обработчики событий.
     *
     * @param savedInstanceState Состояние активности, если оно ранее сохранялось.
     */
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_view_applic_qr);


        auth = FirebaseAuth.getInstance();
        rootView = findViewById(android.R.id.content);
        user = auth.getCurrentUser();
        assert user != null;
        userId = user.getUid();

        dateV = findViewById(R.id.date);
        timeV = findViewById(R.id.time);
        emailV = findViewById(R.id.email);
        fioV = findViewById(R.id.fio);
        phone_numberV = findViewById(R.id.phone_number);
        birthV = findViewById(R.id.birth);
        StatusB = findViewById(R.id.status);
        errorV = findViewById(R.id.error);

        linearLayoutError = findViewById(R.id.linearLayoutError);
        linearLayoutDate = findViewById(R.id.linearLayoutDate);
        linearLayoutTime = findViewById(R.id.linearLayoutTime);
        linearLayoutEmail = findViewById(R.id.linearLayoutEmail);
        linearLayoutFio = findViewById(R.id.linearLayoutFio);
        linearLayoutPhone = findViewById(R.id.linearLayoutPhone);
        linearLayoutBirth = findViewById(R.id.linearLayoutBirth);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        userRef = mDatabase.child("Users").child(userId);

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

        mDatabase.child("Users").child(userId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    DataSnapshot snapshot = task.getResult();
                    if (snapshot.exists()) {
                        center_name = snapshot.child("center_name").getValue(String.class);
                    }
                }
            }
        });

        mDatabase.child("Applications").child(id).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    DataSnapshot snapshot = task.getResult();
                    if (snapshot.exists()) {
                        String center = snapshot.child("center").getValue(String.class);
                        status = snapshot.child("status").getValue(String.class);
                        if (!center_name.equals(center)) {
                            errorV.setText("Заявка отправлена в другой центр");
                            linearLayoutDate.setVisibility(View.GONE);
                            linearLayoutTime.setVisibility(View.GONE);
                            linearLayoutEmail.setVisibility(View.GONE);
                            linearLayoutFio.setVisibility(View.GONE);
                            linearLayoutPhone.setVisibility(View.GONE);
                            linearLayoutBirth.setVisibility(View.GONE);
                            StatusB.setVisibility(View.GONE);
                        }
                        else if (!"Одобрено".equals(status)){
                            errorV.setText("Заявка уже выдана");
                            linearLayoutDate.setVisibility(View.GONE);
                            linearLayoutTime.setVisibility(View.GONE);
                            linearLayoutEmail.setVisibility(View.GONE);
                            linearLayoutFio.setVisibility(View.GONE);
                            linearLayoutPhone.setVisibility(View.GONE);
                            linearLayoutBirth.setVisibility(View.GONE);
                            StatusB.setVisibility(View.GONE);
                        }
                        else {
                            errorV.setVisibility(View.GONE);
                            linearLayoutError.setVisibility(View.GONE);
                            email = snapshot.child("email").getValue(String.class);
                            fio = snapshot.child("fio").getValue(String.class);
                            phone_number = snapshot.child("phone_number").getValue(String.class);
                            birth = snapshot.child("birth").getValue(String.class);
                            date = snapshot.child("date").getValue(String.class);
                            time = snapshot.child("time").getValue(String.class);
                            dateV.setText(date);
                            timeV.setText(time);
                            emailV.setText(email);
                            fioV.setText(fio);
                            phone_numberV.setText(phone_number);
                            birthV.setText(birth);
                            loadListData();
                            loadListU3Data();
                        }
                    }
                }
            }
        });



        StatusB.setOnClickListener(v -> {
            mDatabase.child("Applications").child(id).child("status").setValue("Выдано")
                    .addOnSuccessListener(aVoid -> updateItemQuantities())
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Ошибка обновления статуса", Toast.LENGTH_SHORT).show());
            Intent intent = new Intent(ViewApplicQR.this, CenterApplicationsFragment.class);
            startActivity(intent);
            finish();
        });
    }

    /**
     * Обновляет количество товаров в центре после выдачи заявки.
     * Сравнивает запрошенные товары с имеющимися и вычитает выданное количество.
     * Сохраняет обновлённый список в Firebase.
     */
    void updateItemQuantities() {
        if (center_name == null || center_name.isEmpty()) {
            Toast.makeText(this, "Центр не определен", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference centerItemsRef = mDatabase.child("Users").child(userId).child("list_c");

        centerItemsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    Toast.makeText(ViewApplicQR.this, "Товары центра не найдены", Toast.LENGTH_SHORT).show();
                    return;
                }

                List<Map<String, String>> updatedItems = new ArrayList<>();
                boolean changesMade = false;

                List<Map<String, String>> centerItems = new ArrayList<>();
                for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                    Map<String, String> item = (Map<String, String>) itemSnapshot.getValue();
                    centerItems.add(item);
                }

                for (Map<String, String> centerItem : centerItems) {
                    String centerItemName = centerItem.get("name");
                    String centerItemQtyStr = centerItem.get("quantity");


                    for (Map<String, String> applicationItem : listC) {
                        String applicationItemName = applicationItem.get("name");

                        if (centerItemName.equals(applicationItemName)) {
                            try {
                                int centerQty = Integer.parseInt(centerItemQtyStr);
                                int applicationQty = Integer.parseInt(applicationItem.get("quantity"));

                                int newQty = centerQty - applicationQty;
                                if (newQty < 0) newQty = 0;

                                centerItem.put("quantity", String.valueOf(newQty));
                                changesMade = true;

                                Log.d("UPDATE", "Обновлено: " + centerItemName +
                                        " Было: " + centerQty +
                                        " Стало: " + newQty);
                            } catch (NumberFormatException e) {
                                Log.e("UPDATE", "Ошибка формата количества", e);
                            }
                            break;
                        }
                    }
                    updatedItems.add(centerItem);
                }

                if (changesMade) {
                    centerItemsRef.setValue(updatedItems)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(ViewApplicQR.this, "Статус заявки изменен на Выдано", Toast.LENGTH_SHORT).show();

                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(ViewApplicQR.this, "Ошибка при сохранении", Toast.LENGTH_SHORT).show();

                            });
                } else {
                    Toast.makeText(ViewApplicQR.this, "Нет товаров для списания", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ViewApplicQR.this, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show();

            }
        });
    }

    /**
     * Загружает список выбранных пользователем товаров из Firebase.
     * Отображает только те товары, у которых количество больше 0.
     * Обновляет адаптер списка товаров.
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
     * Загружает дополнительную информацию из заявки.
     * Используется для отображения меток и значений, переданных вместе с заявкой.
     * Обновляет адаптер дополнительной информации.
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