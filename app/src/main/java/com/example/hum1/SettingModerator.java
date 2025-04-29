package com.example.hum1;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

public class SettingModerator extends AppCompatActivity {


    FirebaseAuth auth;
    FirebaseUser user;
    private DatabaseReference userRef;
    String userId = "";
    private DatabaseReference mDatabase;
    BottomNavigationView bottomNavigationView;
    TextView emailV, logoutV, deleteV;
    Button edit_passwordB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }


        setContentView(R.layout.activity_setting_moderator);
        emailV = findViewById(R.id.email);
        logoutV = findViewById(R.id.logout);
        deleteV = findViewById(R.id.delete);

        edit_passwordB = findViewById(R.id.edit_password);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        assert user != null;
        userId = user.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        userRef = mDatabase.child("Users").child(userId);
        loadUserData();
        setupButtons();
        setupBottomNavigation();
    }

    private void loadUserData() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    emailV.setText(snapshot.child("email").getValue(String.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                }
        });
    }


    private void setupButtons() {
        logoutV.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, Login.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        });

        deleteV.setOnClickListener(v -> showDeleteConfirmationDialog());

        edit_passwordB.setOnClickListener(v ->
                startActivity(new Intent(this, ChangePasswordActivity.class)));

    }

    private void setupBottomNavigation() {


        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_setting);
        bottomNavigationView.invalidate();
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navigation_see) {
                startActivity(new Intent(this, ModeratorList.class));
                finish();
                return true;
            }
            return false;
        });

    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Подтверждение удаления")
                .setMessage("Вы хотите удалить свой аккаунт?")
                .setPositiveButton("Удалить", (dialog, which) -> deleteAccount())
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void deleteAccount() {
        userRef.removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        user.delete()
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        Toast.makeText(this, "Аккаунт удалён", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(this, Login.class));
                                        finish();
                                    } else {
                                        Toast.makeText(this, "Ошибка удаления аккаунта: " + task1.getException(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(this, "Ошибка удаления данных из базы: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

}