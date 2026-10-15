package kr.ac.baekseok.savemate.ui.scan;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import kr.ac.baekseok.savemate.R;
import kr.ac.baekseok.savemate.database.ReceiptRepository;
import kr.ac.baekseok.savemate.model.Category;
import kr.ac.baekseok.savemate.model.Receipt;
import kr.ac.baekseok.savemate.utils.DateUtils;

public class EditReceiptActivity extends AppCompatActivity {

    public static final String EXTRA_RECEIPT_ID = "receipt_id";

    private MaterialToolbar toolbar;
    private EditText etStoreName, etDate, etTotalAmount, etMemo;
    private Spinner spinnerCategory;
    private Button btnSave, btnCancel;

    private ReceiptRepository repository;
    private Handler mainHandler;
    private Receipt receipt;
    private long receiptId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_receipt);

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
            getSupportActionBar().setTitle("영수증 수정");
        }

        // Repository & Handler 초기화
        repository = new ReceiptRepository(this);
        mainHandler = new Handler(Looper.getMainLooper());

        // 카테고리 Spinner 설정
        setupCategorySpinner();

        // Receipt ID 가져오기
        receiptId = getIntent().getLongExtra(EXTRA_RECEIPT_ID, -1);
        if (receiptId == -1) {
            Toast.makeText(this, "영수증을 찾을 수 없습니다", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 데이터 로드
        loadReceipt();

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

    private void loadReceipt() {
        new Thread(() -> {
            kr.ac.baekseok.savemate.database.AppDatabase db =
                    kr.ac.baekseok.savemate.database.AppDatabase.getInstance(this);
            receipt = db.receiptDao().getReceiptById(receiptId);

            mainHandler.post(() -> {
                if (receipt != null) {
                    fillData();
                } else {
                    Toast.makeText(this, "영수증 정보를 불러올 수 없습니다", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        }).start();
    }

    private void fillData() {
        etStoreName.setText(receipt.getStoreName());
        etDate.setText(DateUtils.formatDate(receipt.getPurchaseDate()));
        etTotalAmount.setText(String.valueOf(receipt.getTotalAmount()));

        if (receipt.getMemo() != null) {
            etMemo.setText(receipt.getMemo());
        }

        // 카테고리 선택
        for (int i = 0; i < Category.values().length; i++) {
            if (Category.values()[i] == receipt.getCategory()) {
                spinnerCategory.setSelection(i);
                break;
            }
        }
    }

    private void saveReceipt() {
        // 입력값 검증
        String storeName = etStoreName.getText().toString().trim();
        String dateStr = etDate.getText().toString().trim();
        String amountStr = etTotalAmount.getText().toString().trim();
        String memo = etMemo.getText().toString().trim();

        if (storeName.isEmpty()) {
            etStoreName.setError("상호명을 입력하세요");
            return;
        }

        if (dateStr.isEmpty()) {
            etDate.setError("날짜를 입력하세요");
            return;
        }

        if (amountStr.isEmpty()) {
            etTotalAmount.setError("금액을 입력하세요");
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
                return;
            }
        }

        int totalAmount;
        try {
            totalAmount = Integer.parseInt(amountStr);
        } catch (NumberFormatException e) {
            etTotalAmount.setError("올바른 금액이 아닙니다");
            return;
        }

        // 카테고리 선택
        int categoryPosition = spinnerCategory.getSelectedItemPosition();
        Category category = Category.values()[categoryPosition];

        // Receipt 업데이트
        receipt.setStoreName(storeName);
        receipt.setPurchaseDate(purchaseDate);
        receipt.setTotalAmount(totalAmount);
        receipt.setCategory(category);
        receipt.setMemo(memo.isEmpty() ? null : memo);

        // DB에 저장
        new Thread(() -> {
            kr.ac.baekseok.savemate.database.AppDatabase db =
                    kr.ac.baekseok.savemate.database.AppDatabase.getInstance(this);
            db.receiptDao().update(receipt);

            mainHandler.post(() -> {
                Toast.makeText(this, "수정되었습니다", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            });
        }).start();
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