package kr.ac.baekseok.savemate.ui.statistics;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.ac.baekseok.savemate.R;
import kr.ac.baekseok.savemate.database.AppDatabase;
import kr.ac.baekseok.savemate.database.ReceiptDao;
import kr.ac.baekseok.savemate.model.Category;
import kr.ac.baekseok.savemate.model.Receipt;
import kr.ac.baekseok.savemate.utils.DateUtils;

public class StatisticsFragment extends Fragment {

    private TextView tvSelectedMonth;
    private TextView tvMonthlyTotal, tvTopCategory, tvAvgDaily, tvTransactionCount;
    private ImageButton btnPrevMonth, btnNextMonth;
    private PieChart pieChart;
    private LineChart lineChart;

    private Handler mainHandler;
    private AppDatabase database;

    // 선택된 년/월
    private int selectedYear;
    private int selectedMonth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_statistics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // View 초기화
        tvSelectedMonth = view.findViewById(R.id.tv_selected_month);
        btnPrevMonth = view.findViewById(R.id.btn_prev_month);
        btnNextMonth = view.findViewById(R.id.btn_next_month);
        tvMonthlyTotal = view.findViewById(R.id.tv_monthly_total);
        tvTopCategory = view.findViewById(R.id.tv_top_category);
        tvAvgDaily = view.findViewById(R.id.tv_avg_daily);
        tvTransactionCount = view.findViewById(R.id.tv_transaction_count);
        pieChart = view.findViewById(R.id.pie_chart);
        lineChart = view.findViewById(R.id.line_chart);

        mainHandler = new Handler(Looper.getMainLooper());
        database = AppDatabase.getInstance(requireContext());

        // 현재 년/월로 초기화
        Calendar calendar = Calendar.getInstance();
        selectedYear = calendar.get(Calendar.YEAR);
        selectedMonth = calendar.get(Calendar.MONTH) + 1;

        // 버튼 리스너
        btnPrevMonth.setOnClickListener(v -> {
            selectedMonth--;
            if (selectedMonth < 1) {
                selectedMonth = 12;
                selectedYear--;
            }
            updateMonthDisplay();
            loadStatistics();
        });

        btnNextMonth.setOnClickListener(v -> {
            selectedMonth++;
            if (selectedMonth > 12) {
                selectedMonth = 1;
                selectedYear++;
            }
            updateMonthDisplay();
            loadStatistics();
        });

        // 차트 설정
        setupPieChart();
        setupLineChart();

        // 초기 데이터 로드
        updateMonthDisplay();
        loadStatistics();
    }

    private void updateMonthDisplay() {
        tvSelectedMonth.setText(selectedYear + "년 " + selectedMonth + "월");
    }

    private void setupPieChart() {
        pieChart.setUsePercentValues(false);
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(5, 10, 5, 5);
        pieChart.setDragDecelerationFrictionCoef(0.95f);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setTransparentCircleRadius(61f);
        pieChart.setDrawEntryLabels(false);
        pieChart.getLegend().setEnabled(true);
        pieChart.getLegend().setTextSize(12f);
    }

    private void setupLineChart() {
        lineChart.getDescription().setEnabled(false);
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(false);
        lineChart.setPinchZoom(false);
        lineChart.setDrawGridBackground(false);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(7);

        lineChart.getAxisLeft().setDrawGridLines(true);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getLegend().setEnabled(false);
    }

    private void loadStatistics() {
        new Thread(() -> {
            ReceiptDao dao = database.receiptDao();

            // 선택된 월의 시작/끝 계산
            Calendar calendar = Calendar.getInstance();
            calendar.set(selectedYear, selectedMonth - 1, 1, 0, 0, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            long startOfMonth = calendar.getTimeInMillis();

            calendar.add(Calendar.MONTH, 1);
            long startOfNextMonth = calendar.getTimeInMillis();

            // 선택된 월의 영수증 가져오기
            List<Receipt> monthlyReceipts = dao.getMonthlyReceipts(startOfMonth, startOfNextMonth);

            mainHandler.post(() -> {
                if (monthlyReceipts != null && !monthlyReceipts.isEmpty()) {
                    calculateStatistics(monthlyReceipts);
                    displayCategoryChart(monthlyReceipts);
                    displayDailyChart(monthlyReceipts);
                } else {
                    showEmptyState();
                }
            });
        }).start();
    }

    private void calculateStatistics(List<Receipt> receipts) {
        int total = 0;
        Map<Category, Integer> categoryTotals = new HashMap<>();

        for (Receipt receipt : receipts) {
            total += receipt.getTotalAmount();

            Category category = receipt.getCategory();
            categoryTotals.put(category,
                    categoryTotals.getOrDefault(category, 0) + receipt.getTotalAmount());
        }

        // 월 총액
        tvMonthlyTotal.setText(DateUtils.formatCurrency(total));

        // 거래 건수
        tvTransactionCount.setText(receipts.size() + "건");

        // 일 평균 (선택된 월의 일수로 계산)
        Calendar calendar = Calendar.getInstance();
        calendar.set(selectedYear, selectedMonth - 1, 1);
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        // 오늘 날짜가 선택된 월과 같으면 오늘까지만 계산
        Calendar today = Calendar.getInstance();
        int divisor = daysInMonth;
        if (selectedYear == today.get(Calendar.YEAR) &&
                selectedMonth == today.get(Calendar.MONTH) + 1) {
            divisor = today.get(Calendar.DAY_OF_MONTH);
        }

        int avgDaily = divisor > 0 ? total / divisor : 0;
        tvAvgDaily.setText(DateUtils.formatCurrency(avgDaily));

        // 최다 지출 카테고리
        Category topCategory = null;
        int maxAmount = 0;
        for (Map.Entry<Category, Integer> entry : categoryTotals.entrySet()) {
            if (entry.getValue() > maxAmount) {
                maxAmount = entry.getValue();
                topCategory = entry.getKey();
            }
        }

        if (topCategory != null) {
            tvTopCategory.setText(topCategory.getDisplayName());
        }
    }

    private void displayCategoryChart(List<Receipt> receipts) {
        Map<Category, Integer> categoryTotals = new HashMap<>();

        for (Receipt receipt : receipts) {
            Category category = receipt.getCategory();
            categoryTotals.put(category,
                    categoryTotals.getOrDefault(category, 0) + receipt.getTotalAmount());
        }

        // PieEntry 생성
        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        for (Map.Entry<Category, Integer> entry : categoryTotals.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey().getDisplayName()));
            try {
                colors.add(Color.parseColor(entry.getKey().getColor()));
            } catch (Exception e) {
                colors.add(Color.GRAY);
            }
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return DateUtils.formatAmount((int) value);
            }
        });

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.invalidate();
    }

    private void displayDailyChart(List<Receipt> receipts) {
        // 일별 지출 집계
        Map<Integer, Integer> dailyTotals = new HashMap<>();

        Calendar calendar = Calendar.getInstance();
        for (Receipt receipt : receipts) {
            calendar.setTime(receipt.getPurchaseDate());
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            dailyTotals.put(day,
                    dailyTotals.getOrDefault(day, 0) + receipt.getTotalAmount());
        }

        // Entry 생성 (선택된 월의 모든 일)
        List<Entry> entries = new ArrayList<>();
        calendar.set(selectedYear, selectedMonth - 1, 1);
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        for (int i = 1; i <= daysInMonth; i++) {
            int amount = dailyTotals.getOrDefault(i, 0);
            entries.add(new Entry(i, amount));
        }

        LineDataSet dataSet = new LineDataSet(entries, "일별 지출");
        dataSet.setColor(getResources().getColor(R.color.primary, null));
        dataSet.setCircleColor(getResources().getColor(R.color.primary, null));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawCircleHole(false);
        dataSet.setValueTextSize(10f);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(getResources().getColor(R.color.primary_light, null));
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value == 0) return "";
                return DateUtils.formatAmount((int) value);
            }
        });

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);
        lineChart.invalidate();
    }

    private void showEmptyState() {
        tvMonthlyTotal.setText("0원");
        tvTopCategory.setText("-");
        tvAvgDaily.setText("0원");
        tvTransactionCount.setText("0건");

        pieChart.setNoDataText("영수증을 추가하면\n통계가 표시됩니다");
        pieChart.setNoDataTextColor(getResources().getColor(R.color.text_hint, null));

        lineChart.setNoDataText("영수증을 추가하면\n그래프가 표시됩니다");
        lineChart.setNoDataTextColor(getResources().getColor(R.color.text_hint, null));
    }

    @Override
    public void onResume() {
        super.onResume();
        // 화면 돌아올 때마다 새로고침
        loadStatistics();
    }
}