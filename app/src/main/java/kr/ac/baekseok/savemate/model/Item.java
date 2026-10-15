package kr.ac.baekseok.savemate.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "items",
        foreignKeys = @ForeignKey(
                entity = Receipt.class,
                parentColumns = "id",
                childColumns = "receipt_id",
                onDelete = ForeignKey.CASCADE
        ))
public class Item {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "receipt_id", index = true)
    private long receiptId;

    @ColumnInfo(name = "item_name")
    private String itemName;

    @ColumnInfo(name = "quantity")
    private int quantity;

    @ColumnInfo(name = "unit_price")
    private int unitPrice;

    @ColumnInfo(name = "total_price")
    private int totalPrice;

    // Constructor
    public Item(long receiptId, String itemName, int quantity, int unitPrice, int totalPrice) {
        this.receiptId = receiptId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getReceiptId() {
        return receiptId;
    }

    public void setReceiptId(long receiptId) {
        this.receiptId = receiptId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(int unitPrice) {
        this.unitPrice = unitPrice;
    }

    public int getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(int totalPrice) {
        this.totalPrice = totalPrice;
    }
}