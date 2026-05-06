package com.example.hum1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StatisticPage extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private TextView tvCenterName;
    private TextView tvLoadingStatus;
    private TextView tvWarmStartHint;
    private TextView tvAccessMessage;
    private TextView tvDateRange;

    private TextView tvTotal;
    private TextView tvReviewing;
    private TextView tvApproved;
    private TextView tvRejected;
    private TextView tvIssued;
    private TextView tvCompletionRate;

    private TextView tvForecastAppsNextMonth;
    private TextView tvForecastItemsNextMonth;
    private TextView tvForecastRecommendedOrder;
    private TextView tvForecastItemsAtRisk;
    private TextView tvLoadLevelBadge;
    private TextView tvLoadLevelDetails;

    private LinearLayout layoutBusySlots;
    private TextView tvPeakMonths;

    private PieChart pieChart;
    private TextView tvNoPieData;

    private BarChart barChartMonthly;
    private TextView tvNoMonthlyData;

    private TableLayout tableProcurement;
    private TextView tvNoProcurementData;
    private Button btnToggleProcurementTable;
    private TextView tvProcurementCount;

    private LineChart lineChartForecast;
    private TextView tvNoTimelineData;

    private HorizontalBarChart hBarTopItems;
    private TextView tvNoTopItemsData;
    private Button btnToggleTopItemsChart;
    private TextView tvTopItemsChartCount;

    private View cardModelNotes;
    private TextView tvModelNotes;

    private Spinner spinnerTimeRange;
    private Button btnDateRange;
    private boolean suppressSpinnerListener = false;

    private String currentUserCenterName;
    private String currentUserId;

    private Query appsQuery;
    private ValueEventListener appsListener;

    private final List<ApplicationRecord> applications = new ArrayList<>();
    private final List<CenterLoadForecastModel.InventoryItem> inventoryItems = new ArrayList<>();
    private final List<CenterLoadForecastModel.InventoryItem> reservedInventoryItems = new ArrayList<>();

    private final Gson gson = new Gson();
    private SharedPreferences cachePreferences;
    private CenterLoadForecastModel.CenterLoadForecast cachedForecast;

    private boolean hasLoadedApps = false;
    private boolean authLoading = true;
    private boolean appsLoading = false;
    private boolean procurementTableExpanded = false;
    private boolean topItemsChartExpanded = false;
    private CenterLoadForecastModel.CenterLoadForecast currentForecastToRender;
    private static final String FORECAST_CACHE_VERSION = "v2";

    private int selectedTimeRangeMonths = 6;
    private Calendar customRangeFrom;
    private Calendar customRangeTo;
    private static final int COMPACT_ITEMS_LIMIT = 12;

    private final SimpleDateFormat uiDateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
    private final ValueFormatter integerValueFormatter = new ValueFormatter() {
        @Override
        public String getFormattedValue(float value) {
            return String.valueOf(Math.round(value));
        }
    };

    private enum StatusGroup {
        reviewing,
        approved,
        rejected,
        issued,
        other
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleUtil.initAppLocale(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistic_page);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        cachePreferences = getSharedPreferences("statistics_forecast_cache", MODE_PRIVATE);

        initializeViews();
        setupCharts();
        setupTimeRangeSpinner();
        setupDateRangeButton();
        checkUserAuthentication();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (appsQuery != null && appsListener != null) {
            appsQuery.removeEventListener(appsListener);
        }
    }

    private void initializeViews() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        tvCenterName = findViewById(R.id.tvCenterName);
        tvLoadingStatus = findViewById(R.id.tvLoadingStatus);
        tvWarmStartHint = findViewById(R.id.tvWarmStartHint);
        tvAccessMessage = findViewById(R.id.tvAccessMessage);
        tvDateRange = findViewById(R.id.tvDateRange);

        tvTotal = findViewById(R.id.tvTotal);
        tvReviewing = findViewById(R.id.tvReviewing);
        tvApproved = findViewById(R.id.tvApproved);
        tvRejected = findViewById(R.id.tvRejected);
        tvIssued = findViewById(R.id.tvIssued);
        tvCompletionRate = findViewById(R.id.tvCompletionRate);

        tvForecastAppsNextMonth = findViewById(R.id.tvForecastAppsNextMonth);
        tvForecastItemsNextMonth = findViewById(R.id.tvForecastItemsNextMonth);
        tvForecastRecommendedOrder = findViewById(R.id.tvForecastRecommendedOrder);
        tvForecastItemsAtRisk = findViewById(R.id.tvForecastItemsAtRisk);
        tvLoadLevelBadge = findViewById(R.id.tvLoadLevelBadge);
        tvLoadLevelDetails = findViewById(R.id.tvLoadLevelDetails);

        layoutBusySlots = findViewById(R.id.layoutBusySlots);
        tvPeakMonths = findViewById(R.id.tvPeakMonths);

        pieChart = findViewById(R.id.pieChart);
        tvNoPieData = findViewById(R.id.tvNoPieData);

        barChartMonthly = findViewById(R.id.barChartMonthly);
        tvNoMonthlyData = findViewById(R.id.tvNoMonthlyData);

        tableProcurement = findViewById(R.id.tableProcurement);
        tvNoProcurementData = findViewById(R.id.tvNoProcurementData);
        btnToggleProcurementTable = findViewById(R.id.btnToggleProcurementTable);
        tvProcurementCount = findViewById(R.id.tvProcurementCount);

        lineChartForecast = findViewById(R.id.lineChartForecast);
        tvNoTimelineData = findViewById(R.id.tvNoTimelineData);

        hBarTopItems = findViewById(R.id.hBarTopItems);
        tvNoTopItemsData = findViewById(R.id.tvNoTopItemsData);
        btnToggleTopItemsChart = findViewById(R.id.btnToggleTopItemsChart);
        tvTopItemsChartCount = findViewById(R.id.tvTopItemsChartCount);

        cardModelNotes = findViewById(R.id.cardModelNotes);
        tvModelNotes = findViewById(R.id.tvModelNotes);

        spinnerTimeRange = findViewById(R.id.spinnerTimeRange);
        btnDateRange = findViewById(R.id.btnDateRange);

        tvLoadingStatus.setText(getString(R.string.stats_loading_access));
        updateRangeLabel();
        setupExpansionButtons();
    }

    private void setupExpansionButtons() {
        btnToggleProcurementTable.setOnClickListener(v -> {
            procurementTableExpanded = !procurementTableExpanded;
            updateProcurementTable(currentForecastToRender);
        });

        btnToggleTopItemsChart.setOnClickListener(v -> {
            topItemsChartExpanded = !topItemsChartExpanded;
            updateTopItemsChart(currentForecastToRender);
        });
    }

    private void setupCharts() {
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(38f);
        pieChart.setTransparentCircleRadius(43f);
        pieChart.setEntryLabelTextSize(11f);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.getLegend().setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);

        barChartMonthly.getDescription().setEnabled(false);
        barChartMonthly.setDrawGridBackground(false);
        barChartMonthly.setDrawBarShadow(false);
        barChartMonthly.setPinchZoom(true);
        barChartMonthly.setScaleEnabled(true);
        barChartMonthly.getAxisRight().setEnabled(false);
        XAxis barXAxis = barChartMonthly.getXAxis();
        barXAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        barXAxis.setGranularity(1f);
        barXAxis.setDrawGridLines(false);
        YAxis barLeft = barChartMonthly.getAxisLeft();
        barLeft.setAxisMinimum(0f);
        barLeft.setGranularity(1f);
        barLeft.setValueFormatter(integerValueFormatter);

        lineChartForecast.getDescription().setEnabled(false);
        lineChartForecast.setDrawGridBackground(false);
        lineChartForecast.getAxisRight().setEnabled(false);
        lineChartForecast.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChartForecast.getXAxis().setGranularity(1f);
        lineChartForecast.getXAxis().setDrawGridLines(false);
        lineChartForecast.getAxisLeft().setAxisMinimum(0f);
        lineChartForecast.getAxisLeft().setGranularity(1f);
        lineChartForecast.getAxisLeft().setValueFormatter(integerValueFormatter);

        hBarTopItems.getDescription().setEnabled(false);
        hBarTopItems.setDrawGridBackground(false);
        hBarTopItems.setFitBars(true);
        hBarTopItems.getAxisRight().setEnabled(false);
        hBarTopItems.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        hBarTopItems.getXAxis().setGranularity(1f);
        hBarTopItems.getAxisLeft().setAxisMinimum(0f);
        hBarTopItems.getAxisLeft().setGranularity(1f);
        hBarTopItems.getAxisLeft().setValueFormatter(integerValueFormatter);
    }

    private void setupTimeRangeSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.stats_time_ranges,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTimeRange.setAdapter(adapter);
        spinnerTimeRange.setSelection(1, false);

        spinnerTimeRange.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (suppressSpinnerListener) return;

                switch (position) {
                    case 0:
                        selectedTimeRangeMonths = 3;
                        applyRelativeDateRange(3);
                        break;
                    case 1:
                        selectedTimeRangeMonths = 6;
                        applyRelativeDateRange(6);
                        break;
                    case 2:
                        selectedTimeRangeMonths = 12;
                        applyRelativeDateRange(12);
                        break;
                    case 3:
                        selectedTimeRangeMonths = 24;
                        applyRelativeDateRange(24);
                        break;
                    case 4:
                    default:
                        selectedTimeRangeMonths = 999;
                        customRangeFrom = null;
                        customRangeTo = null;
                        break;
                }
                updateRangeLabel();
                updateAllSections();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        applyRelativeDateRange(6);
        updateRangeLabel();
    }

    private void setupDateRangeButton() {
        btnDateRange.setOnClickListener(v -> showDateRangePicker());
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
                        if (!snapshot.exists()) {
                            finish();
                            return;
                        }

                        String role = snapshot.child("role").getValue(String.class);
                        String centerName = snapshot.child("center_name").getValue(String.class);
                        String status = snapshot.child("status").getValue(String.class);
                        if (!"center".equals(role) || TextUtils.isEmpty(centerName)) {
                            finish();
                            return;
                        }

                        currentUserId = userId;
                        currentUserCenterName = centerName;
                        tvCenterName.setText(centerName);

                        if (!isApprovedCenterStatus(status)) {
                            authLoading = false;
                            showStatsAccessDenied();
                            return;
                        }

                        loadInventoryFromUserSnapshot(snapshot);
                        loadCachedForecast(centerName);

                        authLoading = false;
                        setLoadingStatus(getString(R.string.stats_loading_center_apps));
                        subscribeApplications(centerName);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        finish();
                    }
                });
    }

    private void loadInventoryFromUserSnapshot(DataSnapshot userSnapshot) {
        inventoryItems.clear();
        reservedInventoryItems.clear();
        DataSnapshot listSnapshot = userSnapshot.child("list_c");
        if (listSnapshot.exists()) {
            for (DataSnapshot itemSnapshot : listSnapshot.getChildren()) {
                String name = itemSnapshot.child("name").getValue(String.class);
                Object quantity = itemSnapshot.child("quantity").getValue();

                if (TextUtils.isEmpty(name)) {
                    String fallbackName = itemSnapshot.getKey();
                    if (!TextUtils.isEmpty(fallbackName)) {
                        name = fallbackName;
                        Object value = itemSnapshot.getValue();
                        if (value instanceof Number || value instanceof String) {
                            quantity = value;
                        } else if (value instanceof Map) {
                            Object nestedQty = ((Map<?, ?>) value).get("quantity");
                            if (nestedQty != null) quantity = nestedQty;
                        }
                    }
                }

                if (!TextUtils.isEmpty(name)) {
                    CenterLoadForecastModel.InventoryItem inventoryItem = new CenterLoadForecastModel.InventoryItem();
                    inventoryItem.name = name.trim();
                    inventoryItem.quantity = quantity;
                    inventoryItems.add(inventoryItem);
                }
            }
        }

        DataSnapshot reservedSnapshot = userSnapshot.child("reserved_items");
        for (DataSnapshot itemSnapshot : reservedSnapshot.getChildren()) {
            String name = itemSnapshot.getKey();
            if (TextUtils.isEmpty(name)) continue;
            CenterLoadForecastModel.InventoryItem reservedItem = new CenterLoadForecastModel.InventoryItem();
            reservedItem.name = name.trim();
            reservedItem.quantity = itemSnapshot.getValue();
            reservedInventoryItems.add(reservedItem);
        }
    }

    private void loadCachedForecast(String centerName) {
        String key = "forecast-cache:" + FORECAST_CACHE_VERSION + ":" + centerName;
        String json = cachePreferences.getString(key, null);
        if (json == null) {
            cachedForecast = null;
            return;
        }
        try {
            cachedForecast = gson.fromJson(json, CenterLoadForecastModel.CenterLoadForecast.class);
        } catch (Exception ignored) {
            cachedForecast = null;
        }
    }

    private void saveCachedForecast(CenterLoadForecastModel.CenterLoadForecast forecast) {
        if (TextUtils.isEmpty(currentUserCenterName) || forecast == null) return;
        String key = "forecast-cache:" + FORECAST_CACHE_VERSION + ":" + currentUserCenterName;
        cachePreferences.edit().putString(key, gson.toJson(forecast)).apply();
    }

    private void subscribeApplications(String centerName) {
        if (appsQuery != null && appsListener != null) {
            appsQuery.removeEventListener(appsListener);
        }

        appsQuery = mDatabase.child("Applications");
        appsLoading = true;
        setLoadingStatus(getString(R.string.stats_loading_center_apps));
        updateWarmStartHint();

        appsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                applications.clear();
                for (DataSnapshot appSnapshot : snapshot.getChildren()) {
                    if (!applicationBelongsToCurrentCenter(appSnapshot, centerName)) {
                        continue;
                    }
                    ApplicationRecord record = parseApplicationRecord(appSnapshot);
                    if (record != null) {
                        applications.add(record);
                    }
                }

                hasLoadedApps = true;
                appsLoading = false;
                setLoadingStatus(getString(R.string.stats_loading_forecast));
                updateAllSections();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                appsLoading = false;
                setLoadingStatus(getString(R.string.stats_loading_ready));
                updateAllSections();
            }
        };

        appsQuery.addValueEventListener(appsListener);
    }

    private boolean applicationBelongsToCurrentCenter(DataSnapshot appSnapshot, String centerName) {
        String appCenterId = appSnapshot.child("centerId").getValue(String.class);
        if (!TextUtils.isEmpty(appCenterId) && appCenterId.equals(currentUserId)) {
            return true;
        }

        String appCenterName = appSnapshot.child("center").getValue(String.class);
        return normalizeCenterName(appCenterName).equals(normalizeCenterName(centerName));
    }

    private String normalizeCenterName(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private ApplicationRecord parseApplicationRecord(DataSnapshot appSnapshot) {
        String dateRaw = appSnapshot.child("date").getValue(String.class);
        if (TextUtils.isEmpty(dateRaw)) return null;

        Calendar date = parseDate(dateRaw);
        if (date == null) return null;

        ApplicationRecord record = new ApplicationRecord();
        record.dateRaw = dateRaw;
        record.timeRaw = appSnapshot.child("time").getValue(String.class);
        record.status = appSnapshot.child("status").getValue(String.class);
        record.date = date;

        Map<String, Integer> selectedItems = new HashMap<>();
        DataSnapshot itemsSnapshot = appSnapshot.child("selected_items");
        if (itemsSnapshot.exists()) {
            for (DataSnapshot itemSnapshot : itemsSnapshot.getChildren()) {
                String itemName = itemSnapshot.getKey();
                if (TextUtils.isEmpty(itemName)) continue;

                int qty = toRoundedNonNegativeNumber(itemSnapshot.getValue());
                if (qty <= 0) continue;

                String normalizedName = itemName.trim();
                selectedItems.put(normalizedName, selectedItems.getOrDefault(normalizedName, 0) + qty);
            }
        }
        record.selectedItems = selectedItems;
        return record;
    }

    private void updateAllSections() {
        if (authLoading) return;

        List<ApplicationRecord> filtered = getFilteredApplications();
        updateStatisticsCards(filtered);
        updateStatusPieChart(filtered);
        updateMonthlyDynamicsChart(filtered);

        CenterLoadForecastModel.CenterLoadForecast liveForecast = buildForecast(applications);
        CenterLoadForecastModel.CenterLoadForecast forecastToRender = hasLoadedApps
                ? liveForecast
                : (cachedForecast != null ? cachedForecast : liveForecast);
        currentForecastToRender = forecastToRender;

        if (hasLoadedApps) {
            saveCachedForecast(liveForecast);
            cachedForecast = liveForecast;
            setLoadingStatus(getString(R.string.stats_loading_ready));
        }

        updateForecastSummaryCards(forecastToRender);
        updateBusySlotsSection(forecastToRender);
        updateProcurementTable(forecastToRender);
        updateTimelineChart(forecastToRender);
        updateTopItemsChart(forecastToRender);
        updateModelNotes(forecastToRender);
        updateWarmStartHint();
    }

    private CenterLoadForecastModel.CenterLoadForecast buildForecast(List<ApplicationRecord> sourceApplications) {
        List<CenterLoadForecastModel.ApplicationData> source = new ArrayList<>();
        for (ApplicationRecord record : sourceApplications) {
            CenterLoadForecastModel.ApplicationData item = new CenterLoadForecastModel.ApplicationData();
            item.date = record.dateRaw;
            item.time = record.timeRaw;
            item.status = record.status;
            item.selectedItems = record.selectedItems;
            source.add(item);
        }

        CenterLoadForecastModel.ForecastOptions options = new CenterLoadForecastModel.ForecastOptions();
        options.horizonMonths = 6;
        options.topItemsCount = 500;
        options.currentInventory = inventoryItems;
        options.reservedInventory = reservedInventoryItems;
        options.asOfDate = new java.util.Date();
        return CenterLoadForecastModel.buildCenterLoadForecast(source, options);
    }

    private void updateStatisticsCards(List<ApplicationRecord> filtered) {
        int total = filtered.size();
        int reviewing = 0;
        int approved = 0;
        int rejected = 0;
        int issued = 0;

        for (ApplicationRecord record : filtered) {
            StatusGroup group = resolveStatus(record.status);
            if (group == StatusGroup.reviewing) reviewing++;
            else if (group == StatusGroup.approved) approved++;
            else if (group == StatusGroup.rejected) rejected++;
            else if (group == StatusGroup.issued) issued++;
        }

        double completionRate = total > 0 ? (issued * 100.0 / total) : 0;

        tvTotal.setText(String.valueOf(total));
        tvReviewing.setText(String.valueOf(reviewing));
        tvApproved.setText(String.valueOf(approved));
        tvRejected.setText(String.valueOf(rejected));
        tvIssued.setText(String.valueOf(issued));
        tvCompletionRate.setText(getString(R.string.stats_completion_format, completionRate));
    }

    private void updateStatusPieChart(List<ApplicationRecord> filtered) {
        int reviewing = 0;
        int approved = 0;
        int rejected = 0;
        int issued = 0;

        for (ApplicationRecord record : filtered) {
            StatusGroup group = resolveStatus(record.status);
            if (group == StatusGroup.reviewing) reviewing++;
            else if (group == StatusGroup.approved) approved++;
            else if (group == StatusGroup.rejected) rejected++;
            else if (group == StatusGroup.issued) issued++;
        }

        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        if (reviewing > 0) {
            entries.add(new PieEntry(reviewing, getString(R.string.status_pending)));
            colors.add(Color.parseColor("#eab308"));
        }
        if (approved > 0) {
            entries.add(new PieEntry(approved, getString(R.string.status_approved)));
            colors.add(Color.parseColor("#3b82f6"));
        }
        if (rejected > 0) {
            entries.add(new PieEntry(rejected, getString(R.string.status_rejected)));
            colors.add(Color.parseColor("#ef4444"));
        }
        if (issued > 0) {
            entries.add(new PieEntry(issued, getString(R.string.status_issued)));
            colors.add(Color.parseColor("#22c55e"));
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
        dataSet.setValueTextSize(11f);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf(Math.round(value));
            }
        });

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.invalidate();
    }

    private void updateMonthlyDynamicsChart(List<ApplicationRecord> filtered) {
        Map<Integer, MonthlyData> monthMap = new HashMap<>();

        for (ApplicationRecord record : filtered) {
            int monthIndex = toMonthIndex(record.date);
            MonthlyData data = monthMap.get(monthIndex);
            if (data == null) {
                data = new MonthlyData();
                monthMap.put(monthIndex, data);
            }

            data.total++;
            if (resolveStatus(record.status) == StatusGroup.issued) {
                data.issued++;
            }
        }

        if (monthMap.isEmpty()) {
            barChartMonthly.setVisibility(View.GONE);
            tvNoMonthlyData.setVisibility(View.VISIBLE);
            return;
        }

        List<Integer> keys = new ArrayList<>(monthMap.keySet());
        Collections.sort(keys);

        if (selectedTimeRangeMonths != 999 && keys.size() > selectedTimeRangeMonths) {
            keys = keys.subList(keys.size() - selectedTimeRangeMonths, keys.size());
        }

        List<BarEntry> totalEntries = new ArrayList<>();
        List<BarEntry> issuedEntries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (int i = 0; i < keys.size(); i++) {
            int monthIndex = keys.get(i);
            MonthlyData data = monthMap.get(monthIndex);
            totalEntries.add(new BarEntry(i, data.total));
            issuedEntries.add(new BarEntry(i, data.issued));
            labels.add(monthLabel(monthIndex));
        }

        if (totalEntries.isEmpty()) {
            barChartMonthly.setVisibility(View.GONE);
            tvNoMonthlyData.setVisibility(View.VISIBLE);
            return;
        }

        barChartMonthly.setVisibility(View.VISIBLE);
        tvNoMonthlyData.setVisibility(View.GONE);

        BarDataSet totalSet = new BarDataSet(totalEntries, getString(R.string.stats_dataset_total));
        totalSet.setColor(Color.parseColor("#3b82f6"));
        totalSet.setValueTextSize(10f);
        totalSet.setValueFormatter(integerValueFormatter);

        BarDataSet issuedSet = new BarDataSet(issuedEntries, getString(R.string.stats_dataset_issued));
        issuedSet.setColor(Color.parseColor("#22c55e"));
        issuedSet.setValueTextSize(10f);
        issuedSet.setValueFormatter(integerValueFormatter);

        BarData barData = new BarData(totalSet, issuedSet);
        barData.setBarWidth(0.38f);

        XAxis xAxis = barChartMonthly.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setGranularity(1f);

        barChartMonthly.setData(barData);
        barChartMonthly.setFitBars(true);
        barChartMonthly.invalidate();
    }

    private void updateForecastSummaryCards(CenterLoadForecastModel.CenterLoadForecast forecast) {
        if (forecast == null) return;

        CenterLoadForecastModel.LoadLevel displayLoadLevel = computeDisplayLoadLevel(forecast);
        LoadBadge badge = getLoadBadge(displayLoadLevel);

        tvForecastAppsNextMonth.setText(String.valueOf(forecast.nextMonthExpectedApplications));
        tvForecastItemsNextMonth.setText(String.valueOf(forecast.nextMonthExpectedItems));
        tvForecastRecommendedOrder.setText(getString(R.string.stats_recommended_prepare_format, forecast.totalRecommendedOrder));
        tvForecastItemsAtRisk.setText(getString(R.string.stats_items_at_risk_format, forecast.itemsAtRisk));

        tvLoadLevelBadge.setText(badge.label);
        tvLoadLevelBadge.setBackgroundColor(badge.backgroundColor);
        tvLoadLevelBadge.setTextColor(badge.textColor);
        tvLoadLevelDetails.setText(getString(
                R.string.stats_load_details_format,
                forecast.nextMonthExpectedItems,
                forecast.totalRecommendedOrder,
                forecast.itemsAtRisk
        ));
    }

    private void updateBusySlotsSection(CenterLoadForecastModel.CenterLoadForecast forecast) {
        layoutBusySlots.removeAllViews();

        if (forecast == null || forecast.busyTimeSlots == null || forecast.busyTimeSlots.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText(getString(R.string.stats_busy_slots_empty));
            empty.setTextSize(12f);
            empty.setTextColor(Color.parseColor("#6b7280"));
            layoutBusySlots.addView(empty);
        } else {
            for (CenterLoadForecastModel.BusyTimeSlot slot : forecast.busyTimeSlots) {
                TextView chip = new TextView(this);
                chip.setText(getString(R.string.stats_busy_slot_line, slot.slot, slot.expectedVisits, slot.sharePercent));
                chip.setTextSize(12f);
                chip.setTextColor(Color.parseColor("#374151"));
                chip.setPadding(12, 8, 12, 8);
                chip.setBackgroundColor(Color.parseColor("#eef2ff"));
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.bottomMargin = 8;
                chip.setLayoutParams(params);
                layoutBusySlots.addView(chip);
            }
        }

        String peakLoad = TextUtils.isEmpty(forecast == null ? null : forecast.peakLoadMonth) ? "—" : forecast.peakLoadMonth;
        String peakItems = TextUtils.isEmpty(forecast == null ? null : forecast.peakItemsMonth) ? "—" : forecast.peakItemsMonth;
        tvPeakMonths.setText(getString(R.string.stats_peaks_format, peakLoad, peakItems));
    }

    private void updateProcurementTable(CenterLoadForecastModel.CenterLoadForecast forecast) {
        tableProcurement.removeAllViews();

        if (forecast == null || forecast.itemProcurementPlan == null || forecast.itemProcurementPlan.isEmpty()) {
            tvNoProcurementData.setVisibility(View.VISIBLE);
            btnToggleProcurementTable.setVisibility(View.GONE);
            tvProcurementCount.setVisibility(View.GONE);
            return;
        }
        tvNoProcurementData.setVisibility(View.GONE);
        btnToggleProcurementTable.setVisibility(View.VISIBLE);
        tvProcurementCount.setVisibility(View.VISIBLE);

        addProcurementHeaderRow();

        List<CenterLoadForecastModel.ItemProcurementPlan> rows = new ArrayList<>(forecast.itemProcurementPlan);
        rows.sort(new Comparator<CenterLoadForecastModel.ItemProcurementPlan>() {
            @Override
            public int compare(CenterLoadForecastModel.ItemProcurementPlan a, CenterLoadForecastModel.ItemProcurementPlan b) {
                return a.item.compareToIgnoreCase(b.item);
            }
        });

        int visibleRows = procurementTableExpanded ? rows.size() : Math.min(COMPACT_ITEMS_LIMIT, rows.size());
        updateTableToggleButton(visibleRows, rows.size());

        for (int index = 0; index < visibleRows; index++) {
            CenterLoadForecastModel.ItemProcurementPlan row = rows.get(index);
            TableRow tableRow = new TableRow(this);
            tableRow.setPadding(0, 6, 0, 6);

            tableRow.addView(createTableCell(row.item, false, Gravity.START));
            tableRow.addView(createTableCell(String.valueOf(row.currentStock), false, Gravity.END));
            tableRow.addView(createTableCell(String.valueOf(row.reservedStock), false, Gravity.END));
            tableRow.addView(createTableCell(String.valueOf(row.nextMonthDemand), false, Gravity.END));
            tableRow.addView(createTableCell(String.valueOf(row.threeMonthDemand), false, Gravity.END));
            tableRow.addView(createTableCell(String.valueOf(row.recommendedOrder), true, Gravity.END));

            TextView riskCell = createTableCell(getRiskLabel(row.riskLevel), true, Gravity.CENTER);
            riskCell.setTextColor(getRiskColor(row.riskLevel));
            tableRow.addView(riskCell);

            tableProcurement.addView(tableRow);
        }
    }

    private void updateTableToggleButton(int visibleRows, int totalRows) {
        tvProcurementCount.setText(getString(R.string.stats_items_visible_count, visibleRows, totalRows));

        if (totalRows <= COMPACT_ITEMS_LIMIT) {
            btnToggleProcurementTable.setEnabled(false);
            btnToggleProcurementTable.setText(getString(R.string.stats_all_items_visible));
            return;
        }

        btnToggleProcurementTable.setEnabled(true);
        btnToggleProcurementTable.setText(procurementTableExpanded
                ? getString(R.string.stats_show_less_items)
                : getString(R.string.stats_show_all_items, totalRows));
    }

    private void addProcurementHeaderRow() {
        TableRow header = new TableRow(this);
        header.setPadding(0, 8, 0, 8);
        header.setBackgroundColor(Color.parseColor("#f1f5f9"));

        header.addView(createTableHeaderCell(getString(R.string.stats_table_item), Gravity.START));
        header.addView(createTableHeaderCell(getString(R.string.stats_table_stock), Gravity.END));
        header.addView(createTableHeaderCell(getString(R.string.stats_table_reserved), Gravity.END));
        header.addView(createTableHeaderCell(getString(R.string.stats_table_need_1m), Gravity.END));
        header.addView(createTableHeaderCell(getString(R.string.stats_table_need_3m), Gravity.END));
        header.addView(createTableHeaderCell(getString(R.string.stats_table_reorder), Gravity.END));
        header.addView(createTableHeaderCell(getString(R.string.stats_table_risk), Gravity.CENTER));

        tableProcurement.addView(header);
    }

    private TextView createTableHeaderCell(String text, int gravity) {
        TextView view = createTableCell(text, true, gravity);
        view.setTextSize(11f);
        view.setTextColor(Color.parseColor("#334155"));
        return view;
    }

    private TextView createTableCell(String text, boolean bold, int gravity) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(11f);
        view.setTextColor(Color.parseColor("#111827"));
        view.setPadding(10, 0, 10, 0);
        view.setGravity(gravity);
        if (bold) {
            view.setTypeface(view.getTypeface(), android.graphics.Typeface.BOLD);
        }
        return view;
    }

    private void updateTimelineChart(CenterLoadForecastModel.CenterLoadForecast forecast) {
        if (forecast == null || forecast.timeline == null || forecast.timeline.isEmpty()) {
            lineChartForecast.setVisibility(View.GONE);
            tvNoTimelineData.setVisibility(View.VISIBLE);
            return;
        }

        List<Entry> expectedEntries = new ArrayList<>();
        List<Entry> committedEntries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (int i = 0; i < forecast.timeline.size(); i++) {
            CenterLoadForecastModel.ForecastTimelinePoint point = forecast.timeline.get(i);
            labels.add(point.month);
            expectedEntries.add(new Entry(i, point.expectedApplications));
            committedEntries.add(new Entry(i, point.committedApplications));
        }

        LineDataSet expectedSet = new LineDataSet(expectedEntries, getString(R.string.stats_timeline_expected_apps));
        expectedSet.setColor(Color.parseColor("#06b6d4"));
        expectedSet.setCircleColor(Color.parseColor("#06b6d4"));
        expectedSet.setLineWidth(2f);
        expectedSet.setCircleRadius(3f);
        expectedSet.setValueTextSize(9f);
        expectedSet.setValueFormatter(integerValueFormatter);

        LineDataSet committedSet = new LineDataSet(committedEntries, getString(R.string.stats_timeline_committed_apps));
        committedSet.setColor(Color.parseColor("#f97316"));
        committedSet.setCircleColor(Color.parseColor("#f97316"));
        committedSet.setLineWidth(2f);
        committedSet.setCircleRadius(3f);
        committedSet.setValueTextSize(9f);
        committedSet.enableDashedLine(10f, 8f, 0f);
        committedSet.setValueFormatter(integerValueFormatter);

        LineData lineData = new LineData(expectedSet, committedSet);
        lineChartForecast.setData(lineData);

        XAxis xAxis = lineChartForecast.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(-35f);

        lineChartForecast.setVisibility(View.VISIBLE);
        tvNoTimelineData.setVisibility(View.GONE);
        lineChartForecast.invalidate();
    }

    private void updateTopItemsChart(CenterLoadForecastModel.CenterLoadForecast forecast) {
        if (forecast == null || forecast.itemProcurementPlan == null || forecast.itemProcurementPlan.isEmpty()) {
            hBarTopItems.setVisibility(View.GONE);
            tvNoTopItemsData.setVisibility(View.VISIBLE);
            btnToggleTopItemsChart.setVisibility(View.GONE);
            tvTopItemsChartCount.setVisibility(View.GONE);
            return;
        }
        btnToggleTopItemsChart.setVisibility(View.VISIBLE);
        tvTopItemsChartCount.setVisibility(View.VISIBLE);

        List<CenterLoadForecastModel.ItemProcurementPlan> rows = new ArrayList<>(forecast.itemProcurementPlan);
        rows.sort(new Comparator<CenterLoadForecastModel.ItemProcurementPlan>() {
            @Override
            public int compare(CenterLoadForecastModel.ItemProcurementPlan a, CenterLoadForecastModel.ItemProcurementPlan b) {
                if (a.recommendedOrder != b.recommendedOrder) {
                    return Integer.compare(b.recommendedOrder, a.recommendedOrder);
                }
                if (a.nextMonthDemand != b.nextMonthDemand) {
                    return Integer.compare(b.nextMonthDemand, a.nextMonthDemand);
                }
                return a.item.compareToIgnoreCase(b.item);
            }
        });

        int visibleRows = topItemsChartExpanded ? rows.size() : Math.min(COMPACT_ITEMS_LIMIT, rows.size());
        updateTopItemsToggleButton(visibleRows, rows.size());
        rows = new ArrayList<>(rows.subList(0, visibleRows));

        List<BarEntry> demandEntries = new ArrayList<>();
        List<BarEntry> reorderEntries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (int i = 0; i < rows.size(); i++) {
            CenterLoadForecastModel.ItemProcurementPlan row = rows.get(i);
            labels.add(row.item);
            demandEntries.add(new BarEntry(i, row.nextMonthDemand));
            reorderEntries.add(new BarEntry(i, row.recommendedOrder));
        }

        BarDataSet demandSet = new BarDataSet(demandEntries, getString(R.string.stats_chart_demand));
        demandSet.setColor(Color.parseColor("#8b5cf6"));
        demandSet.setValueTextSize(8f);
        demandSet.setValueFormatter(integerValueFormatter);

        BarDataSet reorderSet = new BarDataSet(reorderEntries, getString(R.string.stats_chart_reorder));
        reorderSet.setColor(Color.parseColor("#f59e0b"));
        reorderSet.setValueTextSize(8f);
        reorderSet.setValueFormatter(integerValueFormatter);

        BarData data = new BarData(demandSet, reorderSet);
        float groupSpace = 0.34f;
        float barSpace = 0.04f;
        float barWidth = 0.29f;
        data.setBarWidth(barWidth);

        setTopItemsChartHeight(rows.size());
        hBarTopItems.setData(data);
        hBarTopItems.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        hBarTopItems.getXAxis().setGranularity(1f);
        hBarTopItems.getXAxis().setLabelRotationAngle(0f);
        hBarTopItems.getXAxis().setTextSize(10f);

        float groupWidth = data.getGroupWidth(groupSpace, barSpace);
        hBarTopItems.getXAxis().setAxisMinimum(0f);
        hBarTopItems.getXAxis().setAxisMaximum(groupWidth * labels.size());
        hBarTopItems.getAxisLeft().setValueFormatter(integerValueFormatter);
        hBarTopItems.groupBars(0f, groupSpace, barSpace);

        hBarTopItems.setVisibility(View.VISIBLE);
        tvNoTopItemsData.setVisibility(View.GONE);
        hBarTopItems.invalidate();
    }

    private void updateTopItemsToggleButton(int visibleRows, int totalRows) {
        tvTopItemsChartCount.setText(getString(R.string.stats_items_visible_count, visibleRows, totalRows));

        if (totalRows <= COMPACT_ITEMS_LIMIT) {
            btnToggleTopItemsChart.setEnabled(false);
            btnToggleTopItemsChart.setText(getString(R.string.stats_full_chart_visible));
            return;
        }

        btnToggleTopItemsChart.setEnabled(true);
        btnToggleTopItemsChart.setText(topItemsChartExpanded
                ? getString(R.string.stats_collapse_chart)
                : getString(R.string.stats_expand_chart, totalRows));
    }

    private void setTopItemsChartHeight(int rowCount) {
        int heightDp = Math.max(340, 90 + rowCount * 42);
        ViewGroup.LayoutParams params = hBarTopItems.getLayoutParams();
        params.height = dpToPx(heightDp);
        hBarTopItems.setLayoutParams(params);
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private void updateModelNotes(CenterLoadForecastModel.CenterLoadForecast forecast) {
        if (cardModelNotes != null) {
            cardModelNotes.setVisibility(View.GONE);
        }
    }

    private void updateWarmStartHint() {
        if (!hasLoadedApps && cachedForecast != null) {
            tvWarmStartHint.setText(getString(R.string.stats_warm_start_hint));
        } else {
            tvWarmStartHint.setText(getString(R.string.stats_forecast_updated_center));
        }
    }

    private CenterLoadForecastModel.LoadLevel computeDisplayLoadLevel(CenterLoadForecastModel.CenterLoadForecast forecast) {
        int nextItems = forecast.nextMonthExpectedItems;
        int reorder = forecast.totalRecommendedOrder;
        int atRisk = forecast.itemsAtRisk;
        int totalItemGroups = Math.max(forecast.itemProcurementPlan == null ? 0 : forecast.itemProcurementPlan.size(), 1);
        double riskShare = atRisk / (double) totalItemGroups;

        if (nextItems <= 0 && reorder <= 0 && atRisk <= 0) return CenterLoadForecastModel.LoadLevel.low;

        int score = 0;
        if (nextItems >= 20) score++;
        if (nextItems >= 60) score++;
        if (reorder >= 10) score++;
        if (reorder >= 40) score++;
        if (atRisk >= 1) score++;
        if (riskShare >= 0.25 || atRisk >= 5) score++;
        if (forecast.nextMonthExpectedApplications >= 25) score++;

        if (score >= 6) return CenterLoadForecastModel.LoadLevel.critical;
        if (score >= 4) return CenterLoadForecastModel.LoadLevel.high;
        if (score >= 2) return CenterLoadForecastModel.LoadLevel.medium;
        return CenterLoadForecastModel.LoadLevel.low;
    }

    private LoadBadge getLoadBadge(CenterLoadForecastModel.LoadLevel level) {
        if (level == CenterLoadForecastModel.LoadLevel.critical) {
            return new LoadBadge(getString(R.string.stats_load_critical_label), Color.parseColor("#dc2626"), Color.WHITE);
        }
        if (level == CenterLoadForecastModel.LoadLevel.high) {
            return new LoadBadge(getString(R.string.stats_load_high_label), Color.parseColor("#f97316"), Color.WHITE);
        }
        if (level == CenterLoadForecastModel.LoadLevel.medium) {
            return new LoadBadge(getString(R.string.stats_load_medium_label), Color.parseColor("#eab308"), Color.BLACK);
        }
        return new LoadBadge(getString(R.string.stats_load_low_label), Color.parseColor("#059669"), Color.WHITE);
    }

    private String getRiskLabel(CenterLoadForecastModel.StockRiskLevel level) {
        if (level == CenterLoadForecastModel.StockRiskLevel.critical) return getString(R.string.stats_risk_critical);
        if (level == CenterLoadForecastModel.StockRiskLevel.watch) return getString(R.string.stats_risk_watch);
        return getString(R.string.stats_risk_ok);
    }

    private int getRiskColor(CenterLoadForecastModel.StockRiskLevel level) {
        if (level == CenterLoadForecastModel.StockRiskLevel.critical) return Color.parseColor("#dc2626");
        if (level == CenterLoadForecastModel.StockRiskLevel.watch) return Color.parseColor("#d97706");
        return Color.parseColor("#059669");
    }

    private void showDateRangePicker() {
        MaterialDatePicker.Builder<Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker();
        builder.setTitleText(getString(R.string.stats_choose_period));

        if (customRangeFrom != null && customRangeTo != null) {
            builder.setSelection(Pair.create(customRangeFrom.getTimeInMillis(), customRangeTo.getTimeInMillis()));
        }

        MaterialDatePicker<Pair<Long, Long>> picker = builder.build();
        picker.addOnPositiveButtonClickListener(selection -> {
            if (selection == null || selection.first == null || selection.second == null) return;

            Calendar newFrom = Calendar.getInstance();
            newFrom.setTimeInMillis(selection.first);
            zeroTime(newFrom, true);

            Calendar newTo = Calendar.getInstance();
            newTo.setTimeInMillis(selection.second);
            zeroTime(newTo, false);

            boolean sameAsCurrent = customRangeFrom != null
                    && customRangeTo != null
                    && customRangeFrom.getTimeInMillis() == newFrom.getTimeInMillis()
                    && customRangeTo.getTimeInMillis() == newTo.getTimeInMillis();

            if (sameAsCurrent) {
                customRangeFrom = null;
                customRangeTo = null;
            } else {
                customRangeFrom = newFrom;
                customRangeTo = newTo;
            }

            selectedTimeRangeMonths = 999;
            suppressSpinnerListener = true;
            spinnerTimeRange.setSelection(4, false);
            suppressSpinnerListener = false;

            updateRangeLabel();
            updateAllSections();
        });

        picker.show(getSupportFragmentManager(), "stats_date_range");
    }

    private void applyRelativeDateRange(int months) {
        Calendar now = Calendar.getInstance();
        Calendar from = (Calendar) now.clone();
        from.add(Calendar.MONTH, -months);
        zeroTime(from, true);
        zeroTime(now, false);
        customRangeFrom = from;
        customRangeTo = now;
    }

    private void updateRangeLabel() {
        if (customRangeFrom == null && customRangeTo == null) {
            tvDateRange.setText(getString(R.string.stats_all_time_active));
            return;
        }

        if (customRangeFrom != null && customRangeTo != null) {
            String from = uiDateFormat.format(customRangeFrom.getTime());
            String to = uiDateFormat.format(customRangeTo.getTime());
            tvDateRange.setText(getString(R.string.stats_date_range_format, from, to));
            return;
        }

        if (customRangeFrom != null) {
            tvDateRange.setText(getString(R.string.stats_date_single_format, uiDateFormat.format(customRangeFrom.getTime())));
            return;
        }

        tvDateRange.setText(getString(R.string.stats_all_time_active));
    }

    private List<ApplicationRecord> getFilteredApplications() {
        if (customRangeFrom == null && customRangeTo == null) {
            return new ArrayList<>(applications);
        }

        List<ApplicationRecord> filtered = new ArrayList<>();
        for (ApplicationRecord record : applications) {
            if (record.date == null) continue;
            if (customRangeFrom != null && record.date.before(customRangeFrom)) continue;
            if (customRangeTo != null && record.date.after(customRangeTo)) continue;
            filtered.add(record);
        }
        return filtered;
    }

    private StatusGroup resolveStatus(String rawStatus) {
        String status = rawStatus == null ? "" : rawStatus.trim();
        String lower = status.toLowerCase(Locale.ROOT);

        if (lower.contains("рассматри") || lower.contains("under review") || lower.equals("pending") || "Р Р°СЃСЃРјР°С‚СЂРёРІР°РµС‚СЃСЏ".equals(status)) {
            return StatusGroup.reviewing;
        }
        if (lower.contains("одобрен") || lower.equals("approved") || "РћРґРѕР±СЂРµРЅРѕ".equals(status)) {
            return StatusGroup.approved;
        }
        if (lower.contains("отклон") || lower.equals("rejected") || "РћС‚РєР»РѕРЅРµРЅРѕ".equals(status)) {
            return StatusGroup.rejected;
        }
        if (lower.contains("выдан") || lower.equals("issued") || "Р’С‹РґР°РЅРѕ".equals(status)) {
            return StatusGroup.issued;
        }
        return StatusGroup.other;
    }

    private Calendar parseDate(String rawDate) {
        if (rawDate == null) return null;
        String value = rawDate.trim();
        if (value.isEmpty()) return null;

        String[] parts = value.split("[./-]");
        if (parts.length < 3) return null;

        try {
            int day = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            int year = Integer.parseInt(parts[2]);
            if (year < 100) year += 2000;

            Calendar calendar = Calendar.getInstance();
            calendar.setLenient(false);
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month - 1);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            zeroTime(calendar, true);
            calendar.getTime();
            return calendar;
        } catch (Exception ignored) {
            return null;
        }
    }

    private void zeroTime(Calendar calendar, boolean startOfDay) {
        calendar.set(Calendar.HOUR_OF_DAY, startOfDay ? 0 : 23);
        calendar.set(Calendar.MINUTE, startOfDay ? 0 : 59);
        calendar.set(Calendar.SECOND, startOfDay ? 0 : 59);
        calendar.set(Calendar.MILLISECOND, startOfDay ? 0 : 999);
    }

    private int toMonthIndex(Calendar date) {
        return date.get(Calendar.YEAR) * 12 + date.get(Calendar.MONTH);
    }

    private String monthLabel(int monthIndex) {
        int year = monthIndex / 12;
        int month = (monthIndex % 12) + 1;
        return String.format(Locale.getDefault(), "%02d/%d", month, year);
    }

    private int toRoundedNonNegativeNumber(Object rawValue) {
        if (rawValue == null) return 0;

        if (rawValue instanceof Number) {
            double value = ((Number) rawValue).doubleValue();
            return value > 0 ? (int) Math.round(value) : 0;
        }

        if (rawValue instanceof String) {
            String text = ((String) rawValue).trim().replace(",", ".");
            if (text.isEmpty()) return 0;
            try {
                double value = Double.parseDouble(text);
                return value > 0 ? (int) Math.round(value) : 0;
            } catch (NumberFormatException ignored) {
                return 0;
            }
        }

        return 0;
    }

    private void setLoadingStatus(String text) {
        tvLoadingStatus.setText(text);
    }

    private boolean isApprovedCenterStatus(String rawStatus) {
        if (rawStatus == null) return false;
        String status = rawStatus.trim().toLowerCase(Locale.ROOT);
        return status.contains("одобрен") || status.equals("approved");
    }

    private void showStatsAccessDenied() {
        applications.clear();
        inventoryItems.clear();
        reservedInventoryItems.clear();
        hasLoadedApps = false;
        setLoadingStatus(getString(R.string.stats_loading_ready));
        tvWarmStartHint.setText(getString(R.string.stats_access_denied_center_pending));
        tvAccessMessage.setVisibility(View.VISIBLE);
        tvAccessMessage.setText(getString(R.string.stats_access_denied_center_pending));
        spinnerTimeRange.setEnabled(false);
        btnDateRange.setEnabled(false);
        tvDateRange.setText(getString(R.string.stats_access_denied_center_pending));
        updateStatisticsCards(new ArrayList<>());
        updateStatusPieChart(new ArrayList<>());
        updateMonthlyDynamicsChart(new ArrayList<>());
        updateForecastSummaryCards(CenterLoadForecastModel.buildCenterLoadForecast(
                new ArrayList<>(),
                new CenterLoadForecastModel.ForecastOptions()
        ));
        updateBusySlotsSection(null);
        updateProcurementTable(null);
        updateTimelineChart(null);
        updateTopItemsChart(null);
        updateModelNotes(null);
    }

    private static class ApplicationRecord {
        String dateRaw;
        String timeRaw;
        String status;
        Calendar date;
        Map<String, Integer> selectedItems = new HashMap<>();
    }

    private static class MonthlyData {
        int total = 0;
        int issued = 0;
    }

    private static class LoadBadge {
        final String label;
        final int backgroundColor;
        final int textColor;

        LoadBadge(String label, int backgroundColor, int textColor) {
            this.label = label;
            this.backgroundColor = backgroundColor;
            this.textColor = textColor;
        }
    }
}
