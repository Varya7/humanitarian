package com.example.hum1;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserListActivity extends AppCompatActivity {


    private FirebaseAuth mAuth;
    private Button btnReg, btnAddRow;
    private LinearLayout containerFields;
    private List<String> dataList = new ArrayList<>();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_user_list2);


        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        btnReg = findViewById(R.id.btn_register);
        btnAddRow = findViewById(R.id.btn_add_row);
        containerFields = findViewById(R.id.container_fields);

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

        EditText etMargin = new EditText(this);
        etMargin.setHint("Поле");
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        etMargin.setLayoutParams(params);

        rowLayout.addView(etMargin);
        containerFields.addView(rowLayout);
    }

    private boolean validateAndCollectData() {
        dataList.clear();
        for (int i = 0; i < containerFields.getChildCount(); i++) {
            View row = containerFields.getChildAt(i);
            if (row instanceof LinearLayout) {
                LinearLayout rowLayout = (LinearLayout) row;
                EditText etMargin = (EditText) rowLayout.getChildAt(0);

                String margin = etMargin.getText().toString().trim();

                if (margin.isEmpty()) {
                    Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
                    return false;
                }
                dataList.add(margin);
            }
        }
        return true;
    }

    private void saveDataToFirebase() {
        String idU = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(idU);

        userRef.child("list_u").setValue(dataList);

        Toast.makeText(this, "Данные сохранены", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getApplicationContext(), CenterApplicationsFragment.class);
        startActivity(intent);
        finish();
    }
}