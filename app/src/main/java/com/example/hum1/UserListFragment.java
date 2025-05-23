package com.example.hum1;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import androidx.appcompat.widget.SearchView;


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
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class UserListFragment extends Fragment {

    private ArrayList<Center> centers = new ArrayList<>();
    private CenterAdapter adapter;
    private DatabaseReference mDatabase, listCRef;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private String userId;

    private String id, center_name, address, email, fio, work_time, phone_number, list;
    private List<String> listC;

    private SearchView searchView;
    private RecyclerView recyclerView;

    public UserListFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_list, container, false);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        userId = user.getUid();

        listCRef = FirebaseDatabase.getInstance().getReference("list_c");
        listC = new ArrayList<>();

        searchView = view.findViewById(R.id.searchView);
        recyclerView = view.findViewById(R.id.list);

        adapter = new CenterAdapter(getContext(), centers, (center, position) -> {
            Intent intent = new Intent(getContext(), ViewCenter.class);
            intent.putExtra("id", center.getId());
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), layoutManager.getOrientation()));

        readListC();
        loadCenters();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { return false; }

            @Override public boolean onQueryTextChange(String newText) {
                filterList(newText);
                return true;
            }
        });

        return view;
    }

    private void readListC() {
        listCRef.addValueEventListener(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listC.clear();
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    String item = itemSnapshot.getValue(String.class);
                    if (item != null) listC.add(item);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadCenters() {
        mDatabase.child("Users").get().addOnCompleteListener((Task<DataSnapshot> task) -> {
            if (!task.isSuccessful()) {
                Log.e("firebase", "Error getting data", task.getException());
                return;
            }

            DataSnapshot snapshot = task.getResult();
            if (snapshot.exists()) {
                for (DataSnapshot applicationSnapshot : snapshot.getChildren()) {
                    String role = applicationSnapshot.child("role").getValue(String.class);
                    if ("center".equals(role)) {
                        String status = applicationSnapshot.child("status").getValue(String.class);
                        if ("Одобрено".equals(status)) {
                            center_name = applicationSnapshot.child("center_name").getValue(String.class);
                            address = applicationSnapshot.child("address").getValue(String.class);
                            email = applicationSnapshot.child("email").getValue(String.class);
                            fio = applicationSnapshot.child("fio").getValue(String.class);
                            work_time = applicationSnapshot.child("work_time").getValue(String.class);
                            phone_number = applicationSnapshot.child("phone_number").getValue(String.class);
                            id = applicationSnapshot.child("id").getValue(String.class);
                            centers.add(new Center(id, center_name, address, email, fio, work_time, phone_number, list));
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void filterList(String text) {
        ArrayList<Center> filteredList = new ArrayList<>();
        for (Center center : centers) {
            if (center.getCenter_name().toLowerCase().contains(text.toLowerCase()) ||
                    center.getAddress().toLowerCase().contains(text.toLowerCase()) ||
                    center.getFIO().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(center);
            }
        }
        adapter.updateList(filteredList);
    }
}
