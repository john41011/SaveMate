package kr.ac.baekseok.savemate.utils;

import android.content.Context;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirebaseHelper {

    private static FirebaseHelper instance;
    private final FirebaseAuth auth;
    private final FirebaseFirestore firestore;
    private final PreferenceHelper preferenceHelper;

    private FirebaseHelper(Context context) {
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        preferenceHelper = new PreferenceHelper(context);
    }

    public static synchronized FirebaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new FirebaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    // 익명 로그인
    public void signInAnonymously(OnAuthCompleteListener listener) {
        auth.signInAnonymously()
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user != null && listener != null) {
                        listener.onSuccess(user.getUid());
                    }
                })
                .addOnFailureListener(e -> {
                    if (listener != null) {
                        listener.onFailure(e);
                    }
                });
    }

    // 현재 사용자 UID 가져오기
    public String getCurrentUserId() {
        FirebaseUser user = auth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    // 로그인 여부 확인
    public boolean isSignedIn() {
        return auth.getCurrentUser() != null;
    }

    // Firestore 인스턴스 가져오기
    public FirebaseFirestore getFirestore() {
        return firestore;
    }

    // 콜백 인터페이스
    public interface OnAuthCompleteListener {
        void onSuccess(String userId);

        void onFailure(Exception e);
    }
}