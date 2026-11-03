package com.mad.goaltracker.activities;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.mad.goaltracker.R;
import com.mad.goaltracker.models.Badge;
import com.mad.goaltracker.utils.BadgeIconHelper;

import android.content.Context;
import android.content.DialogInterface;
import java.util.ArrayList;
import java.util.List;
import com.mad.goaltracker.gamification.AchievementEventCoordinator;

/**
 * DialogFragment hiển thị chúc mừng thành tích huy hiệu mới hoặc chi tiết huy hiệu.
 * Tuân thủ nguyên tắc:
 * - Không mở nhiều dialog chồng nhau khi mở nhiều badge đồng thời.
 * - Lưu trữ trạng thái trong Bundle Arguments chống mất dữ liệu khi xoay màn hình.
 * - Hỗ trợ cả 2 chế độ: Chúc mừng thành tích mới (Celebration) và Xem chi tiết (Detail).
 */
public class AchievementDialogFragment extends DialogFragment {

    public static final String TAG = "AchievementDialog";
    private static final String ARG_MODE = "arg_mode";
    private static final String ARG_BADGES = "arg_badges";
    private static final String ARG_SINGLE_BADGE = "arg_single_badge";

    public static final int MODE_CELEBRATION = 1;
    public static final int MODE_DETAIL = 2;

    private boolean isAcknowledged = false;

    /**
     * Tạo Dialog chúc mừng một hoặc nhiều huy hiệu vừa mở khóa.
     */
    public static AchievementDialogFragment newCelebrationInstance(ArrayList<Badge> newlyUnlockedBadges) {
        AchievementDialogFragment fragment = new AchievementDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_MODE, MODE_CELEBRATION);
        args.putSerializable(ARG_BADGES, newlyUnlockedBadges);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Tạo Dialog xem thông tin chi tiết một huy hiệu cụ thể (khi click từ danh sách).
     */
    public static AchievementDialogFragment newDetailInstance(Badge badge) {
        AchievementDialogFragment fragment = new AchievementDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_MODE, MODE_DETAIL);
        args.putSerializable(ARG_SINGLE_BADGE, badge);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (dialog.getWindow() != null) {
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_achievement, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView ivDialogHeaderIcon = view.findViewById(R.id.ivDialogHeaderIcon);
        TextView tvDialogTitle = view.findViewById(R.id.tvDialogTitle);
        TextView tvDialogSubtitle = view.findViewById(R.id.tvDialogSubtitle);
        LinearLayout layoutBadgeItemsContainer = view.findViewById(R.id.layoutBadgeItemsContainer);
        Button btnDialogDismiss = view.findViewById(R.id.btnDialogDismiss);

        btnDialogDismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acknowledgeCelebration();
                dismiss();
            }
        });

        Bundle args = getArguments();
        if (args == null) {
            dismiss();
            return;
        }

        int mode = args.getInt(ARG_MODE, MODE_CELEBRATION);

        if (mode == MODE_CELEBRATION) {
            // Chế độ chúc mừng thành tích mới
            @SuppressWarnings("unchecked")
            ArrayList<Badge> badges = (ArrayList<Badge>) args.getSerializable(ARG_BADGES);
            if (badges == null || badges.isEmpty()) {
                dismiss();
                return;
            }

            ivDialogHeaderIcon.setImageResource(R.drawable.ic_celebration);
            tvDialogTitle.setText(R.string.dialog_achievement_title);
            tvDialogSubtitle.setText(R.string.dialog_achievement_congrats);
            btnDialogDismiss.setText(R.string.btn_dialog_awesome);

            populateBadgeRows(layoutBadgeItemsContainer, badges);

        } else {
            // Chế độ xem chi tiết huy hiệu
            Badge badge = (Badge) args.getSerializable(ARG_SINGLE_BADGE);
            if (badge == null) {
                dismiss();
                return;
            }

            int iconRes = BadgeIconHelper.getBadgeDrawableResId(badge.getIconName());
            ivDialogHeaderIcon.setImageResource(iconRes);
            tvDialogTitle.setText(badge.getTitle() != null ? badge.getTitle() : "");

            String statusStr = badge.isUnlocked()
                    ? getString(R.string.badge_status_unlocked)
                    : getString(R.string.badge_status_locked);
            tvDialogSubtitle.setText(statusStr);
            tvDialogSubtitle.setTextColor(badge.isUnlocked()
                    ? Color.parseColor("#06D6A0")
                    : Color.parseColor("#8D99AE"));

            btnDialogDismiss.setText(R.string.btn_dialog_close);

            ArrayList<Badge> singleList = new ArrayList<>();
            singleList.add(badge);
            populateBadgeRows(layoutBadgeItemsContainer, singleList);
        }
    }

    private void populateBadgeRows(LinearLayout container, ArrayList<Badge> badges) {
        container.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext());

        for (Badge b : badges) {
            View row = inflater.inflate(R.layout.item_dialog_badge_row, container, false);
            ImageView ivRowIcon = row.findViewById(R.id.ivRowBadgeIcon);
            TextView tvRowTitle = row.findViewById(R.id.tvRowBadgeTitle);
            TextView tvRowDesc = row.findViewById(R.id.tvRowBadgeDesc);
            TextView tvRowTime = row.findViewById(R.id.tvRowBadgeTime);

            ivRowIcon.setImageResource(BadgeIconHelper.getBadgeDrawableResId(b.getIconName()));
            tvRowTitle.setText(b.getTitle() != null ? b.getTitle() : "");
            tvRowDesc.setText(b.getDescription() != null ? b.getDescription() : "");

            if (b.isUnlocked()) {
                row.setBackgroundResource(R.drawable.bg_badge_unlocked);
                ivRowIcon.setAlpha(1.0f);
                if (b.getUnlockedAt() != null && !b.getUnlockedAt().trim().isEmpty()) {
                    tvRowTime.setText(getString(R.string.badge_unlocked_at_label, b.getUnlockedAt()));
                    tvRowTime.setVisibility(View.VISIBLE);
                } else {
                    tvRowTime.setVisibility(View.GONE);
                }
            } else {
                row.setBackgroundResource(R.drawable.bg_badge_locked);
                ivRowIcon.setAlpha(0.35f);
                tvRowTime.setVisibility(View.GONE);
            }

            container.addView(row);
        }
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (isRemoving() || !isAdded()) {
            acknowledgeCelebration();
        }
    }

    private void acknowledgeCelebration() {
        if (isAcknowledged) return;
        isAcknowledged = true;
        Context context = getContext();
        Bundle args = getArguments();
        if (context != null && args != null) {
            int mode = args.getInt(ARG_MODE, MODE_CELEBRATION);
            if (mode == MODE_CELEBRATION) {
                @SuppressWarnings("unchecked")
                ArrayList<Badge> badges = (ArrayList<Badge>) args.getSerializable(ARG_BADGES);
                if (badges != null && !badges.isEmpty()) {
                    List<String> codes = new ArrayList<>();
                    for (Badge b : badges) {
                        if (b != null && b.getCode() != null) {
                            codes.add(b.getCode());
                        }
                    }
                    AchievementEventCoordinator.markBadgesCelebrated(context, codes);
                }
            }
        }
    }
}
