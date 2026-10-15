package kr.ac.baekseok.savemate.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import kr.ac.baekseok.savemate.R;
import kr.ac.baekseok.savemate.model.Receipt;
import kr.ac.baekseok.savemate.utils.DateUtils;
import java.util.ArrayList;
import java.util.List;

public class ReceiptAdapter extends RecyclerView.Adapter<ReceiptAdapter.ReceiptViewHolder> {

    private List<Receipt> receipts = new ArrayList<>();
    private OnReceiptClickListener listener;

    public interface OnReceiptClickListener {
        void onReceiptClick(Receipt receipt);
    }

    public void setOnReceiptClickListener(OnReceiptClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ReceiptViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_receipt_card, parent, false);
        return new ReceiptViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReceiptViewHolder holder, int position) {
        Receipt receipt = receipts.get(position);
        holder.bind(receipt);
    }

    @Override
    public int getItemCount() {
        return receipts.size();
    }

    public void setReceipts(List<Receipt> receipts) {
        this.receipts = receipts;
        notifyDataSetChanged();
    }

    public void addReceipt(Receipt receipt) {
        this.receipts.add(0, receipt);
        notifyItemInserted(0);
    }

    class ReceiptViewHolder extends RecyclerView.ViewHolder {
        private final View categoryIndicator;
        private final TextView tvStoreName;
        private final TextView tvAmount;
        private final TextView tvDate;
        private final TextView tvCategory;

        public ReceiptViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryIndicator = itemView.findViewById(R.id.category_indicator);
            tvStoreName = itemView.findViewById(R.id.tv_store_name);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvCategory = itemView.findViewById(R.id.tv_category);

            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onReceiptClick(receipts.get(getAdapterPosition()));
                }
            });
        }

        public void bind(Receipt receipt) {
            tvStoreName.setText(receipt.getStoreName());
            tvAmount.setText(DateUtils.formatCurrency(receipt.getTotalAmount()));
            tvDate.setText(DateUtils.formatDate(receipt.getPurchaseDate()));
            tvCategory.setText(receipt.getCategory().getDisplayName());

            // 카테고리 색상 적용
            try {
                int color = Color.parseColor(receipt.getCategory().getColor());
                categoryIndicator.setBackgroundColor(color);
                tvCategory.setBackgroundColor(color);
            } catch (Exception e) {
                // 색상 파싱 실패 시 기본 색상
            }
        }
    }
}