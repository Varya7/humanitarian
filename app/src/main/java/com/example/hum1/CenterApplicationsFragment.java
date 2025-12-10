package com.example.hum1;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hum1.adapters.AppAdapter;
import com.example.hum1.classes.Application;
import com.example.hum1.views.ViewAppComplete;
import com.example.hum1.views.ViewApplicC;
import com.example.hum1.views.ViewApplicQR;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Фрагмент, отображающий список заявок текущего центра.
 * Фильтрация по статусу (с локализованными подписями), поиск по ФИО и сортировка по дате.
 */
public class CenterApplicationsFragment extends Fragment {

    private final ArrayList<Application> allCenterApps = new ArrayList<>();
    private final ArrayList<Application> statusFiltered = new ArrayList<>();
    private final ArrayList<Application> applications = new ArrayList<>();

    private DatabaseReference mDatabase;
    private FirebaseUser user;
    private FirebaseAuth auth;

    private Spinner spinnerStatus;
    private Spinner spinnerSort;
    private EditText etSearchFio;
    private AppAdapter adapter;
    private ImageButton scannerB;

    private String center_name;
    private int sortMode = 1; // 0 – сначала старые, 1 – сначала новые

    private final SimpleDateFormat dateOnlyFormat =
            new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_center_applications, container, false);

        spinnerStatus = view.findViewById(R.id.spinner);
        spinnerSort = view.findViewById(R.id.spinnerSort);
        etSearchFio = view.findViewById(R.id.etSearchFio);
        scannerB = view.findViewById(R.id.scanner);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        if (user == null) {
            requireActivity().finish();
            return view;
        }

        String userId = user.getUid();

        scannerB.setOnClickListener(v -> showQrChoiceDialog());

        RecyclerView recyclerView = view.findViewById(R.id.list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        recyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(recyclerView.getContext(), layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        AppAdapter.OnAppClickListener appClickListener = (app, position) -> {
            Intent intent;
            if ("Выдано".equals(app.getStatus())) { // статус в БД по-русски
                intent = new Intent(requireContext(), ViewAppComplete.class);
            } else {
                intent = new Intent(requireContext(), ViewApplicC.class);
            }
            intent.putExtra("id", app.getId_appl());
            startActivity(intent);
        };

        adapter = new AppAdapter(requireContext(), applications, appClickListener);
        recyclerView.setAdapter(adapter);

        // Локализованные статусы для спиннера
        List<String> statuses = new ArrayList<>();
        statuses.add(getString(R.string.status_pending));
        statuses.add(getString(R.string.status_approved));
        statuses.add(getString(R.string.status_rejected));
        statuses.add(getString(R.string.status_issued));

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                statuses
        );
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);

        ArrayAdapter<CharSequence> sortAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.sort_options,
                android.R.layout.simple_spinner_item
        );
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSort.setAdapter(sortAdapter);
        spinnerSort.setSelection(1);

        mDatabase.child("Users").child(userId).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e("firebase", "Ошибка при получении данных", task.getException());
            } else {
                if (!isAdded()) return;
                DataSnapshot snapshot = task.getResult();
                if (snapshot.exists()) {
                    center_name = snapshot.child("center_name").getValue(String.class);
                    loadCenterApplications();
                }
            }
        });

        spinnerStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view1, int position, long id) {
                applyFiltersAndSort();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view1, int position, long id) {
                sortMode = position;
                applyFiltersAndSort();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        etSearchFio.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override public void afterTextChanged(Editable s) {
                applyFiltersAndSort();
            }
        });

        return view;
    }

    /**
     * Загружаем все заявки центра один раз.
     */
    private void loadCenterApplications() {
        if (center_name == null) return;

        mDatabase.child("Applications").get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e("firebase", "Ошибка при получении заявок", task.getException());
            } else {
                if (!isAdded()) return;

                DataSnapshot snapshot = task.getResult();
                allCenterApps.clear();

                if (snapshot.exists()) {
                    for (DataSnapshot applicationSnapshot : snapshot.getChildren()) {
                        String center = applicationSnapshot.child("center").getValue(String.class);
                        if (center == null || !center.equals(center_name)) continue;

                        String status = applicationSnapshot.child("status").getValue(String.class);
                        String date = applicationSnapshot.child("date").getValue(String.class);
                        String time = applicationSnapshot.child("time").getValue(String.class);
                        String email = applicationSnapshot.child("email").getValue(String.class);
                        String fio = applicationSnapshot.child("fio").getValue(String.class);
                        String phone_number = applicationSnapshot.child("phone_number").getValue(String.class);
                        String birth = applicationSnapshot.child("birth").getValue(String.class);
                        String family_members = applicationSnapshot.child("family_members").getValue(String.class);
                        String list = applicationSnapshot.child("list").getValue(String.class);
                        String id_appl = applicationSnapshot.child("id_appl").getValue(String.class);

                        allCenterApps.add(new Application(
                                id_appl, date, time, email, fio, phone_number,
                                birth, family_members, list, status
                        ));
                    }
                }
                applyFiltersAndSort();
            }
        });
    }

    /**
     * Применяет:
     * 1) фильтр по статусу;
     * 2) поиск по ФИО;
     * 3) сортировку по дате и времени.
     */
    private void applyFiltersAndSort() {
        if (!isAdded() || getContext() == null) return;
        if (spinnerStatus.getSelectedItem() == null) return;

        String displayStatus = (String) spinnerStatus.getSelectedItem();
        String selectedDbStatus = mapDisplayStatusToDb(displayStatus);

        String query = etSearchFio.getText().toString().trim();


        statusFiltered.clear();
        for (Application app : allCenterApps) {
            String status = app.getStatus();
            if (status != null && status.equals(selectedDbStatus)) {
                statusFiltered.add(app);
            }
        }

        applications.clear();
        if (query.isEmpty()) {
            applications.addAll(statusFiltered);
        } else {
            String lowerQuery = query.toLowerCase(Locale.getDefault());
            for (Application app : statusFiltered) {
                String fio = app.getFIO();
                if (fio != null && fio.toLowerCase(Locale.getDefault()).contains(lowerQuery)) {
                    applications.add(app);
                }
            }
        }

        Collections.sort(applications, (a, b) -> {
            Date da = parseDateTime(a.getDate(), a.getTime());
            Date db = parseDateTime(b.getDate(), b.getTime());

            if (da == null && db == null) return 0;
            if (da == null) return 1;
            if (db == null) return -1;

            int cmp = da.compareTo(db);
            // sortMode == 0 — "сначала старые", == 1 — "сначала новые"
            return (sortMode == 0) ? cmp : -cmp;
        });

        adapter.notifyDataSetChanged();
    }

    private Date parseDateTime(String dateStr, String timeStr) {
        if (dateStr == null) return null;
        try {
            String time = (timeStr != null && !timeStr.isEmpty()) ? timeStr : "00:00";
            return new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    .parse(dateStr + " " + time);
        } catch (ParseException e) {
            return null;
        }
    }

    private void showQrChoiceDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_qr_choice, null, false);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        Button btnScan = dialogView.findViewById(R.id.btnScan);
        Button btnManual = dialogView.findViewById(R.id.btnManual);

        btnScan.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(getActivity(), ScanActivity.class);
            startActivity(intent);
        });

        btnManual.setOnClickListener(v -> {
            dialog.dismiss();
            showManualCodeDialog();
        });

        dialog.show();
    }

    private void showManualCodeDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_manual_code, null, false);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        EditText etCode = dialogView.findViewById(R.id.etCode);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnOk = dialogView.findViewById(R.id.btnOk);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnOk.setOnClickListener(v -> {
            String code = etCode.getText().toString().trim();
            if (!code.isEmpty()) {
                dialog.dismiss();
                Intent intent = new Intent(getActivity(), ViewApplicQR.class);
                intent.putExtra("id", code);
                startActivity(intent);
            }
        });

        dialog.show();
    }

    /**
     * Локализованный статус (из спиннера) -> русский статус в БД.
     */
    private String mapDisplayStatusToDb(String displayStatus) {
        if (!isAdded() || getContext() == null) {
            return displayStatus;
        }

        if (displayStatus.equals(getString(R.string.status_pending))) {
            return "Рассматривается";
        } else if (displayStatus.equals(getString(R.string.status_approved))) {
            return "Одобрено";
        } else if (displayStatus.equals(getString(R.string.status_rejected))) {
            return "Отклонено";
        } else if (displayStatus.equals(getString(R.string.status_issued))) {
            return "Выдано";
        }
        return displayStatus;
    }
}
