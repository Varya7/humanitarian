package com.example.hum1.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.hum1.MainActivity2;
import com.example.hum1.MyApplications;
import com.example.hum1.R;
import com.example.hum1.RegisterC;
import com.example.hum1.UserListActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CenterListActivity extends AppCompatActivity {

    private FirebaseUser user;
    private FirebaseAuth mAuth;
    private Button btnReg, btnAddRow;
    private LinearLayout containerFields;
    private Double latitude, longitude;
    private List<Map<String, String>> dataList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_center_list);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        btnReg = findViewById(R.id.btn_register);
        btnAddRow = findViewById(R.id.btn_add_row);
        containerFields = findViewById(R.id.container_fields);

        latitude = getIntent().getDoubleExtra("latitude", 0.0);
        longitude = getIntent().getDoubleExtra("longitude", 0.0);

        addRow();

        btnAddRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addRow();
            }
        });


        btnReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateAndCollectData()) {
                    saveDataToFirebase();
                }
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

        rowLayout.addView(etName);
        rowLayout.addView(etQuantity);

        containerFields.addView(rowLayout);
    }

    private boolean validateAndCollectData() {
        dataList.clear();
        for (int i = 0; i < containerFields.getChildCount(); i++) {
            View row = containerFields.getChildAt(i);
            if (row instanceof LinearLayout) {
                LinearLayout rowLayout = (LinearLayout) row;
                EditText etName = (EditText) rowLayout.getChildAt(0);
                EditText etQuantity = (EditText) rowLayout.getChildAt(1);

                String name = etName.getText().toString().trim();
                String quantity = etQuantity.getText().toString().trim();

                if (name.isEmpty() || quantity.isEmpty()) {
                    Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
                    return false;
                }

                Map<String, String> data = new HashMap<>();
                data.put("name", name);
                data.put("quantity", quantity);
                dataList.add(data);
            }
        }
        return true;
    }


    private void saveDataToFirebase() {
        String idU = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(idU);

        userRef.child("list_c").setValue(dataList);
        userRef.child("id").setValue(idU);
        userRef.child("latitude").setValue(latitude);
        userRef.child("longitude").setValue(longitude);

        Toast.makeText(this, "Данные сохранены", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getApplicationContext(), UserListActivity.class);
        startActivity(intent);
        finish();
    }
}