package com.example.examapplication;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import android.os.CountDownTimer;

public class PomodoroViewModel extends ViewModel {
    private static final int POMODORODURATIONSECONDS = 1500;
    private static final int BREAKDURATIONSECONDS = 300;
    private static final int LONGBREAKDURATIONSECONDS = 1200;
    private static final int POMODORIFORLONGBREAK = 4;

    private MutableLiveData<Long> timerLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> timerExpiredLiveData = new MutableLiveData<>();
    private MutableLiveData<Integer> pomodoriCompletedLiveData = new MutableLiveData<>(0);
    private MutableLiveData<Boolean> showShortBreakButtonLiveData = new MutableLiveData<>(false);
    private MutableLiveData<Boolean> showLongBreakButtonLiveData = new MutableLiveData<>(false);
    private MutableLiveData<Boolean> showStartPomodoroButtonLiveData = new MutableLiveData<>(true);

    private CountDownTimer countDownTimer;
    private boolean isTimerRunning = false;
    private int pomodoriCount = 0; //  VARIABILE LOCALE SINCRO

    public LiveData<Long> getTimerLiveData() { return timerLiveData; }
    public LiveData<Boolean> getTimerExpiredLiveData() { return timerExpiredLiveData; }
    public LiveData<Integer> getPomodoriCompletedLiveData() { return pomodoriCompletedLiveData; }
    public LiveData<Boolean> getShowShortBreakButtonLiveData() { return showShortBreakButtonLiveData; }
    public LiveData<Boolean> getShowLongBreakButtonLiveData() { return showLongBreakButtonLiveData; }
    public LiveData<Boolean> getShowStartPomodoroButtonLiveData() { return showStartPomodoroButtonLiveData; }

    public boolean isTimerRunning() { return isTimerRunning; }

    public void startPomodoro() {
        if (isTimerRunning) return;

        isTimerRunning = true;
        timerExpiredLiveData.postValue(false);
        hideAllButtons();

        countDownTimer = new CountDownTimer(POMODORODURATIONSECONDS * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timerLiveData.postValue(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                timerLiveData.postValue(0L);
                timerExpiredLiveData.postValue(true);
                isTimerRunning = false;

                //  INCREMENTO SINCRO
                pomodoriCount++;
                pomodoriCompletedLiveData.postValue(pomodoriCount);

                //  CONTROLLO SINCRO sul valore locale
                if (pomodoriCount < POMODORIFORLONGBREAK) {
                    showOnlyShortBreak();
                } else {
                    showOnlyLongBreak();
                }
            }
        }.start();
    }

    public void startShortBreak() {
        if (isTimerRunning) return;

        isTimerRunning = true;
        timerExpiredLiveData.postValue(false);
        hideAllButtons();

        countDownTimer = new CountDownTimer(BREAKDURATIONSECONDS * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timerLiveData.postValue(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                timerLiveData.postValue(0L);
                timerExpiredLiveData.postValue(true);
                isTimerRunning = false;
                showStartPomodoroButton();
            }
        }.start();
    }

    public void startLongBreak() {
        if (isTimerRunning) return;

        // RESET SINCRO
        pomodoriCount = 0;
        pomodoriCompletedLiveData.postValue(0);

        isTimerRunning = true;
        timerExpiredLiveData.postValue(false);
        hideAllButtons();

        countDownTimer = new CountDownTimer(LONGBREAKDURATIONSECONDS * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timerLiveData.postValue(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                timerLiveData.postValue(0L);
                timerExpiredLiveData.postValue(true);
                isTimerRunning = false;
                showStartPomodoroButton();
            }
        }.start();
    }

    private void showOnlyShortBreak() {
        showShortBreakButtonLiveData.postValue(true);
        showLongBreakButtonLiveData.postValue(false);
        showStartPomodoroButtonLiveData.postValue(false);
    }

    private void showOnlyLongBreak() {
        showShortBreakButtonLiveData.postValue(false);
        showLongBreakButtonLiveData.postValue(true);
        showStartPomodoroButtonLiveData.postValue(false);
    }

    private void showStartPomodoroButton() {
        showShortBreakButtonLiveData.postValue(false);
        showLongBreakButtonLiveData.postValue(false);
        showStartPomodoroButtonLiveData.postValue(true);
    }

    private void hideAllButtons() {
        showShortBreakButtonLiveData.postValue(false);
        showLongBreakButtonLiveData.postValue(false);
        showStartPomodoroButtonLiveData.postValue(false);
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

