package com.example.hum1;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.hum1.ui.CenterListActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.HashMap;

public class RegisterC extends AppCompatActivity {

    EditText editTextNameCenter, editTextAddress, editTextEmail, editTextPassword, editTextFIO, editTextWorkTime, editTextPhone_number, editDoc;
    Button buttonReg;
    TextView textView, textView2;

    FirebaseAuth mAuth;
    ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_register_c);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        mAuth = FirebaseAuth.getInstance();
        editTextNameCenter = findViewById(R.id.name_center);
        editTextAddress = findViewById(R.id.address);
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        editTextFIO = findViewById(R.id.fio);
        editTextWorkTime = findViewById(R.id.work_time);
        editTextPhone_number = findViewById(R.id.phone_number);
        editDoc = findViewById(R.id.doc);
        buttonReg = findViewById(R.id.btn_register);
        progressBar = findViewById(R.id.progressBar);
        textView = findViewById(R.id.loginNow);
        textView2 = findViewById(R.id.reg_u);

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
            }
        });

        textView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Register.class);
                startActivity(intent);
            }
        });

        buttonReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                String center_name, address, email, password, fio, work_time, phone_number, doc;
                center_name = String.valueOf(editTextNameCenter.getText());
                address = String.valueOf(editTextAddress.getText());
                email = String.valueOf(editTextEmail.getText());
                password = String.valueOf(editTextPassword.getText());
                fio = String.valueOf(editTextFIO.getText());
                work_time = String.valueOf(editTextWorkTime.getText());
                phone_number = String.valueOf(editTextPhone_number.getText());
                doc = String.valueOf(editDoc.getText());

                if (TextUtils.isEmpty(center_name)){
                    Toast.makeText(RegisterC.this, "Введите название центра", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(address)){
                    Toast.makeText(RegisterC.this, "Введите адрес", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(email)){
                    Toast.makeText(RegisterC.this, "Введите email", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(password)){
                    Toast.makeText(RegisterC.this, "Введите пароль", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(fio)){
                    Toast.makeText(RegisterC.this, "Введите ФИО", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(work_time)){
                    Toast.makeText(RegisterC.this, "Enter часы работы", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(phone_number)){
                    Toast.makeText(RegisterC.this, "Введите номер телефона", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(doc)){
                    Toast.makeText(RegisterC.this, "Введите список необходимых документов", Toast.LENGTH_SHORT).show();
                    return;
                }
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>(){
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task){
                                progressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()){
                                    HashMap<String, String> userInfo = new HashMap<>();
                                    userInfo.put("center_name", center_name);
                                    userInfo.put("address", address);
                                    userInfo.put("email", email);
                                    userInfo.put("role", "center");
                                    userInfo.put("fio", fio);
                                    userInfo.put("work_time", work_time);
                                    userInfo.put("phone_number", phone_number);
                                    userInfo.put("status", "Рассматривается");
                                    userInfo.put("doc", doc);
                                    userInfo.put("list_c", "");
                                    userInfo.put("id", "");
                                    userInfo.put("comment", "");
                                    userInfo.put("latitude", "");
                                    userInfo.put("longitude", "");

                                    FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                            .setValue(userInfo);

                                    Intent intent = new Intent(getApplicationContext(), MapActivityC.class);
                                    startActivity(intent);


                                } else{
                                    Toast.makeText(RegisterC.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

    }
}