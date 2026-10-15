package kr.ac.baekseok.savemate.database;

import androidx.room.TypeConverter;
import kr.ac.baekseok.savemate.model.Category;
import java.util.Date;

public class Converters {

    // Date <-> Long 변환
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }

    // Category <-> String 변환
    @TypeConverter
    public static Category fromString(String value) {
        return value == null ? null : Category.valueOf(value);
    }

    @TypeConverter
    public static String categoryToString(Category category) {
        return category == null ? null : category.name();
    }
}