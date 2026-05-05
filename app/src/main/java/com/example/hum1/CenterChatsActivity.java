package com.example.hum1;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Список диалогов центра. В списке отображается только ФИО пользователя,
 * чтобы центр не видел лишние персональные данные до открытия конкретного чата.
 */
public class CenterChatsActivity extends AppCompatActivity {

    private LinearLayout chatsContainer;
    private DatabaseReference rootRef;
    private String centerId;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleUtil.initAppLocale(this);
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_center_chats);

        centerId = FirebaseAuth.getInstance().getCurrentUser() == null
                ? ""
                : FirebaseAuth.getInstance().getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();
        chatsContainer = findViewById(R.id.chats_container);

        loadChats();
    }

    private void loadChats() {
        rootRef.child("CenterChats").child(centerId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatsContainer.removeAllViews();
                if (!snapshot.exists()) {
                    addChatRow("", getString(R.string.chat_empty));
                    return;
                }

                for (DataSnapshot child : snapshot.getChildren()) {
                    String chatId = child.getKey();
                    String fio = child.getValue(String.class);
                    addChatRow(chatId, fio == null ? "-" : fio);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                chatsContainer.removeAllViews();
                addChatRow("", getString(R.string.error_load_data));
            }
        });
    }

    private void addChatRow(String chatId, String title) {
        TextView row = new TextView(this);
        row.setText(title);
        row.setTextSize(17f);
        row.setTextColor(getResources().getColor(android.R.color.black));
        row.setPadding(16, 16, 16, 16);
        row.setBackgroundResource(R.drawable.bg_white_card);
        if (chatId != null && !chatId.isEmpty()) {
            row.setOnClickListener(v -> {
                Intent intent = new Intent(this, ChatActivity.class);
                intent.putExtra("chat_id", chatId);
                intent.putExtra("center_id", centerId);
                startActivity(intent);
            });
        }
        chatsContainer.addView(row);
    }
}
