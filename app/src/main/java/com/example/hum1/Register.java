package com.example.hum1;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
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
import com.google.firebase.database.FirebaseDatabase;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Calendar;
import java.util.HashMap;

public class Register extends AppCompatActivity {

    EditText editTextEmail, editTextPassword, editTextFIO, editTextBirth, editTextPhone_number;
    Button buttonReg;
    TextView textView, textView2;

    FirebaseAuth mAuth;
    ProgressBar progressBar;



    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        editTextFIO = findViewById(R.id.fio);
        editTextBirth = findViewById(R.id.birth);
        editTextPhone_number = findViewById(R.id.phone_number);
        buttonReg = findViewById(R.id.btn_register);
        progressBar = findViewById(R.id.progressBar);
        textView = findViewById(R.id.loginNow);
        textView2 = findViewById(R.id.reg_c);

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                //finish();
            }
        });

        textView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), RegisterC.class);
                startActivity(intent);
                //finish();
            }
        });
        editTextBirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBirthPickerDialog();
            }
        });

        buttonReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                String email, password, fio, birth, phone_number;
                email = String.valueOf(editTextEmail.getText());
                password = String.valueOf(editTextPassword.getText());
                fio = String.valueOf(editTextFIO.getText());
                birth = String.valueOf(editTextBirth.getText());
                phone_number = String.valueOf(editTextPhone_number.getText());

                if (TextUtils.isEmpty(email)){
                    Toast.makeText(Register.this, "Введите почту", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(password)){
                    Toast.makeText(Register.this, "Введите пароль", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(fio)){
                    Toast.makeText(Register.this, "Введите ФИО", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(birth)){
                    Toast.makeText(Register.this, "Введите дату рождения", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(phone_number)){
                    Toast.makeText(Register.this, "Введите номер телефона", Toast.LENGTH_SHORT).show();
                    return;
                }
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>(){
                    @Override
                            public void onComplete(@NonNull Task<AuthResult> task){
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()){
                            HashMap<String, String> userInfo = new HashMap<>();
                            userInfo.put("email", email);
                            userInfo.put("role", "user");
                            userInfo.put("fio", fio);
                            userInfo.put("birth", birth);
                            userInfo.put("phone_number", phone_number);
                            ///запись в Firebase
                            FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(userInfo);

                            Intent intent = new Intent(getApplicationContext(), MyApplications.class);
                            startActivity(intent);
                            finish();
                            //Log.d(TAG, "createUserWithEmail:success");
                            //FirebaseUser user = mAuth.getCurrentUser();
                            //updateUI(user);
                            Toast.makeText(Register.this, "Аккаунт создан",
                                    Toast.LENGTH_SHORT).show();
                        } else{
                            Toast.makeText(Register.this, "Ошибка авторизации.",
                                    Toast.LENGTH_SHORT).show();

                        }
                    }
                });
            }
        });
    }

    private void showBirthPickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);


        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {

            String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
            editTextBirth.setText(selectedDate);
        }, year, month, day);

        datePickerDialog.show();
    }
}