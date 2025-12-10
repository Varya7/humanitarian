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

import com.example.hum1.classes.Application;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StatisticPage extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private TextView tvCenterName, tvTotal, tvReviewing, tvApproved, tvRejected, tvIssued, tvCompletionRate;
    private TextView tvNoPieData, tvNoBarData;
    private Spinner spinnerTimeRange;
    private PieChart pieChart;
    private BarChart barChart;

    private String currentUserCenterName;
    private List<Application> applications = new ArrayList<>();
    private int selectedTimeRange = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleUtil.initAppLocale(this);
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
        tvNoBarData = findViewById(R.id.tvNoBarData);

        spinnerTimeRange = findViewById(R.id.spinnerTimeRange);
        pieChart = findViewById(R.id.pieChart);
        barChart = findViewById(R.id.barChart);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.time_ranges,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTimeRange.setAdapter(adapter);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTimeRange.setAdapter(adapter);

        spinnerTimeRange.setSelection(1);

        spinnerTimeRange.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        selectedTimeRange = 3;
                        break;
                    case 1:
                        selectedTimeRange = 6;
                        break;
                    case 2:
                        selectedTimeRange = 12;
                        break;
                    case 3:
                        selectedTimeRange = 24;
                        break;
                    case 4:
                        selectedTimeRange = 999;
                        break;
                }
                updateStatistics();
                updateCharts();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void setupCharts() {
        // PieChart
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(40f);
        pieChart.setTransparentCircleRadius(45f);
        pieChart.setEntryLabelTextSize(12f);
        pieChart.setEntryLabelColor(getResources().getColor(android.R.color.black));
        pieChart.getLegend().setEnabled(true);

        // BarChart
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
        mDatabase.child("Users").child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
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
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
    }

    private void loadApplications() {
        mDatabase.child("Applications")
                .addListenerForSingleValueEvent(new ValueEventListener() {
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
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
    }

    private ArrayList<Application> getFilteredApplicationsByTimeRange() {
        if (selectedTimeRange == 999) {
            return new ArrayList<>(applications);
        }

        ArrayList<Application> filtered = new ArrayList<>();

        Calendar calNow = Calendar.getInstance();
        calNow.set(Calendar.HOUR_OF_DAY, 0);
        calNow.set(Calendar.MINUTE, 0);
        calNow.set(Calendar.SECOND, 0);
        calNow.set(Calendar.MILLISECOND, 0);

        Calendar calFrom = (Calendar) calNow.clone();
        calFrom.add(Calendar.MONTH, -selectedTimeRange);

        for (Application app : applications) {
            String dateStr = app.getDate();
            if (dateStr == null || dateStr.isEmpty()) continue;

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

            Calendar calApp = Calendar.getInstance();
            calApp.set(Calendar.YEAR, year);
            calApp.set(Calendar.MONTH, month - 1);
            calApp.set(Calendar.DAY_OF_MONTH, day);
            calApp.set(Calendar.HOUR_OF_DAY, 0);
            calApp.set(Calendar.MINUTE, 0);
            calApp.set(Calendar.SECOND, 0);
            calApp.set(Calendar.MILLISECOND, 0);

            if (calApp.after(calFrom) || calApp.equals(calNow)) {
                filtered.add(app);
            }
        }

        return filtered;
    }


    private void updateStatistics() {
        ArrayList<Application> filtered = getFilteredApplicationsByTimeRange();

        int total = filtered.size();
        int reviewing = 0, approved = 0, rejected = 0, issued = 0;

        for (Application app : filtered) {
            String status = app.getStatus();
            if (status != null) {

                switch (status) {
                    case "Рассматривается":
                        reviewing++;
                        break;
                    case "Одобрено":
                        approved++;
                        break;
                    case "Отклонено":
                        rejected++;
                        break;
                    case "Выдано":
                        issued++;
                        break;
                }
            }
        }

        double completionRate = total > 0 ? (issued * 100.0 / total) : 0;

        tvTotal.setText(String.valueOf(total));
        tvReviewing.setText(String.valueOf(reviewing));
        tvApproved.setText(String.valueOf(approved));
        tvRejected.setText(String.valueOf(rejected));
        tvIssued.setText(String.valueOf(issued));
        tvCompletionRate.setText(
                String.format(Locale.getDefault(), "%.1f%% от всех", completionRate)
        );
    }

    private void updateCharts() {
        updatePieChart();
        updateBarChart();
    }

    private void updatePieChart() {
        ArrayList<Application> filtered = getFilteredApplicationsByTimeRange();

        int reviewing = 0, approved = 0, rejected = 0, issued = 0;

        for (Application app : filtered) {
            String status = app.getStatus();
            if (status != null) {
                switch (status) {
                    case "Рассматривается":
                        reviewing++;
                        break;
                    case "Одобрено":
                        approved++;
                        break;
                    case "Отклонено":
                        rejected++;
                        break;
                    case "Выдано":
                        issued++;
                        break;
                }
            }
        }

        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        if (reviewing > 0) {
            entries.add(new PieEntry(reviewing, getString(R.string.status_pending)));
            colors.add(getResources().getColor(R.color.yellow_600));
        }
        if (approved > 0) {
            entries.add(new PieEntry(approved, getString(R.string.status_approved)));
            colors.add(getResources().getColor(R.color.blue_600));
        }
        if (rejected > 0) {
            entries.add(new PieEntry(rejected, getString(R.string.status_rejected)));
            colors.add(getResources().getColor(R.color.red_600));
        }
        if (issued > 0) {
            entries.add(new PieEntry(issued, getString(R.string.status_issued)));
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
        pieChart.notifyDataSetChanged();
        pieChart.invalidate();
    }

    private void updateBarChart() {
        ArrayList<Application> filtered = getFilteredApplicationsByTimeRange();

        if (filtered.isEmpty()) {
            barChart.setVisibility(View.GONE);
            tvNoBarData.setVisibility(View.VISIBLE);
            return;
        }

        Map<String, MonthlyData> monthsMap = new HashMap<>();

        for (Application app : filtered) {
            String dateStr = app.getDate();
            if (dateStr == null || dateStr.isEmpty()) continue;

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

        List<BarEntry> totalEntries = new ArrayList<>();
        List<BarEntry> issuedEntries = new ArrayList<>();
        List<String> xLabels = new ArrayList<>();

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

        BarDataSet totalSet = new BarDataSet(totalEntries, "Всего заявок");
        totalSet.setColor(getResources().getColor(R.color.blue_600));
        totalSet.setValueTextSize(10f);

        BarDataSet issuedSet = new BarDataSet(issuedEntries, "Выдано");
        issuedSet.setColor(getResources().getColor(R.color.green_600));
        issuedSet.setValueTextSize(10f);

        BarData barData = new BarData(totalSet, issuedSet);
        barData.setBarWidth(0.4f);

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
