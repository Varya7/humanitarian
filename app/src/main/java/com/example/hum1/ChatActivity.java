package com.example.hum1;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

/**
 * Экран переписки пользователя с конкретным центром.
 * История сообщений хранится в Firebase, а центр видит в списке диалогов
 * только минимальную информацию о пользователе: ФИО и id чата.
 */
public class ChatActivity extends AppCompatActivity {

    private LinearLayout messagesContainer;
    private EditText messageInput;
    private DatabaseReference rootRef;
    private String chatId;
    private String centerId;
    private String userId;
    private String currentRole = "user";
    private String userFio = "";
    private String centerName = "";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleUtil.initAppLocale(this);
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_chat);

        rootRef = FirebaseDatabase.getInstance().getReference();
        messagesContainer = findViewById(R.id.messages_container);
        messageInput = findViewById(R.id.message_input);
        Button sendB = findViewById(R.id.send_message);

        centerId = getIntent().getStringExtra("center_id");
        chatId = getIntent().getStringExtra("chat_id");
        centerName = getIntent().getStringExtra("center_name");
        userId = FirebaseAuth.getInstance().getCurrentUser() == null
                ? ""
                : FirebaseAuth.getInstance().getCurrentUser().getUid();

        rootRef.child("Users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String role = snapshot.child("role").getValue(String.class);
                currentRole = TextUtils.isEmpty(role) ? "user" : role;
                userFio = snapshot.child("fio").getValue(String.class);

                if ("center".equals(currentRole)) {
                    centerId = userId;
                }
                if (TextUtils.isEmpty(chatId)) {
                    chatId = centerId + "_" + userId;
                }
                subscribeMessages();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChatActivity.this, getString(R.string.error_load_data), Toast.LENGTH_SHORT).show();
            }
        });

        sendB.setOnClickListener(v -> sendMessage());
    }

    private void subscribeMessages() {
        rootRef.child("Chats").child(chatId).child("messages")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messagesContainer.removeAllViews();
                        if (!snapshot.exists()) {
                            TextView empty = createMessageView(getString(R.string.chat_empty), false);
                            messagesContainer.addView(empty);
                            return;
                        }
                        for (DataSnapshot child : snapshot.getChildren()) {
                            String text = child.child("text").getValue(String.class);
                            String senderRole = child.child("senderRole").getValue(String.class);
                            messagesContainer.addView(createMessageView(text, TextUtils.equals(senderRole, currentRole)));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ChatActivity.this, getString(R.string.error_load_data), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private TextView createMessageView(String text, boolean mine) {
        TextView view = new TextView(this);
        view.setText(text == null ? "" : text);
        view.setTextSize(15f);
        view.setTextColor(getResources().getColor(android.R.color.black));
        view.setPadding(14, 10, 14, 10);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.gravity = mine ? Gravity.END : Gravity.START;
        params.bottomMargin = 8;
        view.setLayoutParams(params);
        view.setBackgroundResource(R.drawable.comment_background);
        return view;
    }

    private void sendMessage() {
        String text = messageInput.getText().toString().trim();
        if (TextUtils.isEmpty(text) || TextUtils.isEmpty(chatId)) return;

        Map<String, Object> message = new HashMap<>();
        message.put("text", text);
        message.put("senderId", userId);
        message.put("senderRole", currentRole);
        message.put("created_at", ServerValue.TIMESTAMP);

        Map<String, Object> updates = new HashMap<>();
        String messageKey = rootRef.child("Chats").child(chatId).child("messages").push().getKey();
        updates.put("Chats/" + chatId + "/centerId", centerId);
        updates.put("Chats/" + chatId + "/userId", "center".equals(currentRole) ? getChatUserId() : userId);
        updates.put("Chats/" + chatId + "/centerName", centerName);
        updates.put("Chats/" + chatId + "/lastMessage", text);
        updates.put("Chats/" + chatId + "/updated_at", ServerValue.TIMESTAMP);
        updates.put("Chats/" + chatId + "/messages/" + messageKey, message);
        if (!"center".equals(currentRole)) {
            updates.put("Chats/" + chatId + "/userFio", TextUtils.isEmpty(userFio) ? "-" : userFio);
            updates.put("CenterChats/" + centerId + "/" + chatId, TextUtils.isEmpty(userFio) ? "-" : userFio);
        }

        rootRef.updateChildren(updates)
                .addOnSuccessListener(unused -> {
                    messageInput.setText("");
                    Toast.makeText(this, getString(R.string.chat_message_sent), Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(error ->
                        Toast.makeText(this, getString(R.string.error_save_data), Toast.LENGTH_SHORT).show());
    }

    private String getChatUserId() {
        if (chatId == null) return "";
        int index = chatId.indexOf("_");
        return index >= 0 && index + 1 < chatId.length() ? chatId.substring(index + 1) : "";
    }
}
