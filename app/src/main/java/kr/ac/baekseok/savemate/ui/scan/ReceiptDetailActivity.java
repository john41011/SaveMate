package kr.ac.baekseok.savemate.ui.scan;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import kr.ac.baekseok.savemate.R;
import kr.ac.baekseok.savemate.adapter.ItemAdapter;
import kr.ac.baekseok.savemate.database.ReceiptRepository;
import kr.ac.baekseok.savemate.model.Receipt;
import kr.ac.baekseok.savemate.utils.DateUtils;

public class ReceiptDetailActivity extends AppCompatActivity {

    public static final String EXTRA_RECEIPT_ID = "receipt_id";
    private static final int REQUEST_EDIT = 1001;

    private MaterialToolbar toolbar;
    private ImageView ivReceiptImage;
    private TextView tvStoreName, tvDate, tvCategory, tvTotalAmount, tvMemo;
    private RecyclerView rvItems;
    private FloatingActionButton fabEdit;
    private View layoutNoImage, layoutMemo;

    private ReceiptRepository repository;
    private Handler mainHandler;
    private Receipt receipt;
    private ItemAdapter itemAdapter;
    private long receiptId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt_detail);

        // View 초기화
        toolbar = findViewById(R.id.toolbar);
        ivReceiptImage = findViewById(R.id.iv_receipt_image);
        tvStoreName = findViewById(R.id.tv_store_name);
        tvDate = findViewById(R.id.tv_date);
        tvCategory = findViewById(R.id.tv_category);
        tvTotalAmount = findViewById(R.id.tv_total_amount);
        tvMemo = findViewById(R.id.tv_memo);
        rvItems = findViewById(R.id.rv_items);
        fabEdit = findViewById(R.id.fab_edit);
        layoutNoImage = findViewById(R.id.layout_no_image);
        layoutMemo = findViewById(R.id.layout_memo);

        // Toolbar 설정
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("영수증 상세");
        }

        // Repository & Handler 초기화
        repository = new ReceiptRepository(this);
        mainHandler = new Handler(Looper.getMainLooper());

        // RecyclerView 설정
        itemAdapter = new ItemAdapter();
        rvItems.setLayoutManager(new LinearLayoutManager(this));
        rvItems.setAdapter(itemAdapter);

        // Receipt ID 가져오기
        receiptId = getIntent().getLongExtra(EXTRA_RECEIPT_ID, -1);
        if (receiptId == -1) {
            Toast.makeText(this, "영수증을 찾을 수 없습니다", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 데이터 로드
        loadReceiptDetail();

        // FAB 클릭 - 수정
        fabEdit.setOnClickListener(v -> editReceipt());

        // 이미지 클릭 - 확대
        ivReceiptImage.setOnClickListener(v -> {
            if (receipt != null && receipt.getImagePath() != null) {
                openImageViewer(receipt.getImagePath());
            }
        });
    }

    private void loadReceiptDetail() {
        // 영수증 정보 로드
        new Thread(() -> {
            kr.ac.baekseok.savemate.database.AppDatabase db =
                    kr.ac.baekseok.savemate.database.AppDatabase.getInstance(this);
            receipt = db.receiptDao().getReceiptById(receiptId);

            mainHandler.post(() -> {
                if (receipt != null) {
                    displayReceiptInfo();
                } else {
                    Toast.makeText(this, "영수증 정보를 불러올 수 없습니다", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        }).start();

        // 품목 리스트 로드
        repository.getItemsByReceiptId(receiptId, items -> mainHandler.post(() -> {
            if (items != null && !items.isEmpty()) {
                itemAdapter.setItems(items);
                rvItems.setVisibility(View.VISIBLE);
            } else {
                rvItems.setVisibility(View.GONE);
            }
        }));
    }

    private void displayReceiptInfo() {
        tvStoreName.setText(receipt.getStoreName());
        tvDate.setText(DateUtils.formatDate(receipt.getPurchaseDate()));
        tvCategory.setText(receipt.getCategory().getDisplayName());
        tvTotalAmount.setText(DateUtils.formatCurrency(receipt.getTotalAmount()));

        // 카테고리 색상
        try {
            int color = android.graphics.Color.parseColor(receipt.getCategory().getColor());
            tvCategory.setBackgroundColor(color);
        } catch (Exception ignored) {
        }

        // 메모
        if (receipt.getMemo() != null && !receipt.getMemo().isEmpty()) {
            tvMemo.setText(receipt.getMemo());
            layoutMemo.setVisibility(View.VISIBLE);
        } else {
            layoutMemo.setVisibility(View.GONE);
        }

        // 이미지
        if (receipt.getImagePath() != null && !receipt.getImagePath().isEmpty()) {
            try {
                Uri imageUri = Uri.parse(receipt.getImagePath());
                ivReceiptImage.setImageURI(imageUri);
                ivReceiptImage.setVisibility(View.VISIBLE);
                layoutNoImage.setVisibility(View.GONE);
            } catch (Exception e) {
                ivReceiptImage.setVisibility(View.GONE);
                layoutNoImage.setVisibility(View.VISIBLE);
            }
        } else {
            ivReceiptImage.setVisibility(View.GONE);
            layoutNoImage.setVisibility(View.VISIBLE);
        }
    }

    private void editReceipt() {
        Intent intent = new Intent(this, EditReceiptActivity.class);
        intent.putExtra(EditReceiptActivity.EXTRA_RECEIPT_ID, receiptId);
        startActivityForResult(intent, REQUEST_EDIT);
    }

    private void openImageViewer(String imagePath) {
        Intent intent = new Intent(this, ImageViewerActivity.class);
        intent.putExtra(ImageViewerActivity.EXTRA_IMAGE_URI, imagePath);
        startActivity(intent);
    }

    private void deleteReceipt() {
        new AlertDialog.Builder(this)
                .setTitle("영수증 삭제")
                .setMessage("정말로 이 영수증을 삭제하시겠습니까?")
                .setPositiveButton("삭제", (dialog, which) -> {
                    repository.deleteReceipt(receipt, () -> mainHandler.post(() -> {
                        Toast.makeText(this, "삭제되었습니다", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    }));
                })
                .setNegativeButton("취소", null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_receipt_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        } else if (itemId == R.id.action_delete) {
            deleteReceipt();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_EDIT && resultCode == RESULT_OK) {
            // 수정 후 다시 로드
            loadReceiptDetail();
        }
    }
}