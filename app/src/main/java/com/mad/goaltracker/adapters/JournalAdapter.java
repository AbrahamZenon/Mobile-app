package com.mad.goaltracker.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mad.goaltracker.R;
import com.mad.goaltracker.models.Journal;

import java.util.List;

public class JournalAdapter extends RecyclerView.Adapter<JournalAdapter.JournalViewHolder> {

    private final Context context;
    private List<Journal> journalList;

    public JournalAdapter(Context context, List<Journal> journalList) {
        this.context = context;
        this.journalList = journalList;
    }

    public void setJournalList(List<Journal> journalList) {
        this.journalList = journalList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public JournalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_journal, parent, false);
        return new JournalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JournalViewHolder holder, int position) {
        Journal journal = journalList.get(position);
        holder.tvDate.setText(journal.getDate());
        holder.tvMood.setText("Tâm trạng: " + journal.getMood());
        holder.tvContent.setText(journal.getContent());
        if (journal.getStrategyAdjustment() != null && !journal.getStrategyAdjustment().isEmpty()) {
            holder.tvStrategy.setVisibility(View.VISIBLE);
            holder.tvStrategy.setText("💡 Điều chỉnh chiến lược: " + journal.getStrategyAdjustment());
        } else {
            holder.tvStrategy.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return journalList != null ? journalList.size() : 0;
    }

    static class JournalViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvMood, tvContent, tvStrategy;

        public JournalViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvJournalDate);
            tvMood = itemView.findViewById(R.id.tvJournalMood);
            tvContent = itemView.findViewById(R.id.tvJournalContent);
            tvStrategy = itemView.findViewById(R.id.tvJournalStrategy);
        }
    }
}
