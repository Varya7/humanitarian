package com.example.hum1;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class CenterLoadForecastModel {

    private CenterLoadForecastModel() {
    }

    public enum LoadLevel {
        low, medium, high, critical
    }

    public enum ItemTrend {
        up, stable, down
    }

    public enum StockRiskLevel {
        ok, watch, critical
    }

    private enum DemandPattern {
        smooth, intermittent, erratic, lumpy
    }

    private enum ForecastMethod {
        holt_seasonal, seasonal_naive, moving_average, croston_sba
    }

    public static class InventoryItem {
        public String name;
        public Object quantity;
    }

    public static class ApplicationData {
        public String date;
        public String time;
        public String status;
        public Map<String, Integer> selectedItems = new HashMap<>();
    }

    public static class ForecastTimelinePoint {
        public String month;
        public int predictedApplications;
        public int committedApplications;
        public int expectedApplications;
        public int predictedItems;
        public int committedItems;
        public int expectedItems;
    }

    public static class ItemDemandPrediction {
        public String item;
        public int expectedQuantity;
        public int committedQuantity;
        public ItemTrend trend;
    }

    public static class ItemProcurementPlan {
        public String item;
        public int currentStock;
        public int reservedStock;
        public int nextMonthDemand;
        public int threeMonthDemand;
        public int safetyStock;
        public int recommendedOrder;
        public double coverageMonths;
        public StockRiskLevel riskLevel;
    }

    public static class BusyTimeSlot {
        public String slot;
        public int sharePercent;
        public int expectedVisits;
    }

    public static class CenterLoadForecast {
        public int nextMonthExpectedApplications;
        public int nextMonthExpectedItems;
        public int pendingApplications;
        public double capacityBaseline;
        public double loadIndex;
        public LoadLevel loadLevel;
        public int confidence;
        public int historyMonths;
        public int syntheticMonths;
        public List<ForecastTimelinePoint> timeline = new ArrayList<>();
        public List<ItemDemandPrediction> topItems = new ArrayList<>();
        public List<ItemProcurementPlan> itemProcurementPlan = new ArrayList<>();
        public List<BusyTimeSlot> busyTimeSlots = new ArrayList<>();
        public int totalRecommendedOrder;
        public int itemsAtRisk;
        public String peakLoadMonth = "";
        public String peakItemsMonth = "";
        public List<String> modelNotes = new ArrayList<>();
    }

    public static class ForecastOptions {
        public int horizonMonths = 3;
        public int topItemsCount = 6;
        public List<InventoryItem> currentInventory = new ArrayList<>();
        public List<InventoryItem> reservedInventory = new ArrayList<>();
        public Date asOfDate = new Date();
    }

    private static class ForecastSeriesResult {
        final List<Double> values;
        final int syntheticMonths;

        ForecastSeriesResult(List<Double> values, int syntheticMonths) {
            this.values = values;
            this.syntheticMonths = syntheticMonths;
        }
    }

    private static class ForecastResult {
        final List<Double> prediction;
        final int syntheticMonths;
        final double volatility;
        final ForecastMethod method;

        ForecastResult(List<Double> prediction, int syntheticMonths, double volatility, ForecastMethod method) {
            this.prediction = prediction;
            this.syntheticMonths = syntheticMonths;
            this.volatility = volatility;
            this.method = method;
        }
    }

    private static class ItemForecastSnapshot {
        final String item;
        final int nextExpected;
        final int threeMonthExpected;
        final int committedNextMonth;
        final ItemTrend trend;
        final ForecastMethod method;
        final DemandPattern pattern;

        ItemForecastSnapshot(String item, int nextExpected, int threeMonthExpected, int committedNextMonth, ItemTrend trend, ForecastMethod method, DemandPattern pattern) {
            this.item = item;
            this.nextExpected = nextExpected;
            this.threeMonthExpected = threeMonthExpected;
            this.committedNextMonth = committedNextMonth;
            this.trend = trend;
            this.method = method;
            this.pattern = pattern;
        }
    }

    public static CenterLoadForecast buildCenterLoadForecast(List<ApplicationData> applications, ForecastOptions options) {
        ForecastOptions safeOptions = options == null ? new ForecastOptions() : options;
        int horizonMonths = Math.round((float) clamp(safeOptions.horizonMonths, 1, 12));
        int topItemsCount = Math.round((float) clamp(safeOptions.topItemsCount, 1, 500));
        Date asOfDate = safeOptions.asOfDate == null ? new Date() : safeOptions.asOfDate;
        int asOfMonth = toMonthIndex(asOfDate);
        List<InventoryItem> inventory = safeOptions.currentInventory == null ? new ArrayList<>() : safeOptions.currentInventory;

        Map<String, Integer> inventoryByItem = new HashMap<>();
        for (InventoryItem item : inventory) {
            if (item == null || item.name == null) continue;
            String normalizedName = item.name.trim();
            if (normalizedName.isEmpty()) continue;
            inventoryByItem.put(normalizedName, toPositiveNumber(item.quantity));
        }

        List<InventoryItem> reservedInventory = safeOptions.reservedInventory == null ? new ArrayList<>() : safeOptions.reservedInventory;
        Map<String, Integer> reservedByItem = new HashMap<>();
        for (InventoryItem item : reservedInventory) {
            if (item == null || item.name == null) continue;
            String normalizedName = item.name.trim();
            if (normalizedName.isEmpty()) continue;
            reservedByItem.put(normalizedName, toPositiveNumber(item.quantity));
        }

        Map<Integer, Integer> totalByMonth = new HashMap<>();
        Map<Integer, Integer> issuedByMonth = new HashMap<>();
        Map<Integer, Integer> totalItemsByMonth = new HashMap<>();
        Map<Integer, Integer> committedByMonth = new HashMap<>();
        Map<Integer, Integer> committedItemsByMonth = new HashMap<>();

        Map<String, Map<Integer, Integer>> itemHistoryByMonth = new HashMap<>();
        Map<String, Map<Integer, Integer>> itemCommittedByMonth = new HashMap<>();
        Set<Integer> seenHistoryMonths = new HashSet<>();
        Map<Integer, Integer> visitSlotCounts = new HashMap<>();

        int pendingApplications = 0;

        for (ApplicationData application : applications) {
            if (application == null) continue;
            Calendar parsedDate = parseApplicationDate(application.date);
            if (parsedDate == null) continue;

            int monthIndex = toMonthIndex(parsedDate);
            Map<String, Integer> itemQuantities = extractItemQuantities(application);
            int totalItems = 0;
            for (int qty : itemQuantities.values()) {
                totalItems += qty;
            }

            Integer hour = parseHour(application.time);
            String status = normalizeStatus(application.status);

            if (hour != null) {
                int slotStart = (hour / 2) * 2;
                visitSlotCounts.put(slotStart, visitSlotCounts.getOrDefault(slotStart, 0) + 1);
            }

            if (isReviewingStatus(status) || isApprovedStatus(status)) {
                pendingApplications += 1;
            }

            if (monthIndex <= asOfMonth) {
                addToMonthMap(totalByMonth, monthIndex, 1);
                addToMonthMap(totalItemsByMonth, monthIndex, totalItems);
                seenHistoryMonths.add(monthIndex);

                if (isIssuedStatus(status)) {
                    addToMonthMap(issuedByMonth, monthIndex, 1);
                }

                for (Map.Entry<String, Integer> entry : itemQuantities.entrySet()) {
                    Map<Integer, Integer> itemMap = itemHistoryByMonth.get(entry.getKey());
                    if (itemMap == null) {
                        itemMap = new HashMap<>();
                        itemHistoryByMonth.put(entry.getKey(), itemMap);
                    }
                    addToMonthMap(itemMap, monthIndex, entry.getValue());
                }
                continue;
            }

            addToMonthMap(committedByMonth, monthIndex, 1);
            addToMonthMap(committedItemsByMonth, monthIndex, totalItems);

            for (Map.Entry<String, Integer> entry : itemQuantities.entrySet()) {
                Map<Integer, Integer> itemMap = itemCommittedByMonth.get(entry.getKey());
                if (itemMap == null) {
                    itemMap = new HashMap<>();
                    itemCommittedByMonth.put(entry.getKey(), itemMap);
                }
                addToMonthMap(itemMap, monthIndex, entry.getValue());
            }
        }

        int startMonth = seenHistoryMonths.size() > 0 ? Collections.min(seenHistoryMonths) : asOfMonth - 5;

        List<Double> totalHistorySeries = buildSeries(totalByMonth, startMonth, asOfMonth);
        List<Double> issuedHistorySeries = buildSeries(issuedByMonth, startMonth, asOfMonth);
        List<Double> totalItemsHistorySeries = buildSeries(totalItemsByMonth, startMonth, asOfMonth);

        ForecastResult demandForecast = forecastWithMethod(ForecastMethod.holt_seasonal, totalHistorySeries, startMonth, asOfMonth, horizonMonths);

        ForecastMethod totalItemsMethod = chooseForecastMethod(totalItemsHistorySeries).method;
        ForecastResult itemDemandForecast = forecastWithMethod(totalItemsMethod, totalItemsHistorySeries, startMonth, asOfMonth, horizonMonths);

        List<ForecastTimelinePoint> timeline = new ArrayList<>();
        for (int step = 1; step <= horizonMonths; step++) {
            int monthIndex = asOfMonth + step;
            int predictedApplications = Math.max(0, (int) Math.round(itemAt(demandForecast.prediction, step - 1)));
            int predictedItems = Math.max(0, (int) Math.round(itemAt(itemDemandForecast.prediction, step - 1)));

            int committedApplications = Math.round(getMapValue(committedByMonth, monthIndex));
            int committedItems = Math.round(getMapValue(committedItemsByMonth, monthIndex));

            ForecastTimelinePoint point = new ForecastTimelinePoint();
            point.month = monthLabel(monthIndex);
            point.predictedApplications = predictedApplications;
            point.committedApplications = committedApplications;
            point.expectedApplications = Math.max(predictedApplications, committedApplications);
            point.predictedItems = predictedItems;
            point.committedItems = committedItems;
            point.expectedItems = Math.max(predictedItems, committedItems);
            timeline.add(point);
        }

        List<Double> recentIssued = tail(issuedHistorySeries, 3);
        List<Double> recentTotal = tail(totalHistorySeries, 3);
        double issuedBaseline = average(recentIssued);
        double fallbackBaseline = average(recentTotal) * 0.65;
        double capacityBaseline = round2(Math.max(1, issuedBaseline > 0 ? issuedBaseline : fallbackBaseline));

        ForecastTimelinePoint nextMonth = timeline.size() > 0 ? timeline.get(0) : null;
        int nextMonthExpectedApplications = nextMonth == null ? 0 : nextMonth.expectedApplications;
        int nextMonthExpectedItems = nextMonth == null ? 0 : nextMonth.expectedItems;

        double loadIndex = round2((nextMonthExpectedApplications + pendingApplications * 0.7) / capacityBaseline);

        LoadLevel loadLevel = LoadLevel.low;
        if (loadIndex >= 1.6) loadLevel = LoadLevel.critical;
        else if (loadIndex >= 1.15) loadLevel = LoadLevel.high;
        else if (loadIndex >= 0.85) loadLevel = LoadLevel.medium;

        int historyMonths = 0;
        for (double value : totalHistorySeries) {
            if (value > 0) historyMonths++;
        }

        int syntheticMonths = demandForecast.syntheticMonths;
        double confidenceRaw =
                0.44
                        + Math.min(historyMonths / 12.0, 1) * 0.34
                        - Math.min(syntheticMonths * 0.04, 0.24)
                        - Math.min(demandForecast.volatility * 0.22, 0.2)
                        + ((nextMonth != null && nextMonth.committedApplications > 0) ? 0.08 : 0);
        int confidence = Math.round((float) (clamp(confidenceRaw, 0.2, 0.97) * 100));

        Set<String> itemNames = new HashSet<>();
        itemNames.addAll(itemHistoryByMonth.keySet());
        itemNames.addAll(itemCommittedByMonth.keySet());
        itemNames.addAll(inventoryByItem.keySet());
        itemNames.addAll(reservedByItem.keySet());

        List<ItemForecastSnapshot> itemSnapshots = new ArrayList<>();
        Map<ForecastMethod, Integer> methodUsage = new HashMap<>();
        Map<DemandPattern, Integer> patternUsage = new HashMap<>();

        for (String item : itemNames) {
            Map<Integer, Integer> itemHistoryMap = itemHistoryByMonth.containsKey(item) ? itemHistoryByMonth.get(item) : new HashMap<>();
            Map<Integer, Integer> itemCommittedMap = itemCommittedByMonth.containsKey(item) ? itemCommittedByMonth.get(item) : new HashMap<>();
            List<Double> itemSeries = buildSeries(itemHistoryMap, startMonth, asOfMonth);

            MethodAndPattern mp = chooseForecastMethod(itemSeries);
            ForecastResult itemForecast = forecastWithMethod(mp.method, itemSeries, startMonth, asOfMonth, horizonMonths);

            methodUsage.put(mp.method, methodUsage.getOrDefault(mp.method, 0) + 1);
            patternUsage.put(mp.pattern, patternUsage.getOrDefault(mp.pattern, 0) + 1);

            List<Integer> expectedByMonth = new ArrayList<>();
            for (int step = 1; step <= horizonMonths; step++) {
                int monthIndex = asOfMonth + step;
                int predicted = (int) Math.round(itemAt(itemForecast.prediction, step - 1));
                int committed = Math.round(getMapValue(itemCommittedMap, monthIndex));
                expectedByMonth.add(Math.max(predicted, committed));
            }

            int nextExpected = expectedByMonth.size() > 0 ? expectedByMonth.get(0) : 0;
            int threeMonthExpected = 0;
            for (int i = 0; i < Math.min(3, expectedByMonth.size()); i++) {
                threeMonthExpected += expectedByMonth.get(i);
            }

            double recentAverage = average(tail(itemSeries, 3));

            itemSnapshots.add(new ItemForecastSnapshot(
                    item,
                    nextExpected,
                    threeMonthExpected,
                    Math.round(getMapValue(itemCommittedMap, asOfMonth + 1)),
                    getTrend(nextExpected, recentAverage),
                    mp.method,
                    mp.pattern
            ));
        }

        List<ItemDemandPrediction> topItems = new ArrayList<>();
        List<ItemForecastSnapshot> topSource = new ArrayList<>(itemSnapshots);
        topSource.sort(new Comparator<ItemForecastSnapshot>() {
            @Override
            public int compare(ItemForecastSnapshot a, ItemForecastSnapshot b) {
                if (a.nextExpected != b.nextExpected) return Integer.compare(b.nextExpected, a.nextExpected);
                return a.item.compareToIgnoreCase(b.item);
            }
        });
        for (int i = 0; i < Math.min(topItemsCount, topSource.size()); i++) {
            ItemForecastSnapshot snapshot = topSource.get(i);
            ItemDemandPrediction prediction = new ItemDemandPrediction();
            prediction.item = snapshot.item;
            prediction.expectedQuantity = snapshot.nextExpected;
            prediction.committedQuantity = snapshot.committedNextMonth;
            prediction.trend = snapshot.trend;
            topItems.add(prediction);
        }

        double uncertaintyFactor = confidence >= 80 ? 0.15 : confidence >= 60 ? 0.25 : 0.35;
        List<ItemProcurementPlan> itemProcurementPlan = new ArrayList<>();
        for (ItemForecastSnapshot snapshot : itemSnapshots) {
            int currentStock = Math.round(inventoryByItem.getOrDefault(snapshot.item, 0));
            int reservedStock = Math.round(reservedByItem.getOrDefault(snapshot.item, 0));
            int protectedStock = currentStock + reservedStock;
            int safetyStock = (int) Math.ceil(snapshot.nextExpected * uncertaintyFactor);
            int targetWithSafety = snapshot.nextExpected + safetyStock;
            int recommendedOrder = Math.max(0, (int) Math.ceil(targetWithSafety - protectedStock));
            double coverageMonths = snapshot.nextExpected > 0
                    ? round1(protectedStock / (double) snapshot.nextExpected)
                    : 99;

            ItemProcurementPlan plan = new ItemProcurementPlan();
            plan.item = snapshot.item;
            plan.currentStock = currentStock;
            plan.reservedStock = reservedStock;
            plan.nextMonthDemand = snapshot.nextExpected;
            plan.threeMonthDemand = snapshot.threeMonthExpected;
            plan.safetyStock = safetyStock;
            plan.recommendedOrder = recommendedOrder;
            plan.coverageMonths = coverageMonths;
            plan.riskLevel = getRiskLevel(protectedStock, snapshot.nextExpected, targetWithSafety);
            itemProcurementPlan.add(plan);
        }

        itemProcurementPlan.sort(new Comparator<ItemProcurementPlan>() {
            @Override
            public int compare(ItemProcurementPlan a, ItemProcurementPlan b) {
                int riskA = riskWeight(a.riskLevel);
                int riskB = riskWeight(b.riskLevel);
                if (riskA != riskB) return Integer.compare(riskB, riskA);
                if (a.recommendedOrder != b.recommendedOrder) return Integer.compare(b.recommendedOrder, a.recommendedOrder);
                if (a.nextMonthDemand != b.nextMonthDemand) return Integer.compare(b.nextMonthDemand, a.nextMonthDemand);
                return a.item.compareToIgnoreCase(b.item);
            }
        });

        int totalRecommendedOrder = 0;
        int itemsAtRisk = 0;
        for (ItemProcurementPlan item : itemProcurementPlan) {
            totalRecommendedOrder += item.recommendedOrder;
            if (item.riskLevel != StockRiskLevel.ok) itemsAtRisk++;
        }

        int totalSlots = 0;
        for (int count : visitSlotCounts.values()) {
            totalSlots += count;
        }

        List<BusyTimeSlot> busyTimeSlots = new ArrayList<>();
        if (totalSlots > 0) {
            List<Map.Entry<Integer, Integer>> slots = new ArrayList<>(visitSlotCounts.entrySet());
            slots.sort(new Comparator<Map.Entry<Integer, Integer>>() {
                @Override
                public int compare(Map.Entry<Integer, Integer> a, Map.Entry<Integer, Integer> b) {
                    return Integer.compare(b.getValue(), a.getValue());
                }
            });

            for (int i = 0; i < Math.min(3, slots.size()); i++) {
                Map.Entry<Integer, Integer> entry = slots.get(i);
                double share = entry.getValue() / (double) totalSlots;

                BusyTimeSlot slot = new BusyTimeSlot();
                slot.slot = toTimeSlotLabel(entry.getKey());
                slot.sharePercent = Math.round((float) (share * 100));
                slot.expectedVisits = Math.round((float) (share * nextMonthExpectedApplications));
                busyTimeSlots.add(slot);
            }
        }

        String peakLoadMonth = "";
        String peakItemsMonth = "";
        if (!timeline.isEmpty()) {
            ForecastTimelinePoint maxApps = timeline.get(0);
            ForecastTimelinePoint maxItems = timeline.get(0);
            for (ForecastTimelinePoint point : timeline) {
                if (point.expectedApplications > maxApps.expectedApplications) maxApps = point;
                if (point.expectedItems > maxItems.expectedItems) maxItems = point;
            }
            peakLoadMonth = maxApps.month;
            peakItemsMonth = maxItems.month;
        }

        List<String> modelNotes = new ArrayList<>();
        modelNotes.add("Прогноз строится по истории заявок, текущим остаткам и уже забронированным вещам.");
        if (syntheticMonths > 0) {
            modelNotes.add("Если заявок пока мало, прогноз дополнительно опирается на предварительную историю.");
        }
        if (nextMonth != null && nextMonth.committedApplications > 0) {
            modelNotes.add("Учтены уже запланированные заявки на следующий месяц: " + nextMonth.committedApplications + ".");
        }
        if (itemsAtRisk > 0) {
            modelNotes.add("Позиции с риском нехватки: " + itemsAtRisk + ".");
        }
        int reservedTotal = 0;
        for (int value : reservedByItem.values()) {
            reservedTotal += value;
        }
        if (reservedTotal > 0) {
            modelNotes.add("Учтены уже забронированные вещи: " + reservedTotal + ".");
        }

        CenterLoadForecast result = new CenterLoadForecast();
        result.nextMonthExpectedApplications = nextMonthExpectedApplications;
        result.nextMonthExpectedItems = nextMonthExpectedItems;
        result.pendingApplications = pendingApplications;
        result.capacityBaseline = capacityBaseline;
        result.loadIndex = loadIndex;
        result.loadLevel = loadLevel;
        result.confidence = confidence;
        result.historyMonths = historyMonths;
        result.syntheticMonths = syntheticMonths;
        result.timeline = timeline;
        result.topItems = topItems;
        result.itemProcurementPlan = itemProcurementPlan;
        result.busyTimeSlots = busyTimeSlots;
        result.totalRecommendedOrder = totalRecommendedOrder;
        result.itemsAtRisk = itemsAtRisk;
        result.peakLoadMonth = peakLoadMonth;
        result.peakItemsMonth = peakItemsMonth;
        result.modelNotes = modelNotes;
        return result;
    }

    private static class MethodAndPattern {
        final ForecastMethod method;
        final DemandPattern pattern;

        MethodAndPattern(ForecastMethod method, DemandPattern pattern) {
            this.method = method;
            this.pattern = pattern;
        }
    }

    private static MethodAndPattern chooseForecastMethod(List<Double> series) {
        DemandPattern pattern = classifyDemandPattern(series);
        int nonZeroCount = nonZeroValues(series).size();

        if (nonZeroCount == 0) return new MethodAndPattern(ForecastMethod.moving_average, pattern);
        if (series.size() < 4) return new MethodAndPattern(ForecastMethod.moving_average, pattern);
        if (pattern == DemandPattern.intermittent || pattern == DemandPattern.lumpy) return new MethodAndPattern(ForecastMethod.croston_sba, pattern);
        if (series.size() >= 12) return new MethodAndPattern(ForecastMethod.seasonal_naive, pattern);
        return new MethodAndPattern(ForecastMethod.holt_seasonal, pattern);
    }

    private static ForecastResult forecastWithMethod(ForecastMethod method, List<Double> historySeries, int startMonth, int asOfMonth, int horizon) {
        List<Double> recentWindow = tail(historySeries, 6);
        double volatility = coefficientOfVariation(recentWindow);

        if (method == ForecastMethod.moving_average) {
            return new ForecastResult(movingAverageForecast(historySeries, horizon, 3), 0, volatility, method);
        }

        if (method == ForecastMethod.seasonal_naive) {
            return new ForecastResult(seasonalNaiveForecast(historySeries, horizon, 12), 0, volatility, method);
        }

        if (method == ForecastMethod.croston_sba) {
            return new ForecastResult(crostonSbaForecast(historySeries, horizon, 0.2), 0, volatility, method);
        }

        Map<Integer, Double> seasonality = buildSeasonality(historySeries, startMonth);
        ForecastSeriesResult extended = extendSeriesWithSynthetic(historySeries, 8);
        List<Double> rawPrediction = holtForecast(extended.values, horizon);

        List<Double> prediction = new ArrayList<>();
        for (int offset = 0; offset < rawPrediction.size(); offset++) {
            int monthIndex = asOfMonth + offset + 1;
            double monthSeasonality = seasonality.containsKey(monthOfYear(monthIndex)) ? seasonality.get(monthOfYear(monthIndex)) : 1;
            double dampedSeasonality = 1 + (monthSeasonality - 1) * 0.55;
            prediction.add(Math.max(0, rawPrediction.get(offset) * dampedSeasonality));
        }

        return new ForecastResult(prediction, extended.syntheticMonths, volatility, method);
    }

    private static List<Double> holtForecast(List<Double> series, int horizon) {
        List<Double> predictions = new ArrayList<>();
        if (series.isEmpty()) {
            for (int i = 0; i < horizon; i++) predictions.add(0.0);
            return predictions;
        }

        double level = series.get(0);
        double trend = series.size() > 1 ? series.get(1) - series.get(0) : 0;
        double alpha = 0.56;
        double beta = 0.28;

        for (int i = 1; i < series.size(); i++) {
            double value = series.get(i);
            double previousLevel = level;
            level = alpha * value + (1 - alpha) * (level + trend);
            trend = beta * (level - previousLevel) + (1 - beta) * trend;
        }

        for (int step = 1; step <= horizon; step++) {
            predictions.add(Math.max(0, level + trend * step));
        }

        return predictions;
    }

    private static List<Double> movingAverageForecast(List<Double> series, int horizon, int window) {
        double value = movingAverageValue(series, window);
        List<Double> result = new ArrayList<>();
        for (int i = 0; i < horizon; i++) result.add(value);
        return result;
    }

    private static double movingAverageValue(List<Double> series, int window) {
        if (series.isEmpty()) return 0;
        List<Double> slice = tail(series, window);
        return Math.max(0, average(slice));
    }

    private static List<Double> seasonalNaiveForecast(List<Double> series, int horizon, int seasonLength) {
        if (series.isEmpty()) {
            List<Double> zeros = new ArrayList<>();
            for (int i = 0; i < horizon; i++) zeros.add(0.0);
            return zeros;
        }

        if (series.size() < seasonLength) {
            return movingAverageForecast(series, horizon, 3);
        }

        List<Double> lastSeason = series.subList(series.size() - seasonLength, series.size());
        List<Double> result = new ArrayList<>();
        for (int index = 0; index < horizon; index++) {
            int seasonIndex = index % seasonLength;
            result.add(Math.max(0, lastSeason.get(seasonIndex)));
        }
        return result;
    }

    private static List<Double> crostonSbaForecast(List<Double> series, int horizon, double alpha) {
        if (series.isEmpty()) {
            List<Double> zeros = new ArrayList<>();
            for (int i = 0; i < horizon; i++) zeros.add(0.0);
            return zeros;
        }

        double demandEstimate = 0;
        double intervalEstimate = 1;
        double interval = 1;
        boolean initialized = false;

        for (double value : series) {
            if (value > 0) {
                if (!initialized) {
                    demandEstimate = value;
                    intervalEstimate = interval;
                    initialized = true;
                } else {
                    demandEstimate = demandEstimate + alpha * (value - demandEstimate);
                    intervalEstimate = intervalEstimate + alpha * (interval - intervalEstimate);
                }
                interval = 1;
            } else {
                interval += 1;
            }
        }

        if (!initialized) {
            List<Double> zeros = new ArrayList<>();
            for (int i = 0; i < horizon; i++) zeros.add(0.0);
            return zeros;
        }

        double denominator = Math.max(intervalEstimate, 1e-6);
        double baseForecast = (1 - alpha / 2.0) * (demandEstimate / denominator);

        int trailingZeros = 0;
        for (int i = series.size() - 1; i >= 0; i--) {
            if (series.get(i) > 0) break;
            trailingZeros++;
        }

        double obsolescenceDamping = 1.0 / (1.0 + trailingZeros * 0.08);
        double forecast = Math.max(0, baseForecast * obsolescenceDamping);

        List<Double> result = new ArrayList<>();
        for (int i = 0; i < horizon; i++) result.add(forecast);
        return result;
    }

    private static DemandPattern classifyDemandPattern(List<Double> series) {
        if (series.size() < 2) return DemandPattern.smooth;
        double adi = calculateAdi(series);
        double cv2 = calculateCvSquaredNonZero(series);

        if (adi >= 1.32 && cv2 >= 0.49) return DemandPattern.lumpy;
        if (adi >= 1.32 && cv2 < 0.49) return DemandPattern.intermittent;
        if (adi < 1.32 && cv2 >= 0.49) return DemandPattern.erratic;
        return DemandPattern.smooth;
    }

    private static double calculateAdi(List<Double> series) {
        int nonZero = nonZeroValues(series).size();
        if (nonZero == 0) return Double.POSITIVE_INFINITY;
        return series.size() / (double) nonZero;
    }

    private static double calculateCvSquaredNonZero(List<Double> series) {
        List<Double> values = nonZeroValues(series);
        if (values.size() < 2) return 0;
        double mean = average(values);
        if (mean <= 0) return 0;
        double cv = standardDeviation(values) / mean;
        return cv * cv;
    }

    private static ForecastSeriesResult extendSeriesWithSynthetic(List<Double> originalSeries, int minimumLength) {
        List<Double> series = new ArrayList<>();
        for (double value : originalSeries) {
            series.add(Math.max(0, value));
        }

        if (series.size() >= minimumLength) {
            return new ForecastSeriesResult(series, 0);
        }

        int syntheticMonths = minimumLength - series.size();
        if (series.isEmpty()) {
            List<Double> zeros = new ArrayList<>();
            for (int i = 0; i < minimumLength; i++) zeros.add(0.0);
            return new ForecastSeriesResult(zeros, syntheticMonths);
        }

        double baseline = average(tail(series, 3));
        double trend = series.size() > 1
                ? (series.get(series.size() - 1) - series.get(0)) / (series.size() - 1)
                : 0;

        List<Double> syntheticPrefix = new ArrayList<>();
        for (int step = syntheticMonths; step >= 1; step--) {
            double trendBack = baseline - trend * (step + 0.5);
            double wave = 1 + Math.sin((step / (double) (syntheticMonths + 1)) * Math.PI) * 0.06;
            syntheticPrefix.add(Math.max(0, trendBack * wave));
        }

        List<Double> values = new ArrayList<>(syntheticPrefix);
        values.addAll(series);
        return new ForecastSeriesResult(values, syntheticMonths);
    }

    private static Map<Integer, Double> buildSeasonality(List<Double> series, int startMonth) {
        Map<Integer, Double> seasonality = new HashMap<>();
        if (series.size() < 12) return seasonality;

        double mean = average(series);
        if (mean <= 0) return seasonality;

        Map<Integer, List<Double>> buckets = new HashMap<>();
        for (int offset = 0; offset < series.size(); offset++) {
            int month = monthOfYear(startMonth + offset);
            List<Double> ratios = buckets.containsKey(month) ? buckets.get(month) : new ArrayList<Double>();
            ratios.add(series.get(offset) / mean);
            buckets.put(month, ratios);
        }

        for (Map.Entry<Integer, List<Double>> entry : buckets.entrySet()) {
            seasonality.put(entry.getKey(), clamp(average(entry.getValue()), 0.75, 1.25));
        }

        return seasonality;
    }

    private static List<Double> buildSeries(Map<Integer, Integer> monthMap, int startMonth, int endMonth) {
        List<Double> series = new ArrayList<>();
        for (int month = startMonth; month <= endMonth; month++) {
            series.add((double) getMapValue(monthMap, month));
        }
        return series;
    }

    private static int getMapValue(Map<Integer, Integer> map, int key) {
        return map.containsKey(key) ? map.get(key) : 0;
    }

    private static void addToMonthMap(Map<Integer, Integer> map, int key, int value) {
        map.put(key, getMapValue(map, key) + value);
    }

    private static Map<String, Integer> extractItemQuantities(ApplicationData application) {
        Map<String, Integer> source = application.selectedItems == null ? new HashMap<String, Integer>() : application.selectedItems;
        Map<String, Integer> normalized = new HashMap<>();

        for (Map.Entry<String, Integer> entry : source.entrySet()) {
            String itemName = entry.getKey() == null ? "" : entry.getKey().trim();
            if (itemName.isEmpty()) continue;
            int quantity = toPositiveNumber(entry.getValue());
            if (quantity > 0) {
                normalized.put(itemName, normalized.getOrDefault(itemName, 0) + quantity);
            }
        }
        return normalized;
    }

    private static Calendar parseApplicationDate(String dateRaw) {
        if (dateRaw == null) return null;
        String date = dateRaw.trim();
        if (date.isEmpty()) return null;

        String[] formats = new String[]{"d/M/yyyy", "d.M.yyyy", "d-M-yyyy", "d/M/yy", "d.M.yy", "d-M-yy"};
        for (String format : formats) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.ROOT);
                sdf.setLenient(false);
                Date parsed = sdf.parse(date);
                if (parsed != null) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(parsed);
                    return calendar;
                }
            } catch (ParseException ignored) {
            }
        }

        try {
            Date parsed = new Date(date);
            if (parsed.getTime() > 0) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(parsed);
                return calendar;
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    private static Integer parseHour(String timeRaw) {
        if (timeRaw == null) return null;
        String trimmed = timeRaw.trim();
        if (trimmed.isEmpty()) return null;

        String[] parts = trimmed.split(":");
        if (parts.length < 1 || parts.length > 2) return null;

        try {
            int hour = Integer.parseInt(parts[0]);
            if (hour < 0 || hour > 23) return null;
            return hour;
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static ItemTrend getTrend(int expected, double recentAverage) {
        if (recentAverage <= 0) return expected > 0 ? ItemTrend.up : ItemTrend.stable;
        double ratio = expected / recentAverage;
        if (ratio > 1.15) return ItemTrend.up;
        if (ratio < 0.85) return ItemTrend.down;
        return ItemTrend.stable;
    }

    private static StockRiskLevel getRiskLevel(int currentStock, int demand, int demandWithSafety) {
        if (demand <= 0) return StockRiskLevel.ok;
        if (currentStock < demand * 0.75) return StockRiskLevel.critical;
        if (currentStock < demandWithSafety) return StockRiskLevel.watch;
        return StockRiskLevel.ok;
    }

    private static int riskWeight(StockRiskLevel level) {
        if (level == StockRiskLevel.critical) return 3;
        if (level == StockRiskLevel.watch) return 2;
        return 1;
    }

    private static Map<ForecastMethod, String> methodLabels() {
        Map<ForecastMethod, String> labels = new HashMap<>();
        labels.put(ForecastMethod.holt_seasonal, "Holt + сезонность");
        labels.put(ForecastMethod.seasonal_naive, "Сезонный наивный");
        labels.put(ForecastMethod.moving_average, "Скользящее среднее");
        labels.put(ForecastMethod.croston_sba, "Croston-SBA");
        return labels;
    }

    private static Map<DemandPattern, String> patternLabels() {
        Map<DemandPattern, String> labels = new HashMap<>();
        labels.put(DemandPattern.smooth, "Ровный спрос");
        labels.put(DemandPattern.intermittent, "Интервальный спрос");
        labels.put(DemandPattern.erratic, "Неровный спрос");
        labels.put(DemandPattern.lumpy, "Редкий всплесковый спрос");
        return labels;
    }

    private static <T> String formatUsage(Map<T, Integer> usageMap, Map<T, String> labels) {
        List<Map.Entry<T, Integer>> entries = new ArrayList<>(usageMap.entrySet());
        entries.sort(new Comparator<Map.Entry<T, Integer>>() {
            @Override
            public int compare(Map.Entry<T, Integer> a, Map.Entry<T, Integer> b) {
                return Integer.compare(b.getValue(), a.getValue());
            }
        });

        List<String> parts = new ArrayList<>();
        for (Map.Entry<T, Integer> entry : entries) {
            String label = labels.containsKey(entry.getKey()) ? labels.get(entry.getKey()) : String.valueOf(entry.getKey());
            parts.add(label + ": " + entry.getValue());
        }
        return String.join(", ", parts);
    }

    private static String normalizeStatus(String status) {
        return status == null ? "" : status.trim();
    }

    private static boolean isReviewingStatus(String status) {
        String s = normalizeStatus(status);
        String lower = s.toLowerCase(Locale.ROOT);
        return lower.contains("рассматри")
                || lower.contains("under review")
                || "pending".equals(lower)
                || "Р Р°СЃСЃРјР°С‚СЂРёРІР°РµС‚СЃСЏ".equals(s);
    }

    private static boolean isApprovedStatus(String status) {
        String s = normalizeStatus(status);
        String lower = s.toLowerCase(Locale.ROOT);
        return lower.contains("одобрен")
                || "approved".equals(lower)
                || "РћРґРѕР±СЂРµРЅРѕ".equals(s);
    }

    private static boolean isIssuedStatus(String status) {
        String s = normalizeStatus(status);
        String lower = s.toLowerCase(Locale.ROOT);
        return lower.contains("выдан")
                || "issued".equals(lower)
                || "Р’С‹РґР°РЅРѕ".equals(s);
    }

    private static int toMonthIndex(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return toMonthIndex(calendar);
    }

    private static int toMonthIndex(Calendar date) {
        return date.get(Calendar.YEAR) * 12 + date.get(Calendar.MONTH);
    }

    private static int monthOfYear(int monthIndex) {
        return ((monthIndex % 12) + 12) % 12;
    }

    private static String monthLabel(int monthIndex) {
        int year = monthIndex / 12;
        int month = monthOfYear(monthIndex) + 1;
        return String.format(Locale.ROOT, "%02d.%d", month, year);
    }

    private static String toTimeSlotLabel(int slotStartHour) {
        int endHour = Math.min(slotStartHour + 2, 24);
        return String.format(Locale.ROOT, "%02d:00-%02d:00", slotStartHour, endHour);
    }

    private static int toPositiveNumber(Object value) {
        if (value instanceof Number) {
            double numeric = ((Number) value).doubleValue();
            return Double.isFinite(numeric) && numeric > 0 ? (int) Math.round(numeric) : 0;
        }
        if (value instanceof String) {
            try {
                double numeric = Double.parseDouble(((String) value).replace(",", "."));
                return Double.isFinite(numeric) && numeric > 0 ? (int) Math.round(numeric) : 0;
            } catch (NumberFormatException ignored) {
                return 0;
            }
        }
        return 0;
    }

    private static double average(List<Double> values) {
        if (values.isEmpty()) return 0;
        double sum = 0;
        for (double value : values) sum += value;
        return sum / values.size();
    }

    private static double standardDeviation(List<Double> values) {
        if (values.size() < 2) return 0;
        double mean = average(values);
        double variance = 0;
        for (double value : values) variance += Math.pow(value - mean, 2);
        variance /= values.size();
        return Math.sqrt(variance);
    }

    private static double coefficientOfVariation(List<Double> values) {
        double mean = average(values);
        if (mean <= 0) return 0;
        return standardDeviation(values) / mean;
    }

    private static List<Double> nonZeroValues(List<Double> series) {
        List<Double> values = new ArrayList<>();
        for (double value : series) {
            if (value > 0) values.add(value);
        }
        return values;
    }

    private static List<Double> tail(List<Double> values, int maxSize) {
        if (values.isEmpty()) return new ArrayList<>();
        int from = Math.max(0, values.size() - maxSize);
        return new ArrayList<>(values.subList(from, values.size()));
    }

    private static double itemAt(List<Double> values, int index) {
        if (index < 0 || index >= values.size()) return 0;
        return values.get(index);
    }

    private static double clamp(double value, double min, double max) {
        return Math.min(Math.max(value, min), max);
    }

    private static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private static double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
