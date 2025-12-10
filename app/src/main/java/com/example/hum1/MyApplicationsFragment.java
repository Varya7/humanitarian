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

import com.example.hum1.adapters.AppAdapterU;
import com.example.hum1.classes.ApplicationU;
import com.example.hum1.views.ViewApplic;
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
 * Статусы в Firebase хранятся по-русски, но в UI отображаются локализованные строки.
 */
public class MyApplicationsFragment extends Fragment {

    public ArrayList<ApplicationU> applications = new ArrayList<>();
    private DatabaseReference mDatabase;
    private FirebaseUser user;
    private FirebaseAuth auth;

    private Spinner spinner;
    private Button appl;

    private List<String> statusDisplayList;
    private ArrayAdapter<String> adapterSpinner;
    public AppAdapterU adapter;

    private String id_appl, userId, center, date, time, email, fio, phone_number, birth, family_members, list;

    public MyApplicationsFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
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

        adapter = new AppAdapterU(
                getContext(),
                applications,
                (app, position) -> {
                    Intent intent = new Intent(getContext(), ViewApplic.class);
                    intent.putExtra("id", app.getId_appl());
                    startActivity(intent);
                }
        );

        recyclerView.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(
                new DividerItemDecoration(getContext(), layoutManager.getOrientation())
        );

        // Локализованные подписи статусов для спиннера
        statusDisplayList = new ArrayList<>();
        statusDisplayList.add(getString(R.string.status_pending));
        statusDisplayList.add(getString(R.string.status_approved));
        statusDisplayList.add(getString(R.string.status_rejected));
        statusDisplayList.add(getString(R.string.status_issued));

        adapterSpinner = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                statusDisplayList
        );
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapterSpinner);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View itemView, int position, long id) {
                String displayStatus = (String) parent.getItemAtPosition(position);
                // Преобразуем локализованную подпись обратно в русский статус из БД
                String dbStatus = mapDisplayStatusToDb(displayStatus);
                loadApplicationsByStatus(dbStatus);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        return view;
    }

    /**
     * Загружает заявки пользователя из Firebase по русскому статусу (как хранится в БД).
     * В объект ApplicationU писаем уже локализованный статус для отображения.
     */
    void loadApplicationsByStatus(String selectedDbStatus) {
        applications.clear();
        mDatabase.child("Applications").get().addOnCompleteListener((Task<DataSnapshot> task) -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    String userIdFromDb = snapshot.child("id").getValue(String.class);
                    String statusFromDb = snapshot.child("status").getValue(String.class);

                    if (userId.equals(userIdFromDb) && selectedDbStatus.equals(statusFromDb)) {
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

                        // Локализованный статус для отображения
                        String displayStatus = mapDbStatusToDisplay(statusFromDb);

                        applications.add(new ApplicationU(
                                id_appl,
                                date,
                                time,
                                email,
                                fio,
                                phone_number,
                                birth,
                                family_members,
                                list,
                                displayStatus,
                                center
                        ));
                    }
                }
                adapter.notifyDataSetChanged();
            } else {
                Log.e("Firebase", "Failed to fetch applications", task.getException());
            }
        });
    }

    /**
     * Маппинг: отображаемый (локализованный) статус -> русский статус в БД.
     */
    private String mapDisplayStatusToDb(String displayStatus) {
        if (displayStatus.equals(getString(R.string.status_pending))) {
            return "Рассматривается";
        } else if (displayStatus.equals(getString(R.string.status_approved))) {
            return "Одобрено";
        } else if (displayStatus.equals(getString(R.string.status_rejected))) {
            return "Отклонено";
        } else if (displayStatus.equals(getString(R.string.status_issued))) {
            return "Выдано";
        }
        // fallback: если что-то пошло не так — не фильтруем
        return displayStatus;
    }

    /**
     * Маппинг: русский статус из БД -> локализованный текст для UI.
     */
    private String mapDbStatusToDisplay(String dbStatus) {
        if (dbStatus == null) return "";

        switch (dbStatus) {
            case "Рассматривается":
                return getString(R.string.status_pending);
            case "Одобрено":
                return getString(R.string.status_approved);
            case "Отклонено":
                return getString(R.string.status_rejected);
            case "Выдано":
                return getString(R.string.status_issued);
            default:
                return dbStatus;
        }
    }
}
