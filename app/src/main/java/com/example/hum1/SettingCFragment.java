package com.example.hum1;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hum1.adapters.ListAdapter;
import com.example.hum1.auth.ChangePasswordActivity;
import com.example.hum1.editdata.EditDataCenterActivity;
import com.example.hum1.editdata.EditListActivity;
import com.example.hum1.editdata.EditListUActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Map;

import androidx.appcompat.widget.SwitchCompat;
import androidx.core.os.LocaleListCompat;
import androidx.appcompat.app.AppCompatDelegate;


/**
 * Фрагмент настроек для пользователя с ролью центра.
 * Отображает информацию о центре, список заявок и предоставляет
 * функционал редактирования данных, изменения пароля, управления списками,
 * а также выхода из аккаунта и удаления аккаунта.
 */
public class SettingCFragment extends Fragment {
    FirebaseAuth auth;
    private FirebaseUser user;
    private DatabaseReference userRef;
    DatabaseReference mDatabase;
    private ArrayList<Map<String, String>> listC;
    private RecyclerView recyclerView;
    private com.example.hum1.adapters.ListAdapter adapter;

    private String userId = "";

    private LinearLayout commentLayout;
    private TextView commV, statusV, emailV, fioV, work_timeV, phone_numberV, logoutV, deleteV, center_nameV, addressV, docV;
    private Button edit_dataB, edit_passwordB, edit_listB, edit_listU;
    private ImageButton statB;
    private Spinner spinnerLanguage;


    /**
     * Инициализация View-компонентов и адаптера списка при создании представления фрагмента.
     *
     * @param inflater           Инфлейтер для разметки
     * @param container          Родительский контейнер
     * @param savedInstanceState Сохранённое состояние
     * @return Возвращает корневой View фрагмента
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting_c, container, false);

        initViews(view);
        setupFirebase();
        setupButtons();

        loadUserData();
        loadListData();


        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.languages_display,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLanguage.setAdapter(adapter);

        String currentLang = LangPrefs.loadLang(requireContext()); // "ru" или "en"
        int position = currentLang.startsWith("en") ? 1 : 0;
        spinnerLanguage.setSelection(position, false); // false, чтобы не триггерить listener при установке

        spinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String newLang = (pos == 1) ? "en" : "ru";
                String current = LangPrefs.loadLang(requireContext());

                if (newLang.equals(current)) return;

                LocaleUtil.applyAppLocale(requireContext(), newLang);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });


        return view;
    }

    /**
     * Инициализация всех элементов пользовательского интерфейса.
     *
     * @param view Корневой View фрагмента
     */
    private void initViews(View view) {
        emailV = view.findViewById(R.id.email);
        fioV = view.findViewById(R.id.fio);
        work_timeV = view.findViewById(R.id.work_time);
        phone_numberV = view.findViewById(R.id.phone_number);
        edit_dataB = view.findViewById(R.id.btn_edit_data);
        edit_passwordB = view.findViewById(R.id.btn_edit_password);
        edit_listB = view.findViewById(R.id.btn_edit_list);
        edit_listU = view.findViewById(R.id.btn_edit_list_user);
        center_nameV = view.findViewById(R.id.center_name);
        addressV = view.findViewById(R.id.address);
        logoutV = view.findViewById(R.id.logout);
        deleteV = view.findViewById(R.id.delete);
        docV = view.findViewById(R.id.doc);
        commV = view.findViewById(R.id.comm);
        statusV = view.findViewById(R.id.status);
        commentLayout = view.findViewById(R.id.commentLayout);
        recyclerView = view.findViewById(R.id.recyclerView_list);
        statB = view.findViewById(R.id.statButton);
        spinnerLanguage = view.findViewById(R.id.spinner_language);

        listC = new ArrayList<>();
        adapter = new ListAdapter(listC);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);




    }

    /**
     * Настройка подключения к Firebase и получение текущего пользователя.
     */
    private void setupFirebase() {
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        if (user == null) return;

        userId = user.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        userRef = mDatabase.child("Users").child(userId);
    }

    /**
     * Загрузка и отображение данных пользователя из Firebase Realtime Database.
     * Включает информацию о центре, статус заявки на регистрацию и комментарий.
     */
    void loadUserData() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    center_nameV.setText(snapshot.child("center_name").getValue(String.class));
                    addressV.setText(snapshot.child("address").getValue(String.class));
                    fioV.setText(snapshot.child("fio").getValue(String.class));
                    work_timeV.setText(snapshot.child("work_time").getValue(String.class));
                    emailV.setText(snapshot.child("email").getValue(String.class));
                    phone_numberV.setText(snapshot.child("phone_number").getValue(String.class));
                    docV.setText(snapshot.child("doc").getValue(String.class));

                    String status = snapshot.child("status").getValue(String.class);
                    if ("Рассматривается".equals(status)) {
                        statusV.setText(getString(R.string.status_center_pending));
                    } else if ("Одобрено".equals(status)) {
                        statusV.setText(getString(R.string.status_center_approved));
                    } else {
                        statusV.setText(getString(R.string.status_center_rejected));
                    }

                    String com = snapshot.child("comment").getValue(String.class);
                    if (com == null || com.isEmpty()) {
                        commentLayout.setVisibility(View.GONE);
                    } else {
                        commV.setText(com);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    /**
     * Загрузка списка заявок центра из базы данных и обновление адаптера RecyclerView.
     */
    private void loadListData() {
        userRef.child("list_c").addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listC.clear();
                for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                    Map<String, String> item = (Map<String, String>) itemSnapshot.getValue();
                    if (item != null && item.containsKey("name") && item.containsKey("quantity")) {
                        listC.add(item);
                    }
                }
                adapter.notifyDataSetChanged();
                updateRecyclerViewHeight();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    /**
     * Динамическое изменение высоты RecyclerView в зависимости от количества элементов списка.
     * Позволяет избежать проблем с прокруткой внутри ограниченного пространства.
     */
    private void updateRecyclerViewHeight() {
        if (adapter.getItemCount() > 0 && getContext() != null) {
            int heightInDp = adapter.getItemCount() * 56;
            int heightInPx = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, heightInDp, getResources().getDisplayMetrics());

            ViewGroup.LayoutParams params = recyclerView.getLayoutParams();
            params.height = heightInPx;
            recyclerView.setLayoutParams(params);
        }
    }

    /**
     * Настройка обработчиков нажатий для кнопок редактирования, выхода и удаления аккаунта.
     */
    private void setupButtons() {
        logoutV.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getActivity(), AuthActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        });

        edit_dataB.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), EditDataCenterActivity.class)));

        edit_passwordB.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), ChangePasswordActivity.class)));

        edit_listB.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), EditListActivity.class)));

        edit_listU.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), EditListUActivity.class)));

        deleteV.setOnClickListener(v -> showDeleteConfirmationDialog());

        statB.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), StatisticPage.class)));

    }

    /**
     * Отображение диалогового окна с подтверждением удаления аккаунта пользователя.
     */
    private void showDeleteConfirmationDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.confirm_delete_title))
                .setMessage(getString(R.string.confirm_delete_message))
                .setPositiveButton(getString(R.string.delete), (dialog, which) -> deleteAccount())
                .setNegativeButton(getString(android.R.string.cancel), null);

    }

    /**
     * Удаление аккаунта пользователя и связанных с ним данных из базы Firebase.
     * При успешном удалении перенаправляет на экран аутентификации.
     */
    private void deleteAccount() {
        userRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                user.delete().addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        Toast.makeText(
                                getContext(),
                                getString(R.string.account_deleted),
                                Toast.LENGTH_SHORT
                        ).show();
                        startActivity(new Intent(getActivity(), AuthActivity.class));
                        requireActivity().finish();
                    } else {
                        Toast.makeText(
                                getContext(),
                                getString(R.string.error_delete_account),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
            } else {
                Toast.makeText(
                        getContext(),
                        getString(R.string.error_delete_data),
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

}
