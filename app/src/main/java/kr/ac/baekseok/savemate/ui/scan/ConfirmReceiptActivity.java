package kr.ac.baekseok.savemate.ui.scan;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.mlkit.vision.common.InputImage;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import kr.ac.baekseok.savemate.R;
import kr.ac.baekseok.savemate.database.ReceiptRepository;
import kr.ac.baekseok.savemate.model.Category;
import kr.ac.baekseok.savemate.model.Receipt;
import kr.ac.baekseok.savemate.utils.OcrHelper;

public class ConfirmReceiptActivity extends AppCompatActivity {

    public static final String EXTRA_IMAGE_URI = "image_uri";
    private static final String TAG = "ConfirmReceipt";

    private ImageView ivReceipt;
    private EditText etStoreName, etDate, etTotalAmount;
    private Spinner spinnerCategory;
    private Button btnSave, btnCancel;
    private ProgressBar progressBar;
    private TextView tvProcessing;

    private Uri imageUri;
    private OcrHelper ocrHelper;
    private ReceiptRepository repository;
    private Handler mainHandler;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_receipt);

        // View 초기화
        ivReceipt = findViewById(R.id.iv_receipt);
        etStoreName = findViewById(R.id.et_store_name);
        etDate = findViewById(R.id.et_date);
        etTotalAmount = findViewById(R.id.et_total_amount);
        spinnerCategory = findViewById(R.id.spinner_category);
        btnSave = findViewById(R.id.btn_save);
        btnCancel = findViewById(R.id.btn_cancel);
        progressBar = findViewById(R.id.progress_bar);
        tvProcessing = findViewById(R.id.tv_processing);

        // Helper 초기화
        ocrHelper = new OcrHelper();
        repository = new ReceiptRepository(this);
        mainHandler = new Handler(Looper.getMainLooper());

        // 카테고리 Spinner 설정
        setupCategorySpinner();

        // Intent에서 이미지 URI 가져오기
        String uriString = getIntent().getStringExtra(EXTRA_IMAGE_URI);
        if (uriString != null) {
            imageUri = Uri.parse(uriString);
            ivReceipt.setImageURI(imageUri);
            processImage();
        } else {
            Toast.makeText(this, "이미지를 불러올 수 없습니다", Toast.LENGTH_SHORT).show();
            finish();
        }

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

    private void processImage() {
        showProcessing(true);

        try {
            InputImage image = InputImage.fromFilePath(this, imageUri);
            ocrHelper.extractTextFromImage(image, new OcrHelper.OnTextExtractedListener() {
                @Override
                public void onSuccess(String text, com.google.mlkit.vision.text.Text visionText) {
                    mainHandler.post(() -> {
                        OcrHelper.ReceiptData data = OcrHelper.parseReceiptText(text);
                        fillData(data);
                        showProcessing(false);
                    });
                }

                @Override
                public void onFailure(Exception e) {
                    mainHandler.post(() -> {
                        Toast.makeText(ConfirmReceiptActivity.this,
                                "텍스트 인식 실패: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        showProcessing(false);
                    });
                }
            });
        } catch (IOException e) {
            Toast.makeText(this, "이미지 처리 실패", Toast.LENGTH_SHORT).show();
            showProcessing(false);
        }
    }

    private void fillData(OcrHelper.ReceiptData data) {
        etStoreName.setText(data.storeName);
        etDate.setText(data.date);
        etTotalAmount.setText(String.valueOf(data.totalAmount));
    }

    private void showProcessing(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        tvProcessing.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSave.setEnabled(!show);
    }

    private void saveReceipt() {
        // 입력값 검증
        String storeName = etStoreName.getText().toString().trim();
        String dateStr = etDate.getText().toString().trim();
        String amountStr = etTotalAmount.getText().toString().trim();

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
            // 다른 형식 시도
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

        // Receipt 객체 생성
        Receipt receipt = new Receipt(storeName, purchaseDate, totalAmount, category);
        receipt.setImagePath(imageUri.toString());

        // DB에 저장
        repository.insertReceiptWithItems(receipt, new java.util.ArrayList<>(),
                receiptId -> mainHandler.post(() -> {
                    Toast.makeText(this, "저장되었습니다", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                }));
    }
}