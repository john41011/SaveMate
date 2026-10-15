package kr.ac.baekseok.savemate;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import kr.ac.baekseok.savemate.ui.home.HomeFragment;
import kr.ac.baekseok.savemate.ui.statistics.StatisticsFragment;
import kr.ac.baekseok.savemate.ui.scan.ScanFragment;
import kr.ac.baekseok.savemate.ui.challenge.ChallengeFragment;
import kr.ac.baekseok.savemate.ui.profile.ProfileFragment;

public class MainActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // View 초기화
        toolbar = findViewById(R.id.toolbar);
        bottomNavigation = findViewById(R.id.bottom_navigation);

        // Toolbar 설정
        setSupportActionBar(toolbar);

        // 첫 화면 설정 (HomeFragment)
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
            toolbar.setTitle(R.string.nav_home);
        }

        // Bottom Navigation 리스너 설정
        bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            String title = "";

            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                selectedFragment = new HomeFragment();
                title = getString(R.string.nav_home);
            } else if (itemId == R.id.navigation_statistics) {
                selectedFragment = new StatisticsFragment();
                title = getString(R.string.nav_statistics);
            } else if (itemId == R.id.navigation_scan) {
                selectedFragment = new ScanFragment();
                title = getString(R.string.nav_scan);
            } else if (itemId == R.id.navigation_challenge) {
                selectedFragment = new ChallengeFragment();
                title = getString(R.string.nav_challenge);
            } else if (itemId == R.id.navigation_profile) {
                selectedFragment = new ProfileFragment();
                title = getString(R.string.nav_profile);
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
                toolbar.setTitle(title);
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.nav_host_fragment, fragment)
                .commit();
    }
}