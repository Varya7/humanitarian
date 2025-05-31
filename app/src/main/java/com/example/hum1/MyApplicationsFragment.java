package com.example.hum1;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Фрагмент для отображения списка заявок пользователя с возможностью фильтрации по статусу.
 * Позволяет просматривать заявки текущего пользователя и фильтровать их по статусу через Spinner.
 */
public class MyApplicationsFragment extends Fragment {

    private ArrayList<ApplicationU> applications = new ArrayList<>();
    DatabaseReference mDatabase;
    private FirebaseUser user;
    FirebaseAuth auth;

    private Spinner spinner;
    private Button appl;

    List<String> a1;
    ArrayAdapter<String> adapter1;
    AppAdapterU adapter;

    private String id_appl, userId, status, center, date, time, email, fio, phone_number, birth, family_members, list;

    public MyApplicationsFragment() {}


    /**
     * Создает и инициализирует представление фрагмента.
     * Настраивает RecyclerView для отображения списка заявок,
     * Spinner для выбора статуса заявки,
     * а также кнопку для перехода к созданию новой заявки.
     *
     * @param inflater           объект для раздувания макета фрагмента
     * @param container          родительская ViewGroup, к которой будет присоединён фрагмент
     * @param savedInstanceState сохраненное состояние фрагмента (если есть)
     * @return созданное View для данного фрагмента
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_applications, container, false);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        assert user != null;
        userId = user.getUid();

        spinner = view.findViewById(R.id.spinner);
        appl = view.findViewById(R.id.appl);
        RecyclerView recyclerView = view.findViewById(R.id.list);

        appl.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), MainActivity.class);
            startActivity(intent);
        });

        adapter = new AppAdapterU(getContext(), applications, (app, position) -> {
            Intent intent = new Intent(getContext(), ViewApplic.class);
            intent.putExtra("id", app.getId_appl());
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), layoutManager.getOrientation()));

        a1 = new ArrayList<>();
        a1.add("Рассматривается");
        a1.add("Одобрено");
        a1.add("Отклонено");
        a1.add("Выдано");

        adapter1 = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, a1);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter1);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = (String) parent.getItemAtPosition(position);
                loadApplicationsByStatus(item);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        return view;
    }

    /**
     * Загружает заявки пользователя из базы данных Firebase по выбранному статусу.
     * Очищает текущий список и обновляет его согласно полученным данным,
     * затем обновляет адаптер RecyclerView.
     *
     * @param selectedStatus статус заявки для фильтрации ("Рассматривается", "Одобрено" и т.д.)
     */
    void loadApplicationsByStatus(String selectedStatus) {
        applications.clear();
        mDatabase.child("Applications").get().addOnCompleteListener((Task<DataSnapshot> task) -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    String userIdFromDb = snapshot.child("id").getValue(String.class);
                    String statusFromDb = snapshot.child("status").getValue(String.class);

                    if (userId.equals(userIdFromDb) && selectedStatus.equals(statusFromDb)) {
                        center = snapshot.child("center").getValue(String.class);
                        date = snapshot.child("date").getValue(String.class);
                        time = snapshot.child("time").getValue(String.class);
                        email = snapshot.child("email").getValue(String.class);
                        fio = snapshot.child("fio").getValue(String.class);
                        phone_number = snapshot.child("phone_number").getValue(String.class);
                        birth = snapshot.child("birth").getValue(String.class);
                        family_members = snapshot.child("family_members").getValue(String.class);
                        list = snapshot.child("list").getValue(String.class);
                        id_appl = snapshot.child("id_appl").getValue(String.class);

                        applications.add(new ApplicationU(id_appl, date, time, email, fio, phone_number, birth, family_members, list, selectedStatus, center));
                    }
                }
                adapter.notifyDataSetChanged();
            } else {
                Log.e("Firebase", "Failed to fetch applications", task.getException());
            }
        });
    }
}
