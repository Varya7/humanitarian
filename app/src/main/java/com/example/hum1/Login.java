package com.example.hum1;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Login extends AppCompatActivity {
    EditText editTextEmail, editTextPassword;
    Button buttonLogin;
    TextView textView;
    FirebaseUser user;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    String userId="", role="";
    FirebaseAuth auth;
    private DatabaseReference mDatabase;
    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser!=null){
            mDatabase = FirebaseDatabase.getInstance().getReference();
            String idU = currentUser.getUid();

            mDatabase.child("Users").child(idU).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (!task.isSuccessful()) {
                    } else {
                        DataSnapshot snapshot = task.getResult();
                        if (snapshot.exists()) {
                            role = snapshot.child("role").getValue(String.class);
                            if (role.equals("center")) {
                                Intent intent = new Intent(getApplicationContext(), MainActivity2.class);
                                startActivity(intent);
                                finish();
                            }
                            else if (role.equals("moderator")){
                                Intent intent = new Intent(getApplicationContext(), ModeratorList.class);
                                startActivity(intent);
                                finish();
                            }
                            else {
                                Intent intent = new Intent(getApplicationContext(), MyApplications.class);
                                startActivity(intent);
                                finish();
                            }
                        }
                    }
                }
            });

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_login);
        auth = FirebaseAuth.getInstance();

        mAuth = FirebaseAuth.getInstance();
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        buttonLogin = findViewById(R.id.btn_login);
        progressBar = findViewById(R.id.progressBar);
        textView = findViewById(R.id.registerNow);
        mDatabase = FirebaseDatabase.getInstance().getReference();

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Register.class);
                startActivity(intent);
            }
        });

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                String email, password;
                email = String.valueOf(editTextEmail.getText());
                password = String.valueOf(editTextPassword.getText());

                if (TextUtils.isEmpty(email)){
                    Toast.makeText(Login.this, "Введите адрес электронной почты", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(password)){
                    Toast.makeText(Login.this, "Введите пароль", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>(){
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task){
                                progressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    Toast.makeText(getApplicationContext(), "Авторизация прошла успешно", Toast.LENGTH_SHORT).show();
                                    user = auth.getCurrentUser();
                                    userId = user.getUid();

                                    mDatabase.child("Users").child(userId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                                            if (!task.isSuccessful()) {
                                                Log.e("firebase", "Error getting data", task.getException());
                                            } else {
                                                DataSnapshot snapshot = task.getResult();
                                                if (snapshot.exists()) {
                                                    role = snapshot.child("role").getValue(String.class);
                                                    if (role.equals("center")) {
                                                        Intent intent = new Intent(getApplicationContext(), MainActivity2.class);
                                                        startActivity(intent);
                                                        finish();
                                                    }
                                                    else if (role.equals("moderator")){
                                                        Intent intent = new Intent(getApplicationContext(), ModeratorList.class);
                                                        startActivity(intent);
                                                        finish();
                                                    }
                                                    else {
                                                        Intent intent = new Intent(getApplicationContext(), MyApplications.class);
                                                        startActivity(intent);
                                                        finish();
                                                    }
                                                }
                                            }
                                        }
                                    });


                                } else {
                                    Toast.makeText(Login.this, "Ошибка авторизации",
                                            Toast.LENGTH_SHORT).show();

                                }
                            }
                        });

            }
        });
    }

}