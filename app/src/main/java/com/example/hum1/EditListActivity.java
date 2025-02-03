package com.example.hum1;

import android.annotation.SuppressLint;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditListActivity extends AppCompatActivity {
Button saveB;
EditText center_listV;
String list="", userId="";
    FirebaseAuth auth;
    private DatabaseReference mDatabase;
    FirebaseUser user;
    FirebaseAuth mAuth;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_edit_list);
        saveB = findViewById(R.id.save);
        center_listV = findViewById(R.id.list_center);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        assert user != null;
        userId = user.getUid();


        mDatabase.child("Users").child(userId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                } else {
                    DataSnapshot snapshot = task.getResult();
                    if (snapshot.exists()) {
                        list = snapshot.child("list_c").getValue(String.class);

                        center_listV.setText(list);

                    } else {
                        Log.e("firebase", "No data found");
                    }
                }

                }
                });

        saveB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                list = String.valueOf(center_listV.getText());


                FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child("list_c").setValue(list);


                Toast.makeText(EditListActivity.this, "Изменения сохранены",
                        Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(EditListActivity.this, SettingCenter.class);
                startActivity(intent);
                finish();
            }
        });

    }
}