package com.example.hum1.editdata;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hum1.LocaleUtil;
import com.example.hum1.R;
import com.example.hum1.adapters.ListAdapter;
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

/**
 * Активность для редактирования списка доступных вещей в центре.
 * Позволяет добавлять, редактировать и удалять элементы из списка,
 * хранящегося в Firebase Realtime Database.
 */
public class EditListActivity extends AppCompatActivity {
    FirebaseAuth auth;
    FirebaseUser user;
    private DatabaseReference listCRef;
    private DatabaseReference userListRef;
    private LinearLayout containerFields;
    private Button saveB, btnAddRow;
    private DatabaseReference userRef;
    ArrayList<Map<String, String>> listC;
    ListAdapter adapter;
    RecyclerView recyclerView;
    private String userId;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    List<View> addedRows = new ArrayList<>();

    /**
     * Метод жизненного цикла активности. Инициализирует все компоненты UI,
     * загружает существующие элементы из базы данных, задаёт обработчики на кнопки.
     */
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleUtil.initAppLocale(this);
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_edit_list);

        // Инициализация компонентов
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

        // Загрузка существующих данных
        loadListData();

        // Сохранение всех добавленных вручную строк
        saveB.setOnClickListener(view -> {
            saveAllAddedItems();

            finish();
        });

        // Добавление новой строки
        btnAddRow.setOnClickListener(v -> addRow());
    }

    /**
     * Создаёт новую строку ввода для добавления элемента.
     */
    public void addRow() {
        LinearLayout rowLayout = new LinearLayout(this);
        rowLayout.setOrientation(LinearLayout.HORIZONTAL);
        rowLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        EditText etName = new EditText(this);
        etName.setHint(getString(R.string.hint_item_name));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        etName.setLayoutParams(params);

        EditText etQuantity = new EditText(this);
        etQuantity.setHint(getString(R.string.hint_item_quantity));
        etQuantity.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        etQuantity.setLayoutParams(params);

        Button btnSaveRow = new Button(this);
        btnSaveRow.setText(getString(R.string.btn_save));
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
                Toast.makeText(this,
                        getString(R.string.msg_fill_all_fields_short),
                        Toast.LENGTH_SHORT).show();
            }
        });

        rowLayout.addView(etName);
        rowLayout.addView(etQuantity);
        rowLayout.addView(btnSaveRow);

        containerFields.addView(rowLayout, containerFields.indexOfChild(btnAddRow));
        addedRows.add(rowLayout);
    }

    /**
     * Сохраняет новый элемент в Firebase.
     *
     * @param name     Название элемента.
     * @param quantity Количество.
     */
    private void saveNewItem(String name, String quantity) {
        Map<String, String> newItem = new HashMap<>();
        newItem.put("name", name);
        newItem.put("quantity", quantity);
        String key = userRef.child("list_c").push().getKey();
        if (key != null) {
            userRef.child("list_c").child(key).setValue(newItem)
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(this,
                                    getString(R.string.msg_item_added),
                                    Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(this,
                                    getString(R.string.msg_item_add_error),
                                    Toast.LENGTH_SHORT).show());
        }

    }

    /**
     * Сохраняет все добавленные строки перед выходом из активности.
     */
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

    /**
     * Отображает диалог редактирования существующего элемента.
     *
     * @param position Позиция элемента в списке.
     */
    private void showEditDialog(int position) {
        Map<String, String> item = listC.get(position);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.dialog_edit_item_title));

        View view = getLayoutInflater().inflate(R.layout.dialog_edit_item, null);
        EditText nameEditText = view.findViewById(R.id.edit_name);
        EditText quantityEditText = view.findViewById(R.id.edit_quantity);

        nameEditText.setText(item.get("name"));
        quantityEditText.setText(item.get("quantity"));

        builder.setView(view);

        builder.setPositiveButton(getString(R.string.btn_save), (dialog, which) -> {
            String newName = nameEditText.getText().toString().trim();
            String newQuantity = quantityEditText.getText().toString().trim();

            if (!newName.isEmpty() && !newQuantity.isEmpty()) {
                updateItem(position, newName, newQuantity);
            }
        });

        builder.setNegativeButton(getString(R.string.btn_cancel), null);
        builder.setNeutralButton(getString(R.string.btn_delete),
                (dialog, which) -> deleteItem(position));
        builder.show();
    }

    /**
     * Обновляет данные элемента в Firebase по его позиции.
     */
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

    /**
     * Удаляет элемент из Firebase по его позиции в списке.
     */
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

    /**
     * Загружает список элементов из Firebase и отображает в RecyclerView.
     */
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
