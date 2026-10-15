package kr.ac.baekseok.savemate.utils;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy.MM.dd", Locale.KOREA);
    private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.KOREA);
    private static final SimpleDateFormat MONTH_FORMAT = new SimpleDateFormat("MM월", Locale.KOREA);
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getInstance(Locale.KOREA);

    // yyyy.MM.dd 형식
    public static String formatDate(Date date) {
        if (date == null) return "";
        return DATE_FORMAT.format(date);
    }

    // yyyy.MM.dd HH:mm 형식
    public static String formatDateTime(Date date) {
        if (date == null) return "";
        return DATE_TIME_FORMAT.format(date);
    }

    // 월만 표시
    public static String formatMonth(Date date) {
        if (date == null) return "";
        return MONTH_FORMAT.format(date);
    }

    // 금액 포맷 "1,234,567원"
    public static String formatCurrency(int amount) {
        return CURRENCY_FORMAT.format(amount) + "원";
    }

    // 금액 포맷 (원 제외)
    public static String formatAmount(int amount) {
        return CURRENCY_FORMAT.format(amount);
    }
}