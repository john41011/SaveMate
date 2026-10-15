package kr.ac.baekseok.savemate.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import kr.ac.baekseok.savemate.database.Converters;
import java.util.Date;

@Entity(tableName = "receipts")
@TypeConverters(Converters.class)
public class Receipt {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "store_name")
    private String storeName;

    @ColumnInfo(name = "purchase_date")
    private Date purchaseDate;

    @ColumnInfo(name = "total_amount")
    private int totalAmount;

    @ColumnInfo(name = "category")
    private Category category;

    @ColumnInfo(name = "memo")
    private String memo;

    @ColumnInfo(name = "image_path")
    private String imagePath;

    @ColumnInfo(name = "created_at")
    private Date createdAt;

    // Constructor
    public Receipt(String storeName, Date purchaseDate, int totalAmount, Category category) {
        this.storeName = storeName;
        this.purchaseDate = purchaseDate;
        this.totalAmount = totalAmount;
        this.category = category;
        this.createdAt = new Date();
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public Date getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(Date purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public int getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(int totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}