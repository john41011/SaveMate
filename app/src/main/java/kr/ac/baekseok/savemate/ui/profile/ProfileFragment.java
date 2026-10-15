package kr.ac.baekseok.savemate.ui.profile;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;

import kr.ac.baekseok.savemate.R;
import kr.ac.baekseok.savemate.database.AppDatabase;
import kr.ac.baekseok.savemate.utils.DateUtils;
import kr.ac.baekseok.savemate.utils.PreferenceHelper;

public class ProfileFragment extends Fragment {

    private TextView tvUserName, tvMonthlyBudget, tvVersion;
    private Switch switchNotification;
    private MaterialCardView cardEditName, cardEditBudget, cardCategoryBudget,
            cardNotification, cardClearData, cardAbout;

    private PreferenceHelper preferenceHelper;
    private Handler mainHandler;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // View 초기화
        tvUserName = view.findViewById(R.id.tv_user_name);
        tvMonthlyBudget = view.findViewById(R.id.tv_monthly_budget);
        tvVersion = view.findViewById(R.id.tv_version);
        switchNotification = view.findViewById(R.id.switch_notification);

        cardEditName = view.findViewById(R.id.card_edit_name);
        cardEditBudget = view.findViewById(R.id.card_edit_budget);
        cardCategoryBudget = view.findViewById(R.id.card_category_budget);
        cardNotification = view.findViewById(R.id.card_notification);
        cardClearData = view.findViewById(R.id.card_clear_data);
        cardAbout = view.findViewById(R.id.card_about);

        preferenceHelper = new PreferenceHelper(requireContext());
        mainHandler = new Handler(Looper.getMainLooper());

        // 데이터 로드
        loadUserInfo();

        // 클릭 리스너 설정
        setupClickListeners();
    }

    private void loadUserInfo() {
        // 사용자 이름
        String userName = preferenceHelper.getUserName();
        tvUserName.setText(userName);

        // 월 예산
        int budget = preferenceHelper.getMonthlyBudget();
        if (budget > 0) {
            tvMonthlyBudget.setText(DateUtils.formatCurrency(budget));
        } else {
            tvMonthlyBudget.setText("설정 안 함");
        }

        // 알림 설정
        switchNotification.setChecked(preferenceHelper.isNotificationEnabled());

        // 앱 버전
        try {
            String versionName = requireContext().getPackageManager()
                    .getPackageInfo(requireContext().getPackageName(), 0).versionName;
            tvVersion.setText("버전 " + versionName);
        } catch (Exception e) {
            tvVersion.setText("버전 1.0");
        }
    }

    private void setupClickListeners() {
        // 이름 수정
        cardEditName.setOnClickListener(v -> showEditNameDialog());

        // 예산 설정
        cardEditBudget.setOnClickListener(v -> showEditBudgetDialog());

        // 카테고리별 예산
        cardCategoryBudget.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), CategoryBudgetActivity.class);
            startActivity(intent);
        });

        // 알림 스위치
        cardNotification.setOnClickListener(v -> switchNotification.toggle());

        switchNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferenceHelper.setNotificationEnabled(isChecked);
            Toast.makeText(requireContext(),
                    isChecked ? "알림이 활성화되었습니다" : "알림이 비활성화되었습니다",
                    Toast.LENGTH_SHORT).show();
        });

        // 데이터 전체 삭제
        cardClearData.setOnClickListener(v -> showClearDataDialog());

        // 앱 정보
        cardAbout.setOnClickListener(v -> showAboutDialog());
    }

    private void showEditNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("이름 변경");

        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(preferenceHelper.getUserName());
        input.setSelection(input.getText().length());

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(50, 20, 50, 20);
        input.setLayoutParams(lp);

        builder.setView(input);

        builder.setPositiveButton("확인", (dialog, which) -> {
            String name = input.getText().toString().trim();
            if (!name.isEmpty()) {
                preferenceHelper.setUserName(name);
                tvUserName.setText(name);
                Toast.makeText(requireContext(), "이름이 변경되었습니다", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("취소", null);

        builder.show();
    }

    private void showEditBudgetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("월 예산 설정");

        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("금액 입력 (원)");

        int currentBudget = preferenceHelper.getMonthlyBudget();
        if (currentBudget > 0) {
            input.setText(String.valueOf(currentBudget));
        }

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(50, 20, 50, 20);
        input.setLayoutParams(lp);

        builder.setView(input);

        builder.setPositiveButton("확인", (dialog, which) -> {
            String budgetStr = input.getText().toString().trim();
            if (!budgetStr.isEmpty()) {
                try {
                    int budget = Integer.parseInt(budgetStr);
                    preferenceHelper.setMonthlyBudget(budget);
                    tvMonthlyBudget.setText(DateUtils.formatCurrency(budget));
                    Toast.makeText(requireContext(), "예산이 설정되었습니다", Toast.LENGTH_SHORT).show();
                } catch (NumberFormatException e) {
                    Toast.makeText(requireContext(), "올바른 금액을 입력하세요", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("취소", null);

        builder.show();
    }

    private void showClearDataDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("데이터 전체 삭제")
                .setMessage("모든 영수증 데이터가 삭제됩니다.\n정말로 삭제하시겠습니까?")
                .setPositiveButton("삭제", (dialog, which) -> {
                    new Thread(() -> {
                        AppDatabase db = AppDatabase.getInstance(requireContext());
                        db.receiptDao().deleteAll();
                        db.itemDao().deleteAll();

                        mainHandler.post(() -> {
                            Toast.makeText(requireContext(),
                                    "모든 데이터가 삭제되었습니다",
                                    Toast.LENGTH_SHORT).show();
                        });
                    }).start();
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private void showAboutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("SaveMate 정보")
                .setMessage("SaveMate v1.0\n\n" +
                        "영수증 분석을 통한 절약 앱\n\n" +
                        "© 2024 SaveMate")
                .setPositiveButton("확인", null)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserInfo();
    }

    // ItemDao에 deleteAll 메서드 추가 필요
    private void deleteAll() {
        // ItemDao.java에 추가:
        // @Query("DELETE FROM items")
        // void deleteAll();
    }
}