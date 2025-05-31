package com.example.hum1;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Фрагмент для отображения списка заявок  на регистрацию центра у модератора с возможностью фильтрации по статусу.
 * Позволяет выбирать статус заявки через Spinner и просматривать соответствующие заявки.
 */
public class ModeratorListFragment extends Fragment {

    ArrayList<CenterApp> centers = new ArrayList<>();
    List<String> a1;
    DatabaseReference mDatabase;
    FirebaseUser user;
    FirebaseAuth auth;
    Spinner spinner;
    CenterAppAdapter adapter;
    ArrayAdapter<String> adapter1;
    private String id_appl, userId, center, role, status;

    /**
     * Создает и инициализирует представление фрагмента.
     * Настраивает Spinner для выбора статуса заявки, RecyclerView для отображения списка заявок,
     * а также обрабатывает выбор заявки для перехода к подробному просмотру.
     *
     * @param inflater           объект для раздувания макета фрагмента
     * @param container          родительская ViewGroup, к которой будет присоединён фрагмент
     * @param savedInstanceState сохраненное состояние фрагмента (если есть)
     * @return созданное View для данного фрагмента
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_moderator_list, container, false);

        spinner = view.findViewById(R.id.spinner);


        mDatabase = FirebaseDatabase.getInstance().getReference();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        assert user != null;
        userId = user.getUid();

        RecyclerView recyclerView = view.findViewById(R.id.list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        CenterAppAdapter.OnCenterAppClickListener appClickListener = new CenterAppAdapter.OnCenterAppClickListener() {
            @Override
            public void onCenterAppClick(CenterApp centerApp, int position) {
                Intent intent = new Intent(getActivity(), ViewCenterApp.class);
                intent.putExtra("id", centerApp.getId_appl());
                startActivity(intent);
            }
        };

        adapter = new CenterAppAdapter(getContext(), centers, appClickListener);
        recyclerView.setAdapter(adapter);

        a1 = new ArrayList<>();
        a1.add("Рассматривается");
        a1.add("Одобрено");
        a1.add("Отклонено");
        adapter1 = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, a1);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter1);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = (String) parent.getItemAtPosition(position);
                centers.clear();
                mDatabase.child("Users").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if (task.isSuccessful()) {
                            DataSnapshot snapshot = task.getResult();
                            if (snapshot.exists()) {
                                for (DataSnapshot applicationSnapshot : snapshot.getChildren()) {
                                    role = applicationSnapshot.child("role").getValue(String.class);
                                    status = applicationSnapshot.child("status").getValue(String.class);
                                    if (role != null && status != null && item != null && status.equals(item)) {
                                        center = applicationSnapshot.child("center_name").getValue(String.class);
                                        id_appl = applicationSnapshot.getKey();
                                        centers.add(new CenterApp(center, id_appl));
                                    }
                                }
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        return view;
    }


}
