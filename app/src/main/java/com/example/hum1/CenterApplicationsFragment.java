package com.example.hum1;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Фрагмент, отображающий список заявок текущего центра.
 * Пользователь может фильтровать заявки по их статусу с помощью выпадающего списка (Spinner).
 * Также доступен переход к сканеру и детальный просмотр заявки.
 */
public class CenterApplicationsFragment extends Fragment {

    ArrayList<Application> applications = new ArrayList<>();
    List<String> a1;
    private DatabaseReference mDatabase;
    FirebaseUser user;
    FirebaseAuth auth;
    Spinner spinner;
    AppAdapter adapter;
    ImageButton scannerB;
    ArrayAdapter<String> adapter1;
    private String center_name;

    /**
     * Создает и инициализирует пользовательский интерфейс фрагмента.
     *
     * @param inflater           объект для создания представлений из XML
     * @param container          родительский контейнер для фрагмента
     * @param savedInstanceState сохраненное состояние
     * @return корневое представление фрагмента
     */
    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_center_applications, container, false);

        spinner = view.findViewById(R.id.spinner);
        scannerB = view.findViewById(R.id.scanner);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        spinner.setEnabled(true);

         //Проверка, авторизован ли пользователь
        if (user == null) {
            requireActivity().finish(); // Завершение активности, если пользователь не авторизован
            return view;
        }

        String userId = user.getUid();

        // Обработчик нажатия на кнопку сканера
        scannerB.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ScanActivity.class);
            startActivity(intent);
        });

        // Настройка RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        recyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        // Обработчик кликов по заявкам
        AppAdapter.OnAppClickListener appClickListener = (app, position) -> {
            Intent intent;
            if ("Выдано".equals(app.getStatus())) {
                intent = new Intent(requireContext(), ViewAppComplete.class);
            } else {
                intent = new Intent(requireContext(), ViewApplicC.class);
            }
            intent.putExtra("id", app.getId_appl());
            startActivity(intent);
        };

        // Инициализация адаптера и установка в RecyclerView
        adapter = new AppAdapter(requireContext(), applications, appClickListener);
        recyclerView.setAdapter(adapter);

        // Список доступных статусов заявок
        a1 = new ArrayList<>();
        a1.add("Рассматривается");
        a1.add("Одобрено");
        a1.add("Отклонено");
        a1.add("Выдано");

        // Настройка адаптера для Spinner
        adapter1 = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, a1);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter1);

        // Получение информации о центре пользователя из базы данных
        mDatabase.child("Users").child(userId).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e("firebase", "Ошибка при получении данных", task.getException());
            } else {
                DataSnapshot snapshot = task.getResult();
                if (snapshot.exists()) {
                    center_name = snapshot.child("center_name").getValue(String.class);
                }
            }
        });

        // Обработчик выбора статуса в Spinner
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view1, int position, long id) {
                String item = (String) parent.getItemAtPosition(position);
                applications.clear();

                // Загрузка и фильтрация заявок по статусу и центру
                mDatabase.child("Applications").get().addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.e("firebase", "Ошибка при получении данных", task.getException());
                    } else {
                        DataSnapshot snapshot = task.getResult();

                        if (snapshot.exists()) {
                            for (DataSnapshot applicationSnapshot : snapshot.getChildren()) {
                                String center = applicationSnapshot.child("center").getValue(String.class);
                                String status = applicationSnapshot.child("status").getValue(String.class);

                                if (center != null && center_name != null && status != null && item != null &&
                                        center.equals(center_name) && status.equals(item)) {
                                    String date = applicationSnapshot.child("date").getValue(String.class);
                                    String time = applicationSnapshot.child("time").getValue(String.class);
                                    String email = applicationSnapshot.child("email").getValue(String.class);
                                    String fio = applicationSnapshot.child("fio").getValue(String.class);
                                    String phone_number = applicationSnapshot.child("phone_number").getValue(String.class);
                                    String birth = applicationSnapshot.child("birth").getValue(String.class);
                                    String family_members = applicationSnapshot.child("family_members").getValue(String.class);
                                    String list = applicationSnapshot.child("list").getValue(String.class);
                                    String id_appl = applicationSnapshot.child("id_appl").getValue(String.class);

                                    applications.add(new Application(id_appl, date, time, email, fio, phone_number, birth, family_members, list, status));
                                }
                            }
                            adapter.notifyDataSetChanged(); // Обновление списка после загрузки данных
                        }
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return view;
    }

    /**
     * Метод вызывается при возвращении фрагмента на экран.
     */
    @Override
    public void onResume() {
        super.onResume();
    }
}
