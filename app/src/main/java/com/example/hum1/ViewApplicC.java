package com.example.hum1;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Класс {@code ViewApplicC} представляет экран просмотра и управления заявкой
 * от имени сотрудника центра.
 * Отображает данные заявки, список выбранных позиций, дополнительную информацию,
 * а также предоставляет возможность одобрить или отклонить заявку с комментарием.
 *
 * Использует Firebase Realtime Database для получения и изменения данных заявки.
 */
public class ViewApplicC extends AppCompatActivity {

    DatabaseReference mDatabase;

    private ArrayList<Map<String, String>> listC;
    Button statusT;
    RecyclerView recyclerView;
    RecyclerView recyclerView2;
    ListU3Adapter adapter2;
    private ArrayList<ListU3> listU3List;
    EditText comV;
    ListAdapter adapter;
    TextView statusF, dateV, timeV, emailV, fioV, phone_numberV, birthV, listV;
    String id, date, time, email, fio, phone_number, birth, status;

    /**
     * Метод {@code onCreate} вызывается при создании Activity.
     * Инициализирует компоненты интерфейса, загружает данные заявки из Firebase
     * и настраивает обработчики кнопок изменения статуса заявки.
     *
     * @param savedInstanceState Состояние, сохраненное при предыдущем запуске (если есть).
     */
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_view_applic_c);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        dateV = findViewById(R.id.date);
        timeV = findViewById(R.id.time);
        emailV = findViewById(R.id.email);
        fioV = findViewById(R.id.fio);
        phone_numberV = findViewById(R.id.phone_number);
        birthV = findViewById(R.id.birth);
        listV = findViewById(R.id.list);
        statusT = findViewById(R.id.statusT);
        statusF = findViewById(R.id.statusF);
        comV = findViewById(R.id.comm);

        listC = new ArrayList<>();
        adapter = new ListAdapter(listC);
        recyclerView = findViewById(R.id.recyclerView_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        recyclerView.setNestedScrollingEnabled(false);

        listU3List = new ArrayList<>();
        adapter2 = new ListU3Adapter(listU3List);
        recyclerView2 = findViewById(R.id.recyclerView_list2);
        recyclerView2.setLayoutManager(new LinearLayoutManager(this));
        recyclerView2.setAdapter(adapter2);

        Bundle bundle = getIntent().getExtras();
        id = bundle.getString("id");

        mDatabase.child("Applications").child(id).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                } else {
                    DataSnapshot snapshot = task.getResult();
                    if (snapshot.exists()) {
                        email = snapshot.child("email").getValue(String.class);
                        fio = snapshot.child("fio").getValue(String.class);

                        phone_number = snapshot.child("phone_number").getValue(String.class);
                        birth = snapshot.child("birth").getValue(String.class);
                        date = snapshot.child("date").getValue(String.class);
                        time = snapshot.child("time").getValue(String.class);
                        status = snapshot.child("status").getValue(String.class);
                        dateV.setText(date);
                        timeV.setText(time);
                        emailV.setText(email);
                        fioV.setText(fio);
                        phone_numberV.setText(phone_number);
                        birthV.setText(birth);
                        if (status.equals("Одобрено")){
                            statusT.setText("Заявка одобрена!");
                        }

                    }
                }
            }
        });

        loadListData();
        loadListU3Data();




        statusT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String commentText = String.valueOf(comV.getText());

                // ТЕСТ: Проверяем все данные перед отправкой
                mDatabase.child("Applications").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String userId = snapshot.child("id").getValue(String.class);
                            Log.d("FCM_TEST", "User ID: " + userId);

                            // Проверяем есть ли токен у пользователя
                            mDatabase.child("Users").child(userId).child("fcmToken")
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot tokenSnapshot) {
                                            String token = tokenSnapshot.getValue(String.class);
                                            Log.d("FCM_TEST", "FCM Token: " + token);

                                            if (token == null || token.isEmpty()) {
                                                Log.e("FCM_TEST", "❌ ТОКЕН НЕ НАЙДЕН!");
                                                // Показываем тестовое уведомление
                                                showTestNotification();
                                            } else {
                                                Log.d("FCM_TEST", "✅ Токен найден, отправляем уведомление");
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Log.e("FCM_TEST", "Ошибка получения токена");
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("FCM_TEST", "Ошибка получения заявки");
                    }
                });





                // Обновляем статус в базе
                mDatabase.child("Applications").child(id).child("status").setValue("Одобрено");
                if (!commentText.equals("")) {
                    mDatabase.child("Applications").child(id).child("comment").setValue(commentText);
                }

                // Отправляем уведомление
                sendStatusNotification("Одобрено", commentText);

                Toast.makeText(ViewApplicC.this, "Статус заявки изменен на Одобрено", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ViewApplicC.this, CenterActivity.class);
                startActivity(intent);
                finish();
            }
        });

        statusF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String commentText = String.valueOf(comV.getText());

                // Обновляем статус в базе
                mDatabase.child("Applications").child(id).child("status").setValue("Отклонено");
                if (!commentText.equals("")) {
                    mDatabase.child("Applications").child(id).child("comment").setValue(commentText);
                }

                // Отправляем уведомление
                sendStatusNotification("Отклонено", commentText);

                Toast.makeText(ViewApplicC.this, "Статус заявки изменен на Отклонено", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ViewApplicC.this, CenterApplicationsFragment.class);
                startActivity(intent);
                finish();
            }
        });

    }

    /**
     * Загружает список выбранных предметов из заявки (selected_items) из Firebase
     * и отображает их в {@link RecyclerView}.
     */
    private void loadListData() {
        mDatabase.child("Applications").child(id).child("selected_items").addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listC.clear();
                for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                    String itemName = itemSnapshot.getKey();
                    Object itemValue = itemSnapshot.getValue();
                    if (itemValue instanceof Long && (Long)itemValue == 0) {
                        continue;
                    }
                    if (itemValue instanceof String && "0".equals(itemValue)) {
                        continue;
                    }

                    Map<String, String> item = new HashMap<>();
                    item.put("name", itemName);

                    if (itemValue instanceof Long) {
                        item.put("quantity", String.valueOf((Long) itemValue));
                    } else if (itemValue instanceof String) {
                        item.put("quantity", (String) itemValue);
                    }

                    listC.add(item);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                  }
        });
    }

    /**
     * Загружает дополнительную информацию о пользователе из заявки в Firebase
     * и отображает её в {@link RecyclerView}.
     */
    private void loadListU3Data() {
        mDatabase.child("Applications").child(id).child("list_u").addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listU3List.clear();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    String label = childSnapshot.getKey();
                    String value = childSnapshot.getValue(String.class);
                    if (label != null && value != null) {
                        listU3List.add(new ListU3(label, value));
                    }
                }
                adapter2.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                  }
        });
    }


    /**
     * Отправляет уведомление пользователю при изменении статуса заявки
     */
    private void sendStatusNotification(String newStatus, String comment) {
        Log.d("FCM_DEBUG", "=== НАЧАЛО ОТПРАВКИ УВЕДОМЛЕНИЯ ===");
        Log.d("FCM_DEBUG", "Статус: " + newStatus + ", Комментарий: " + comment);

        // Сначала покажем локальное уведомление чтобы убедиться что код выполняется
        showLocalNotification(newStatus, comment);

        mDatabase.child("Applications").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String userId = snapshot.child("id").getValue(String.class);
                    String applicationDate = snapshot.child("date").getValue(String.class);
                    String userEmail = snapshot.child("email").getValue(String.class);

                    Log.d("FCM_DEBUG", "📄 Данные заявки:");
                    Log.d("FCM_DEBUG", "User ID: " + userId);
                    Log.d("FCM_DEBUG", "Date: " + applicationDate);
                    Log.d("FCM_DEBUG", "Email: " + userEmail);

                    if (userId != null) {
                        Log.d("FCM_DEBUG", "🔍 Ищем FCM токен для пользователя: " + userId);

                        mDatabase.child("Users").child(userId).child("fcmToken")
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot tokenSnapshot) {
                                        String userFcmToken = tokenSnapshot.getValue(String.class);

                                        if (userFcmToken != null && !userFcmToken.isEmpty()) {
                                            Log.d("FCM_DEBUG", "✅ ТОКЕН НАЙДЕН: " + userFcmToken);
                                            Log.d("FCM_DEBUG", "Длина токена: " + userFcmToken.length());

                                            // Отправляем FCM уведомление
                                            sendFCMNotification(userFcmToken, newStatus, applicationDate, comment);
                                        } else {
                                            Log.e("FCM_DEBUG", "❌ ТОКЕН НЕ НАЙДЕН в базе!");
                                            Log.d("FCM_DEBUG", "Проверьте что пользователь запускал приложение и токен сохранился");
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Log.e("FCM_DEBUG", "❌ Ошибка чтения токена из базы", error.toException());
                                    }
                                });
                    } else {
                        Log.e("FCM_DEBUG", "❌ User ID is null - не могу найти пользователя");
                    }
                } else {
                    Log.e("FCM_DEBUG", "❌ Заявка не найдена в базе данных");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FCM_DEBUG", "❌ Ошибка чтения заявки из базы", error.toException());
            }
        });
    }

    private void showLocalNotification(String status, String comment) {
        try {
            String channelId = "debug_channel";
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(
                        channelId, "Debug Notifications", NotificationManager.IMPORTANCE_HIGH);
                manager.createNotificationChannel(channel);
            }

            String body = status.equals("Одобрено")
                    ? "✅ ЛОКАЛЬНОЕ: Заявка одобрена!"
                    : "❌ ЛОКАЛЬНОЕ: Заявка отклонена";

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                    .setContentTitle("Тест уведомления")
                    .setContentText(body)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_HIGH);

            manager.notify(9999, builder.build());

            Log.d("FCM_DEBUG", "📱 Локальное уведомление показано");

        } catch (Exception e) {
            Log.e("FCM_DEBUG", "❌ Ошибка показа локального уведомления", e);
        }
    }

    /**
     * Отправляет FCM уведомление через HTTP запрос
     */
    /**
     * Отправляет FCM уведомление через HTTP запрос
     */
    private void sendFCMNotification(String token, String status, String date, String comment) {
        try {
            // ✅ ПРАВИЛЬНЫЙ КЛЮЧ (тот что начинается с AIza)
            final String SERVER_KEY = "key=AIzaSyCMdJRwMVsqLU2JrsuJQ9bNQl-86z0xB8I";

            // ✅ ПРАВИЛЬНЫЙ URL для Legacy API
            final String FCM_URL = "https://fcm.googleapis.com/fcm/send";

            String title = "Обновление статуса заявки";
            String body = getNotificationBody(status, date, comment);

            Log.d("FCM_DEBUG", "🔐 Ключ: " + SERVER_KEY.substring(0, 15) + "...");
            Log.d("FCM_DEBUG", "🌐 URL: " + FCM_URL);

            // Создаем JSON для FCM Legacy API
            String json = "{" +
                    "\"to\": \"" + token + "\"," +
                    "\"notification\": {" +
                    "\"title\": \"" + title + "\"," +
                    "\"body\": \"" + body + "\"," +
                    "\"sound\": \"default\"" +
                    "}," +
                    "\"data\": {" +
                    "\"applicationId\": \"" + id + "\"," +
                    "\"status\": \"" + status + "\"," +
                    "\"type\": \"status_update\"" +
                    "}" +
                    "}";

            Log.d("FCM_DEBUG", "📦 JSON: " + json);

            // Отправляем запрос в отдельном потоке
            new Thread(() -> {
                try {
                    java.net.URL url = new java.net.URL(FCM_URL);
                    java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                    conn.setRequestProperty("Authorization", SERVER_KEY);
                    conn.setDoOutput(true);
                    conn.setConnectTimeout(10000);
                    conn.setReadTimeout(10000);

                    // Пишем данные
                    java.io.OutputStream os = conn.getOutputStream();
                    os.write(json.getBytes("UTF-8"));
                    os.flush();
                    os.close();

                    // Получаем ответ
                    int responseCode = conn.getResponseCode();
                    Log.d("FCM_RESPONSE", "📨 Response Code: " + responseCode);

                    // Читаем ответ сервера
                    java.io.InputStream inputStream;
                    if (responseCode == 200) {
                        inputStream = conn.getInputStream();
                        Log.d("FCM_RESPONSE", "✅ УСПЕХ! FCM принял запрос");

                        java.io.BufferedReader reader = new java.io.BufferedReader(
                                new java.io.InputStreamReader(inputStream, "UTF-8"));
                        String line;
                        StringBuilder response = new StringBuilder();
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();
                        Log.d("FCM_RESPONSE", "📄 Response Body: " + response.toString());

                        // Проверяем успешность доставки
                        if (response.toString().contains("\"success\":1")) {
                            Log.d("FCM_RESPONSE", "🎉 УВЕДОМЛЕНИЕ ДОСТАВЛЕНО НА УСТРОЙСТВО!");
                        } else {
                            Log.e("FCM_RESPONSE", "❌ FCM не смог доставить уведомление устройству");
                        }
                    } else {
                        inputStream = conn.getErrorStream();
                        Log.e("FCM_RESPONSE", "❌ ОШИБКА HTTP: " + responseCode);

                        java.io.BufferedReader reader = new java.io.BufferedReader(
                                new java.io.InputStreamReader(inputStream, "UTF-8"));
                        String line;
                        StringBuilder errorResponse = new StringBuilder();
                        while ((line = reader.readLine()) != null) {
                            errorResponse.append(line);
                        }
                        reader.close();
                        Log.e("FCM_RESPONSE", "📄 Error Body: " + errorResponse.toString());
                    }

                    conn.disconnect();

                } catch (java.net.SocketTimeoutException e) {
                    Log.e("FCM_ERROR", "⏰ Таймаут подключения", e);
                } catch (Exception e) {
                    Log.e("FCM_ERROR", "💥 Ошибка отправки уведомления", e);
                }
            }).start();

        } catch (Exception e) {
            Log.e("FCM", "Ошибка создания уведомления", e);
        }
    }

    private String getNotificationBody(String status, String date, String comment) {
        switch(status) {
            case "Одобрено":
                return "✅ Ваша заявка от " + date + " одобрена!";
            case "Отклонено":
                String body = "❌ Заявка от " + date + " отклонена";
                return (comment != null && !comment.isEmpty()) ? body + ". " + comment : body;
            default:
                return "Статус заявки от " + date + " изменен на: " + status;
        }
    }


    private void showTestNotification() {
        // Показываем локальное уведомление для проверки
        String channelId = "test_channel";
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId, "Test Channel", NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setContentTitle("Тестовое уведомление")
                .setContentText("Проверка работы уведомлений")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setAutoCancel(true);

        manager.notify(999, builder.build());
        Log.d("FCM_TEST", "✅ Локальное уведомление показано");
    }

}