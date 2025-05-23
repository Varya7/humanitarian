package com.example.hum1;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ViewCenterApp extends AppCompatActivity {
    private ArrayList<Map<String, String>> listC;
    public ArrayList<ListU> listU;

    Button statusT;
    String id, email, fio, phone_number, work_time, center_name, address, doc, status;
    private DatabaseReference mDatabase;
    private ListAdapter adapter;
    private ListUAdapter adapter2;
    private RecyclerView recyclerView, recyclerView2;
    TextView statusF, emailV, fioV, work_timeV, phone_numberV, logoutV, deleteV, center_nameV, addressV, docV;
    EditText comV;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_view_center_app);
        initViews();

        mDatabase = FirebaseDatabase.getInstance().getReference();


        listC = new ArrayList<>();
        adapter = new ListAdapter(listC);
        recyclerView = findViewById(R.id.recyclerView_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        recyclerView.setNestedScrollingEnabled(false);

        listU = new ArrayList<>();
        adapter2 = new ListUAdapter(listU);
        recyclerView2 = findViewById(R.id.recyclerView_list2);
        recyclerView2.setLayoutManager(new LinearLayoutManager(this));
        recyclerView2.setAdapter(adapter2);
        recyclerView2.setNestedScrollingEnabled(false);

        Bundle bundle = getIntent().getExtras();
        id = bundle.getString("id");


        mDatabase.child("Users").child(id).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
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
                        center_name = snapshot.child("center_name").getValue(String.class);
                        work_time = snapshot.child("work_time").getValue(String.class);
                        address = snapshot.child("address").getValue(String.class);
                        doc = snapshot.child("doc").getValue(String.class);
                        status = snapshot.child("status").getValue(String.class);
                        work_timeV.setText(work_time);
                        addressV.setText(address);
                        emailV.setText(email);
                        fioV.setText(fio);
                        phone_numberV.setText(phone_number);
                        docV.setText(doc);
                        center_nameV.setText(center_name);
                        if (status.equals("Одобрено")){
                            statusT.setText("Заявка одобрена!");
                        }

                    }
                }
            }
        });
        loadListData();
        loadListUData();

        statusT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDatabase.child("Users").child(id).child("status").setValue("Одобрено");
                if (! String.valueOf(comV.getText()).equals("")){
                    mDatabase.child("Users").child(id).child("comment").setValue(String.valueOf(comV.getText()));
                }
                Toast.makeText(ViewCenterApp.this, "Статус заявки изменен на Одобрено", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ViewCenterApp.this, ModeratorListFragment.class);
                startActivity(intent);
                finish();
            }
        });


        statusF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDatabase.child("Users").child(id).child("status").setValue("Отклонено");
                if (! String.valueOf(comV.getText()).equals("")){
                    mDatabase.child("Users").child(id).child("comment").setValue(String.valueOf(comV.getText()));
                }
                Toast.makeText(ViewCenterApp.this, "Статус заявки изменен на Отклонено", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ViewCenterApp.this, ModeratorListFragment.class);
                startActivity(intent);
                finish();
            }
        });
    }



    private void initViews() {
        emailV = findViewById(R.id.email);
        fioV = findViewById(R.id.fio);
        work_timeV = findViewById(R.id.work_time);
        phone_numberV = findViewById(R.id.phone_number);
        statusT = findViewById(R.id.statusT);
        statusF = findViewById(R.id.statusF);
        comV = findViewById(R.id.comm);
        center_nameV = findViewById(R.id.center_name);
        addressV = findViewById(R.id.address);
        logoutV = findViewById(R.id.logout);
        deleteV = findViewById(R.id.delete);
        docV = findViewById(R.id.doc);
    }

    private void loadListData() {
        mDatabase.child("Users").child(id).child("list_c").addValueEventListener(new ValueEventListener() {
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
                Log.w("FirebaseError", "Ошибка чтения списка", databaseError.toException());
            }
        });
    }

    private void loadListUData() {
        mDatabase.child("Users").child(id).child("list_u").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<ListU> newList = new ArrayList<>();

                for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                    String itemText = itemSnapshot.getValue(String.class);
                    if (itemText != null && !itemText.isEmpty()) {
                        newList.add(new ListU(itemText));
                    }
                }

                updateListUData(newList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FirebaseError", "Ошибка чтения списка list_u", databaseError.toException());
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateListUData(List<ListU> newList) {
        if (adapter2 == null) {
            adapter2 = new ListUAdapter(newList);
            recyclerView2.setAdapter(adapter2);
        } else {
            adapter2.updateList(newList);
        }
        if (recyclerView2 != null) {
            recyclerView2.post(this::updateRecyclerViewHeight2);
        }
    }

    private void updateRecyclerViewHeight2() {
        if (adapter2.getItemCount() > 0) {

            int heightInDp = adapter2.getItemCount() * 56;
            int heightInPx = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, heightInDp, getResources().getDisplayMetrics());

            ViewGroup.LayoutParams params = recyclerView2.getLayoutParams();
            params.height = heightInPx;
            recyclerView2.setLayoutParams(params);
        }
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

}