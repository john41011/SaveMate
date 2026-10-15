package kr.ac.baekseok.savemate.model;

import java.util.HashMap;
import java.util.Map;

public class Challenge {

    private String id;
    private String userId;
    private String userName;
    private int monthlyTotal;
    private Map<String, Integer> categoryTotals;
    private long timestamp;
    private int year;
    private int month;

    public Challenge() {
        // Firestore용 기본 생성자
        categoryTotals = new HashMap<>();
    }

    public Challenge(String userId, String userName, int monthlyTotal,
                     Map<String, Integer> categoryTotals, int year, int month) {
        this.userId = userId;
        this.userName = userName;
        this.monthlyTotal = monthlyTotal;
        this.categoryTotals = categoryTotals != null ? categoryTotals : new HashMap<>();
        this.timestamp = System.currentTimeMillis();
        this.year = year;
        this.month = month;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getMonthlyTotal() {
        return monthlyTotal;
    }

    public void setMonthlyTotal(int monthlyTotal) {
        this.monthlyTotal = monthlyTotal;
    }

    public Map<String, Integer> getCategoryTotals() {
        return categoryTotals;
    }

    public void setCategoryTotals(Map<String, Integer> categoryTotals) {
        this.categoryTotals = categoryTotals;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    // Firestore 저장용 Map으로 변환
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("userName", userName);
        map.put("monthlyTotal", monthlyTotal);
        map.put("categoryTotals", categoryTotals);
        map.put("timestamp", timestamp);
        map.put("year", year);
        map.put("month", month);
        return map;
    }
}