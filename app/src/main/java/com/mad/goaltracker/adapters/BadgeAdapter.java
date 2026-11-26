package com.mad.goaltracker.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.mad.goaltracker.R;
import com.mad.goaltracker.models.Badge;
import com.mad.goaltracker.utils.BadgeIconHelper;

import java.util.ArrayList;
import java.util.List;


public class BadgeAdapter extends RecyclerView.Adapter<BadgeAdapter.BadgeViewHolder> {

    public interface OnBadgeClickListener {
        void onBadgeClick(Badge badge);
    }

    private final List<Badge> badges = new ArrayList<>();
    private final OnBadgeClickListener listener;

    public BadgeAdapter(List<Badge> badgeList, OnBadgeClickListener listener) {
        if (badgeList != null) {
            this.badges.addAll(badgeList);
        }
        this.listener = listener;
    }

    public void updateBadges(List<Badge> newBadges) {
        this.badges.clear();
        if (newBadges != null) {
            this.badges.addAll(newBadges);
        }
        notifyDataSetChanged();
    }

    public List<Badge> getBadges() {
        return badges;
    }

    @NonNull
    @Override
    public BadgeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_badge, parent, false);
        return new BadgeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BadgeViewHolder holder, int position) {
        final Badge badge = badges.get(position);
        Context context = holder.itemView.getContext();

        // 1. Tên và mô tả
        holder.tvBadgeTitle.setText(badge.getTitle() != null ? badge.getTitle() : "");
        holder.tvBadgeDesc.setText(badge.getDescription() != null ? badge.getDescription() : "");

        // 2. Icon an toàn qua BadgeIconHelper
        int iconRes = BadgeIconHelper.getBadgeDrawableResId(badge.getIconName());
        holder.ivBadgeIcon.setImageResource(iconRes);

        // 3. Phân biệt trạng thái Đã mở khóa vs Chưa mở khóa phong cách Duolingo 3D
        if (badge.isUnlocked()) {
            holder.layoutBadgeContainer.setBackgroundResource(R.drawable.bg_duo_card_gold);
            holder.ivBadgeIcon.setAlpha(1.0f);

            holder.tvBadgeStatus.setText(R.string.badge_status_unlocked);
            holder.tvBadgeStatus.setBackgroundResource(R.drawable.bg_status_pill_unlocked);
            holder.tvBadgeStatus.setTextColor(Color.parseColor("#58A700"));

            if (badge.getUnlockedAt() != null && !badge.getUnlockedAt().trim().isEmpty()) {
                String dateOnly = badge.getUnlockedAt().split(" ")[0];
                holder.tvBadgeUnlockedAt.setText(context.getString(R.string.badge_unlocked_at_label, dateOnly));
                holder.tvBadgeUnlockedAt.setVisibility(View.VISIBLE);
            } else {
                holder.tvBadgeUnlockedAt.setVisibility(View.GONE);
            }
        } else {
            holder.layoutBadgeContainer.setBackgroundResource(R.drawable.bg_duo_card_locked);
            holder.ivBadgeIcon.setAlpha(0.35f);

            holder.tvBadgeStatus.setText(R.string.badge_status_locked);
            holder.tvBadgeStatus.setBackgroundResource(R.drawable.bg_status_pill_locked);
            holder.tvBadgeStatus.setTextColor(Color.parseColor("#AFAFAF"));

            holder.tvBadgeUnlockedAt.setVisibility(View.GONE);
        }

        // 4. Sự kiện click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onBadgeClick(badge);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return badges.size();
    }

    static class BadgeViewHolder extends RecyclerView.ViewHolder {
        CardView cardBadgeItem;
        View layoutBadgeContainer;
        ImageView ivBadgeIcon;
        TextView tvBadgeTitle;
        TextView tvBadgeStatus;
        TextView tvBadgeDesc;
        TextView tvBadgeUnlockedAt;

        BadgeViewHolder(@NonNull View itemView) {
            super(itemView);
            cardBadgeItem = itemView.findViewById(R.id.cardBadgeItem);
            layoutBadgeContainer = itemView.findViewById(R.id.layoutBadgeContainer);
            ivBadgeIcon = itemView.findViewById(R.id.ivBadgeIcon);
            tvBadgeTitle = itemView.findViewById(R.id.tvBadgeTitle);
            tvBadgeStatus = itemView.findViewById(R.id.tvBadgeStatus);
            tvBadgeDesc = itemView.findViewById(R.id.tvBadgeDesc);
            tvBadgeUnlockedAt = itemView.findViewById(R.id.tvBadgeUnlockedAt);
        }
    }
}
