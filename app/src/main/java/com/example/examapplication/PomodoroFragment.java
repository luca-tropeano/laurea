package com.example.examapplication;

import android.os.Bundle;
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

    private TextView timerTextView;
    private Button startPomodoroButton, startShortBreakButton, startLongBreakButton;
    private LinearLayout tomatoImagesGrid;

    private PomodoroViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pomodoro, container, false);

        timerTextView = view.findViewById(R.id.timerTextView);
        startPomodoroButton = view.findViewById(R.id.startPomodoroButton);
        startShortBreakButton = view.findViewById(R.id.startShortBreakButton);
        startLongBreakButton = view.findViewById(R.id.startLongBreakButton);
        tomatoImagesGrid = view.findViewById(R.id.tomatoImagesGrid);

        viewModel = new ViewModelProvider(requireActivity()).get(PomodoroViewModel.class);

        // Timer display
        viewModel.getTimerLiveData().observe(getViewLifecycleOwner(), millis -> {
            if (millis > 0) {
                long minutes = millis / 60000;
                long seconds = (millis % 60000) / 1000;
                timerTextView.setText(String.format("%02d:%02d", minutes, seconds));
            }
        });

        viewModel.getTimerExpiredLiveData().observe(getViewLifecycleOwner(), expired -> {
            if (expired != null && expired) {
                timerTextView.setText("Timer scaduto!");
            }
        });

        // Visibilità pulsanti
        viewModel.getShowShortBreakButtonLiveData().observe(getViewLifecycleOwner(), show -> {
            startShortBreakButton.setVisibility(show ? View.VISIBLE : View.GONE);
        });

        viewModel.getShowLongBreakButtonLiveData().observe(getViewLifecycleOwner(), show -> {
            startLongBreakButton.setVisibility(show ? View.VISIBLE : View.GONE);
        });

        viewModel.getShowStartPomodoroButtonLiveData().observe(getViewLifecycleOwner(), show -> {
            startPomodoroButton.setVisibility(show ? View.VISIBLE : View.GONE);
        });

        // Pomodori completati
        viewModel.getPomodoriCompletedLiveData().observe(getViewLifecycleOwner(), count -> {
            tomatoImagesGrid.removeAllViews();
            if (count != null && count > 0) {
                for (int i = 0; i < count; i++) {
                    addTomatoImage();
                }
            }
        });

        // Click listener SEPARATI
        startPomodoroButton.setOnClickListener(v -> {
            if (!viewModel.isTimerRunning()) {
                viewModel.startPomodoro();
            }
        });

        startShortBreakButton.setOnClickListener(v -> {
            if (!viewModel.isTimerRunning()) {
                viewModel.startShortBreak();
            }
        });

        startLongBreakButton.setOnClickListener(v -> {
            if (!viewModel.isTimerRunning()) {
                viewModel.startLongBreak(); // Azzera IMMEDIATAMENTE i pomodori
            }
        });

        return view;
    }

    private void addTomatoImage() {
        ImageView tomato = new ImageView(requireContext());
        tomato.setImageResource(R.drawable.ic_tomato);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(8, 8, 8, 8);
        tomato.setLayoutParams(params);
        tomatoImagesGrid.addView(tomato);
    }
}
