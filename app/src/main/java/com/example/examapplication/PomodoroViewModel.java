package com.example.examapplication;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import android.os.CountDownTimer;

public class PomodoroViewModel extends ViewModel {
    private MutableLiveData<Long> timerLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> timerExpiredLiveData = new MutableLiveData<>();
    private CountDownTimer countDownTimer;
    private boolean isTimerRunning = false;

    public LiveData<Long> getTimerLiveData() { return timerLiveData; }

    public LiveData<Boolean> getTimerExpiredLiveData() { return timerExpiredLiveData; }

    public boolean isTimerRunning() { return isTimerRunning; }

    public void startTimer(long durationMillis) {
        if (isTimerRunning) return; // evita timer sovrapposti

        isTimerRunning = true;
        timerExpiredLiveData.postValue(false);

        countDownTimer = new CountDownTimer(durationMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timerLiveData.postValue(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                timerLiveData.postValue(0L);
                timerExpiredLiveData.postValue(true);
                isTimerRunning = false;
            }
        }.start();
    }

    public void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        isTimerRunning = false;
        timerExpiredLiveData.postValue(false);
        timerLiveData.postValue(0L);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        stopTimer();
    }
}
