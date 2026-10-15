package kr.ac.baekseok.savemate.utils;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OcrHelper {
    private com.google.mlkit.vision.text.TextRecognizer recognizer;

    public OcrHelper() {
        recognizer = com.google.mlkit.vision.text.TextRecognition.getClient(
                new com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions.Builder().build()
        );
    }

    public void extractTextFromImage(InputImage image, OnTextExtractedListener listener) {
        recognizer.process(image)
                .addOnSuccessListener(visionText -> {
                    String text = visionText.getText();
                    listener.onSuccess(text, visionText);
                })
                .addOnFailureListener(listener::onFailure);
    }

    public static ReceiptData parseReceiptText(String text) {
        ReceiptData data = new ReceiptData();

        if (text == null || text.isEmpty()) {
            return data;
        }

        String[] lines = text.split("\n");

        data.storeName = extractStoreName(lines);
        data.date = extractDate(lines);
        data.totalAmount = extractTotalAmount(lines);

        return data;
    }

    private static String extractStoreName(String[] lines) {
        // 우선순위 1: 상단 3줄 내에서 회사명 찾기
        for (int i = 0; i < Math.min(5, lines.length); i++) {
            String line = lines[i].trim();

            // 제외할 키워드
            if (line.contains("사업자") || line.contains("대표") ||
                    line.contains("전화") || line.contains("주소") ||
                    line.contains("TEL") || line.isEmpty()) {
                continue;
            }

            // 회사명 키워드
            if (line.contains("(주)") || line.contains("주식회사") ||
                    line.contains("유한회사") || line.contains("㈜")) {
                // 괄호나 특수문자 제거
                String cleaned = line.replaceAll("[\\[\\]()]", "").trim();
                if (cleaned.length() >= 2 && cleaned.length() <= 30) {
                    return cleaned;
                }
            }

            // 한글이 많은 첫 번째 줄 (회사명 가능성)
            if (countKorean(line) >= 2 && line.length() >= 2 && line.length() <= 30) {
                // 숫자가 너무 많으면 제외 (전화번호, 사업자번호)
                if (line.replaceAll("[^0-9]", "").length() < line.length() / 2) {
                    return line;
                }
            }
        }

        // 우선순위 2: 전체에서 회사명 패턴 찾기
        for (String line : lines) {
            String cleaned = line.trim();
            if (cleaned.contains("(주)") || cleaned.contains("㈜")) {
                cleaned = cleaned.replaceAll("[\\[\\]()]", "").trim();
                if (cleaned.length() >= 2 && cleaned.length() <= 30) {
                    return cleaned;
                }
            }
        }

        return "상호명 없음";
    }

    private static String extractDate(String[] lines) {
        String fullText = String.join("\n", lines);

        // 날짜 관련 키워드 근처에서 찾기
        String[] dateKeywords = {"판매일", "거래일", "재인쇄", "승인일시", "date", "Date"};

        for (String keyword : dateKeywords) {
            int keywordIndex = fullText.indexOf(keyword);
            if (keywordIndex != -1) {
                // 키워드 이후 100자 내에서 날짜 찾기
                String searchArea = fullText.substring(keywordIndex,
                        Math.min(keywordIndex + 100, fullText.length()));

                // yy-MM-dd 또는 yyyy-MM-dd 패턴
                Pattern pattern1 = Pattern.compile("(\\d{2,4})[-./년](\\d{1,2})[-./월](\\d{1,2})");
                Matcher matcher1 = pattern1.matcher(searchArea);
                if (matcher1.find()) {
                    return convertToStandardDate(matcher1.group());
                }
            }
        }

        // 키워드 없이 날짜 패턴 찾기
        // yyyy-MM-dd 형식
        Pattern pattern1 = Pattern.compile("(20\\d{2})[-./년]?(\\d{1,2})[-./월]?(\\d{1,2})");
        Matcher matcher1 = pattern1.matcher(fullText);
        if (matcher1.find()) {
            return convertToStandardDate(matcher1.group());
        }

        // yy-MM-dd 형식 (2자리 연도)
        Pattern pattern2 = Pattern.compile("\\b(\\d{2})[-./](\\d{1,2})[-./](\\d{1,2})\\b");
        Matcher matcher2 = pattern2.matcher(fullText);
        if (matcher2.find()) {
            String dateStr = matcher2.group();
            // 26-11-24 → 2024-11-24로 변환
            return convertToStandardDate(dateStr);
        }

        // 기본값: 오늘 날짜
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
        return sdf.format(new Date());
    }

    private static String convertToStandardDate(String dateStr) {
        // 공백, 한글 제거
        dateStr = dateStr.replaceAll("[년월일\\s]", "-")
                .replaceAll("[./]", "-")
                .replaceAll("-+", "-")
                .trim();

        // yy-MM-dd → yyyy-MM-dd 변환
        String[] parts = dateStr.split("-");
        if (parts.length == 3) {
            String year = parts[0];
            String month = parts[1];
            String day = parts[2];

            // 2자리 연도를 4자리로 변환
            if (year.length() == 2) {
                int yy = Integer.parseInt(year);
                // 00-50 → 2000-2050, 51-99 → 1951-1999
                if (yy <= 50) {
                    year = "20" + year;
                } else {
                    year = "19" + year;
                }
            }

            // 월, 일을 2자리로
            if (month.length() == 1) month = "0" + month;
            if (day.length() == 1) day = "0" + day;

            return year + "-" + month + "-" + day;
        }

        return dateStr;
    }

    private static int extractTotalAmount(String[] lines) {
        // 우선순위 1: "합계" 키워드가 있는 줄에서 직접 추출
        String[] highPriorityKeywords = {
                "합계", "합  계", "합   계",
                "총매출액", "총 매출액", "총 매 출 액",
                "단기금액", "단 기 금 액", "단기 금액",
                "받은금액", "받 은 금 액", "받은 금액",
                "결제금액", "결 제 금 액",
                "지불금액", "지 불 금 액"
        };

        // 가장 높은 우선순위: "합계" 줄
        for (String line : lines) {
            String cleanLine = line.replaceAll("\\s+", "");

            // "합계" 키워드 확인
            for (String keyword : highPriorityKeywords) {
                String cleanKeyword = keyword.replace(" ", "");

                if (cleanLine.contains(cleanKeyword)) {
                    // 같은 줄에서 금액 추출
                    List<Integer> amounts = extractAmountsFromLine(line);

                    if (!amounts.isEmpty()) {
                        // 가장 큰 금액 반환 (일반적으로 합계가 가장 큼)
                        int maxAmount = 0;
                        for (int amount : amounts) {
                            if (amount > maxAmount && amount >= 100 && amount <= 10000000) {
                                maxAmount = amount;
                            }
                        }

                        if (maxAmount > 0) {
                            return maxAmount;
                        }
                    }

                    // 같은 줄에 금액이 없으면 다음 줄 확인
                    int currentIndex = java.util.Arrays.asList(lines).indexOf(line);
                    if (currentIndex < lines.length - 1) {
                        List<Integer> nextLineAmounts = extractAmountsFromLine(lines[currentIndex + 1]);
                        if (!nextLineAmounts.isEmpty()) {
                            int maxAmount = 0;
                            for (int amount : nextLineAmounts) {
                                if (amount > maxAmount && amount >= 100 && amount <= 10000000) {
                                    maxAmount = amount;
                                }
                            }
                            if (maxAmount > 0) {
                                return maxAmount;
                            }
                        }
                    }
                }
            }
        }

        // 우선순위 2: 전체에서 가장 빈도 높고 큰 금액
        Map<Integer, Integer> amountFrequency = new HashMap<>();

        for (String line : lines) {
            List<Integer> amounts = extractAmountsFromLine(line);
            for (int amount : amounts) {
                if (amount >= 1000 && amount <= 10000000) {
                    amountFrequency.put(amount,
                            amountFrequency.getOrDefault(amount, 0) + 1);
                }
            }
        }

        // 빈도가 높은 큰 금액 찾기 (3번 이상 나타나는 금액)
        int bestAmount = 0;
        int maxFrequency = 0;

        for (Map.Entry<Integer, Integer> entry : amountFrequency.entrySet()) {
            int amount = entry.getKey();
            int frequency = entry.getValue();

            // 빈도가 높고 금액이 크면 우선
            if (frequency >= 3 && amount > bestAmount) {
                bestAmount = amount;
                maxFrequency = frequency;
            }
        }

        if (bestAmount > 0) {
            return bestAmount;
        }

        // 우선순위 3: 가장 큰 금액
        int maxAmount = 0;
        for (int amount : amountFrequency.keySet()) {
            if (amount > maxAmount) {
                maxAmount = amount;
            }
        }

        return maxAmount;
    }

    private static List<Integer> extractAmountsFromLine(String line) {
        List<Integer> amounts = new ArrayList<>();

        // 콤마 포함 숫자 패턴: 12,200
        Pattern pattern1 = Pattern.compile("(\\d{1,3}(?:,\\d{3})+)");
        Matcher matcher1 = pattern1.matcher(line);
        while (matcher1.find()) {
            try {
                String amountStr = matcher1.group(1).replace(",", "");
                int amount = Integer.parseInt(amountStr);
                if (amount <= 10000000) {
                    amounts.add(amount);
                }
            } catch (NumberFormatException ignored) {}
        }

        // 연속된 숫자 패턴 (3자리 이상): 12200
        Pattern pattern2 = Pattern.compile("\\b(\\d{3,})\\b");
        Matcher matcher2 = pattern2.matcher(line);
        while (matcher2.find()) {
            try {
                int amount = Integer.parseInt(matcher2.group(1));
                // 합리적인 범위의 금액만
                if (amount >= 100 && amount <= 10000000) {
                    // 전화번호나 긴 숫자 제외 (7자리 이상은 제외)
                    if (matcher2.group(1).length() <= 6) {
                        amounts.add(amount);
                    }
                }
            } catch (NumberFormatException ignored) {}
        }

        return amounts;
    }

    private static List<Integer> extractAmounts(String text) {
        List<Integer> amounts = new ArrayList<>();

        // 콤마 포함 숫자 패턴: 1,000 또는 10,000
        Pattern pattern1 = Pattern.compile("(\\d{1,3}(?:,\\d{3})+)");
        Matcher matcher1 = pattern1.matcher(text);
        while (matcher1.find()) {
            try {
                String amountStr = matcher1.group(1).replace(",", "");
                int amount = Integer.parseInt(amountStr);
                amounts.add(amount);
            } catch (NumberFormatException ignored) {}
        }

        // 연속된 숫자 패턴 (3자리 이상)
        Pattern pattern2 = Pattern.compile("\\b(\\d{3,})\\b");
        Matcher matcher2 = pattern2.matcher(text);
        while (matcher2.find()) {
            try {
                int amount = Integer.parseInt(matcher2.group(1));
                // 너무 큰 숫자는 제외 (전화번호, 사업자번호 등)
                if (amount <= 10000000) {
                    amounts.add(amount);
                }
            } catch (NumberFormatException ignored) {}
        }

        return amounts;
    }

    private static int countKorean(String text) {
        int count = 0;
        for (char c : text.toCharArray()) {
            if (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.HANGUL_SYLLABLES ||
                    Character.UnicodeBlock.of(c) == Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO ||
                    Character.UnicodeBlock.of(c) == Character.UnicodeBlock.HANGUL_JAMO) {
                count++;
            }
        }
        return count;
    }

    public static class ReceiptData {
        public String storeName = "";
        public String date = "";
        public int totalAmount = 0;
    }

    public interface OnTextExtractedListener {
        void onSuccess(String text, Text visionText);
        void onFailure(Exception e);
    }
}