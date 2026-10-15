package kr.ac.baekseok.savemate.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceHelper {

    private static final String PREF_NAME = "SaveMatePrefs";

    // Keys
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_MONTHLY_BUDGET = "monthly_budget";
    private static final String KEY_NOTIFICATION_ENABLED = "notification_enabled";
    private static final String KEY_FIRST_RUN = "first_run";

    private final SharedPreferences prefs;

    public PreferenceHelper(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // 사용자 이름
    public void setUserName(String name) {
        prefs.edit().putString(KEY_USER_NAME, name).apply();
    }

    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, "사용자");
    }

    // 월 예산
    public void setMonthlyBudget(int budget) {
        prefs.edit().putInt(KEY_MONTHLY_BUDGET, budget).apply();
    }

    public int getMonthlyBudget() {
        return prefs.getInt(KEY_MONTHLY_BUDGET, 0);
    }

    // 알림 설정
    public void setNotificationEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_NOTIFICATION_ENABLED, enabled).apply();
    }

    public boolean isNotificationEnabled() {
        return prefs.getBoolean(KEY_NOTIFICATION_ENABLED, true);
    }

    // 첫 실행 여부
    public void setFirstRun(boolean isFirst) {
        prefs.edit().putBoolean(KEY_FIRST_RUN, isFirst).apply();
    }

    public boolean isFirstRun() {
        return prefs.getBoolean(KEY_FIRST_RUN, true);
    }

    // 카테고리별 예산
    public void setCategoryBudget(String categoryName, int budget) {
        prefs.edit().putInt("budget_" + categoryName, budget).apply();
    }

    public int getCategoryBudget(String categoryName) {
        return prefs.getInt("budget_" + categoryName, 0);
    }
}