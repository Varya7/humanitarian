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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CenterListActivity extends AppCompatActivity {

    FirebaseUser user;
    FirebaseAuth mAuth;
    Button btn_reg;
    EditText center_list;
    Double latitude, longitude;
    private DatabaseReference mDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_center_list);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        btn_reg = findViewById(R.id.btn_register);
        center_list = findViewById(R.id.list_center);

        latitude = getIntent().getDoubleExtra("latitude", 0.0);
        longitude = getIntent().getDoubleExtra("longitude", 0.0);



        btn_reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String list = String.valueOf(center_list.getText());
                if (list.isEmpty()){
                    Toast.makeText(CenterListActivity.this, "Введите список", Toast.LENGTH_SHORT).show();
                    return;
                }
                else{
                    String idU = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    FirebaseDatabase.getInstance().getReference().child("Users").child(idU)
                            .child("list_c").setValue(list);
                    FirebaseDatabase.getInstance().getReference().child("Users").child(idU)
                            .child("id").setValue(idU);
                    FirebaseDatabase.getInstance().getReference().child("Users").child(idU)
                                    .child("longitude").setValue(latitude);
                    FirebaseDatabase.getInstance().getReference().child("Users").child(idU)
                                    .child("longitude").setValue(longitude);
                    Toast.makeText(CenterListActivity.this, "Аккаунт создан",
                            Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), MainActivity2.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }
}