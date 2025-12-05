package com.example.examapplication;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class FlashcardsFragment extends Fragment {

    private EditText editQuestion, editAnswer;
    private Button buttonAddFlashcard;
    private RecyclerView recyclerFlashcards;

    private FlashcardsAdapter adapter;
    private final List<Flashcard> flashcardList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_flashcards, container, false);

        editQuestion = view.findViewById(R.id.editQuestion);
        editAnswer = view.findViewById(R.id.editAnswer);
        buttonAddFlashcard = view.findViewById(R.id.buttonAddFlashcard);
        recyclerFlashcards = view.findViewById(R.id.recyclerFlashcards);

        recyclerFlashcards.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new FlashcardsAdapter(flashcardList);
        recyclerFlashcards.setAdapter(adapter);

        buttonAddFlashcard.setOnClickListener(v -> addFlashcard());

        return view;
    }

    private void addFlashcard() {
        String question = editQuestion.getText().toString().trim();
        String answer = editAnswer.getText().toString().trim();

        if (TextUtils.isEmpty(question) || TextUtils.isEmpty(answer)) {
            Toast.makeText(getContext(), "Compila domanda e risposta", Toast.LENGTH_SHORT).show();
            return;
        }

        Flashcard card = new Flashcard(String.valueOf(System.currentTimeMillis()), question, answer);
        flashcardList.add(card);
        adapter.notifyItemInserted(flashcardList.size() - 1);

        editQuestion.setText("");
        editAnswer.setText("");
    }
}
