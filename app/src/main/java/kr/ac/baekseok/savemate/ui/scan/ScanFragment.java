package kr.ac.baekseok.savemate.ui.scan;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.card.MaterialCardView;
import kr.ac.baekseok.savemate.R;
import kr.ac.baekseok.savemate.utils.PermissionHelper;

public class ScanFragment extends Fragment {

    private MaterialCardView cardCamera, cardGallery, cardManualInput;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<String[]> permissionLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_scan, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cardCamera = view.findViewById(R.id.card_camera);
        cardGallery = view.findViewById(R.id.card_gallery);
        cardManualInput = view.findViewById(R.id.card_manual_input);

        setupActivityLaunchers();

        // 카메라 버튼
        cardCamera.setOnClickListener(v -> {
            if (PermissionHelper.hasCameraPermission((Activity) requireContext())) {
                launchCamera();
            } else {
                requestCameraPermission();
            }
        });

        // 갤러리 버튼
        cardGallery.setOnClickListener(v -> {
            if (PermissionHelper.hasGalleryPermission((Activity) requireContext())) {
                launchGallery();
            } else {
                requestGalleryPermission();
            }
        });

        // 직접 입력 버튼 (새로 추가!)
        cardManualInput.setOnClickListener(v -> launchManualInput());
    }

    private void setupActivityLaunchers() {
        // 카메라 결과
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == android.app.Activity.RESULT_OK) {
                        // 홈으로 이동하거나 새로고침
                        Toast.makeText(requireContext(), "영수증이 저장되었습니다", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // 갤러리 결과
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                        android.net.Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            Intent intent = new Intent(requireContext(), ConfirmReceiptActivity.class);
                            intent.putExtra(ConfirmReceiptActivity.EXTRA_IMAGE_URI, imageUri.toString());
                            startActivity(intent);
                        }
                    }
                }
        );

        // 권한 요청 결과
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                permissions -> {
                    Boolean cameraGranted = permissions.get(android.Manifest.permission.CAMERA);
                    if (cameraGranted != null && cameraGranted) {
                        launchCamera();
                    } else {
                        Toast.makeText(requireContext(), "카메라 권한이 필요합니다", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void launchCamera() {
        Intent intent = new Intent(requireContext(), CameraActivity.class);
        cameraLauncher.launch(intent);
    }

    private void launchGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    // 직접 입력
    private void launchManualInput() {
        Intent intent = new Intent(requireContext(), ManualInputActivity.class);
        startActivity(intent);
    }

    private void requestCameraPermission() {
        permissionLauncher.launch(new String[]{android.Manifest.permission.CAMERA});
    }

    private void requestGalleryPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(new String[]{android.Manifest.permission.READ_MEDIA_IMAGES});
        } else {
            permissionLauncher.launch(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE});
        }
    }
}