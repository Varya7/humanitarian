package com.example.hum1.editdata;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hum1.LocaleUtil;
import com.example.hum1.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Активность для редактирования списка полей для подачи заявки на получение помощи в Firebase Realtime Database.
 *
 * Позволяет пользователю:
 * - загружать текущие данные из базы данных;
 * - добавлять новые строки;
 * - редактировать и удалять существующие строки;
 * - сохранять изменения обратно в базу данных.
 *
 * Использует RecyclerView для отображения данных и AlertDialog для взаимодействия с пользователем.
 */
public class EditListUActivity extends AppCompatActivity {

    /**
     * Компоненты пользовательского интерфейса.
     */
    RecyclerView recyclerView;
    static List3Adapter adapter;
    static List<String> listU;
    private Button btnAddRow, btnSave;
    private DatabaseReference userRef;

    /**
     * Метод вызывается при создании активности.
     * Инициализирует компоненты интерфейса, настраивает RecyclerView и загружает данные из Firebase.
     *
     * @param savedInstanceState Сохранённое состояние активности (если есть).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleUtil.initAppLocale(this);
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_edit_list_uactivity);

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        recyclerView = findViewById(R.id.recyclerView_list);
        btnAddRow = findViewById(R.id.btn_add_row);
        btnSave = findViewById(R.id.save);

        listU = new ArrayList<>();
        adapter = new List3Adapter(listU);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadDataFromFirebase();

        btnAddRow.setOnClickListener(v -> addNewField());
        btnSave.setOnClickListener(v -> saveChanges());
    }

    /**
     * Загружает список строк с полями из Firebase и обновляет RecyclerView.
     */
    private void loadDataFromFirebase() {
        userRef.child("list_u").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listU.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String item = dataSnapshot.getValue(String.class);
                    if (item != null) {
                        listU.add(item);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditListUActivity.this,
                        getString(R.string.error_load_data),
                        Toast.LENGTH_SHORT).show();
            }

        });
    }

    /**
     * Добавляет новое пустое поле в список через диалоговое окно.
     */
    public void addNewField() {
        showEditDialog("", -1);
    }

    /**
     * Показывает диалог для добавления или редактирования элемента списка.
     *
     * @param currentValue Текущее значение элемента
     * @param position     Позиция элемента в списке
     */
    void showEditDialog(String currentValue, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(position >= 0
                ? getString(R.string.dialog_edit_field_title)
                : getString(R.string.dialog_add_field_title));

        final EditText input = new EditText(this);
        input.setText(currentValue);
        builder.setView(input);

        builder.setPositiveButton(getString(R.string.btn_ok), (dialog, which) -> {
            String newValue = input.getText().toString().trim();
            if (!newValue.isEmpty()) {
                if (position >= 0) {
                    listU.set(position, newValue);
                } else {
                    listU.add(newValue);
                }
                adapter.notifyDataSetChanged();
            }
        });

        if (position >= 0) {
            builder.setNeutralButton(getString(R.string.btn_delete), (dialog, which) -> {
                listU.remove(position);
                adapter.notifyDataSetChanged();
            });
        }

        builder.setNegativeButton(getString(R.string.btn_cancel),
                (dialog, which) -> dialog.cancel());

        builder.show();
    }


    /**
     * Сохраняет изменения в список полей для подачи заявки в Firebase.
     * Завершает активность при успешном сохранении.
     */
    private void saveChanges() {
        userRef.child("list_u").setValue(listU)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(EditListUActivity.this,
                                getString(R.string.changes_saved),
                                Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(EditListUActivity.this,
                                getString(R.string.error_save_short),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }


    /**
     * Адаптер для отображения списка строк в RecyclerView.
     */
    class List3Adapter extends RecyclerView.Adapter<List3Adapter.ViewHolder> {

        private List<String> items;

        /**
         * Конструктор адаптера.
         *
         * @param items Список строк для отображения.
         */
        public List3Adapter(List<String> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_1, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.textView.setText(items.get(position));
            holder.itemView.setOnClickListener(v ->
                    showEditDialog(items.get(position), position));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        /**
         * ViewHolder для отображения одного элемента списка.
         */
        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView textView;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                textView = itemView.findViewById(android.R.id.text1);
            }
        }
    }
}
