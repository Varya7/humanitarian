package com.example.hum1.auth;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.app.DatePickerDialog;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.hum1.R;
import com.example.hum1.UserActivity;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashMap;

/**
 * Фрагмент для регистрации нового пользователя.
 * Содержит поля для ввода email, пароля, ФИО, даты рождения и телефона.
 * Позволяет зарегистрировать пользователя через Firebase Authentication
 * и сохранить информацию о нем в базе данных Firebase Realtime Database.
 */
public class RegisterFragment extends Fragment {

    private EditText editTextEmail, editTextPassword, editTextFIO, editTextBirth, editTextPhoneNumber;
    private Button buttonReg;
    private TextView textViewLogin, textViewC;
    private ProgressBar progressBar;
    FirebaseAuth mAuth;


    /**
     * Создает и инициализирует представление фрагмента, настраивает обработчики событий.
     *
     * @param inflater           объект LayoutInflater для раздувания макета
     * @param container          контейнер для размещения фрагмента
     * @param savedInstanceState сохраненное состояние фрагмента
     * @return корневое View для данного фрагмента
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        mAuth = FirebaseAuth.getInstance();

        editTextEmail = view.findViewById(R.id.email);
        editTextPassword = view.findViewById(R.id.password);
        editTextFIO = view.findViewById(R.id.fio);
        editTextBirth = view.findViewById(R.id.birth);
        editTextPhoneNumber = view.findViewById(R.id.phone_number);
        buttonReg = view.findViewById(R.id.btn_register);
        progressBar = view.findViewById(R.id.progressBar);
        textViewLogin = view.findViewById(R.id.loginNow);
        textViewC = view.findViewById(R.id.reg_c);

        setupListeners();

        return view;
    }

    /**
     * Устанавливает обработчики кликов для элементов интерфейса:
     * кнопок регистрации, выбора даты рождения и перехода к логину и дополнительной регистрации.
     */
    private void setupListeners() {
        textViewLogin.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new LoginFragment())
                        .addToBackStack(null)
                        .commit()
        );

        textViewC.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new RegisterCFragment())
                        .addToBackStack(null)
                        .commit() );

        editTextBirth.setOnClickListener(v -> showBirthPickerDialog());

        buttonReg.setOnClickListener(v -> registerUser());
    }

    /**
     * Выполняет регистрацию пользователя.
     * Проверяет корректность введенных данных, запускает процесс создания пользователя в Firebase.
     */
    private void registerUser() {
        progressBar.setVisibility(View.VISIBLE);

        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String fio = editTextFIO.getText().toString().trim();
        String birth = editTextBirth.getText().toString().trim();
        String phoneNumber = editTextPhoneNumber.getText().toString().trim();

        if (validateInputs(email, password, fio, birth, phoneNumber)) {
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE);
                        handleRegistrationResult(task, email, fio, birth, phoneNumber);
                    });
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }


    /**
     * Проверяет корректность введенных пользователем данных.
     *
     * @param email       email пользователя
     * @param password    пароль пользователя
     * @param fio         ФИО пользователя
     * @param birth       дата рождения пользователя
     * @param phoneNumber номер телефона пользователя
     * @return true, если все данные заполнены корректно, иначе false
     */
    boolean validateInputs(String email, String password, String fio, String birth, String phoneNumber) {
        if (TextUtils.isEmpty(email)) {
            showToast("Введите почту");
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
        if (TextUtils.isEmpty(birth)) {
            showToast("Введите дату рождения");
            return false;
        }
        if (TextUtils.isEmpty(phoneNumber)) {
            showToast("Введите номер телефона");
            return false;
        }
        return true;
    }

    /**
     * Обрабатывает результат регистрации в Firebase.
     * Если регистрация успешна — сохраняет данные пользователя и переходит к следующему экрану,
     * иначе выводит сообщение об ошибке.
     *
     * @param task     результат задачи регистрации Firebase
     * @param email    email пользователя
     * @param fio      ФИО пользователя
     * @param birth    дата рождения пользователя
     * @param phoneNumber номер телефона пользователя
     */
    private void handleRegistrationResult(@NonNull Task<AuthResult> task, String email, String fio, String birth, String phoneNumber) {
        if (task.isSuccessful()) {
            saveUserInfoToDatabase(email, fio, birth, phoneNumber);
            navigateToMyApplications();
            showToast("Аккаунт создан");
        } else {
            showToast("Ошибка регистрации: " + task.getException().getMessage());
        }
    }

    /**
     * Сохраняет информацию о зарегистрированном пользователе в базе Firebase Realtime Database.
     *
     * @param email       email пользователя
     * @param fio         ФИО пользователя
     * @param birth       дата рождения пользователя
     * @param phoneNumber номер телефона пользователя
     */
    private void saveUserInfoToDatabase(String email, String fio, String birth, String phoneNumber) {
        HashMap<String, String> userInfo = new HashMap<>();
        userInfo.put("email", email);
        userInfo.put("role", "user");
        userInfo.put("fio", fio);
        userInfo.put("birth", birth);
        userInfo.put("phone_number", phoneNumber);

        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(mAuth.getCurrentUser().getUid())
                .setValue(userInfo);
    }

    /**
     * Переходит к экрану с пользовательскими заявками и завершает текущую активность.
     */
    private void navigateToMyApplications() {
        startActivity(new Intent(getActivity(), UserActivity.class));
        requireActivity().finish();

    }

    /**
     * Показывает диалог выбора даты для поля даты рождения.
     * После выбора даты обновляет текстовое поле с выбранной датой.
     */
    private void showBirthPickerDialog() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, day) -> {
                    String selectedDate = day + "/" + (month + 1) + "/" + year;
                    editTextBirth.setText(selectedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    /**
     * Отображает Toast-сообщение с переданным текстом.
     *
     * @param message текст сообщения для отображения
     */
    void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
}