package kr.ac.baekseok.savemate.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import kr.ac.baekseok.savemate.model.Item;
import java.util.List;

@Dao
public interface ItemDao {

    @Insert
    long insert(Item item);

    @Insert
    void insertAll(List<Item> items);

    @Update
    void update(Item item);

    @Delete
    void delete(Item item);

    @Query("SELECT * FROM items WHERE receipt_id = :receiptId")
    List<Item> getItemsByReceiptId(long receiptId);

    @Query("DELETE FROM items WHERE receipt_id = :receiptId")
    void deleteByReceiptId(long receiptId);

    @Query("DELETE FROM items")
    void deleteAll();
}