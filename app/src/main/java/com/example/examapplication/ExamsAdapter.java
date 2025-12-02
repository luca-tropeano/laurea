package com.example.examapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExamsAdapter extends RecyclerView.Adapter<ExamsAdapter.ExamViewHolder> {

    public interface OnExamClickListener {
        void onExamClick(Exam exam);
    }

    private List<Exam> exams;
    private SimpleDateFormat fullFormat = new SimpleDateFormat("dd/MM/yyyy 'alle' HH:mm", Locale.ITALY);
    private OnExamClickListener listener;

    public ExamsAdapter(List<Exam> exams, OnExamClickListener listener) {
        this.exams = exams;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ExamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // CAMBIATO: usa layout personalizzato invece di simple_list_item_2
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exam, parent, false);
        return new ExamViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ExamViewHolder holder, int position) {
        Exam exam = exams.get(position);
        holder.title.setText(exam.name);

        // Formato: "28/11/2025 alle 14:30"
        holder.subtitle.setText(fullFormat.format(new Date(exam.dateMillis)));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onExamClick(exam);
        });
    }

    @Override
    public int getItemCount() {
        return exams.size();
    }

    static class ExamViewHolder extends RecyclerView.ViewHolder {
        TextView title, subtitle;

        // COSTRUTTORE: trova i nuovi ID del layout personalizzato
        ExamViewHolder(View v) {
            super(v);
            title = v.findViewById(R.id.examTitle);
            subtitle = v.findViewById(R.id.examDateTime);
        }
    }
}
