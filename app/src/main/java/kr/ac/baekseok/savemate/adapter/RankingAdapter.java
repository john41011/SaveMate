package kr.ac.baekseok.savemate.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import kr.ac.baekseok.savemate.R;
import kr.ac.baekseok.savemate.model.Challenge;
import kr.ac.baekseok.savemate.utils.DateUtils;
import java.util.ArrayList;
import java.util.List;

public class RankingAdapter extends RecyclerView.Adapter<RankingAdapter.RankingViewHolder> {

    private List<Challenge> challenges = new ArrayList<>();
    private String currentUserId;

    public void setChallenges(List<Challenge> challenges, String currentUserId) {
        this.challenges = challenges;
        this.currentUserId = currentUserId;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RankingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ranking, parent, false);
        return new RankingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RankingViewHolder holder, int position) {
        Challenge challenge = challenges.get(position);
        boolean isCurrentUser = challenge.getUserId().equals(currentUserId);
        holder.bind(challenge, position + 1, isCurrentUser);
    }

    @Override
    public int getItemCount() {
        return challenges.size();
    }

    static class RankingViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvRank;
        private final TextView tvUserName;
        private final TextView tvAmount;
        private final View indicatorMe;

        public RankingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tv_rank);
            tvUserName = itemView.findViewById(R.id.tv_user_name);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            indicatorMe = itemView.findViewById(R.id.indicator_me);
        }

        public void bind(Challenge challenge, int rank, boolean isCurrentUser) {
            // 순위 표시
            if (rank <= 3) {
                // 1~3위는 메달
                String[] medals = {"🥇", "🥈", "🥉"};
                tvRank.setText(medals[rank - 1]);
            } else {
                tvRank.setText(rank + "위");
            }

            // 사용자 이름 (본인이 아닐 시 익명화)
            String displayName;
            if (isCurrentUser) { //본인
                displayName = challenge.getUserName() + " (나)";
                indicatorMe.setVisibility(View.VISIBLE);
            } else { // (본인 아님)익명 처리: 첫 글자 + ***
                String name = challenge.getUserName();
                if (name.length() > 1) {
                    displayName = name.substring(0, 1) + "***";
                } else {
                    displayName = name + "***";
                }
                indicatorMe.setVisibility(View.GONE);
            }
            tvUserName.setText(displayName);

            // 금액
            tvAmount.setText(DateUtils.formatCurrency(challenge.getMonthlyTotal()));

            // 내 항목 강조
            if (isCurrentUser) {
                itemView.setBackgroundColor(itemView.getContext().getResources()
                        .getColor(R.color.primary_light, null));
            } else {
                itemView.setBackgroundColor(itemView.getContext().getResources()
                        .getColor(android.R.color.transparent, null));
            }
        }
    }
}