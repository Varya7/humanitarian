package com.example.hum1;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Вкладка переписок центра. В списке показывается только имя пользователя,
 * а вся история открывается в отдельном чате.
 */
public class CenterChatsFragment extends Fragment {

    private LinearLayout chatsContainer;
    private DatabaseReference rootRef;
    private String centerId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_center_chats, container, false);
        chatsContainer = view.findViewById(R.id.chats_container);
        rootRef = FirebaseDatabase.getInstance().getReference();
        centerId = FirebaseAuth.getInstance().getCurrentUser() == null ? "" : FirebaseAuth.getInstance().getCurrentUser().getUid();
        loadChats();
        return view;
    }

    private void loadChats() {
        rootRef.child("CenterChats").child(centerId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;
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
                if (isAdded()) Toast.makeText(requireContext(), getString(R.string.error_load_data), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addChatRow(String chatId, String title) {
        TextView row = new TextView(requireContext());
        row.setText(title);
        row.setTextSize(16f);
        row.setTextColor(getResources().getColor(android.R.color.black));
        row.setPadding(18, 18, 18, 18);
        row.setBackgroundResource(R.drawable.bg_white_card);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 12);
        row.setLayoutParams(params);
        if (chatId != null && !chatId.isEmpty()) {
            row.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), ChatActivity.class);
                intent.putExtra("chat_id", chatId);
                intent.putExtra("center_id", centerId);
                intent.putExtra("role", "center");
                startActivity(intent);
            });
        }
        chatsContainer.addView(row);
    }
}
