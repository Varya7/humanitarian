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

import androidx.appcompat.app.AlertDialog;
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
    private EditText editTextFIO, editTextWorkStart, editTextWorkEnd, editTextInterval, editTextWorkingDays, editTextPhoneNumber, editTextDoc;
    private Button buttonReg;
    private TextView textViewLogin, textViewRegisterUser;
    private ProgressBar progressBar;
    private final boolean[] selectedDays = new boolean[]{false, true, true, true, true, true, false};
    private final String[] dayKeys = new String[]{"sun", "mon", "tue", "wed", "thu", "fri", "sat"};


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
        editTextWorkStart = view.findViewById(R.id.work_start);
        editTextWorkEnd = view.findViewById(R.id.work_end);
        editTextInterval = view.findViewById(R.id.appointment_interval);
        editTextWorkingDays = view.findViewById(R.id.working_days);
        editTextWorkingDays.setFocusable(false);
        editTextWorkingDays.setText(formatSelectedDays());
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
        editTextWorkingDays.setOnClickListener(v -> showWorkingDaysDialog());
    }

    private void showWorkingDaysDialog() {
        String[] labels = new String[]{
                getString(R.string.day_sun),
                getString(R.string.day_mon),
                getString(R.string.day_tue),
                getString(R.string.day_wed),
                getString(R.string.day_thu),
                getString(R.string.day_fri),
                getString(R.string.day_sat)
        };
        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.schedule_days_select))
                .setMultiChoiceItems(labels, selectedDays, (dialog, which, isChecked) -> selectedDays[which] = isChecked)
                .setPositiveButton(getString(R.string.btn_ok), (dialog, which) -> editTextWorkingDays.setText(formatSelectedDays()))
                .setNegativeButton(getString(R.string.btn_cancel), null)
                .show();
    }

    private String formatSelectedDays() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < selectedDays.length; i++) {
            if (!selectedDays[i]) continue;
            if (builder.length() > 0) builder.append(",");
            builder.append(dayKeys[i]);
        }
        return builder.toString();
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
        String workStart = editTextWorkStart.getText().toString().trim();
        String workEnd = editTextWorkEnd.getText().toString().trim();
        String interval = editTextInterval.getText().toString().trim();
        String workingDays = editTextWorkingDays.getText().toString().trim();
        String workTime = workStart + "-" + workEnd;
        String phoneNumber = editTextPhoneNumber.getText().toString().trim();
        String doc = editTextDoc.getText().toString().trim();

        if (validateInputs(centerName, address, email, password, fio, workStart, workEnd, interval, workingDays, phoneNumber, doc)) {
            progressBar.setVisibility(View.GONE);
            navigateToMapActivity(centerName, address, email, password, fio, workTime, workStart, workEnd, interval, workingDays, phoneNumber, doc);
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
                           String password, String fio, String workStart, String workEnd,
                           String interval, String workingDays, String phoneNumber, String doc) {
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
        if (TextUtils.isEmpty(workStart) || TextUtils.isEmpty(workEnd)) {
            showToast(getString(R.string.error_enter_work_time));
            return false;
        }
        if (TextUtils.isEmpty(interval) || TextUtils.isEmpty(workingDays)) {
            showToast(getString(R.string.error_fill_all_fields_short));
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
                                       String workStart, String workEnd, String interval, String workingDays,
                                       String phoneNumber, String doc) {
        Intent intent = new Intent(getActivity(), MapActivityC.class);
        intent.putExtra("center_name", centerName);
        intent.putExtra("address", address);
        intent.putExtra("email", email);
        intent.putExtra("password", password);
        intent.putExtra("fio", fio);
        intent.putExtra("work_time", workTime);
        intent.putExtra("work_start", workStart);
        intent.putExtra("work_end", workEnd);
        intent.putExtra("appointment_interval_minutes", interval);
        intent.putExtra("working_days", workingDays);
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
