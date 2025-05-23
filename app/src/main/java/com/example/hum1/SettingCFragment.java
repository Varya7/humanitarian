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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Map;

public class SettingCFragment extends Fragment {
    private FirebaseAuth auth;
    private FirebaseUser user;
    private DatabaseReference userRef, mDatabase;
    private ArrayList<Map<String, String>> listC;
    private RecyclerView recyclerView;
    private ListAdapter adapter;

    private String userId = "";

    private LinearLayout commentLayout;
    private TextView commV, statusV, emailV, fioV, work_timeV, phone_numberV, logoutV, deleteV, center_nameV, addressV, docV;
    private Button edit_dataB, edit_passwordB, edit_listB, edit_listU;

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

        return view;
    }

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

        listC = new ArrayList<>();
        adapter = new ListAdapter(listC);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupFirebase() {
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        if (user == null) return;

        userId = user.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        userRef = mDatabase.child("Users").child(userId);
    }

    private void loadUserData() {
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
                        statusV.setText("Заявка на регистрацию центра рассматривается. Пользователи пока не могут отправлять заявки центр. Пожалуйста, дождитесь решения модератора.");
                    } else if ("Одобрено".equals(status)) {
                        statusV.setText("Заявка на регистрацию центра одобрена. Пользователи могут отправлять заявки в центр.");
                    } else {
                        statusV.setText("Заявка на регистрацию центра отклонена.");
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
    }

    private void showDeleteConfirmationDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Подтверждение удаления")
                .setMessage("Вы хотите удалить свой аккаунт?")
                .setPositiveButton("Удалить", (dialog, which) -> deleteAccount())
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void deleteAccount() {
        userRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                user.delete().addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        Toast.makeText(getContext(), "Аккаунт удалён", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getActivity(), AuthActivity.class));
                        requireActivity().finish();
                    } else {
                        Toast.makeText(getContext(), "Ошибка удаления аккаунта", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(getContext(), "Ошибка при удалении данных", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
