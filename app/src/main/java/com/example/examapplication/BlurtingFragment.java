package com.example.examapplication;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.graphics.BlurMaskFilter;

import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class BlurtingFragment extends Fragment {

    private TextView timerTextView;
    private Button startStopButton;
    private CountDownTimer countDownTimer;

    private boolean isTimerRunning = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_blurting, container, false);

        timerTextView = view.findViewById(R.id.timerTextView);
        startStopButton = view.findViewById(R.id.startStopButton);

        startStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isTimerRunning) {
                    stopTimer();
                } else {
                    startTimer();
                }
            }
        });

        return view;
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(1800000, 1000) { // 30 minutes, update every 1 second
            @Override
            public void onTick(long millisUntilFinished) {
                long totalSeconds = millisUntilFinished / 1000;
                long minutes = totalSeconds / 60;
                long seconds = totalSeconds % 60;

                timerTextView.setText(String.format("Time remaining: %02d:%02d", minutes, seconds));
            }


            @Override
            public void onFinish() {
                timerTextView.setText("Study session completed!");
                stopTimer();
            }
        };

        countDownTimer.start();
        isTimerRunning = true;
        startStopButton.setText("Stop");
        // Add blur effect or other visual feedback to indicate active study session
        // (You may need to implement a custom view or use a library for the blur effect)
    }

    private void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        timerTextView.setText("Study session stopped");
        isTimerRunning = false;
        startStopButton.setText("Start");
        // Remove blur effect or reset any visual feedback
    }
}
