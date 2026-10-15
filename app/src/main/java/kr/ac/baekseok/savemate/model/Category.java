package kr.ac.baekseok.savemate.model;

public enum Category {
    FOOD("식비", "#FF6B6B"),
    TRANSPORT("교통", "#4ECDC4"),
    SHOPPING("쇼핑", "#95E1D3"),
    ENTERTAINMENT("여가", "#FFA07A"),
    ETC("기타", "#B19CD9");

    private final String displayName;
    private final String color;

    Category(String displayName, String color) {
        this.displayName = displayName;
        this.color = color;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColor() {
        return color;
    }

    // String을 Category로 변환
    public static Category fromString(String text) {
        for (Category category : Category.values()) {
            if (category.displayName.equalsIgnoreCase(text)) {
                return category;
            }
        }
        return ETC; // 기본값
    }
}