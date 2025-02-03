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

public class ChangePasswordActivity extends AppCompatActivity {

    EditText old_passwordV, new_passwordV;
    private DatabaseReference mDatabase;
    String old_password="", userId="", role="";
    Button saveB;
    FirebaseUser user;
    FirebaseAuth mAuth;
    FirebaseAuth auth;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_change_password);
        old_passwordV = findViewById(R.id.old_password);
        new_passwordV = findViewById(R.id.new_password);
        saveB = findViewById(R.id.save);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        assert user != null;
        userId = user.getUid();

        saveB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDatabase.child("Users").child(userId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if (!task.isSuccessful()) {
                            Log.e("firebase", "Error getting data", task.getException());
                        } else {
                            DataSnapshot snapshot = task.getResult();
                            if (snapshot.exists()) {
                                old_password = snapshot.child("password").getValue(String.class);
                                role = snapshot.child("role").getValue(String.class);
                                String new_password = String.valueOf(new_passwordV.getText());

                                String oldV = String.valueOf(old_passwordV.getText());
                                if (old_password.equals(oldV)) {
                                    FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                            .child("password").setValue(new_password);
                                Toast.makeText(ChangePasswordActivity.this, "Пароль изменен", Toast.LENGTH_SHORT).show();
                                if (role.equals("user")){
                                    Intent intent = new Intent(ChangePasswordActivity.this, SettingUser.class);
                                    startActivity(intent);
                                    finish();
                                }
                                else{
                                    Intent intent = new Intent(ChangePasswordActivity.this, SettingCenter.class);
                                    startActivity(intent);
                                    finish();
                                }



                                //else
                                }
                                else{
                                    Toast.makeText(ChangePasswordActivity.this, "Неправильный пароль", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Log.e("firebase", "No data found");
                            }
                        }
                    }
                });
            }


        });

    }
}