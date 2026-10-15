package kr.ac.baekseok.savemate.ui.profile;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

import kr.ac.baekseok.savemate.R;
import kr.ac.baekseok.savemate.model.Category;
import kr.ac.baekseok.savemate.utils.PreferenceHelper;

public class CategoryBudgetActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private EditText etFoodBudget, etTransportBudget, etShoppingBudget,
            etEntertainmentBudget, etEtcBudget;
    private Button btnSave;

    private PreferenceHelper preferenceHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_budget);

        // View 초기화
        toolbar = findViewById(R.id.toolbar);
        etFoodBudget = findViewById(R.id.et_food_budget);
        etTransportBudget = findViewById(R.id.et_transport_budget);
        etShoppingBudget = findViewById(R.id.et_shopping_budget);
        etEntertainmentBudget = findViewById(R.id.et_entertainment_budget);
        etEtcBudget = findViewById(R.id.et_etc_budget);
        btnSave = findViewById(R.id.btn_save);

        // Toolbar 설정
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("카테고리별 예산 설정");
        }

        preferenceHelper = new PreferenceHelper(this);

        // 저장된 예산 불러오기
        loadBudgets();

        // 저장 버튼
        btnSave.setOnClickListener(v -> saveBudgets());
    }

    private void loadBudgets() {
        int foodBudget = preferenceHelper.getCategoryBudget(Category.FOOD.name());
        int transportBudget = preferenceHelper.getCategoryBudget(Category.TRANSPORT.name());
        int shoppingBudget = preferenceHelper.getCategoryBudget(Category.SHOPPING.name());
        int entertainmentBudget = preferenceHelper.getCategoryBudget(Category.ENTERTAINMENT.name());
        int etcBudget = preferenceHelper.getCategoryBudget(Category.ETC.name());

        if (foodBudget > 0) etFoodBudget.setText(String.valueOf(foodBudget));
        if (transportBudget > 0) etTransportBudget.setText(String.valueOf(transportBudget));
        if (shoppingBudget > 0) etShoppingBudget.setText(String.valueOf(shoppingBudget));
        if (entertainmentBudget > 0) etEntertainmentBudget.setText(String.valueOf(entertainmentBudget));
        if (etcBudget > 0) etEtcBudget.setText(String.valueOf(etcBudget));
    }

    private void saveBudgets() {
        try {
            // 식비
            String foodStr = etFoodBudget.getText().toString().trim();
            if (!foodStr.isEmpty()) {
                preferenceHelper.setCategoryBudget(Category.FOOD.name(), Integer.parseInt(foodStr));
            }

            // 교통
            String transportStr = etTransportBudget.getText().toString().trim();
            if (!transportStr.isEmpty()) {
                preferenceHelper.setCategoryBudget(Category.TRANSPORT.name(), Integer.parseInt(transportStr));
            }

            // 쇼핑
            String shoppingStr = etShoppingBudget.getText().toString().trim();
            if (!shoppingStr.isEmpty()) {
                preferenceHelper.setCategoryBudget(Category.SHOPPING.name(), Integer.parseInt(shoppingStr));
            }

            // 여가
            String entertainmentStr = etEntertainmentBudget.getText().toString().trim();
            if (!entertainmentStr.isEmpty()) {
                preferenceHelper.setCategoryBudget(Category.ENTERTAINMENT.name(), Integer.parseInt(entertainmentStr));
            }

            // 기타
            String etcStr = etEtcBudget.getText().toString().trim();
            if (!etcStr.isEmpty()) {
                preferenceHelper.setCategoryBudget(Category.ETC.name(), Integer.parseInt(etcStr));
            }

            Toast.makeText(this, "카테고리별 예산이 설정되었습니다", Toast.LENGTH_SHORT).show();
            finish();

        } catch (NumberFormatException e) {
            Toast.makeText(this, "올바른 금액을 입력하세요", Toast.LENGTH_SHORT).show();
        }
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