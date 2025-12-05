package com.example.examapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FlashcardsAdapter extends RecyclerView.Adapter<FlashcardsAdapter.FlashcardViewHolder> {

    private final List<Flashcard> flashcards;

    public FlashcardsAdapter(List<Flashcard> flashcards) {
        this.flashcards = flashcards;
    }

    @NonNull
    @Override
    public FlashcardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_flashcard, parent, false);
        return new FlashcardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FlashcardViewHolder holder, int position) {
        Flashcard card = flashcards.get(position);

        holder.textQuestion.setText(card.getQuestion());
        holder.textAnswer.setText(card.getAnswer());
        holder.textAnswer.setVisibility(View.GONE);

        // toggle risposta
        holder.itemView.setOnClickListener(v -> {
            if (holder.textAnswer.getVisibility() == View.VISIBLE) {
                holder.textAnswer.setVisibility(View.GONE);
            } else {
                holder.textAnswer.setVisibility(View.VISIBLE);
            }
        });

        // elimina in locale
        holder.buttonDelete.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                flashcards.remove(pos);
                notifyItemRemoved(pos);
            }
        });
    }

    @Override
    public int getItemCount() {
        return flashcards.size();
    }

    static class FlashcardViewHolder extends RecyclerView.ViewHolder {
        TextView textQuestion, textAnswer;
        Button buttonDelete;

        FlashcardViewHolder(@NonNull View itemView) {
            super(itemView);
            textQuestion = itemView.findViewById(R.id.textQuestion);
            textAnswer = itemView.findViewById(R.id.textAnswer);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }
    }
}
