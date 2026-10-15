package kr.ac.baekseok.savemate.ui.scan;

import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

import kr.ac.baekseok.savemate.R;

public class ImageViewerActivity extends AppCompatActivity {

    public static final String EXTRA_IMAGE_URI = "image_uri";

    private MaterialToolbar toolbar;
    private ImageView ivImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        toolbar = findViewById(R.id.toolbar);
        ivImage = findViewById(R.id.iv_image);

        // Toolbar 설정
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("영수증 이미지");
        }

        // 이미지 URI 가져오기
        String imageUriString = getIntent().getStringExtra(EXTRA_IMAGE_URI);
        if (imageUriString != null) {
            try {
                Uri imageUri = Uri.parse(imageUriString);
                ivImage.setImageURI(imageUri);
            } catch (Exception e) {
                Toast.makeText(this, "이미지를 불러올 수 없습니다", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(this, "이미지를 찾을 수 없습니다", Toast.LENGTH_SHORT).show();
            finish();
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