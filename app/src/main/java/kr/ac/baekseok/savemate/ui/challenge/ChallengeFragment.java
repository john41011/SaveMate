package kr.ac.baekseok.savemate.ui.challenge;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.ac.baekseok.savemate.R;
import kr.ac.baekseok.savemate.adapter.RankingAdapter;
import kr.ac.baekseok.savemate.database.AppDatabase;
import kr.ac.baekseok.savemate.database.ReceiptDao;
import kr.ac.baekseok.savemate.model.Challenge;
import kr.ac.baekseok.savemate.model.Receipt;
import kr.ac.baekseok.savemate.utils.FirebaseHelper;
import kr.ac.baekseok.savemate.utils.PreferenceHelper;

public class ChallengeFragment extends Fragment {

    private static final String TAG = "ChallengeFragment";

    private TextView tvMyRank, tvMyTotal, tvTotalUsers;
    private RecyclerView rvRanking;
    private Button btnUploadData, btnRefresh;
    private ProgressBar progressBar;
    private View layoutMyRank;

    private RankingAdapter adapter;
    private FirebaseHelper firebaseHelper;
    private PreferenceHelper preferenceHelper;
    private Handler mainHandler;
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_challenge, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            // View 초기화
            tvMyRank = view.findViewById(R.id.tv_my_rank);
            tvMyTotal = view.findViewById(R.id.tv_my_total);
            tvTotalUsers = view.findViewById(R.id.tv_total_users);
            rvRanking = view.findViewById(R.id.rv_ranking);
            btnUploadData = view.findViewById(R.id.btn_upload_data);
            btnRefresh = view.findViewById(R.id.btn_refresh);
            progressBar = view.findViewById(R.id.progress_bar);
            layoutMyRank = view.findViewById(R.id.layout_my_rank);

            firebaseHelper = FirebaseHelper.getInstance(requireContext());
            preferenceHelper = new PreferenceHelper(requireContext());
            mainHandler = new Handler(Looper.getMainLooper());

            // RecyclerView 설정
            adapter = new RankingAdapter();
            rvRanking.setLayoutManager(new LinearLayoutManager(requireContext()));
            rvRanking.setAdapter(adapter);

            // Firebase 인증 확인
            checkAuthAndLoadData();

            // 버튼 리스너
            btnUploadData.setOnClickListener(v -> uploadMyData());
            btnRefresh.setOnClickListener(v -> loadRankingData());

        } catch (Exception e) {
            Log.e(TAG, "Error in onViewCreated: ", e);
            Toast.makeText(requireContext(),
                    "초기화 오류: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void checkAuthAndLoadData() {
        try {
            if (!firebaseHelper.isSignedIn()) {
                // 익명 로그인
                showLoading(true);
                firebaseHelper.signInAnonymously(new FirebaseHelper.OnAuthCompleteListener() {
                    @Override
                    public void onSuccess(String userId) {
                        currentUserId = userId;
                        mainHandler.post(() -> {
                            showLoading(false);
                            loadRankingData();
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e(TAG, "Firebase Auth Failed: ", e);
                        mainHandler.post(() -> {
                            showLoading(false);
                            Toast.makeText(requireContext(),
                                    "로그인 실패: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        });
                    }
                });
            } else {
                currentUserId = firebaseHelper.getCurrentUserId();
                loadRankingData();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in checkAuthAndLoadData: ", e);
            Toast.makeText(requireContext(),
                    "인증 오류: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadMyData() {
        if (currentUserId == null) {
            Toast.makeText(requireContext(), "로그인이 필요합니다", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);

        new Thread(() -> {
            try {
                // 로컬 DB에서 이번 달 데이터 가져오기
                AppDatabase db = AppDatabase.getInstance(requireContext());
                ReceiptDao dao = db.receiptDao();

                // 이번 달 범위 계산
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                long startOfMonth = calendar.getTimeInMillis();

                calendar.add(Calendar.MONTH, 1);
                long startOfNextMonth = calendar.getTimeInMillis();

                List<Receipt> receipts = dao.getMonthlyReceipts(startOfMonth, startOfNextMonth);

                if (receipts.isEmpty()) {
                    mainHandler.post(() -> {
                        showLoading(false);
                        Toast.makeText(requireContext(),
                                "업로드할 데이터가 없습니다",
                                Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                // 이액 계산
                int monthlyTotal = 0;
                Map<String, Integer> categoryTotals = new HashMap<>();

                for (Receipt receipt : receipts) {
                    monthlyTotal += receipt.getTotalAmount();

                    String categoryName = receipt.getCategory().name();
                    categoryTotals.put(categoryName,
                            categoryTotals.getOrDefault(categoryName, 0) + receipt.getTotalAmount());
                }

                // Challenge 객체 생성
                calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH) + 1;

                String userName = preferenceHelper.getUserName();
                Challenge challenge = new Challenge(currentUserId, userName,
                        monthlyTotal, categoryTotals, year, month);

                // Firestore에 업로드
                firebaseHelper.getFirestore()
                        .collection("challenges")
                        .document(currentUserId + "_" + year + "_" + month)
                        .set(challenge.toMap())
                        .addOnSuccessListener(aVoid -> mainHandler.post(() -> {
                            showLoading(false);
                            Toast.makeText(requireContext(),
                                    "데이터가 업로드되었습니다!",
                                    Toast.LENGTH_SHORT).show();
                            loadRankingData();
                        }))
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Upload failed: ", e);
                            mainHandler.post(() -> {
                                showLoading(false);
                                Toast.makeText(requireContext(),
                                        "업로드 실패: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            });
                        });

            } catch (Exception e) {
                Log.e(TAG, "Error in uploadMyData: ", e);
                mainHandler.post(() -> {
                    showLoading(false);
                    Toast.makeText(requireContext(),
                            "오류: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void loadRankingData() {
        if (currentUserId == null) {
            Toast.makeText(requireContext(), "로그인이 필요합니다", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;

        try {
            firebaseHelper.getFirestore()
                    .collection("challenges")
                    .whereEqualTo("year", year)
                    .whereEqualTo("month", month)
                    .limit(50)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        List<Challenge> challenges = new ArrayList<>();

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Challenge challenge = document.toObject(Challenge.class);
                            challenge.setId(document.getId());
                            challenges.add(challenge);
                        }

                        java.util.Collections.sort(challenges, new java.util.Comparator<Challenge>() {
                            @Override
                            public int compare(Challenge c1, Challenge c2) {
                                return Integer.compare(c1.getMonthlyTotal(), c2.getMonthlyTotal());
                            }
                        });

                        mainHandler.post(() -> {
                            showLoading(false);
                            displayRanking(challenges);
                        });
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to load ranking: ", e);
                        mainHandler.post(() -> {
                            showLoading(false);
                            Toast.makeText(requireContext(),
                                    "랭킹 로드 실패: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        });
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error in loadRankingData: ", e);
            showLoading(false);
            Toast.makeText(requireContext(),
                    "오류: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void displayRanking(List<Challenge> challenges) {
        if (challenges.isEmpty()) {
            tvTotalUsers.setText("참여자: 0명");
            layoutMyRank.setVisibility(View.GONE);
            return;
        }

        // 총 참여자 수
        tvTotalUsers.setText("참여자: " + challenges.size() + "명");

        // 내 순위 찾기
        int myRank = -1;
        for (int i = 0; i < challenges.size(); i++) {
            if (challenges.get(i).getUserId().equals(currentUserId)) {
                myRank = i + 1;
                tvMyRank.setText(myRank + "위");
                tvMyTotal.setText(kr.ac.baekseok.savemate.utils.DateUtils.formatCurrency(
                        challenges.get(i).getMonthlyTotal()));
                break;
            }
        }

        if (myRank != -1) {
            layoutMyRank.setVisibility(View.VISIBLE);
        } else {
            layoutMyRank.setVisibility(View.GONE);
        }
        // RecyclerView 업데이트
        adapter.setChallenges(challenges, currentUserId);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnUploadData.setEnabled(!show);
        btnRefresh.setEnabled(!show);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (currentUserId != null) {
            loadRankingData();
        }
    }
}