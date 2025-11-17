package com.example.hum1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class StatisticPage extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private TextView tvCenterName, tvTotal, tvReviewing, tvApproved, tvRejected, tvIssued, tvCompletionRate;
    private TextView tvNoPieData, tvNoBarData;
    private Spinner spinnerTimeRange;
    private PieChart pieChart;
    private BarChart barChart;
    //private TextView tvNoBarData;


    private String currentUserCenterName;
    private List<Application> applications = new ArrayList<>();
    private int selectedTimeRange = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistic_page);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        initializeViews();
        setupSpinner();
        setupCharts();
        checkUserAuthentication();
    }

    private void initializeViews() {
        tvCenterName = findViewById(R.id.tvCenterName);
        tvTotal = findViewById(R.id.tvTotal);
        tvReviewing = findViewById(R.id.tvReviewing);
        tvApproved = findViewById(R.id.tvApproved);
        tvRejected = findViewById(R.id.tvRejected);
        tvIssued = findViewById(R.id.tvIssued);
        tvCompletionRate = findViewById(R.id.tvCompletionRate);
        tvNoPieData = findViewById(R.id.tvNoPieData);
        
        spinnerTimeRange = findViewById(R.id.spinnerTimeRange);
        pieChart = findViewById(R.id.pieChart);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        tvNoPieData = findViewById(R.id.tvNoPieData);
        tvNoBarData = findViewById(R.id.tvNoBarData);

        spinnerTimeRange = findViewById(R.id.spinnerTimeRange);
        pieChart = findViewById(R.id.pieChart);
        barChart = findViewById(R.id.barChart);

    }

    private void setupSpinner() {
        String[] timeRanges = {"Последние 3 месяца", "Последние 6 месяцев",
                "Последние 12 месяцев", "Последние 2 года", "Все время"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, timeRanges);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTimeRange.setAdapter(adapter);

        spinnerTimeRange.setSelection(1); // 6 месяцев по умолчанию

        spinnerTimeRange.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: selectedTimeRange = 3; break;
                    case 1: selectedTimeRange = 6; break;
                    case 2: selectedTimeRange = 12; break;
                    case 3: selectedTimeRange = 24; break;
                    case 4: selectedTimeRange = 999; break; // Все время
                }
                updateStatistics();
                updateCharts();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupCharts() {
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(40f);
        pieChart.setTransparentCircleRadius(45f);
        pieChart.setEntryLabelTextSize(12f);
        pieChart.setEntryLabelColor(getResources().getColor(android.R.color.black));
        pieChart.getLegend().setEnabled(true);

        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.setDrawBarShadow(false);
        barChart.setPinchZoom(true);
        barChart.setScaleEnabled(true);
        barChart.getAxisRight().setEnabled(false);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);

        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setGranularity(1f);
        leftAxis.setAxisMinimum(0f);


    }

    private void checkUserAuthentication() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, AuthActivity.class));
            finish();
            return;
        }

        loadUserData(currentUser.getUid());
    }

    private void loadUserData(String userId) {
        mDatabase.child("Users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String role = snapshot.child("role").getValue(String.class);
                    String centerName = snapshot.child("center_name").getValue(String.class);

                    if ("center".equals(role) && centerName != null) {
                        currentUserCenterName = centerName;
                        tvCenterName.setText(currentUserCenterName);
                        loadApplications();
                    } else {
                        finish();
                    }
                } else {
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadApplications() {
        mDatabase.child("Applications").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                applications.clear();
                for (DataSnapshot appSnapshot : snapshot.getChildren()) {
                    String center = appSnapshot.child("center").getValue(String.class);
                    if (center == null || !center.equals(currentUserCenterName)) continue;

                    String id_appl = appSnapshot.getKey();
                    String date = appSnapshot.child("date").getValue(String.class);
                    String time = appSnapshot.child("time").getValue(String.class);
                    String email = appSnapshot.child("email").getValue(String.class);
                    String fio = appSnapshot.child("fio").getValue(String.class);
                    String phone_number = appSnapshot.child("phone_number").getValue(String.class);
                    String birth = appSnapshot.child("birth").getValue(String.class);
                    String family_members = appSnapshot.child("family_members").getValue(String.class);
                    String list = appSnapshot.child("list").getValue(String.class);
                    String status = appSnapshot.child("status").getValue(String.class);

                    Application application = new Application(
                            id_appl, date, time, email, fio, phone_number,
                            birth, family_members, list, status
                    );

                    applications.add(application);
                }
                updateStatistics();
                updateCharts();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void updateStatistics() {
        int total = applications.size();
        int reviewing = 0, approved = 0, rejected = 0, issued = 0;

        for (Application app : applications) {
            String status = app.getStatus();
            if (status != null) {
                switch (status) {
                    case "Рассматривается": reviewing++; break;
                    case "Одобрено": approved++; break;
                    case "Отклонено": rejected++; break;
                    case "Выдано": issued++; break;
                }
            }
        }

        double completionRate = total > 0 ? (issued * 100.0 / total) : 0;

        tvTotal.setText(String.valueOf(total));
        tvReviewing.setText(String.valueOf(reviewing));
        tvApproved.setText(String.valueOf(approved));
        tvRejected.setText(String.valueOf(rejected));
        tvIssued.setText(String.valueOf(issued));
        tvCompletionRate.setText(String.format("%.1f%% от всех", completionRate));
    }

    private void updateCharts() {
        updatePieChart();
        updateBarChart();
    }



    private void updatePieChart() {
        int reviewing = 0, approved = 0, rejected = 0, issued = 0;

        for (Application app : applications) {
            String status = app.getStatus();
            if (status != null) {
                switch (status) {
                    case "Рассматривается": reviewing++; break;
                    case "Одобрено": approved++; break;
                    case "Отклонено": rejected++; break;
                    case "Выдано": issued++; break;
                }
            }
        }

        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        if (reviewing > 0) {
            entries.add(new PieEntry(reviewing, "Рассматривается"));
            colors.add(getResources().getColor(R.color.yellow_600));
        }
        if (approved > 0) {
            entries.add(new PieEntry(approved, "Одобрено"));
            colors.add(getResources().getColor(R.color.blue_600));
        }
        if (rejected > 0) {
            entries.add(new PieEntry(rejected, "Отклонено"));
            colors.add(getResources().getColor(R.color.red_600));
        }
        if (issued > 0) {
            entries.add(new PieEntry(issued, "Выдано"));
            colors.add(getResources().getColor(R.color.green_600));
        }

        if (entries.isEmpty()) {
            pieChart.setVisibility(View.GONE);
            tvNoPieData.setVisibility(View.VISIBLE);
            return;
        }

        pieChart.setVisibility(View.VISIBLE);
        tvNoPieData.setVisibility(View.GONE);

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setValueTextSize(12f);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.invalidate();
    }

    private void updateBarChart() {
        if (applications.isEmpty()) {
            barChart.setVisibility(View.GONE);
            tvNoBarData.setVisibility(View.VISIBLE);
            return;
        }

        // 1) Собираем данные по месяцам: ключ "M/YYYY" -> total, issued
        Map<String, MonthlyData> monthsMap = new HashMap<>();

        for (Application app : applications) {
            String dateStr = app.getDate(); // поле date из Firebase
            if (dateStr == null || dateStr.isEmpty()) continue;

            // допустим формат "DD.MM.YYYY" или "DD/MM/YYYY" или "DD-MM-YYYY"
            String[] parts = dateStr.split("[./-]");
            if (parts.length < 3) continue;

            int day, month, year;
            try {
                day = Integer.parseInt(parts[0]);
                month = Integer.parseInt(parts[1]);
                year = Integer.parseInt(parts[2]);
            } catch (NumberFormatException e) {
                continue;
            }

            // ключ как в React: "month/year"
            String monthKey = month + "/" + year;
            MonthlyData mData = monthsMap.get(monthKey);
            if (mData == null) {
                mData = new MonthlyData();
                monthsMap.put(monthKey, mData);
            }

            mData.total++;
            if ("Выдано".equals(app.getStatus())) {
                mData.issued++;
            }
        }

        if (monthsMap.isEmpty()) {
            barChart.setVisibility(View.GONE);
            tvNoBarData.setVisibility(View.VISIBLE);
            return;
        }

        // 2) Сортируем по дате и применяем ограничение selectedTimeRange (3, 6, 12, 24, 999)
        List<String> keys = new ArrayList<>(monthsMap.keySet());
        Collections.sort(keys, (a, b) -> {
            String[] pa = a.split("/");
            String[] pb = b.split("/");

            int ma = Integer.parseInt(pa[0]);
            int ya = Integer.parseInt(pa[1]);
            int mb = Integer.parseInt(pb[0]);
            int yb = Integer.parseInt(pb[1]);

            if (ya != yb) return Integer.compare(ya, yb);
            return Integer.compare(ma, mb);
        });

        if (selectedTimeRange < keys.size()) {
            keys = keys.subList(keys.size() - selectedTimeRange, keys.size());
        }

        // 3) Формируем BarEntry и подписи по X
        List<BarEntry> totalEntries = new ArrayList<>();
        List<BarEntry> issuedEntries = new ArrayList<>();
        List<String> xLabels = new ArrayList<>();

        // Используем индекс i как X (0,1,2,...) и подпись "M/YYYY"
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            MonthlyData mData = monthsMap.get(key);
            if (mData == null) continue;

            totalEntries.add(new BarEntry(i, mData.total));
            issuedEntries.add(new BarEntry(i, mData.issued));
            xLabels.add(key);
        }

        if (totalEntries.isEmpty()) {
            barChart.setVisibility(View.GONE);
            tvNoBarData.setVisibility(View.VISIBLE);
            return;
        }

        barChart.setVisibility(View.VISIBLE);
        tvNoBarData.setVisibility(View.GONE);

        // 4) Создаём два набора данных: "Всего заявок" и "Выдано"
        BarDataSet totalSet = new BarDataSet(totalEntries, "Всего заявок");
        totalSet.setColor(getResources().getColor(R.color.blue_600));
        totalSet.setValueTextSize(10f);

        BarDataSet issuedSet = new BarDataSet(issuedEntries, "Выдано");
        issuedSet.setColor(getResources().getColor(R.color.green_600));
        issuedSet.setValueTextSize(10f);

        BarData barData = new BarData(totalSet, issuedSet);
        barData.setBarWidth(0.4f); // ширина столбца

        // 5) Настройка оси X с подписями "M/YYYY"
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xLabels));
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);

        barChart.setData(barData);
        barChart.setFitBars(true);
        barChart.invalidate();
    }


    private static class MonthlyData {
        int total = 0;
        int issued = 0;
    }
}
