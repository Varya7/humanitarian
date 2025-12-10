package com.example.hum1.auth;

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

import androidx.fragment.app.Fragment;

import com.example.hum1.R;
import com.example.hum1.maps.MapActivityC;


/**
 * Фрагмент для регистрации центра в приложении.
 * Предоставляет пользовательский интерфейс для ввода данных центра
 * и обработки процесса регистрации.
 */
public class RegisterCFragment extends Fragment {

    private EditText editTextNameCenter, editTextAddress, editTextEmail, editTextPassword;
    private EditText editTextFIO, editTextWorkTime, editTextPhoneNumber, editTextDoc;
    private Button buttonReg;
    private TextView textViewLogin, textViewRegisterUser;
    private ProgressBar progressBar;


    /**
     * Создает и возвращает иерархию представлений для фрагмента.
     *
     * @param inflater LayoutInflater для создания представления из XML
     * @param container Родительское представление, к которому будет присоединено представление фрагмента
     * @param savedInstanceState Сохраненное состояние фрагмента (если есть)
     * @return Корневое представление фрагмента
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register_c, container, false);

        initViews(view);
        setupClickListeners();

        return view;
    }

    /**
     * Инициализирует все представления фрагмента.
     *
     * @param view Корневое представление фрагмента
     */
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

    /**
     * Настраивает обработчики кликов для элементов интерфейса.
     */
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

        buttonReg.setOnClickListener(v -> proceedToMapActivity());
    }

    /**
     * Обрабатывает процесс перехода к активности с картой после валидации введенных данных.
     * Показывает ProgressBar во время обработки.
     */
    private void proceedToMapActivity() {
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
            progressBar.setVisibility(View.GONE);
            navigateToMapActivity(centerName, address, email, password, fio, workTime, phoneNumber, doc);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    /**
     * Проверяет корректность введенных пользователем данных.
     *
     * @param centerName Название центра
     * @param address Адрес центра
     * @param email Email пользователя
     * @param password Пароль пользователя
     * @param fio ФИО ответственного лица
     * @param workTime Время работы центра
     * @param phoneNumber Номер телефона
     * @param doc Список необходимых документов
     * @return true если все данные валидны, false если есть ошибки
     */
    boolean validateInputs(String centerName, String address, String email,
                           String password, String fio, String workTime,
                           String phoneNumber, String doc) {
        if (TextUtils.isEmpty(centerName)) {
            showToast(getString(R.string.error_enter_center_name));
            return false;
        }
        if (TextUtils.isEmpty(address)) {
            showToast(getString(R.string.error_enter_address));
            return false;
        }
        if (TextUtils.isEmpty(email)) {
            showToast(getString(R.string.error_enter_email));
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            showToast(getString(R.string.error_enter_password));
            return false;
        }
        if (TextUtils.isEmpty(fio)) {
            showToast(getString(R.string.error_enter_fio));
            return false;
        }
        if (TextUtils.isEmpty(workTime)) {
            showToast(getString(R.string.error_enter_work_time));
            return false;
        }
        if (TextUtils.isEmpty(phoneNumber)) {
            showToast(getString(R.string.error_enter_phone_number));
            return false;
        }
        if (TextUtils.isEmpty(doc)) {
            showToast(getString(R.string.error_enter_doc_list));
            return false;
        }
        return true;
    }


    /**
     * Переходит к активности MapActivityC с передачей введенных данных.
     *
     * @param centerName Название центра
     * @param address Адрес центра
     * @param email Email пользователя
     * @param password Пароль пользователя
     * @param fio ФИО ответственного лица
     * @param workTime Время работы центра
     * @param phoneNumber Номер телефона
     * @param doc Список необходимых документов
     */
    private void navigateToMapActivity(String centerName, String address, String email,
                                       String password, String fio, String workTime,
                                       String phoneNumber, String doc) {
        Intent intent = new Intent(getActivity(), MapActivityC.class);
        intent.putExtra("center_name", centerName);
        intent.putExtra("address", address);
        intent.putExtra("email", email);
        intent.putExtra("password", password);
        intent.putExtra("fio", fio);
        intent.putExtra("work_time", workTime);
        intent.putExtra("phone_number", phoneNumber);
        intent.putExtra("doc", doc);
        startActivity(intent);
        requireActivity().finish();
    }

    /**
     * Показывает Toast-сообщение пользователю.
     *
     * @param message Текст сообщения для отображения
     */

    void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
}