package com.mad.goaltracker.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.mad.goaltracker.R;
import com.mad.goaltracker.activities.GoalDetailActivity;
import com.mad.goaltracker.models.Goal;

import java.util.List;

public class GoalAdapter extends RecyclerView.Adapter<GoalAdapter.GoalViewHolder> {

    private final Context context;
    private List<Goal> goalList;

    public GoalAdapter(Context context, List<Goal> goalList) {
        this.context = context;
        this.goalList = goalList;
    }

    public void setGoalList(List<Goal> goalList) {
        this.goalList = goalList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GoalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_goal, parent, false);
        return new GoalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GoalViewHolder holder, int position) {
        Goal goal = goalList.get(position);
        holder.tvTitle.setText(goal.getTitle());
        holder.tvCategory.setText(goal.getCategory());
        holder.tvDeadline.setText("Hạn: " + goal.getDeadline());
        holder.tvDifficulty.setText(goal.getDifficulty());
        holder.tvType.setText(goal.getType());
        holder.progressBar.setProgress(goal.getProgress());
        holder.tvProgressText.setText(goal.getProgress() + "%");

        // Đổi màu chip danh mục
        switch (goal.getCategory()) {
            case "Sức khỏe": holder.tvCategory.setTextColor(Color.parseColor("#06D6A0")); break;
            case "Công việc": holder.tvCategory.setTextColor(Color.parseColor("#118AB2")); break;
            case "Học tập": holder.tvCategory.setTextColor(Color.parseColor("#7209B7")); break;
            case "Tài chính": holder.tvCategory.setTextColor(Color.parseColor("#FFD166")); break;
            case "Sở thích": holder.tvCategory.setTextColor(Color.parseColor("#F72585")); break;
            default: holder.tvCategory.setTextColor(Color.parseColor("#4361EE")); break;
        }

        holder.cardGoal.setOnClickListener(v -> {
            Intent intent = new Intent(context, GoalDetailActivity.class);
            intent.putExtra("GOAL_OBJECT", goal);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return goalList != null ? goalList.size() : 0;
    }

    static class GoalViewHolder extends RecyclerView.ViewHolder {
        CardView cardGoal;
        TextView tvTitle, tvCategory, tvDeadline, tvDifficulty, tvType, tvProgressText;
        ProgressBar progressBar;

        public GoalViewHolder(@NonNull View itemView) {
            super(itemView);
            cardGoal = itemView.findViewById(R.id.cardGoal);
            tvTitle = itemView.findViewById(R.id.tvGoalTitle);
            tvCategory = itemView.findViewById(R.id.tvGoalCategory);
            tvDeadline = itemView.findViewById(R.id.tvGoalDeadline);
            tvDifficulty = itemView.findViewById(R.id.tvGoalDifficulty);
            tvType = itemView.findViewById(R.id.tvGoalType);
            tvProgressText = itemView.findViewById(R.id.tvProgressText);
            progressBar = itemView.findViewById(R.id.progressBarGoal);
        }
    }
}
