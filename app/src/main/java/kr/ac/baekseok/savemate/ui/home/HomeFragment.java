package kr.ac.baekseok.savemate.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import kr.ac.baekseok.savemate.R;
import kr.ac.baekseok.savemate.adapter.ReceiptAdapter;
import kr.ac.baekseok.savemate.database.ReceiptRepository;
import kr.ac.baekseok.savemate.ui.scan.ReceiptDetailActivity;
import kr.ac.baekseok.savemate.utils.DateUtils;
import kr.ac.baekseok.savemate.utils.PreferenceHelper;

public class HomeFragment extends Fragment {

    private TextView tvTotalAmount;
    private TextView tvNoData;
    private TextView tvBudgetProgress;
    private View layoutBudget;
    private RecyclerView rvRecentReceipts;
    private ReceiptAdapter adapter;
    private ReceiptRepository repository;
    private Handler mainHandler;
    private PreferenceHelper preferenceHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // View 초기화
        tvTotalAmount = view.findViewById(R.id.tv_total_amount);
        tvNoData = view.findViewById(R.id.tv_no_data);
        tvBudgetProgress = view.findViewById(R.id.tv_budget_progress);
        layoutBudget = view.findViewById(R.id.layout_budget);
        rvRecentReceipts = view.findViewById(R.id.rv_recent_receipts);

        // Repository & Handler 초기화
        repository = new ReceiptRepository(requireContext());
        mainHandler = new Handler(Looper.getMainLooper());
        preferenceHelper = new PreferenceHelper(requireContext());

        // RecyclerView 설정
        setupRecyclerView();

        // 데이터 로드
        loadData();
    }

    private void setupRecyclerView() {
        adapter = new ReceiptAdapter();
        rvRecentReceipts.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvRecentReceipts.setAdapter(adapter);

        // 아이템 클릭 리스너
        adapter.setOnReceiptClickListener(receipt -> {
            Intent intent = new Intent(requireContext(), ReceiptDetailActivity.class);
            intent.putExtra(ReceiptDetailActivity.EXTRA_RECEIPT_ID, receipt.getId());
            startActivity(intent);
        });
    }

    private void loadData() {
        // 월 예산 가져오기
        int monthlyBudget = preferenceHelper.getMonthlyBudget();

        // 이번 달 총 지출 로드
        repository.getMonthlyTotal(total -> mainHandler.post(() -> {
            if (total > 0) {
                tvTotalAmount.setText(DateUtils.formatCurrency(total));

                // 예산 진행률 표시
                if (monthlyBudget > 0) {
                    displayBudgetProgress(total, monthlyBudget);
                } else {
                    layoutBudget.setVisibility(View.GONE);
                }
            } else {
                tvTotalAmount.setText("0원");
                layoutBudget.setVisibility(View.GONE);
            }
        }));

        // 최근 영수증 로드
        repository.getRecentReceipts(10, receipts -> mainHandler.post(() -> {
            if (receipts != null && !receipts.isEmpty()) {
                adapter.setReceipts(receipts);
                tvNoData.setVisibility(View.GONE);
                rvRecentReceipts.setVisibility(View.VISIBLE);
            } else { // 데이터가 없으면 빈 화면 표시 (더미 데이터 제거!)
                showEmptyState();
            }
        }));
    }

    private void displayBudgetProgress(int totalSpent, int monthlyBudget) {
        // 예산 진행률 계산
        int percentage = (int) ((totalSpent / (float) monthlyBudget) * 100);
        tvBudgetProgress.setText(percentage + "% 사용");

        // %에 따라 색 변경
        if (percentage >= 100) {
            tvBudgetProgress.setTextColor(getResources().getColor(R.color.error, null));
        } else if (percentage >= 80) {
            tvBudgetProgress.setTextColor(getResources().getColor(R.color.warning, null));
        } else {
            tvBudgetProgress.setTextColor(getResources().getColor(R.color.success, null));
        }

        layoutBudget.setVisibility(View.VISIBLE);
    }

    // 빈 화면 표시
    private void showEmptyState() {
        tvTotalAmount.setText("0원");
        layoutBudget.setVisibility(View.GONE);
        tvNoData.setVisibility(View.VISIBLE);
        rvRecentReceipts.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        // 화면 다시 돌아올 때마다 새로고침
        loadData();
    }
}