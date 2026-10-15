package kr.ac.baekseok.savemate.ui.scan;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import kr.ac.baekseok.savemate.R;
import kr.ac.baekseok.savemate.database.ReceiptRepository;
import kr.ac.baekseok.savemate.model.Category;
import kr.ac.baekseok.savemate.model.Receipt;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ManualInputActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextInputEditText etStoreName, etDate, etTotalAmount, etMemo;
    private Spinner spinnerCategory;
    private Button btnSave, btnCancel;

    private ReceiptRepository repository;
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_input);

        // View 초기화
        toolbar = findViewById(R.id.toolbar);
        etStoreName = findViewById(R.id.et_store_name);
        etDate = findViewById(R.id.et_date);
        etTotalAmount = findViewById(R.id.et_total_amount);
        etMemo = findViewById(R.id.et_memo);
        spinnerCategory = findViewById(R.id.spinner_category);
        btnSave = findViewById(R.id.btn_save);
        btnCancel = findViewById(R.id.btn_cancel);

        // Toolbar 설정
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("영수증 직접 입력");
        }

        repository = new ReceiptRepository(this);
        mainHandler = new Handler(Looper.getMainLooper());

        // 카테고리 Spinner 설정
        setupCategorySpinner();

        // 오늘 날짜 기본값
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
        etDate.setText(sdf.format(new Date()));

        // 버튼 리스너
        btnSave.setOnClickListener(v -> saveReceipt());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void setupCategorySpinner() {
        String[] categories = new String[Category.values().length];
        for (int i = 0; i < Category.values().length; i++) {
            categories[i] = Category.values()[i].getDisplayName();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private void saveReceipt() {
        // 입력값 검증
        String storeName = etStoreName.getText().toString().trim();
        String dateStr = etDate.getText().toString().trim();
        String amountStr = etTotalAmount.getText().toString().trim();
        String memo = etMemo.getText().toString().trim();

        if (storeName.isEmpty()) {
            etStoreName.setError("상호명을 입력하세요");
            etStoreName.requestFocus();
            return;
        }

        if (dateStr.isEmpty()) {
            etDate.setError("날짜를 입력하세요");
            etDate.requestFocus();
            return;
        }

        if (amountStr.isEmpty()) {
            etTotalAmount.setError("금액을 입력하세요");
            etTotalAmount.requestFocus();
            return;
        }

        // 날짜 파싱
        Date purchaseDate;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
            purchaseDate = sdf.parse(dateStr);
        } catch (ParseException e) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd", Locale.KOREA);
                purchaseDate = sdf.parse(dateStr);
            } catch (ParseException ex) {
                etDate.setError("올바른 날짜 형식이 아닙니다 (yyyy-MM-dd)");
                etDate.requestFocus();
                return;
            }
        }

        // 금액 파싱
        int totalAmount;
        try {
            totalAmount = Integer.parseInt(amountStr);
            if (totalAmount <= 0) {
                etTotalAmount.setError("0보다 큰 금액을 입력하세요");
                etTotalAmount.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            etTotalAmount.setError("올바른 금액이 아닙니다");
            etTotalAmount.requestFocus();
            return;
        }

        // 카테고리 선택
        int categoryPosition = spinnerCategory.getSelectedItemPosition();
        Category category = Category.values()[categoryPosition];

        // Receipt 객체 생성
        Receipt receipt = new Receipt(storeName, purchaseDate, totalAmount, category);
        receipt.setMemo(memo.isEmpty() ? null : memo);
        // 이미지 경로 없음 (직접 입력)

        // DB에 저장
        repository.insertReceiptWithItems(receipt, new ArrayList<>(),
                receiptId -> mainHandler.post(() -> {
                    Toast.makeText(this, "저장되었습니다", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                }));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}