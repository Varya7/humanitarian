package com.example.hum1;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
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

import com.example.hum1.adapters.CenterAppAdapter;
import com.example.hum1.classes.CenterApp;
import com.example.hum1.views.ViewCenterApp;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_moderator_list, container, false);

        spinner = view.findViewById(R.id.spinner);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        assert user != null;
        userId = user.getUid();

        RecyclerView recyclerView = view.findViewById(R.id.list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(recyclerView.getContext(),
                        layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        CenterAppAdapter.OnCenterAppClickListener appClickListener =
                new CenterAppAdapter.OnCenterAppClickListener() {
                    @Override
                    public void onCenterAppClick(CenterApp centerApp, int position) {
                        Intent intent = new Intent(getActivity(), ViewCenterApp.class);
                        intent.putExtra("id", centerApp.getId_appl());
                        startActivity(intent);
                    }
                };

        adapter = new CenterAppAdapter(getContext(), centers, appClickListener);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(layoutManager);

        // Локализованные подписи статусов в спиннере
        a1 = new ArrayList<>();
        a1.add(getString(R.string.status_pending));
        a1.add(getString(R.string.status_approved));
        a1.add(getString(R.string.status_rejected));

        adapter1 = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, a1);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter1);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent,
                                       View itemView,
                                       int position,
                                       long id) {
                String displayStatus = (String) parent.getItemAtPosition(position);
                String dbStatus = mapDisplayStatusToDb(displayStatus);

                centers.clear();
                mDatabase.child("Users").get().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        DataSnapshot snapshot = task.getResult();
                        for (DataSnapshot applicationSnapshot : snapshot.getChildren()) {
                            role = applicationSnapshot.child("role").getValue(String.class);
                            status = applicationSnapshot.child("status").getValue(String.class);

                            if (role != null
                                    && status != null
                                    && "center".equals(role)   // чтобы не цеплять обычных юзеров
                                    && status.equals(dbStatus)) {

                                center = applicationSnapshot.child("center_name")
                                        .getValue(String.class);
                                id_appl = applicationSnapshot.getKey();
                                centers.add(new CenterApp(center, id_appl));
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        return view;
    }

    /**
     * Локализованный статус (из спиннера) -> русский статус в БД.
     */
    private String mapDisplayStatusToDb(String displayStatus) {
        if (displayStatus.equals(getString(R.string.status_pending))) {
            return "Рассматривается";
        } else if (displayStatus.equals(getString(R.string.status_approved))) {
            return "Одобрено";
        } else if (displayStatus.equals(getString(R.string.status_rejected))) {
            return "Отклонено";
        }
        return displayStatus;
    }
}
