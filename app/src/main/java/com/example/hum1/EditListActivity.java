package com.example.hum1;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditListActivity extends AppCompatActivity {
    FirebaseAuth auth;
    FirebaseUser user;
    private DatabaseReference listCRef;
    private DatabaseReference userListRef;
    private LinearLayout containerFields;
    private Button saveB, btnAddRow;
    private DatabaseReference userRef;
    private ArrayList<Map<String, String>> listC;
    private ListAdapter adapter;
    private RecyclerView recyclerView;
    private String userId;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private List<View> addedRows = new ArrayList<>();
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_edit_list);

        saveB = findViewById(R.id.save);
        btnAddRow = findViewById(R.id.btn_add_row);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        containerFields = findViewById(R.id.container_fields);
        assert user != null;
        userId = user.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        userRef = mDatabase.child("Users").child(userId);

        listC = new ArrayList<>();
        adapter = new ListAdapter(listC, position -> showEditDialog(position));

        recyclerView = findViewById(R.id.recyclerView_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        loadListData();

        saveB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveAllAddedItems();
                Intent intent = new Intent(EditListActivity.this, SettingCFragment.class);
                startActivity(intent);
                finish();
            }
        });

        btnAddRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addRow();
            }
        });
    }

    private void addRow() {
        LinearLayout rowLayout = new LinearLayout(this);
        rowLayout.setOrientation(LinearLayout.HORIZONTAL);
        rowLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        EditText etName = new EditText(this);
        etName.setHint("Название");
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        etName.setLayoutParams(params);

        EditText etQuantity = new EditText(this);
        etQuantity.setHint("Количество");
        etQuantity.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        etQuantity.setLayoutParams(params);

        Button btnSaveRow = new Button(this);
        btnSaveRow.setText("Сохранить");
        btnSaveRow.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        btnSaveRow.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String quantity = etQuantity.getText().toString().trim();

            if (!name.isEmpty() && !quantity.isEmpty()) {
                saveNewItem(name, quantity);
                containerFields.removeView(rowLayout);
                addedRows.remove(rowLayout);
            } else {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            }
        });

        rowLayout.addView(etName);
        rowLayout.addView(etQuantity);
        rowLayout.addView(btnSaveRow);

        containerFields.addView(rowLayout, containerFields.indexOfChild(btnAddRow));
        addedRows.add(rowLayout);
    }


    private void saveNewItem(String name, String quantity) {
        Map<String, String> newItem = new HashMap<>();
        newItem.put("name", name);
        newItem.put("quantity", quantity);
        String key = userRef.child("list_c").push().getKey();
        if (key != null) {
            userRef.child("list_c").child(key).setValue(newItem)
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Элемент добавлен", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Ошибка добавления", Toast.LENGTH_SHORT).show());
        }
    }

    private void saveAllAddedItems() {
        for (View row : addedRows) {
            LinearLayout rowLayout = (LinearLayout) row;
            EditText etName = (EditText) rowLayout.getChildAt(0);
            EditText etQuantity = (EditText) rowLayout.getChildAt(1);
            String name = etName.getText().toString().trim();
            String quantity = etQuantity.getText().toString().trim();
            if (!name.isEmpty() && !quantity.isEmpty()) {
                saveNewItem(name, quantity);
            }
        }
        addedRows.clear();
    }

    private void showEditDialog(int position) {
        Map<String, String> item = listC.get(position);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Редактировать пункт");

        View view = getLayoutInflater().inflate(R.layout.dialog_edit_item, null);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) EditText nameEditText = view.findViewById(R.id.edit_name);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) EditText quantityEditText = view.findViewById(R.id.edit_quantity);

        nameEditText.setText(item.get("name"));
        quantityEditText.setText(item.get("quantity"));

        builder.setView(view);

        builder.setPositiveButton("Сохранить", (dialog, which) -> {
            String newName = nameEditText.getText().toString().trim();
            String newQuantity = quantityEditText.getText().toString().trim();

            if (!newName.isEmpty() && !newQuantity.isEmpty()) {
                updateItem(position, newName, newQuantity);
            }
        });

        builder.setNegativeButton("Отмена", null);
        builder.setNeutralButton("Удалить", (dialog, which) -> deleteItem(position));

        builder.show();
    }



    private void updateItem(int position, String newName, String newQuantity) {
        Map<String, String> updatedItem = new HashMap<>();
        updatedItem.put("name", newName);
        updatedItem.put("quantity", newQuantity);
        userRef.child("list_c").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int i = 0;
                for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                    if (i == position) {
                        itemSnapshot.getRef().setValue(updatedItem);
                        break;
                    }
                    i++;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                 }
        });
    }

    private void deleteItem(int position) {
        userRef.child("list_c").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int i = 0;
                for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                    if (i == position) {
                        itemSnapshot.getRef().removeValue();
                        break;
                    }
                    i++;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}