package kr.ac.baekseok.savemate.database;

import android.content.Context;
import kr.ac.baekseok.savemate.model.Item;
import kr.ac.baekseok.savemate.model.Receipt;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReceiptRepository {

    private final ReceiptDao receiptDao;
    private final ItemDao itemDao;
    private final ExecutorService executorService;

    public ReceiptRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        receiptDao = database.receiptDao();
        itemDao = database.itemDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    // 영수증 저장 (품목 포함)
    public void insertReceiptWithItems(Receipt receipt, List<Item> items, OnDataInsertedListener listener) {
        executorService.execute(() -> {
            long receiptId = receiptDao.insert(receipt);

            // 품목에 영수증 ID 설정
            for (Item item : items) {
                item.setReceiptId(receiptId);
            }
            itemDao.insertAll(items);

            if (listener != null) {
                listener.onDataInserted(receiptId);
            }
        });
    }

    // 영수증 조회
    public void getAllReceipts(OnReceiptsLoadedListener listener) {
        executorService.execute(() -> {
            List<Receipt> receipts = receiptDao.getAllReceipts();
            if (listener != null) {
                listener.onReceiptsLoaded(receipts);
            }
        });
    }

    // 최근 영수증 조회
    public void getRecentReceipts(int limit, OnReceiptsLoadedListener listener) {
        executorService.execute(() -> {
            List<Receipt> receipts = receiptDao.getRecentReceipts(limit);
            if (listener != null) {
                listener.onReceiptsLoaded(receipts);
            }
        });
    }

    // 이번 달 총 지출
    public void getMonthlyTotal(OnTotalLoadedListener listener) {
        executorService.execute(() -> {
            long[] monthRange = getMonthRange();
            int total = receiptDao.getMonthlyTotal(monthRange[0], monthRange[1]);
            if (listener != null) {
                listener.onTotalLoaded(total);
            }
        });
    }

    // 이번 달 영수증 조회
    public void getMonthlyReceipts(OnReceiptsLoadedListener listener) {
        executorService.execute(() -> {
            long[] monthRange = getMonthRange();
            List<Receipt> receipts = receiptDao.getMonthlyReceipts(monthRange[0], monthRange[1]);
            if (listener != null) {
                listener.onReceiptsLoaded(receipts);
            }
        });
    }

    // 월 범위 계산 헬퍼 메서드
    private long[] getMonthRange() {
        Calendar calendar = Calendar.getInstance();

        // 이번 달 1일 00:00:00
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startOfMonth = calendar.getTimeInMillis();

        // 다음 달 1일 00:00:00
        calendar.add(Calendar.MONTH, 1);
        long startOfNextMonth = calendar.getTimeInMillis();

        return new long[]{startOfMonth, startOfNextMonth};
    }

    // 영수증의 품목 조회
    public void getItemsByReceiptId(long receiptId, OnItemsLoadedListener listener) {
        executorService.execute(() -> {
            List<Item> items = itemDao.getItemsByReceiptId(receiptId);
            if (listener != null) {
                listener.onItemsLoaded(items);
            }
        });
    }

    // 영수증 삭제
    public void deleteReceipt(Receipt receipt, OnDataDeletedListener listener) {
        executorService.execute(() -> {
            receiptDao.delete(receipt);
            if (listener != null) {
                listener.onDataDeleted();
            }
        });
    }

    public interface OnDataInsertedListener {
        void onDataInserted(long id);
    }

    public interface OnReceiptsLoadedListener {
        void onReceiptsLoaded(List<Receipt> receipts);
    }

    public interface OnItemsLoadedListener {
        void onItemsLoaded(List<Item> items);
    }

    public interface OnTotalLoadedListener {
        void onTotalLoaded(int total);
    }

    public interface OnDataDeletedListener {
        void onDataDeleted();
    }
}