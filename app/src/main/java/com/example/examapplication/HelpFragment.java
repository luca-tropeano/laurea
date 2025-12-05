package com.example.examapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

public class HelpFragment extends Fragment {

    public HelpFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_help, container, false);

        // Imposta i click listener per le card
        setupCardListeners(view);

        return view;
    }

    private void setupCardListeners(View view) {
        // Pomodoro
        view.findViewById(R.id.pomodoroHeader).setOnClickListener(v ->
                toggleCard(view, R.id.pomodoroContent, R.id.pomodoroArrow, R.id.pomodoroDivider));

        // Flashcards
        view.findViewById(R.id.flashcardHeader).setOnClickListener(v ->
                toggleCard(view, R.id.flashcardContent, R.id.flashcardArrow, R.id.flashcardDivider));

        // SQ3R
        view.findViewById(R.id.sq3rHeader).setOnClickListener(v ->
                toggleCard(view, R.id.sq3rContent, R.id.sq3rArrow, R.id.sq3rDivider));
    }

    private void toggleCard(View rootView, int contentId, int arrowId, int dividerId) {
        TextView content = rootView.findViewById(contentId);
        TextView arrow = rootView.findViewById(arrowId);
        View divider = rootView.findViewById(dividerId);

        if (content.getVisibility() == View.VISIBLE) {
            // Collassa
            content.setVisibility(View.GONE);
            divider.setVisibility(View.GONE);
            arrow.setText("▼");
        } else {
            // Espandi
            content.setVisibility(View.VISIBLE);
            divider.setVisibility(View.VISIBLE);
            arrow.setText("▲");
        }
    }
}
