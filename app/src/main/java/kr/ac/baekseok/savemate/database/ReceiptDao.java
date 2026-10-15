package kr.ac.baekseok.savemate.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import kr.ac.baekseok.savemate.model.Receipt;
import java.util.List;

@Dao
public interface ReceiptDao {

    @Insert
    long insert(Receipt receipt);

    @Update
    void update(Receipt receipt);

    @Delete
    void delete(Receipt receipt);

    @Query("SELECT * FROM receipts ORDER BY purchase_date DESC")
    List<Receipt> getAllReceipts();

    @Query("SELECT * FROM receipts WHERE id = :id")
    Receipt getReceiptById(long id);

    @Query("SELECT * FROM receipts ORDER BY purchase_date DESC LIMIT :limit")
    List<Receipt> getRecentReceipts(int limit);

    @Query("SELECT COALESCE(SUM(total_amount), 0) FROM receipts " +
            "WHERE purchase_date >= :startOfMonth AND purchase_date < :startOfNextMonth")
    int getMonthlyTotal(long startOfMonth, long startOfNextMonth);

    @Query("SELECT * FROM receipts " +
            "WHERE purchase_date >= :startOfMonth AND purchase_date < :startOfNextMonth " +
            "ORDER BY purchase_date DESC")
    List<Receipt> getMonthlyReceipts(long startOfMonth, long startOfNextMonth);

    @Query("SELECT category, SUM(total_amount) as total FROM receipts " +
            "WHERE purchase_date >= :startOfMonth AND purchase_date < :startOfNextMonth " +
            "GROUP BY category")
    List<CategoryTotal> getCategoryTotals(long startOfMonth, long startOfNextMonth);

    @Query("DELETE FROM receipts")
    void deleteAll();

    // 카테고리별 합계를 위한 데이터 클래스
    class CategoryTotal {
        public String category;
        public int total;
    }
}