package com.example.hum1;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterCFragment extends Fragment {

    private EditText editTextNameCenter, editTextAddress, editTextEmail, editTextPassword;
    private EditText editTextFIO, editTextWorkTime, editTextPhoneNumber, editTextDoc;
    private Button buttonReg;
    private TextView textViewLogin, textViewRegisterUser;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register_c, container, false);

        mAuth = FirebaseAuth.getInstance();

        // Инициализация всех View элементов
        initViews(view);

        // Установка обработчиков событий
        setupClickListeners();

        return view;
    }

    private void initViews(View view) {
        editTextNameCenter = view.findViewById(R.id.name_center);
        editTextAddress = view.findViewById(R.id.address);
        editTextEmail = view.findViewById(R.id.email);
        editTextPassword = view.findViewById(R.id.password);
        editTextFIO = view.findViewById(R.id.fio);
        editTextWorkTime = view.findViewById(R.id.work_time);
        editTextPhoneNumber = view.findViewById(R.id.phone_number);
        editTextDoc = view.findViewById(R.id.doc);
        buttonReg = view.findViewById(R.id.btn_register);
        progressBar = view.findViewById(R.id.progressBar);
        textViewLogin = view.findViewById(R.id.loginNow);
        textViewRegisterUser = view.findViewById(R.id.reg_u);
    }

    private void setupClickListeners() {
        textViewLogin.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new LoginFragment())
                        .addToBackStack(null)
                        .commit());

        textViewRegisterUser.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new RegisterFragment())
                        .addToBackStack(null)
                        .commit());

        buttonReg.setOnClickListener(v -> registerCenter());
    }

    private void registerCenter() {
        progressBar.setVisibility(View.VISIBLE);

        String centerName = editTextNameCenter.getText().toString().trim();
        String address = editTextAddress.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String fio = editTextFIO.getText().toString().trim();
        String workTime = editTextWorkTime.getText().toString().trim();
        String phoneNumber = editTextPhoneNumber.getText().toString().trim();
        String doc = editTextDoc.getText().toString().trim();

        if (validateInputs(centerName, address, email, password, fio, workTime, phoneNumber, doc)) {
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE);
                        handleRegistrationResult(task, centerName, address, email, fio, workTime, phoneNumber, doc);
                    });
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    private boolean validateInputs(String centerName, String address, String email,
                                   String password, String fio, String workTime,
                                   String phoneNumber, String doc) {
        if (TextUtils.isEmpty(centerName)) {
            showToast("Введите название центра");
            return false;
        }
        if (TextUtils.isEmpty(address)) {
            showToast("Введите адрес");
            return false;
        }
        if (TextUtils.isEmpty(email)) {
            showToast("Введите email");
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            showToast("Введите пароль");
            return false;
        }
        if (TextUtils.isEmpty(fio)) {
            showToast("Введите ФИО");
            return false;
        }
        if (TextUtils.isEmpty(workTime)) {
            showToast("Введите часы работы");
            return false;
        }
        if (TextUtils.isEmpty(phoneNumber)) {
            showToast("Введите номер телефона");
            return false;
        }
        if (TextUtils.isEmpty(doc)) {
            showToast("Введите список необходимых документов");
            return false;
        }
        return true;
    }

    private void handleRegistrationResult(@NonNull Task<AuthResult> task, String centerName,
                                          String address, String email, String fio,
                                          String workTime, String phoneNumber, String doc) {
        if (task.isSuccessful()) {
            saveCenterInfoToDatabase(centerName, address, email, fio, workTime, phoneNumber, doc);
            navigateToMapActivity();
            showToast("Центр успешно зарегистрирован");
        } else {
            showToast("Ошибка регистрации: " + task.getException().getMessage());
        }
    }

    private void saveCenterInfoToDatabase(String centerName, String address, String email,
                                          String fio, String workTime, String phoneNumber,
                                          String doc) {
        HashMap<String, String> centerInfo = new HashMap<>();
        centerInfo.put("center_name", centerName);
        centerInfo.put("address", address);
        centerInfo.put("email", email);
        centerInfo.put("role", "center");
        centerInfo.put("fio", fio);
        centerInfo.put("work_time", workTime);
        centerInfo.put("phone_number", phoneNumber);
        centerInfo.put("status", "Рассматривается");
        centerInfo.put("doc", doc);
        centerInfo.put("list_c", "");
        centerInfo.put("id", "");
        centerInfo.put("comment", "");
        centerInfo.put("latitude", "");
        centerInfo.put("longitude", "");

        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(mAuth.getCurrentUser().getUid())
                .setValue(centerInfo);
    }

    private void navigateToMapActivity() {
        startActivity(new Intent(getActivity(), MapActivityC.class));
        requireActivity().finish();
    }

    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
}