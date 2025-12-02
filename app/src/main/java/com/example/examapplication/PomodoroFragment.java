package com.example.examapplication;

import android.os.Bundle;
import android.os.CountDownTimer;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

public class PomodoroFragment extends Fragment {
    private static final int POMODORODURATIONMINUTES = 1;
    private static final int BREAKDURATIONMINUTES = 5;
    private static final int LONGBREAKDURATIONMINUTES = 15;
    private static final int POMODORIFORLONGBREAK = 4;

    private TextView timerTextView;
    private Button startPomodoroButton, startShortBreakButton, startLongBreakButton;
    private ImageView tomatoImageView;
    private LinearLayout tomatoImagesGrid;

    private PomodoroViewModel viewModel;
    private int pomodoriCompleted = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pomodoro, container, false);

        timerTextView = view.findViewById(R.id.timerTextView);
        startPomodoroButton = view.findViewById(R.id.startPomodoroButton);
        startShortBreakButton = view.findViewById(R.id.startShortBreakButton);
        startLongBreakButton = view.findViewById(R.id.startLongBreakButton);
        tomatoImageView = view.findViewById(R.id.tomatoImageView);
        tomatoImagesGrid = view.findViewById(R.id.tomatoImagesGrid);

        // Ottieni il ViewModel condiviso con l'Activity
        viewModel = new ViewModelProvider(requireActivity()).get(PomodoroViewModel.class);

        viewModel.getTimerLiveData().observe(getViewLifecycleOwner(), millis -> {
            long minutes = millis / 60000;
            long seconds = (millis % 60000) / 1000;
            timerTextView.setText(String.format("%02d:%02d", minutes, seconds));

            // Nascondi o mostra il bottone startPomodoro in base al timer attivo
            if (millis > 0) {
                startPomodoroButton.setVisibility(View.INVISIBLE);
            } else {
                startPomodoroButton.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getTimerExpiredLiveData().observe(getViewLifecycleOwner(), expired -> {
            if (expired != null && expired) {
                timerTextView.setText("Timer expired");
                pomodoriCompleted++;
                tomatoImageView.setVisibility(View.VISIBLE);

                // Aggiungi un'immagine pomodoro dinamicamente e centro nel LinearLayout
                ImageView tomato = new ImageView(requireContext());
                tomato.setImageResource(R.drawable.ic_tomato);
                tomatoImagesGrid.addView(tomato);

                // Mostra il bottone startPomodoroButton quando timer finisce
                startPomodoroButton.setVisibility(View.VISIBLE);

                // Ulteriore logica per gestire cicli pomodoro/break se necessario
            }
        });

        startPomodoroButton.setOnClickListener(v -> {
            if (!viewModel.isTimerRunning()) {
                viewModel.startTimer(POMODORODURATIONMINUTES * 60 * 1000);
            }
        });

        startShortBreakButton.setOnClickListener(v -> {
            if (!viewModel.isTimerRunning() && pomodoriCompleted >= 1) {
                viewModel.startTimer(BREAKDURATIONMINUTES * 60 * 1000);
            } else {
                Toast.makeText(requireContext(), "Not enough tomato for Short Break", Toast.LENGTH_SHORT).show();
            }
        });

        startLongBreakButton.setOnClickListener(v -> {
            if (!viewModel.isTimerRunning() && pomodoriCompleted >= POMODORIFORLONGBREAK) {
                viewModel.startTimer(LONGBREAKDURATIONMINUTES * 60 * 1000);
            } else {
                Toast.makeText(requireContext(), "Not enough tomato for Long Break", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}
