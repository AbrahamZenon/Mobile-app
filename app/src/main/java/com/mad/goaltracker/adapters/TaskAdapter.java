package com.mad.goaltracker.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mad.goaltracker.R;
import com.mad.goaltracker.models.Task;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    public interface OnTaskStatusChangeListener {
        void onStatusChanged(Task task, boolean isCompleted);
        void onDeleteTask(Task task);
    }

    private final Context context;
    private List<Task> taskList;
    private final OnTaskStatusChangeListener listener;

    public TaskAdapter(Context context, List<Task> taskList, OnTaskStatusChangeListener listener) {
        this.context = context;
        this.taskList = taskList;
        this.listener = listener;
    }

    public void setTaskList(List<Task> taskList) {
        this.taskList = taskList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.cbTask.setText(task.getTitle());
        holder.cbTask.setChecked(task.isCompleted());
        holder.tvPriority.setText(task.getPriority());
        holder.tvPlanType.setText(task.getPlanType());
        holder.tvDueDate.setText(task.getDueDate() != null && !task.getDueDate().isEmpty() ? "Hạn: " + task.getDueDate() : "");

        // Gạch ngang chữ nếu đã hoàn thành
        if (task.isCompleted()) {
            holder.cbTask.setPaintFlags(holder.cbTask.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.cbTask.setTextColor(Color.GRAY);
        } else {
            holder.cbTask.setPaintFlags(holder.cbTask.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.cbTask.setTextColor(Color.parseColor("#2B2D42"));
        }

        // Màu ưu tiên
        if ("Cao".equalsIgnoreCase(task.getPriority())) {
            holder.tvPriority.setBackgroundColor(Color.parseColor("#EF476F"));
            holder.tvPriority.setTextColor(Color.WHITE);
        } else if ("Trung bình".equalsIgnoreCase(task.getPriority())) {
            holder.tvPriority.setBackgroundColor(Color.parseColor("#FFD166"));
            holder.tvPriority.setTextColor(Color.parseColor("#2B2D42"));
        } else {
            holder.tvPriority.setBackgroundColor(Color.parseColor("#06D6A0"));
            holder.tvPriority.setTextColor(Color.WHITE);
        }

        holder.cbTask.setOnClickListener(v -> {
            boolean isChecked = holder.cbTask.isChecked();
            task.setCompleted(isChecked);
            if (listener != null) {
                listener.onStatusChanged(task, isChecked);
            }
        });

        holder.btnDeleteTask.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteTask(task);
            }
        });
    }

    @Override
    public int getItemCount() {
        return taskList != null ? taskList.size() : 0;
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbTask;
        TextView tvPriority, tvPlanType, tvDueDate;
        ImageButton btnDeleteTask;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            cbTask = itemView.findViewById(R.id.cbTask);
            tvPriority = itemView.findViewById(R.id.tvTaskPriority);
            tvPlanType = itemView.findViewById(R.id.tvTaskPlanType);
            tvDueDate = itemView.findViewById(R.id.tvTaskDueDate);
            btnDeleteTask = itemView.findViewById(R.id.btnDeleteTask);
        }
    }
}
