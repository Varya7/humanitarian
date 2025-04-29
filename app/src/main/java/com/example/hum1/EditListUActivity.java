package com.example.hum1;
import android.app.AlertDialog;
import android.content.DialogInterface;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class EditListUActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List3Adapter adapter;
    private List<String> listU;
    private Button btnAddRow, btnSave;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
                Toast.makeText(EditListUActivity.this, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addNewField() {
        showEditDialog("", -1);
    }

    private void showEditDialog(String currentValue, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(position >= 0 ? "Редактировать поле" : "Добавить поле");

        final EditText input = new EditText(this);
        input.setText(currentValue);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
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
            builder.setNeutralButton("Удалить", (dialog, which) -> {
                listU.remove(position);
                adapter.notifyDataSetChanged();
            });
        }

        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void saveChanges() {
        userRef.child("list_u").setValue(listU)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(EditListUActivity.this, "Изменения сохранены", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(EditListUActivity.this, "Ошибка сохранения", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private class List3Adapter extends RecyclerView.Adapter<List3Adapter.ViewHolder> {

        private List<String> items;

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

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView textView;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                textView = itemView.findViewById(android.R.id.text1);
            }
        }
    }
}